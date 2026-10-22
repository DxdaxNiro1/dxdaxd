"""Two original, soft scores. Different phrases, lengths and timbres; no sampled reference audio."""
from pathlib import Path
import numpy as np,json
import generate_soundscape_16 as synth
synth.OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
reports={}
for name,seconds,tonic,kind,phrase,starts in [('eyes',76,42,'choir',[0,13,7,2,19],[4,19,35,52,65]),('island',63,60,'harp',[0,7,11,14,9,4],[3,10,19,28,39,51])]:
 out=np.zeros((seconds*synth.RATE,2))
 for j,start in enumerate(starts):
  synth.event(out,float(start),tonic+phrase[j%len(phrase)],kind,.046,np.sin(j*1.7)*.60,10.)
  synth.event(out,float(start)+3,tonic-12,'flute' if name=='island' else 'organ',.015,-.3,13.)
 out=synth.finish(out,.048 if name=='eyes' else .057)
 reports[name]=synth.export('music_'+name+'_22',out,'Original '+kind+' with widely spaced notes, '+name)
(Path(__file__).parent/'audio_validation_22.json').write_text(json.dumps(reports,indent=2)+'\n')
