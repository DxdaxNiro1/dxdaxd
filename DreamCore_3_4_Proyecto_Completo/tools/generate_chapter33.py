"""Own synthesized score and filtered variants of the game's existing original cues."""
from pathlib import Path
import subprocess,json,tempfile,wave
import numpy as np
from generate_soundtrack_24 import compose,decode_metrics,RATE
r=Path(__file__).resolve().parents[1];raw=r/'app/src/main/res/raw';report={}
for name,src in [('room','fx_room30'),('train','fx_train30'),('piano','fx_piano0_30'),('whale','fx_whale_20'),('wood','fx_wood_0_17')]+[('piano'+str(i),'fx_piano'+str(i)+'_30')for i in range(7)]+[('tone'+str(i),'fx_tone'+str(i)+'_18')for i in range(4)]:
 dest=raw/f'fx_{name}_wall33.ogg';subprocess.run(['ffmpeg','-v','error','-y','-i',str(raw/(src+'.ogg')),'-af','lowpass=f=720,volume=0.85','-c:a','libvorbis','-q:a','1',str(dest)],check=True);report[dest.stem]=decode_metrics(dest)
c=dict(number=63,world='dream_archive33',title='La voz que no dejamos salir',map='El depósito de las voces',lead='organ',root=42,motif=[0,1,7,2,10,5,0],harmony=[0,-5,1,3],pulse=9.7,seconds=128,description='composición original: órgano, cinta y resonancias del archivo',resource='music_score_63_33',cover='music_cover_63_33',seed=33063)
out=compose(c)
with tempfile.TemporaryDirectory() as td:
 wav=Path(td)/'score.wav'
 with wave.open(str(wav),'wb') as f:f.setnchannels(2);f.setsampwidth(2);f.setframerate(RATE);f.writeframes((np.clip(out,-.98,.98)*32767).astype('<i2').tobytes())
 dest=raw/(c['resource']+'.ogg');subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','1',str(dest)],check=True);report[c['resource']]=decode_metrics(dest)
(r/'tools/music_catalog_33.json').write_text(json.dumps([c],ensure_ascii=False,indent=2)+'\n');(r/'tools/audio_validation_33.json').write_text(json.dumps(report,indent=2)+'\n')
