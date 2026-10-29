from pathlib import Path
import json,tempfile,subprocess,wave
import numpy as np
from scipy.signal import butter,sosfilt
from generate_soundtrack_24 import compose,decode_metrics,RATE
r=Path(__file__).resolve().parents[1];raw=r/'app/src/main/res/raw';report={};catalog=[]
for i,(world,title,mapname)in enumerate([('horizon_doors36','Puertas donde duerme el sol','Las puertas del último sol'),('horizon_crimson36','El peso de otro cielo','Bajo el gigante carmesí'),('horizon_blue36','Antes de abrir los ojos','La roca del océano infinito')]):
 n=65+i;c=dict(number=n,world=world,title=title,map=mapname,lead=['glass','bow','felt'][i],root=[55,39,58][i],motif=[[0,7,9,4,12,7],[0,1,7,6,3,0],[0,4,11,7,2,9]][i],harmony=[0,-5,3,0],pulse=8.8+i*.8,seconds=112+i*4,seed=36000+i*137,resource=f'music_score_{n}_36',cover=f'music_cover_{n}_36')
 data=compose(c)
 with tempfile.TemporaryDirectory()as td:
  p=Path(td)/'sound.wav'
  with wave.open(str(p),'wb')as w:w.setnchannels(2);w.setsampwidth(2);w.setframerate(RATE);w.writeframes((np.clip(data,-.9,.9)*32767).astype('<i2').tobytes())
  dest=raw/(c['resource']+'.ogg');subprocess.run(['ffmpeg','-v','error','-y','-i',str(p),'-c:a','libvorbis','-q:a','0',str(dest)],check=True)
 report[c['resource']]=decode_metrics(dest);catalog.append(c);print(title,'OK',flush=True)
# Continuous forward rush; volume and playback rate follow the actual vertical speed.
rate=22050;t=np.arange(rate*4)/rate;rng=np.random.default_rng(3666);noise=sosfilt(butter(3,[100,6200],btype='bandpass',fs=rate,output='sos'),rng.standard_normal(len(t)))
x=noise*(.55+.12*np.sin(t*np.pi)+.05*np.sin(t*5*np.pi));x*=.72/max(.001,np.max(np.abs(x)));fade=np.minimum(np.minimum(t/.04,(4-t)/.04),1);x*=fade
with tempfile.TemporaryDirectory()as td:
 p=Path(td)/'air.wav'
 with wave.open(str(p),'wb')as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes((x*32767).astype('<i2').tobytes())
 dest=raw/'fx_fall_wind36.ogg';subprocess.run(['ffmpeg','-v','error','-y','-i',str(p),'-c:a','libvorbis','-q:a','1',str(dest)],check=True)
 report['fx_fall_wind36']=decode_metrics(dest)
assert all(x['clipped']==0 for x in report.values())
(r/'tools/audio_validation36.json').write_text(json.dumps(report,indent=2)+'\n');(r/'tools/music_catalog_36.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n')
