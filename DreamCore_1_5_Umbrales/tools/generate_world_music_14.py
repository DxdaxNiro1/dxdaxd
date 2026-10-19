#!/usr/bin/env python3
"""Twelve distinct original ambient themes. NumPy + FFmpeg/libvorbis; no samples."""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

import numpy as np

from generate_abyss_audio import RATE, ROOT, OUTPUT, finish, export, hz


THEMES = [
    dict(name="station", seconds=48, timbre="toy_keys", pad=.035, melody=.065, width=1.7,
         chords=[[52,59,63,66],[49,56,59,63],[57,61,64,68],[54,59,61,66]],
         notes=[(2,75),(6,71),(11,78),(17,75),(22,73),(29,68),(35,71),(42,75)],
         description="Estación: teclas de juguete lejanas, acorde mayor cálido y pausas largas."),
    dict(name="pools", seconds=48, timbre="glass", pad=.025, melody=.068, width=2.8,
         chords=[[50,57,62,64],[53,57,60,65],[48,55,60,62],[50,57,60,64]],
         notes=[(1,81),(3.5,86),(8,79),(13,84),(15.5,81),(20,77),(25,79),(28,86),(33,84),(39,81),(44,79)],
         description="Piscinas: gotas de cristal, quintas abiertas y ecos alternados."),
    dict(name="crater", seconds=48, timbre="celesta", pad=.047, melody=.040, width=3.8,
         chords=[[38,45,50,57],[42,49,54,61],[35,42,50,57],[38,45,52,57]],
         notes=[(4,78),(17,73),(30,76),(42,69)],
         description="Abismo: registro grave, acordes suspendidos y cuatro campanas solitarias."),
    dict(name="garden", seconds=48, timbre="plucked", pad=.025, melody=.080, width=1.4,
         chords=[[53,60,64,69],[50,57,60,65],[46,53,57,62],[48,55,60,64]],
         notes=[(1,77),(4,81),(7,84),(10,81),(14,77),(18,74),(23,72),(27,77),(31,81),(36,79),(40,76),(45,77)],
         description="Jardín: arpegios de cuerda sintética suaves y armonía luminosa."),
    dict(name="meadow", seconds=48, timbre="flute", pad=.032, melody=.050, width=3.0,
         chords=[[55,62,67,71],[52,59,64,67],[48,55,60,64],[50,57,62,69]],
         notes=[(3,79),(10,83),(19,81),(27,76),(35,79),(42,74)],
         description="Pradera: voz de aire sin palabras y cambios mayores amplios."),
    dict(name="aquarium", seconds=48, timbre="glass_low", pad=.040, melody=.058, width=3.4,
         chords=[[43,50,58,62],[39,46,55,60],[41,48,55,62],[38,45,53,60]],
         notes=[(2,74),(7,69),(15,77),(24,72),(31,70),(39,65),(45,74)],
         description="Acuario: vidrio grave, acordes menores lentos y reverberación oscura."),
    dict(name="house_meadow", seconds=32, timbre="piano", pad=.017, melody=.085, width=2.5,
         chords=[[52,59,64,68],[49,56,61,64],[45,52,57,61],[47,54,59,66]],
         notes=[(.5,76),(3,71),(6,68),(10,73),(14,71),(18,69),(23,66),(28,71)],
         description="Casa de la pradera: piano íntimo, memoria tranquila y notas descendentes."),
    dict(name="house_crater", seconds=32, timbre="felt_low", pad=.034, melody=.070, width=3.0,
         chords=[[45,52,60,64],[41,48,57,60],[38,45,53,60],[40,47,55,62]],
         notes=[(1,64),(7.5,67),(14,62),(21,60),(27.5,59)],
         description="Casa del abismo: piano de fieltro grave, silencios y armonía melancólica."),
    dict(name="chess", seconds=48, timbre="celesta", pad=.034, melody=.062, width=4.1,
         chords=[[46,53,58,65],[43,50,58,62],[39,46,55,62],[41,48,58,63]],
         notes=[(3,82),(12,77),(22,79),(31,74),(41,77)],
         description="Ajedrez: celesta solitaria y acordes monumentales, sin percusión."),
    dict(name="tunnels", seconds=32, timbre="glass", pad=.023, melody=.062, width=2.9,
         chords=[[49,56,61,68],[52,59,63,68],[45,52,61,64],[47,54,61,66]],
         notes=[(1,85),(2.8,80),(6.2,87),(11,83),(14.8,78),(18,85),(20.3,80),(25.5,76),(29,83)],
         description="Túneles: notas acuáticas irregulares y cristal con eco corto."),
    dict(name="cloud_hotel", seconds=48, timbre="tape", pad=.038, melody=.055, width=3.3,
         chords=[[56,63,68,72],[53,60,65,68],[49,56,61,65],[51,58,63,70]],
         notes=[(2,80),(8,75),(15,77),(23,72),(31,73),(39,70),(45,75)],
         description="Hotel de nubes: sintetizador cálido desafinado y melodía suspendida."),
    dict(name="road", seconds=48, timbre="piano", pad=.020, melody=.090, width=2.1,
         chords=[[48,55,60,64],[45,52,57,60],[53,60,65,69],[43,50,55,62]],
         notes=[(1,72),(4,67),(7,76),(10,79),(13,69),(16,64),(19,72),(22,76),
                (25,77),(28,72),(31,81),(34,79),(37,74),(40,67),(43,71),(46,74)],
         description="Carretera: piano nostálgico en arpegios y un fondo de cinta tenue."),
]


def pad(theme: dict, seed: int) -> np.ndarray:
    seconds = theme["seconds"]
    size = seconds * RATE
    t = np.arange(size, dtype=np.float64) / RATE
    result = np.zeros((size, 2), dtype=np.float64)
    rng = np.random.default_rng(seed)
    spacing = seconds / len(theme["chords"])
    for chord_index, chord in enumerate(theme["chords"]):
        distance = (t - chord_index * spacing + seconds / 2) % seconds - seconds / 2
        window = np.where(np.abs(distance) < spacing,
                          .5 + .5 * np.cos(np.pi * distance / spacing), 0)
        for note_index, note in enumerate(chord):
            for detune in (-1, 1):
                frequency = round(hz(note) * (1 + detune * .0017) * seconds) / seconds
                phase = rng.uniform(0, 2*np.pi)
                tone = np.sin(2*np.pi*frequency*t+phase)
                tone += .11*np.sin(4*np.pi*frequency*t+phase*2)
                tone += .018*np.sin(6*np.pi*frequency*t+phase*3)
                breath = .84+.10*np.sin(2*np.pi*(note_index+1)*t/seconds+phase)
                tone *= window * breath * theme["pad"]
                result[:, 0] += tone*(1 if detune < 0 else .60)
                result[:, 1] += tone*(.60 if detune < 0 else 1)
    return result


def event(buffer: np.ndarray, second: float, note: int, timbre: str, pan: float,
          level: float, width: float) -> None:
    duration = min(14., width*4.5)
    size = int(duration * RATE)
    t = np.arange(size, dtype=np.float64)/RATE
    f = hz(note)
    phase = 2*np.pi*f*t
    if timbre in ("piano", "felt_low"):
        tone = np.sin(phase)+.23*np.sin(phase*2.001)*np.exp(-t/.7)
        tone += .085*np.sin(phase*3.002)*np.exp(-t/.4)
        envelope = (1-np.exp(-t/.025))**2*np.exp(-t/width)
        if timbre == "felt_low": tone *= .86+.14*np.exp(-t/2)
    elif timbre in ("glass", "glass_low"):
        tone = np.sin(phase)+.15*np.sin(phase*2.756)*np.exp(-t/1.8)
        tone += .04*np.sin(phase*4.01)*np.exp(-t/1.1)
        envelope = (1-np.exp(-t/.09))**2*np.exp(-t/width)
    elif timbre == "celesta":
        tone = np.sin(phase)+.28*np.sin(phase*2.002)*np.exp(-t/1.2)
        tone += .045*np.sin(phase*3.998)*np.exp(-t/.9)
        envelope = (1-np.exp(-t/.06))**2*np.exp(-t/width)
    elif timbre == "toy_keys":
        tone = np.sin(phase)+.20*np.sin(phase*2)*np.exp(-t/.8)
        tone += .075*np.sin(phase*3)*np.exp(-t/.4)
        envelope = (1-np.exp(-t/.04))**2*np.exp(-t/width)
    elif timbre == "plucked":
        tone = np.sin(phase)+.31*np.sin(phase*2)*np.exp(-t/.35)
        tone += .11*np.sin(phase*3)*np.exp(-t/.22)
        envelope = (1-np.exp(-t/.025))**2*np.exp(-t/width)
    elif timbre == "flute":
        vibrato = .014*np.sin(2*np.pi*3.5*t)*(1-np.exp(-t))
        tone = np.sin(phase+vibrato)+.045*np.sin(phase*3)
        envelope = (1-np.exp(-t/.5))**2*np.exp(-t/width)
    elif timbre == "tape":
        wow = .026*np.sin(2*np.pi*.7*t)
        tone = np.sin(phase+wow)+.14*np.sin(phase*2+wow)
        tone += .045*np.sin(phase*3+wow)
        envelope = (1-np.exp(-t/.3))**2*np.exp(-t/width)
    else:
        raise ValueError(timbre)
    end = np.clip((t-duration+1.5)/1.5, 0, 1)
    envelope *= .5+.5*np.cos(np.pi*end)
    mono = tone*envelope*level
    left = np.cos((pan+1)*np.pi/4)
    right = np.sin((pan+1)*np.pi/4)
    indices = (int(second*RATE)+np.arange(size)) % len(buffer)
    buffer[indices, 0] += mono*left
    buffer[indices, 1] += mono*right


def choose_loop_boundary(signal: np.ndarray) -> np.ndarray:
    # Choose a low-slope stereo junction within the final two seconds; no fade gap.
    last = signal[-2*RATE:]
    steps = np.abs(np.diff(last, axis=0)).max(axis=1)
    score = steps + np.abs(last[1:]).max(axis=1)*.002
    index = len(signal)-len(last)+int(np.argmin(score))+1
    return np.roll(signal, -index, axis=0)


def main() -> None:
    report = {}
    for index, theme in enumerate(THEMES):
        signal = pad(theme, seed=14000+index)
        for note_index, (second, note) in enumerate(theme["notes"]):
            pan = .5*np.sin(note_index*1.71+index*.49)
            event(signal, second, note, theme["timbre"], pan,
                  theme["melody"], theme["width"])
        signal = choose_loop_boundary(finish(signal, .125))
        name = "music_"+theme["name"]+"_14"
        result = export(name, signal)
        result["description"] = theme["description"]
        result["timbre"] = theme["timbre"]
        result["sha256"] = hashlib.sha256((OUTPUT/(name+".ogg")).read_bytes()).hexdigest()
        report[name+".ogg"] = result
        print(name+": "+str(result["file_bytes"])+" bytes", flush=True)
    assert len({r["sha256"] for r in report.values()}) == 12
    report_path = ROOT/"tools/audio_validation_14.json"
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
    total = sum(r["file_bytes"] for r in report.values())
    print("PASS 12 unique loops, total "+str(total)+" bytes", flush=True)


if __name__ == "__main__":
    main()
