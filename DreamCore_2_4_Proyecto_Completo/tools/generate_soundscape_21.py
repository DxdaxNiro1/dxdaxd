"""Eleven original ambient themes for the eleven reference worlds; no reference audio is sampled."""
from pathlib import Path
import numpy as np,json
import generate_soundscape_16 as synth
synth.OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
reports={}
settings=[(60,'piano',[0,7,4,9]),(50,'choir',[0,9,14,7]),(45,'glass',[0,2,7,10]),(55,'harp',[0,7,11,4]),(64,'flute',[0,9,7,2]),(57,'choir',[0,4,12,7]),(48,'piano',[0,3,10,7]),(62,'harp',[0,7,14,9]),(49,'piano',[0,1,8,5]),(38,'organ',[0,6,7,1]),(53,'glass',[0,2,10,7])]
for index,(tonic,kind,phrase) in enumerate(settings,1):
 seconds=49+index*2;out=np.zeros((seconds*synth.RATE,2))
 for j,start in enumerate(np.arange(3,seconds-12,8.1+(index%4))):
  synth.event(out,float(start),tonic+phrase[j%len(phrase)],kind,.05,np.sin(j*1.4)*.55,11.)
  if j%2==0:synth.event(out,float(start)+2.3,tonic-12,'choir',.018,-.35,13.)
 out=synth.finish(out,.061)
 reports[f'echo_{index:02d}']=synth.export(f'music_echo_{index:02d}_21',out,f'Original {kind}, phrase {phrase}, world {index}, {seconds}s')
 print(index,flush=True)
(Path(__file__).parent/'audio_validation_21.json').write_text(json.dumps(reports,indent=2)+'\n')
