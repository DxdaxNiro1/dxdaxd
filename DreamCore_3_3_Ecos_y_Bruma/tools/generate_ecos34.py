"""Original atmospheric synthesis; uploaded commercial references are never sampled or distributed."""
from pathlib import Path
import json,subprocess,tempfile,wave,gc
import numpy as np
from scipy.signal import butter,sosfilt,iirpeak,lfilter
from generate_soundtrack_24 import compose,decode_metrics,RATE
r=Path(__file__).resolve().parents[1];raw=r/'app/src/main/res/raw';report={}
def filtered(x,lo,hi):return sosfilt(butter(2,[lo,hi],btype='bandpass',fs=RATE,output='sos'),x).astype(np.float32)
def breath(seconds,seed):
 rng=np.random.default_rng(seed);t=np.arange(round(seconds*RATE),dtype=np.float32)/RATE
 noise=filtered(rng.standard_normal(len(t)).astype(np.float32),180,3800)
 env=np.maximum(0,np.sin(2*np.pi*t/seconds))**1.6+.70*np.maximum(0,-np.sin(2*np.pi*t/seconds))**1.8
 return noise*env

def murmur(seconds,seed,root=65):
 rng=np.random.default_rng(seed);t=np.arange(round(seconds*RATE),dtype=np.float32)/RATE
 freq=root*(1+.035*np.sin(t*2.1));phase=np.cumsum(freq,dtype=np.float64)*2*np.pi/RATE
 excitation=sum(np.sin(phase*k)/k for k in range(1,18)).astype(np.float32)+rng.normal(0,.09,len(t)).astype(np.float32)
 voice=np.zeros(len(t),np.float32)
 for f,q,level in [(380,6,.8),(920,9,.45),(1670,12,.24)]:
  a,b=iirpeak(f,q,fs=RATE);voice+=lfilter(a,b,excitation).astype(np.float32)*level
 envelope=np.sin(np.pi*np.minimum(t/seconds,1))**2*(.7+.3*np.sin(t*2.4+seed))
 return np.tanh(voice*3)*envelope

def write(name,data,loop=False):
 data=np.asarray(data,dtype=np.float32);peak=float(np.max(np.abs(data)));data=data/max(1,peak/.75)
 if not loop:
  fade=min(len(data)//3,RATE//8);data[:fade]*=np.linspace(0,1,fade).reshape((-1,)+(1,)*(data.ndim-1));data[-fade:]*=np.linspace(1,0,fade).reshape((-1,)+(1,)*(data.ndim-1))
 else:
  # Near-zero endpoints avoid clicks; gentle periodic fade is intentional for breath and wind.
  f=min(RATE//20,len(data)//8);data[:f]*=np.linspace(0,1,f).reshape((-1,)+(1,)*(data.ndim-1));data[-f:]*=np.linspace(1,0,f).reshape((-1,)+(1,)*(data.ndim-1))
 with tempfile.TemporaryDirectory() as td:
  wav=Path(td)/'sound.wav'
  with wave.open(str(wav),'wb')as w:w.setnchannels(1 if data.ndim==1 else 2);w.setsampwidth(2);w.setframerate(RATE);w.writeframes((data*32767).astype('<i2').tobytes())
  dest=raw/(name+'.ogg');subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','1',str(dest)],check=True)
 report[name]=decode_metrics(dest);assert report[name]['clipped']==0
 print(name,'OK',flush=True)

rng=np.random.default_rng(3400);seconds=8;t=np.arange(seconds*RATE,dtype=np.float32)/RATE
noise=rng.standard_normal(len(t)).astype(np.float32)
write('fx_wind_loop34',filtered(noise,90,1600)*(.16+.05*np.sin(2*np.pi*t/8)),True)
write('fx_rain_loop34',filtered(noise,450,10500)*.13,True)
write('fx_breath_loop34',breath(3.2,3401)*.65,True)
for i,family in enumerate(['choir','throat','bone','rasp','whisper','wet']):
 length=4.5+i*.23;t=np.arange(round(length*RATE),dtype=np.float32)/RATE;v=murmur(length,3410+i,48+i*8)*.5
 if family=='choir':v+=murmur(length,3490,76)*.32
 if family=='bone':v+=filtered(np.sin(t*139*(1+.7*np.sin(t*.8))).astype(np.float32),100,2700)*np.sin(np.pi*t/length)**3*.14
 if family=='rasp':v+=filtered(rng.standard_normal(len(t)).astype(np.float32),500,2400)*.10*np.sin(np.pi*t/length)**2
 if family=='whisper':v+=breath(length,3491)*.22
 if family=='wet':v+=np.sin(2*np.pi*(48*t+3*np.sin(t*6)))*.1*np.sin(np.pi*t/length)**2
 write('fx_presence_'+family+'34',v)

catalog=[]
# Existing peaceful themes stay byte-identical. Eight horror tracks receive new, unique arrangements.
for number in range(56,65):
 old=json.loads((r/('tools/music_catalog_'+('32'if number<63 else'33')+'.json')).read_text()) if number<64 else []
 base=next((x for x in old if x['number']==number),None)
 if base is None:base=dict(number=64,world='dream_rainward34',title='Todavía hay alguien en el turno',map='La ciudad que escucha llover',seconds=132,resource='music_score_64_34',cover='music_cover_64_34')
 c=dict(base);c.update(seed=34000+number,root=40+(number%6),lead='tape'if number%2 else'felt',motif=[[0,1,7,6,2,0],[0,6,3,1,8,0],[0,1,5,11,6,0]][number%3],harmony=[0,-1,-5,1],pulse=8.2+(number%4)*.6)
 if number<63:c['resource']=f'music_score_{number}_32'
 elif number==63:c['resource']='music_score_63_33'
 out=compose(c);count=len(out);tt=np.arange(count,dtype=np.float32)/RATE;bed=filtered(rng.standard_normal(count).astype(np.float32),100,1900)*.012
 out[:,0]+=bed*(.6+.4*np.sin(tt*.09));out[:,1]+=np.roll(bed,int(.37*RATE))*.8
 for j,at in enumerate([11,36,62,91]):
  at+=number%7;v=murmur(7+(number%4),c['seed']+j,50+(number*3+j*7)%47)*.09;start=int(at*RATE);end=min(count,start+len(v));v=v[:end-start]
  pan=(-1 if j%2 else 1)*.55;out[start:end,0]+=v*np.sqrt((1-pan)*.5);out[start:end,1]+=v*np.sqrt((1+pan)*.5)
 # Rare resonant metal response, authored independently from the reference recordings.
 at=int((c['seconds']*.69)*RATE);ts=np.arange(RATE*5,dtype=np.float32)/RATE;metal=np.sin(2*np.pi*(121+number*2)*ts)*np.exp(-ts*1.2)*.025
 end=min(count,at+len(metal));out[at:end,0]+=metal[:end-at];out[at:end,1]+=metal[:end-at]*.63
 out=np.tanh(out*1.25);peak=np.max(np.abs(out));out*=min(1,.7/max(.001,peak));write(c['resource'],out);c['description']='composición original: voces sintetizadas sin palabras, viento, cinta y resonancias';catalog.append(c);del out,bed,tt;gc.collect()
(r/'tools/music_catalog_34.json').write_text(json.dumps(catalog,ensure_ascii=False,indent=2)+'\n');(r/'tools/audio_validation_34.json').write_text(json.dumps(report,indent=2)+'\n')
