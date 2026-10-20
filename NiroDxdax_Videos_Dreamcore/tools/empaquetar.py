#!/usr/bin/env python3
"""Create independent ZIP downloads under GitHub's individual-file size limit."""
import csv
import hashlib
import html
import io
import json
from pathlib import Path
from urllib.parse import quote
import zipfile

OUT = Path(__file__).resolve().parents[1]
ROOT = OUT.parent
RAW = 'https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/'
BASE = RAW + OUT.name + '/'


def esc(value):
    return html.escape(str(value), quote=True)


def player(tracks):
    cards = '\n'.join(
        f'<button class="track" data-video="{esc(quote(t["video"]))}" data-title="{esc(t["title"])}" data-map="{esc(t["map"])}" data-cover="{esc(quote(t["cover"]))}"><img loading="lazy" src="{esc(quote(t["cover"]))}" alt="Portada de {esc(t["title"])}"><strong>{t["number"]:02d} · {esc(t["title"])}</strong><span>{esc(t["map"])}</span></button>'
        for t in tracks)
    first = tracks[0]
    return f'''<!doctype html>
<html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>NiroDxdax · DreamCore · Videos musicales</title>
<style>*{{box-sizing:border-box}}body{{margin:0;background:#111426;color:#f7e9ef;font:16px system-ui,sans-serif}}main{{max-width:1240px;padding:30px 20px;margin:auto}}h1{{font-family:Georgia,serif;font-size:clamp(30px,5vw,54px);font-weight:normal}}p,small,span{{color:#c3c9dc}}video{{width:100%;max-height:65vh;background:#090c17;border-radius:16px}}.grid{{display:grid;grid-template-columns:repeat(auto-fill,minmax(230px,1fr));gap:18px;margin-top:32px}}.track{{border:1px solid #44475c;background:#20243b;color:inherit;padding:0 0 16px;text-align:left;border-radius:12px;overflow:hidden;cursor:pointer}}.track:focus-visible,.track:hover{{outline:2px solid #edb6db}}.track img{{width:100%;aspect-ratio:16/9;object-fit:contain}}strong,span{{display:block;padding:8px 12px 0}}a{{color:#b7e6ff}}.actions{{display:flex;gap:25px;flex-wrap:wrap;margin:18px 0}}button{{font:inherit}}footer{{margin:35px 0}}</style>
<main><p>NIRODXDAX · RESONANCIAS</p><h1>Un lugar para cada recuerdo</h1>
<p>Selecciona una portada para escuchar su música. Cada MP4 contiene la canción completa y una portada fija con marca de agua. Ilustraciones de los mapas; material creado con asistencia de IA.</p>
<video id="video" controls preload="none" playsinline poster="{esc(quote(first['cover']))}" src="{esc(quote(first['video']))}"></video>
<h2 id="title">{esc(first['title'])}</h2><p id="map">{esc(first['map'])}</p>
<div class="actions"><a id="download" download href="{esc(quote(first['video']))}">Descargar este video</a><a id="cover" download href="{esc(quote(first['cover']))}">Descargar portada con marca</a></div>
<p>1080p · 16:9 · H.264 / AAC estéreo · Sin reproducción automática. Si el navegador abre el MP4, usa su opción Guardar video.</p>
<div class="grid">{cards}</div><footer>Extrae todo el ZIP antes de abrir esta página. Videos y portadas también se pueden abrir directamente desde sus carpetas.</footer></main>
<script>const video=document.getElementById('video');document.querySelectorAll('.track').forEach(button=>button.addEventListener('click',()=>{{video.pause();video.src=button.dataset.video;video.poster=button.dataset.cover;document.getElementById('title').textContent=button.dataset.title;document.getElementById('map').textContent=button.dataset.map;document.getElementById('download').href=button.dataset.video;document.getElementById('cover').href=button.dataset.cover;video.load();video.scrollIntoView({{behavior:'smooth',block:'center'}});}}));</script></html>'''


def readme(tracks):
    return f'''# NiroDxdax · DreamCore · Videos musicales

Este paquete contiene **{len(tracks)} videos MP4** y sus portadas con **NiroDxdax** como marca de agua visible. Cada video combina la portada ilustrada de su mapa con la canción completa correspondiente.

1. Descarga el ZIP y extrae todo su contenido.
2. Abre `VER_VIDEOS.html` para elegir una canción, o reproduce cualquier archivo de `Videos`.
3. Para YouTube, selecciona el MP4 al subir un video. Puedes usar el JPG de `Portadas` como miniatura.

Formato: 1920 × 1080, horizontal 16:9, 24 FPS, H.264 y AAC estéreo a 160 kb/s. Las imágenes permanecen fijas durante la canción. La resolución de exportación es 1080p; las portadas originales se adaptan sin recortar títulos. El álbum fue creado con asistencia de IA.

Cada ZIP es independiente y puede extraerse por separado. Los nombres de archivos son compatibles con Windows. `MANIFIESTO.json` identifica cada pareja y contiene las sumas SHA-256 de los MP4. Las portadas representan los mapas mediante ilustraciones; los videos no son grabaciones de una partida.

## Canciones incluidas

''' + '\n'.join(f'- {t["number"]:02d}. **{t["title"]}** — {t["map"]} ({t["seconds"]//60}:{t["seconds"]%60:02d})' for t in tracks) + '\n'


def main():
    tracks = json.loads((OUT / 'MANIFIESTO.json').read_text())
    validation = json.loads((OUT / 'VALIDACION.json').read_text())
    assert len(tracks) == 49 and validation['videos'] == 49 and validation['full_decode_passed']
    groups, group, size = [], [], 0
    for track in tracks:
        weight = sum((OUT / track[key]).stat().st_size for key in ['video', 'cover'])
        if group and size + weight > 88 * 2**20:
            groups.append(group)
            group, size = [], 0
        group.append(track)
        size += weight
    if group:
        groups.append(group)
    packages = []
    for part, group in enumerate(groups, 1):
        name = f'NiroDxdax_Videos_Parte_{part}.zip'
        root = f'NiroDxdax_Videos_{group[0]["number"]:02d}-{group[-1]["number"]:02d}'
        path = ROOT / name
        with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
            for track in group:
                for key in ['video', 'cover']:
                    archive.write(OUT / track[key], root + '/' + track[key])
            archive.writestr(root + '/VER_VIDEOS.html', player(group))
            archive.writestr(root + '/LEEME.md', readme(group))
            archive.writestr(root + '/MANIFIESTO.json', json.dumps(group, ensure_ascii=False, indent=2))
        assert path.stat().st_size < 100 * 2**20
        with zipfile.ZipFile(path) as archive:
            assert archive.testzip() is None
            assert len([n for n in archive.namelist() if n.endswith('.mp4')]) == len(group)
        item = dict(part=part, first=group[0]['number'], last=group[-1]['number'],
                    count=len(group), file=name, bytes=path.stat().st_size,
                    sha256=hashlib.sha256(path.read_bytes()).hexdigest())
        packages.append(item)
        print(item, flush=True)
    (OUT / 'PAQUETES.json').write_text(json.dumps(packages, ensure_ascii=False, indent=2) + '\n')
    (OUT / 'LEEME.md').write_text(readme(tracks))
    (OUT / 'VER_VIDEOS.html').write_text(player(tracks))
    lines = ['# NiroDxdax · 49 videos DreamCore', '',
             '**49 MP4 con música completa, portada correspondiente y marca de agua NiroDxdax.** 1080p, formato horizontal 16:9. Duración total: **1 h 43 min 36 s**.', '',
             '## Descargar la colección', '',
             'Descarga todos los paquetes para tener los 49 videos. Cada ZIP se abre por separado y también incluye las portadas con marca y un reproductor local (`VER_VIDEOS.html`).', '']
    for p in packages:
        lines.append(f'- **[Descargar parte {p["part"]}: videos {p["first"]:02d}–{p["last"]:02d}]({RAW}{p["file"]})** — {p["bytes"]/1e6:.1f} MB, {p["count"]} videos.')
    lines += ['', 'Extrae el ZIP completo. Puedes abrir los MP4 directamente o subirlos a YouTube y elegir sus JPG como miniatura. Cada video tiene una imagen fija durante su música; son ilustraciones de los mapas.', '',
              '## Descargar un video o su portada', '',
              'Si un enlace abre el video en el navegador, usa **Guardar video como** o el menú de descarga del reproductor.', '',
              '| N.º | Música | Mapa | Duración | Archivos |', '| --- | --- | --- | --- | --- |']
    for t in tracks:
        lines.append(f'| {t["number"]:02d} | {t["title"]} | {t["map"]} | {t["seconds"]//60}:{t["seconds"]%60:02d} | [MP4]({BASE}{quote(t["video"])}) · [Portada]({BASE}{quote(t["cover"])}) |')
    lines += ['', 'Exportaciones verificadas mediante decodificación completa y comparación del audio con cada MP3 correspondiente. Material realizado con asistencia de IA.', '']
    (OUT / 'DESCARGAR.md').write_text('\n'.join(lines))
    buffer = io.StringIO()
    writer = csv.writer(buffer, lineterminator='\n')
    writer.writerow(['Numero', 'Musica', 'Mapa', 'Segundos', 'Video', 'Portada'])
    for t in tracks:
        writer.writerow([t['number'], t['title'], t['map'], t['seconds'], t['video'], t['cover']])
    (OUT / 'CATALOGO.csv').write_text(buffer.getvalue(), encoding='utf-8-sig')
    (OUT / 'CHECKSUMS.txt').write_text('\n'.join(f'{p["sha256"]}  {p["file"]}' for p in packages) + '\n')


if __name__ == '__main__':
    main()
