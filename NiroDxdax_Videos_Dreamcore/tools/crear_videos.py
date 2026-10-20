#!/usr/bin/env python3
"""Export existing covers and compositions as watermarked YouTube videos.

Requires FFmpeg/ffprobe. Run from any directory; no network access is used.
"""
import concurrent.futures
import hashlib
import json
import tempfile
from pathlib import Path
import subprocess
import time

OUT = Path(__file__).resolve().parents[1]
ROOT = OUT.parent
AUDIO = ROOT / 'DreamCore_2_4_Resonancias/Banda_sonora'
COVERS = ROOT / 'DreamCore_2_4_Portadas'
MARK = ROOT / 'NiroDxdax_Kit_YouTube_Dreamcore/Listo_para_YouTube/05_Marca_para_videos_1200x400.png'
TRACKS = json.loads((AUDIO / 'catalogo.json').read_text())
ART = {x['number']: x for x in json.loads((COVERS / 'catalogo.json').read_text())}


def run(args):
    subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', *map(str, args)], check=True)


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def export(track):
    started = time.monotonic()
    art = ART[track['number']]
    assert (track['title'], track['map']) == (art['title'], art['map'])
    source_audio = AUDIO / 'Musica' / track['file']
    source_art = COVERS / 'Imagenes' / art['file']
    cover = OUT / 'Portadas' / art['file']
    video = OUT / 'Videos' / Path(track['file']).with_suffix('.mp4')
    if video.exists():
        probe = subprocess.run(['ffprobe', '-v', 'error', '-show_format', '-of', 'json', str(video)], capture_output=True, text=True)
        duration = float(json.loads(probe.stdout or '{}').get('format', {}).get('duration', '0'))
        if probe.returncode or abs(duration - track['seconds']) >= .1:
            video.unlink()  # Resume safely after an interrupted local export.
    if not cover.exists():
        # One composited frame, reused for the entire video; avoid repeated scaling.
        run(['-filter_complex_threads', '1', '-i', source_art, '-i', MARK,
             '-filter_complex', '[0:v]scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2,setsar=1[base];[1:v]scale=350:-1,format=rgba,colorchannelmixer=aa=0.85[mark];[base][mark]overlay=W-w-36:30,format=rgb24[v]',
             '-map', '[v]', '-frames:v', '1', '-q:v', '2', '-update', '1', cover])
    if not video.exists():
        with tempfile.TemporaryDirectory(prefix='niro-video-') as work:
            segment = Path(work) / 'still.mp4'
            # Encode one 30-second still GOP; repeat it without re-encoding.
            # This preserves the same 24 fps/quality and saves export CPU time.
            run(['-loop', '1', '-framerate', '24', '-i', cover, '-t', '30',
                 '-an', '-c:v', 'libx264', '-threads', '2', '-preset', 'veryfast',
                 '-tune', 'stillimage', '-crf', '20',
                 '-vf', 'scale=in_range=full:out_range=tv:out_color_matrix=bt709,format=yuv420p',
                 '-pix_fmt', 'yuv420p', '-color_range', 'tv', '-colorspace', 'bt709',
                 '-color_primaries', 'bt709', '-color_trc', 'bt709',
                 '-r', '24', '-g', '720', '-keyint_min', '720', '-sc_threshold', '0', segment])
            # Map audio explicitly: MP3 source files also contain attached artwork.
            run(['-stream_loop', '-1', '-i', segment, '-i', source_audio,
                 '-map', '0:v:0', '-map', '1:a:0', '-t', track['seconds'],
                 '-c:v', 'copy', '-c:a', 'aac', '-b:a', '160k', '-ar', '44100',
                 '-movflags', '+faststart', '-metadata', 'title=' + track['title'],
                 '-metadata', 'artist=NiroDxdax', '-metadata', 'album=DreamCore - Resonancias',
                 '-metadata', 'comment=Portada ilustrada y música con asistencia de IA. Mapa: ' + track['map'], video])
    info = json.loads(subprocess.check_output([
        'ffprobe', '-v', 'error', '-show_streams', '-show_format', '-of', 'json', str(video)]))
    vs = [s for s in info['streams'] if s['codec_type'] == 'video']
    au = [s for s in info['streams'] if s['codec_type'] == 'audio']
    assert len(vs) == len(au) == 1
    assert (vs[0]['codec_name'], vs[0]['width'], vs[0]['height'], vs[0]['pix_fmt']) == ('h264', 1920, 1080, 'yuv420p')
    assert vs[0]['r_frame_rate'] == '24/1' and au[0]['codec_name'] == 'aac'
    assert int(au[0]['channels']) == 2
    for stream in [vs[0], au[0]]:
        assert abs(float(stream['duration']) - track['seconds']) < .1
    result = dict(number=track['number'], title=track['title'], map=track['map'],
                  seconds=track['seconds'], video=str(video.relative_to(OUT)),
                  cover=str(cover.relative_to(OUT)), bytes=video.stat().st_size,
                  sha256=sha(video), source_audio_sha256=sha(source_audio),
                  source_cover_sha256=sha(source_art), watermark_sha256=sha(MARK))
    print(f"{track['number']:02d}/49 OK | {track['title']} | {video.stat().st_size / 2**20:.1f} MiB | {time.monotonic()-started:.1f}s", flush=True)
    return result


def main():
    assert len(TRACKS) == len(ART) == 49
    for folder in ['Videos', 'Portadas']:
        (OUT / folder).mkdir(exist_ok=True)
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        results = sorted(pool.map(export, TRACKS), key=lambda x: x['number'])
    (OUT / 'MANIFIESTO.json').write_text(json.dumps(results, ensure_ascii=False, indent=2) + '\n')
    print(f"COMPLETE: {len(results)} videos; {sum(x['seconds'] for x in results)} seconds; {sum(x['bytes'] for x in results) / 2**20:.1f} MiB", flush=True)


if __name__ == '__main__':
    main()
