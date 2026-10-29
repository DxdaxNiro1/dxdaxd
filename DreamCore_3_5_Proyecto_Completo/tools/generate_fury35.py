"""Eighteen original short creature cries; no reference recording is sampled."""
from pathlib import Path
import json,subprocess,tempfile,wave
import numpy as np
from scipy.signal import butter,sosfilt,iirpeak,lfilter
r=Path(__file__).resolve().parents[1];raw=r/'app/src/main/res/raw';rate=22050;report=[]
names=['choir','mask','witness','crimson','antler','gold','star','observatory','mushroom','water_eye','tenant','curious','mourner','window','cone','root_eye','gentleman','seraph']
for i,name in enumerate(names):
 rng=np.random.default_rng(3500+i);seconds=1.15+(i%5)*.12;t=np.arange(round(seconds*rate))/rate
 base=57+i*6;frequency=base*(1+.19*np.sin(t*(3+i*.4))+.30*np.exp(-t*5))
 phase=np.cumsum(frequency)*2*np.pi/rate;voice=sum(np.sin(phase*k)/k for k in range(1,16));out=np.zeros(len(t))
 for f,q,amp in [(430+i*17,5,.8),(1100+i*29,7,.5),(2400+i*37,10,.3)]:
  b,a=iirpeak(f,q,fs=rate);out+=lfilter(b,a,voice)*amp
 noise=sosfilt(butter(2,[250+i*15,4800],btype='bandpass',fs=rate,output='sos'),rng.standard_normal(len(t)))
 out=np.tanh(out*3.5)+noise*(.09+.025*(i%4))
 if name in ['choir','gold','seraph','mourner']:out+=np.sin(phase*1.49)*.23+np.sin(phase*.51)*.18
 if name in ['antler','cone','observatory']:out*=.7+.3*np.sin(t*(29+i))**2
 if name in ['water_eye','root_eye','mushroom']:out+=np.sin(t*2*np.pi*37+np.sin(t*37)*2)*.2
 env=np.minimum(t/.07,1)*np.minimum((seconds-t)/.25,1);env*=.65+.35*np.sin(np.pi*t/seconds)**2
 out*=env;out*=.68/max(np.max(np.abs(out)),.001)
 with tempfile.TemporaryDirectory()as td:
  wav=Path(td)/'voice.wav'
  with wave.open(str(wav),'wb')as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes((out*32767).astype('<i2').tobytes())
  dest=raw/f'fx_attack_{name}35.ogg';subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','2',str(dest)],check=True)
  decoded=subprocess.check_output(['ffmpeg','-v','error','-i',str(dest),'-f','f32le','-ac','1','-ar',str(rate),'-']);samples=np.frombuffer(decoded,dtype='<f4');peak=float(np.max(np.abs(samples)));assert peak<.99
  report.append(dict(name=name,seconds=len(samples)/rate,peak=peak,decoded_bytes=len(samples)*2,bytes=dest.stat().st_size));print(name,'OK',flush=True)
(r/'tools/audio_validation35.json').write_text(json.dumps(dict(original_synthesis=True,sounds=report),indent=2)+'\n')
