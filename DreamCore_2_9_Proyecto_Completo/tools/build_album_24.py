from pathlib import Path
import json,subprocess,shutil,hashlib,html,urllib.parse
p=Path(__file__).resolve().parents[1];album=p/'Banda_sonora';tracks=json.loads((p/'tools/music_catalog_24.json').read_text())
report=json.loads((p/'tools/audio_validation_24.json').read_text())
evidence=[]
for t in tracks:
    file=album/'Musica'/t['file'];cover=album/'Miniaturas'/(t['cover']+'.png')
    assert cover.is_file(),cover
    encoded=file.with_name('tagged_'+file.name)
    before=subprocess.check_output(['ffmpeg','-v','error','-i',str(file),'-map','0:a:0','-f','framemd5','-'])
    subprocess.run(['ffmpeg','-v','error','-y','-i',str(file),'-i',str(cover),'-map','0:a:0','-map','1:v:0','-c','copy','-id3v2_version','3','-metadata:s:v','title='+t['map'],'-metadata:s:v','comment=Cover (front)','-disposition:v','attached_pic',str(encoded)],check=True)
    after=subprocess.check_output(['ffmpeg','-v','error','-i',str(encoded),'-map','0:a:0','-f','framemd5','-'])
    assert before==after,'cover remux must not change decoded audio '+file.name
    probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(encoded)]))
    assert any(s['codec_type']=='video' and s.get('disposition',{}).get('attached_pic')==1 for s in probe['streams'])
    assert probe['format']['tags']['title']==t['title']
    encoded.replace(file)
    item=dict(world=t['world'],file=t['file'],sha256=hashlib.sha256(file.read_bytes()).hexdigest(),bytes=file.stat().st_size,audio_identical_after_cover=True,embedded_cover=True)
    evidence.append(item);report[t['resource']]['mp3'].update(sha256=item['sha256'],bytes=item['bytes'],embedded_cover=True)
    print(f'Portada {t["number"]:02}',flush=True)
(p/'tools/audio_validation_24.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
(p/'tools/album_validation_24.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n')
public=[{k:t[k] for k in ['number','world','title','map','description','seconds','file','cover']} for t in tracks]
(album/'catalogo.json').write_text(json.dumps(public,ensure_ascii=False,indent=2)+'\n')
(album/'DreamCore_Resonancias.m3u8').write_text('#EXTM3U\n'+''.join(f'#EXTINF:{t["seconds"]},{t["title"]} — {t["map"]}\nMusica/{t["file"]}\n' for t in tracks),encoding='utf-8')
page='''<!doctype html>
<html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>DreamCore · Resonancias</title>
<style>
:root{color-scheme:dark;font-family:system-ui,sans-serif;background:#101924;color:#e5eeee}*{box-sizing:border-box}body{margin:0;background:radial-gradient(ellipse at top,#34465766,transparent 65%),#101924}main{max-width:1180px;margin:auto;padding:38px 24px 240px}h1{font-size:clamp(2.2rem,6vw,4.5rem);font-weight:500;letter-spacing:-.05em;margin:10px 0}p{color:#acbbc9;line-height:1.65}.eyebrow{color:#b9d7d4;font-size:.78rem;letter-spacing:.25em}input,button,select,a.download{font:inherit;border:1px solid #435666;border-radius:10px;background:#1b2b39;color:#e9f1ed;padding:10px 14px}button,a{cursor:pointer}button:hover,button:focus-visible,a.download:hover{background:#31485a}button:focus-visible,input:focus-visible,select:focus-visible,a:focus-visible{outline:2px solid #f0d8bc;outline-offset:3px}.filters{display:flex;gap:10px;flex-wrap:wrap;margin:26px 0}input{flex:1;min-width:200px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:20px}.card{overflow:hidden;border:1px solid #314351;border-radius:14px;background:#182532}.card[aria-current=true]{border-color:#d5c7ac;box-shadow:0 0 0 1px #d5c7ac}.card button{padding:0;border:0;border-radius:0;text-align:left;background:transparent;display:block;width:100%}.card img{display:block;width:100%;aspect-ratio:16/9;object-fit:contain;background:#0e1720}.info{display:block;padding:14px}.track-number{font-size:.72rem;letter-spacing:.12em;color:#b2c9c7}.name{display:block;font-size:1rem;margin:6px 0;line-height:1.4}.map{display:block;font-size:.8rem;color:#afc1cf;line-height:1.5}.length{display:block;margin-top:10px;color:#c4b7a7;font-size:.8rem}.player{position:fixed;bottom:0;left:0;right:0;border-top:1px solid #49606d;background:#12202bf5;backdrop-filter:blur(18px);padding:16px 24px}.player-inner{max-width:1132px;margin:auto;display:flex;gap:18px;align-items:center}.player img{width:150px;aspect-ratio:16/9;border-radius:8px;object-fit:contain}.playing{flex:1;min-width:0}.playing strong{font-weight:500;font-size:1.1rem}.playing small{display:block;color:#b2c5d4;margin-top:5px}audio{width:100%;height:36px;margin:12px 0}.actions{display:flex;gap:8px;align-items:center;flex-wrap:wrap}.actions button,.actions a{font-size:.85rem;padding:7px 10px;text-decoration:none}.status{font-size:.8rem;color:#e1bd9b;margin:8px 0 0}.counter{font-size:.85rem;color:#b0c7c4}.empty{padding:30px}footer{margin-top:40px;color:#94a9b8;font-size:.8rem;line-height:1.7}@media(max-width:600px){main{padding:24px 16px 285px}.player{padding:12px 16px}.player-inner{gap:12px;align-items:flex-start}.player img{width:85px;margin-top:4px}.playing strong{font-size:.95rem}.actions{gap:5px}.actions button,.actions a{font-size:.75rem;padding:7px}h1{margin:16px 0}.grid{grid-template-columns:repeat(auto-fill,minmax(190px,1fr))}}
</style>
<main><div class="eyebrow">BANDA SONORA ORIGINAL · VOLUMEN 2.4</div><h1>Resonancias</h1><p>Una música para cada lugar que recuerdas sin haber estado allí.<br>49 composiciones · 1 h 44 min · inquietud y descanso, sin prisa.</p><div class="filters"><input id="search" type="search" aria-label="Buscar música o escenario" placeholder="Buscar un sueño, una pista, un lugar…"><select id="group" aria-label="Filtrar mundos"><option value="all">Todos los lugares</option><option value="main">El viaje y el inicio</option><option value="dark">Dark Fantasy</option><option value="secret">Refugios y fotografías</option><option value="echo">Lugares imposibles</option><option value="new">Desvíos y otros sueños</option></select></div><p id="counter" class="counter"></p><div id="grid" class="grid"></div><footer>Las miniaturas son vistas reales del motor GLES2, renderizadas para este álbum; no son capturas del teléfono. La portada del inicio muestra El Umbral.<br>Las 49 composiciones son síntesis originales. El MP3 de referencia no pudo analizarse porque supera el límite de transferencia de 32 MB.<br>La casa antigua conserva una pista de archivo aunque el recorrido la consolida en la estación del último sueño.<br>Abre este archivo después de extraer todo el ZIP. También puedes abrir los MP3 o la lista M3U8 con tu reproductor habitual. No requiere conexión.</footer></main>
<section class="player" aria-label="Reproductor"><div class="player-inner"><img id="cover" alt=""><div class="playing"><strong id="title"></strong><small id="map"></small><audio id="audio" controls preload="none"></audio><div class="actions"><button id="previous" aria-label="Pista anterior">Anterior</button><button id="next">Siguiente</button><button id="shuffle" aria-pressed="false">Aleatorio: no</button><button id="repeat" aria-pressed="false">Repetir: no</button><a id="download" class="download" download>Descargar MP3</a></div><div id="status" class="status" role="status">Pulsa una miniatura para escuchar.</div></div></div></section>
<script>
const tracks=__CATALOG__;
const $=s=>document.getElementById(s), audio=$('audio');let current=0,shuffle=false,repeat=false;
const path=t=>'Musica/'+encodeURIComponent(t.file),image=t=>'Miniaturas/'+t.cover+'.png';
const normalize=s=>s.normalize('NFD').replace(/[\\u0300-\\u036f]/g,'').toLowerCase();
function group(t){if(t.world.startsWith('dark'))return 'dark';if(t.world.startsWith('echo'))return 'echo';if(t.world.startsWith('pocket')||t.world.startsWith('photo'))return t.world==='pocket_call_house'?'new':'secret';if(t.number>=42)return 'new';return 'main'}
function render(){const q=normalize($('search').value),filter=$('group').value;const grid=$('grid');grid.replaceChildren();let count=0;
 tracks.forEach((t,i)=>{if((filter!=='all'&&group(t)!==filter)||!normalize(t.title+' '+t.map+' '+t.description).includes(q))return;count++;
 const card=document.createElement('article');card.className='card';card.setAttribute('aria-current',i===current?'true':'false');card.dataset.index=i;
 const b=document.createElement('button');b.type='button';b.setAttribute('aria-label','Escuchar '+t.title+' · '+t.map);b.onclick=()=>select(i,true);
 const img=document.createElement('img');img.src=image(t);img.alt=t.map;img.loading='lazy';b.append(img);
 const info=document.createElement('span');info.className='info';for(const [cls,value] of [['track-number',String(t.number).padStart(2,'0')+' / RESONANCIAS'],['name',t.title],['map',t.map],['length',Math.floor(t.seconds/60)+':'+String(t.seconds%60).padStart(2,'0')+' · '+t.description]]){const el=document.createElement('span');el.className=cls;el.textContent=value;info.append(el)}b.append(info);card.append(b);grid.append(card)});
 $('counter').textContent=count+' de '+tracks.length+' composiciones';if(!count){const e=document.createElement('p');e.className='empty';e.textContent='No hay resultados para esa búsqueda.';grid.append(e)}}
function select(i,play){current=(i+tracks.length)%tracks.length;const t=tracks[current];audio.src=path(t);$('title').textContent=t.title;$('map').textContent=t.map;$('cover').src=image(t);$('cover').alt=t.map;$('download').href=path(t);$('download').download=t.file;document.querySelectorAll('.card').forEach(c=>c.setAttribute('aria-current',Number(c.dataset.index)===current?'true':'false'));$('status').textContent=play?'Cargando la música…':'Pulsa reproducir para escuchar.';if(play)audio.play().catch(()=>$('status').textContent='Pulsa reproducir en el control de audio.');}
function next(){let n=current+1;if(shuffle){do{n=Math.floor(Math.random()*tracks.length)}while(n===current)}select(n,true)}
audio.addEventListener('playing',()=>$('status').textContent=tracks[current].description);audio.addEventListener('error',()=>$('status').textContent='No se pudo abrir el MP3. Extrae todo el ZIP y conserva la carpeta Musica junto a este archivo.');audio.addEventListener('ended',()=>repeat?(audio.currentTime=0,audio.play().catch(()=>{})):next());
audio.addEventListener('pause',()=>{if(!audio.ended)$('status').textContent='En pausa · '+tracks[current].map});$('next').onclick=next;$('previous').onclick=()=>select(current-1,true);$('shuffle').onclick=()=>{shuffle=!shuffle;$('shuffle').textContent='Aleatorio: '+(shuffle?'sí':'no');$('shuffle').setAttribute('aria-pressed',String(shuffle))};$('repeat').onclick=()=>{repeat=!repeat;$('repeat').textContent='Repetir: '+(repeat?'sí':'no');$('repeat').setAttribute('aria-pressed',String(repeat))};$('search').oninput=render;$('group').onchange=render;select(0,false);render();
</script></html>'''.replace('__CATALOG__',json.dumps(public,ensure_ascii=False).replace('</','<\\/'))
(album/'ESCUCHAR.html').write_text(page)
readme='''# DreamCore · Resonancias 2.4

49 composiciones originales, 103 minutos y 36 segundos de música. Una pista para cada uno de los 48 escenarios registrados, incluido el interior antiguo conservado como archivo, más una exclusiva para el inicio. El inicio usa una vista del Umbral como portada.

Extrae **todo** el ZIP y abre **ESCUCHAR.html** en tu navegador. Pulsa una miniatura. Puedes buscar por lugar o canción, filtrar familias, avanzar, pausar, repetir, escuchar al azar y descargar un MP3 individual. Hay un único reproductor activo.

Los MP3 también funcionan por separado y llevan título, número de pista y miniatura incrustada. La lista **DreamCore_Resonancias.m3u8** abre el álbum en reproductores compatibles. En algunos gestores Android es más sencillo abrir los MP3 directamente. Conserva las carpetas Musica y Miniaturas junto al HTML.

Cada composición tiene un motivo, armonía, pulso, instrumentación y arreglo propios; los cuatro tramos introducen, responden, interrumpen y cierran la frase. Es una banda sonora de síntesis original. El archivo de referencia supera el límite de transferencia de 32 MB y no pudo escucharse ni analizarse: esta colección interpreta la dirección de calma e inquietud pedida en la conversación. No contiene muestras extraídas del MP3.

Las miniaturas se renderizaron con las geometrías y shaders reales GLES2. Son vistas del escenario, no capturas Android ni promesas de fidelidad fotográfica.

| Nº | Composición | Mapa | Duración | Miniatura |
| --- | --- | --- | --- | --- |
'''
for t in tracks:
    link='Musica/'+urllib.parse.quote(t['file']);img='Miniaturas/'+t['cover']+'.png'
    readme+=f'| {t["number"]:02} | [{t["title"]}]({link}) | {t["map"]} | {t["seconds"]//60}:{t["seconds"]%60:02} | <img src="{img}" width="160" alt="{html.escape(t["map"])}"> |\n'
(album/'CATALOGO.md').write_text(readme)
print('49 MP3 covers verified, offline player and catalog written')
