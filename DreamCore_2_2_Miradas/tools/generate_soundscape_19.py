"""Five original world scores and the piano heard through an empty wall."""
from pathlib import Path
import numpy as np,json
import generate_soundscape_16 as synth
RATE=synth.RATE
synth.OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
reports={}
for name,seconds,tonic,kind,secondary,spacing in [('celestial',67,62,'glass','choir',7.1),('city',63,57,'piano','flute',9.7),('dark_garden',73,38,'organ','glass',12.7),('tower',59,50,'harp','piano',8.3),('cloudbed',71,60,'choir','harp',11.7)]:
 out=np.zeros((seconds*RATE,2))
 pattern={'celestial':[0,9,7,14,4,12,2], 'city':[0,3,10,7,2], 'dark_garden':[0,1,7,6,-5], 'tower':[0,3,7,10,12,2], 'cloudbed':[0,7,14,9,4]}[name]
 for i,start in enumerate(np.arange(4,seconds-8,spacing)):
  m=tonic+pattern[i%len(pattern)]
  synth.event(out,float(start),m,kind,.06,np.sin(i*1.3)*.6,12.)
  if i%2==0:synth.event(out,float(start)+2.1,m+7,secondary,.025,-.4,13.,name=='dark_garden')
 out=synth.finish(out,.065 if name=='dark_garden' else .075)
 reports[name]=synth.export('music_'+name+'_19',out,f'Original {kind}/{secondary}; distinct phrase {pattern}; {seconds}s')
 print(name,flush=True)
out=np.zeros((int(5*RATE),2))
for i,m in enumerate([62,65,69]):synth.event(out,.1+i*.7,m,'piano',.12,0,3.)
reports['piano']=synth.export('fx_piano_19',out,'Empty-room piano phrase',False)
(Path(__file__).parent/'audio_validation_19.json').write_text(json.dumps(reports,indent=2)+'\n')
print('Validated six new OGG files',flush=True)
