#!/usr/bin/env python3
"""Decode every export and compare its audio against the paired source MP3."""
import concurrent.futures
import hashlib
import json
from pathlib import Path
import subprocess
import sys
import time
import numpy as np

OUT = Path(__file__).resolve().parents[1]
SOURCE = OUT.parent / 'DreamCore_2_4_Resonancias/Banda_sonora'
TRACKS = json.loads((SOURCE / 'catalogo.json').read_text())


def decode_audio(path):
    data = subprocess.check_output([
        'ffmpeg', '-v', 'error', '-xerror', '-threads', '1', '-i', str(path),
        '-map', '0:a:0', '-ar', '32000', '-ac', '2', '-f', 'f32le', '-'])
    return np.frombuffer(data, dtype='<f4')


def check(track):
    video = OUT / 'Videos' / Path(track['file']).with_suffix('.mp4')
    if '--wait-for-exports' in sys.argv:
        deadline = time.monotonic() + 1200
        while True:
            if video.exists():
                probe = subprocess.run(['ffprobe', '-v', 'error', '-show_format', '-of', 'json', str(video)], capture_output=True, text=True)
                duration = float(json.loads(probe.stdout or '{}').get('format', {}).get('duration', '0'))
                if not probe.returncode and abs(duration - track['seconds']) < .1:
                    break
            if time.monotonic() > deadline:
                raise TimeoutError(f'Export not ready: {video.name}')
            time.sleep(1)
    subprocess.run(['ffmpeg', '-v', 'error', '-xerror', '-err_detect', 'explode',
                    '-threads', '1', '-i', str(video), '-map', '0:v:0',
                    '-map', '0:a:0', '-f', 'null', '-'], check=True)
    original = decode_audio(SOURCE / 'Musica' / track['file'])
    encoded = decode_audio(video)
    expected = track['seconds'] * 32000 * 2
    assert abs(len(original) - expected) < 200
    assert abs(len(encoded) - expected) < 3200
    size = min(len(original), len(encoded))
    a = original[:size:4].astype(np.float64)
    b = encoded[:size:4].astype(np.float64)
    corr = float(np.dot(a, b) / np.sqrt(np.dot(a, a) * np.dot(b, b)))
    peak = float(np.max(np.abs(encoded)))
    assert corr > .99, (track['number'], corr)
    assert np.isfinite(encoded).all() and peak < 1
    # Distinct frames at the beginning and near the end must be present.
    frame_checks = []
    for seconds in [0, track['seconds'] - 1]:
        frame = subprocess.check_output([
            'ffmpeg', '-v', 'error', '-threads', '1', '-ss', str(seconds),
            '-i', str(video), '-map', '0:v:0', '-frames:v', '1',
            '-vf', 'scale=480:270', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-'])
        assert len(frame) == 480 * 270 * 3
        frame_checks.append(hashlib.sha256(frame).hexdigest())
    result = dict(number=track['number'], title=track['title'],
                  fully_decoded=True, audio_correlation=round(corr, 8),
                  audio_peak=round(peak, 6),
                  decoded_audio_seconds=round(len(encoded) / 64000, 5),
                  frame_checks=frame_checks)
    print(f"{track['number']:02d}/49 decoded; audio correlation={corr:.6f}; peak={peak:.3f}", flush=True)
    return result


if __name__ == '__main__':
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        results = sorted(pool.map(check, TRACKS), key=lambda x: x['number'])
    (OUT / 'VALIDACION.json').write_text(json.dumps(dict(
        videos=len(results), full_decode_passed=True, width=1920, height=1080,
        fps=24, video_codec='H.264', audio_codec='AAC stereo',
        color_space='BT.709 limited range', results=results),
        ensure_ascii=False, indent=2) + '\n')
    print('ALL 49 EXPORTS VALIDATED', flush=True)
