"""Original piano notes, mechanical/air cues and three new ambient scores; no sampled recordings."""
from pathlib import Path
import json,tempfile,wave,subprocess
import numpy as np
from generate_soundtrack_24 import compose,decode_metrics,RATE
r=Path(__file__).resolve().parents[1]; raw=r/'app/src/main/res/raw';report={}
def save(dest,out):
 with tempfile.TemporaryDirectory() as td:
  wav=Path(td)/'sound.wav'
  with wave.open(str(wav),'wb') as f:
   f.setnchannels(2);f.setsampwidth(2);f.setframerate(RATE);f.writeframes((np.clip(out,-.98,.98)*32767).astype('<i2').tobytes())
  subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','1',str(dest)],check=True)
 report[dest.stem]=decode_metrics(dest)
rng=np.random.default_rng(300319)
for i,m in enumerate([60,62,64,65,67,69,71]):
 t=np.arange(int(RATE*3.5))/RATE;phase=t*440*2**((m-69)/12)*2*np.pi
 env=(1-np.exp(-t/.006))*np.exp(-t/1.0)*np.minimum(1,(3.5-t)/.25)
 s=(np.sin(phase)+.23*np.sin(2.003*phase)*np.exp(-t/.7)+.08*np.sin(3.01*phase)*np.exp(-t/.25))*env*.45
 save(raw/f'fx_piano{i}_30.ogg',np.column_stack((s,s)))
for i,name in enumerate(['chalk','train','room','wind','tape','paper','projector','valve','carousel']):
 seconds=[.8,3.2,3.8,4,1.1,.7,1.4,2.8,3.4][i];t=np.arange(int(RATE*seconds))/RATE;noise=rng.normal(size=len(t));noise=np.convolve(noise,np.ones(25)/25,mode='same');env=np.sin(np.pi*t/seconds)**2
 if name in ['room','wind']:s=noise*.30+np.sin(t*2*np.pi*([53,71][name=='wind']))*.018
 elif name=='carousel':s=(np.sin(t*2*np.pi*262)+.4*np.sin(t*2*np.pi*311)+.2*np.sin(t*2*np.pi*392))*.06
 elif name=='train':s=noise*.4+np.sin(t*2*np.pi*87)*.13*(.5+.5*np.sin(t*17))
 else:s=noise*.65+np.sin(t*2*np.pi*(110+i*23))*.025
 s*=env;save(raw/f'fx_{name}30.ogg',np.column_stack((s,s*.97)))
catalog=[]
for i,(world,title,mapname,lead,root,motif) in enumerate([
 ('dream_library30','El índice de lo que olvidamos','Biblioteca de lugares olvidados','felt',53,[0,7,0,2,9,5,1]),
 ('dream_fair30','La última vuelta sin testigos','La feria detenida','musicbox',57,[0,3,10,7,1,8,5]),
 ('dream_station30','El horario de una estación imposible','El andén sin nombre','glass',49,[0,9,2,7,13,5,0])]):
 n=i+53;c=dict(number=n,world=world,title=title,map=mapname,lead=lead,root=root,motif=motif,harmony=[[0,-2,5,1],[0,3,-5,1],[0,7,-2,5]][i],pulse=[9.2,7.7,10.3][i],seconds=[120,128,136][i],description='composición original, calma y ecos inciertos',resource=f'music_score_{n}_30',cover=f'music_cover_{n}_30',seed=30300+i*173)
 save(raw/(c['resource']+'.ogg'),compose(c));catalog.append(c);print(title,flush=True)
(r/'tools/music_catalog_30.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
(r/'tools/audio_validation_30.json').write_text(json.dumps(report,indent=2)+'\n')
