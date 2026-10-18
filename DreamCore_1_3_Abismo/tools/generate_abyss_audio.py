#!/usr/bin/env python3
"""Rebuild original DreamCore 1.3 music; requires Python, NumPy and ffmpeg.

No samples, commercial recordings, reference-video audio or network inputs.
Periodic synthesis and circular reverb preserve the loop boundary.
"""
from __future__ import annotations

import json
import subprocess
import tempfile
import wave
from pathlib import Path

import numpy as np

RATE = 44100
ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "app/src/main/res/raw"


def hz(midi: int) -> float:
    return 440.0 * 2.0 ** ((midi - 69) / 12.0)


def add_note(buffer: np.ndarray, second: float, midi: int, pan: float, level: float) -> None:
    """Finite soft mallet event, with its tail wrapped to the start of the loop."""
    size = min(int(RATE * 12), len(buffer))
    t = np.arange(size, dtype=np.float64) / RATE
    frequency = hz(midi)
    attack = (1.0 - np.exp(-t / .13)) ** 2
    envelope = attack * np.exp(-t / 2.2)
    # Exponential tail reaches exact zero without cutting a waveform abruptly.
    envelope *= .5 + .5 * np.cos(np.pi * np.clip((t - 9) / 3, 0, 1))
    mallet = (np.sin(2 * np.pi * frequency * t)
              + .18 * np.sin(2 * np.pi * frequency * 2.002 * t) * np.exp(-t / 1.3)
              + .035 * np.sin(2 * np.pi * frequency * 3.997 * t) * np.exp(-t / .8))
    event = mallet * envelope * level
    gains = (np.cos((pan + 1) * np.pi / 4), np.sin((pan + 1) * np.pi / 4))
    start = int(second * RATE) % len(buffer)
    first = min(size, len(buffer) - start)
    for channel in (0, 1):
        buffer[start:start + first, channel] += event[:first] * gains[channel]
        if first < size:
            buffer[:size - first, channel] += event[first:] * gains[channel]


def create_pad(seconds: int, notes: list[int], seed: int, darkness: bool) -> np.ndarray:
    size = seconds * RATE
    t = np.arange(size, dtype=np.float64) / RATE
    rng = np.random.default_rng(seed)
    output = np.zeros((size, 2), dtype=np.float64)
    for index, note in enumerate(notes):
        for voice in (-1, 1):
            # Snap to whole cycles in the loop, including stereo detune.
            frequency = round(hz(note) * (1 + voice * .0018) * seconds) / seconds
            phase = rng.uniform(0, 2 * np.pi)
            oscillator = np.sin(2 * np.pi * frequency * t + phase)
            oscillator += .16 * np.sin(4 * np.pi * frequency * t + phase * 2)
            oscillator += .035 * np.sin(6 * np.pi * frequency * t + phase * 3)
            breath = .72 + .18 * np.sin(2 * np.pi * (index % 3 + 1) * t / seconds + phase)
            breath += .08 * np.cos(2 * np.pi * 5 * t / seconds + phase)
            event = oscillator * breath * (.024 if darkness else .033)
            output[:, 0] += event * (1.0 if voice == -1 else .53)
            output[:, 1] += event * (.53 if voice == -1 else 1.0)

    # Quiet filtered air, synthesized as a periodic band-limited signal.
    spectrum = np.fft.rfft(rng.standard_normal(size))
    frequencies = np.fft.rfftfreq(size, 1 / RATE)
    shape = np.exp(-(frequencies / (420.0 if darkness else 1400.0)) ** 2)
    shape *= 1 - np.exp(-(frequencies / 95.0) ** 2)
    air = np.fft.irfft(spectrum * shape, n=size)
    air *= (.006 if darkness else .002) / max(np.sqrt(np.mean(air ** 2)), 1e-12)
    output[:, 0] += air
    output[:, 1] += np.roll(air, int(.031 * RATE))
    return output


def finish(signal: np.ndarray, target_rms: float) -> np.ndarray:
    wet = signal.copy()
    # Circular taps carry decays across the loop; channels alternate in the room.
    for seconds, gain in ((.271, .20), (.523, .15), (.947, .11), (1.431, .075), (2.173, .05)):
        wet += np.roll(signal[:, ::-1], round(seconds * RATE), axis=0) * gain
    wet -= wet.mean(axis=0)
    wet *= target_rms / np.sqrt(np.mean(wet ** 2))
    peak = float(np.abs(wet).max())
    if peak > .69:
        wet *= .69 / peak
    return wet


def metrics(signal: np.ndarray) -> dict:
    adjacent = np.abs(np.diff(signal, axis=0))
    seam = np.abs(signal[0] - signal[-1])
    return {
        "seconds": round(len(signal) / RATE, 6),
        "sample_rate": RATE,
        "channels": int(signal.shape[1]),
        "peak": round(float(np.abs(signal).max()), 7),
        "rms": round(float(np.sqrt(np.mean(signal ** 2))), 7),
        "clipped_samples": int(np.count_nonzero(np.abs(signal) >= .999)),
        "loop_boundary_max_step": round(float(seam.max()), 8),
        "interior_max_step": round(float(adjacent.max()), 8),
        "interior_p99_step": round(float(np.quantile(adjacent, .99)), 8),
        "dc": [round(float(x), 8) for x in signal.mean(axis=0)],
    }


def export(name: str, signal: np.ndarray) -> dict:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="dreamcore-audio-") as tmp:
        wav_path = Path(tmp) / "source.wav"
        with wave.open(str(wav_path), "wb") as wav:
            wav.setnchannels(2)
            wav.setsampwidth(2)
            wav.setframerate(RATE)
            wav.writeframes(np.round(signal * 32767).astype("<i2").tobytes())
        destination = OUTPUT / f"{name}.ogg"
        subprocess.run([
            "ffmpeg", "-v", "error", "-y", "-i", str(wav_path),
            "-c:a", "libvorbis", "-q:a", "5", "-metadata", "ARTIST=DreamCore",
            "-metadata", "COMMENT=Original procedural composition; no external samples",
            str(destination),
        ], check=True)
        decoded = subprocess.run([
            "ffmpeg", "-v", "error", "-i", str(destination),
            "-f", "f32le", "-acodec", "pcm_f32le", "-ar", str(RATE), "-ac", "2", "-",
        ], check=True, stdout=subprocess.PIPE).stdout
        decoded_signal = np.frombuffer(decoded, dtype="<f4").reshape(-1, 2)
        result = {"source": metrics(signal), "decoded_ogg": metrics(decoded_signal),
                  "file_bytes": destination.stat().st_size}
        # Checks the distributed compressed artifact, not just the source waveform.
        assert len(decoded_signal) == len(signal), "Codec changed loop duration"
        assert result["decoded_ogg"]["peak"] < .9
        assert result["decoded_ogg"]["clipped_samples"] == 0
        assert result["decoded_ogg"]["loop_boundary_max_step"] < .012
        assert result["decoded_ogg"]["loop_boundary_max_step"] < result["decoded_ogg"]["interior_max_step"]
        return result


def main() -> None:
    # Dmaj9/F#: spacious suspended harmony, soft detuned tape-like pad.
    music = create_pad(64, [42, 50, 54, 57, 61, 64], seed=1301, darkness=False)
    for second, note, pan in ((2, 78, -.4), (10, 76, .3), (18, 73, -.15),
                              (26, 69, .45), (34, 76, -.35), (42, 73, .2),
                              (50, 78, .4), (58, 81, -.2)):
        add_note(music, second, note, pan, .065)
    music = finish(music, .14)
    # Low nonthreatening distant air for the descent; base melody remains audible.
    abyss = create_pad(32, [33, 38, 45, 50], seed=1302, darkness=True)
    abyss = finish(abyss, .085)
    report = {
        "music_crater_13.ogg": export("music_crater_13", music),
        "layer_abyss_13.ogg": export("layer_abyss_13", abyss),
        "note": "Signal checks do not replace listening on a real Android device.",
    }
    report_path = ROOT / "tools/audio_validation.json"
    report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
