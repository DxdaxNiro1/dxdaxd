"""Seven original ambient compositions. No sound from the video references is sampled."""
from pathlib import Path
import json,tempfile,wave,subprocess
import numpy as np
from generate_soundtrack_24 import compose,decode_metrics,RATE
r=Path(__file__).resolve().parents[1];raw=r/'app/src/main/res/raw';catalog=[];report={}
worlds=['horror_atrium32','horror_garden32','horror_road32','horror_forest32','horror_screens32','horror_house32','horror_pool32']
maps=['El coro de las puertas torcidas','El jardín de las antenas','El camino de los testigos','El bosque del sol vacío','El herbario de las pantallas','La casa del hilo','La piscina del horizonte']
titles=['El coro detrás de la puerta','Las antenas saben tu nombre','El farol que queda encendido','Un sol que no calienta','Flores en una señal vacía','Lo que sostiene una casa','El mar escucha desde abajo']
for i,(world,mapname,title) in enumerate(zip(worlds,maps,titles)):
 n=56+i;c=dict(number=n,world=world,title=title,map=mapname,lead=['choir','glass','tape','bow','musicbox','felt','water'][i],root=[46,49,42,41,51,54,45][i],motif=[[0,1,7,3,10,5],[0,7,1,8,3,12],[0,6,1,9,2,7],[0,1,8,5,11,3],[0,3,1,7,10,6],[0,7,2,9,1,5],[0,5,1,8,3,10]][i],harmony=[0,1,-5,3],pulse=9.0+i*.33,seconds=112+i*4,description='composición original: silencio, resonancias y tensión lenta',resource=f'music_score_{n}_32',cover=f'music_cover_{n}_32',seed=32000+i*137)
 out=compose(c)
 with tempfile.TemporaryDirectory() as td:
  wav=Path(td)/'score.wav'
  with wave.open(str(wav),'wb') as f:
   f.setnchannels(2);f.setsampwidth(2);f.setframerate(RATE);f.writeframes((np.clip(out,-.98,.98)*32767).astype('<i2').tobytes())
  dest=raw/(c['resource']+'.ogg');subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','1',str(dest)],check=True)
 report[c['resource']]=decode_metrics(dest);catalog.append(c);print(title,flush=True)
(r/'tools/music_catalog_32.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
(r/'tools/audio_validation_32.json').write_text(json.dumps(report,indent=2)+'\n')
