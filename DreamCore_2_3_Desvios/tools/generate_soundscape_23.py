"""Original ambient scores for the investigation and five new worlds; no reference audio samples."""
from pathlib import Path
import numpy as np,json
import generate_soundscape_16 as synth
synth.OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
report={}
themes=[('backrooms',59,38,'organ',[0,1,7],[5,27,46],.032),('sky',73,57,'choir',[0,7,14,9],[4,19,39,58],.05),('cloud',67,64,'harp',[0,11,7,14],[3,15,32,50],.05),('city',71,60,'piano',[0,9,7,16,11],[4,13,26,43,58],.05),('edge',79,45,'glass',[0,7,13,2],[5,22,43,63],.044),('call_house',61,49,'piano',[0,1,8],[8,29,48],.037)]
for name,seconds,tonic,kind,phrase,starts,rms in themes:
 out=np.zeros((seconds*synth.RATE,2))
 for j,t in enumerate(starts):
  synth.event(out,float(t),tonic+phrase[j%len(phrase)],kind,.04,np.sin(j*1.4)*.4,12.)
  if name!='backrooms':synth.event(out,float(t)+4.1,tonic-12,'choir',.013,-.2,14.)
 out=synth.finish(out,rms);report['music_'+name+'_23']=synth.export('music_'+name+'_23',out,'Original '+kind+' phrase for '+name)
seconds=32;t=np.arange(seconds*synth.RATE)/synth.RATE
hum=(np.sin(2*np.pi*60*t)*.010+np.sin(2*np.pi*120*t+.2)*.003+np.sin(2*np.pi*181*t)*.0015)*(.8+.15*np.sin(2*np.pi*t/seconds))
out=np.column_stack([hum,np.roll(hum,100)]);out=synth.finish(out,.012)
report['air_backrooms_23']=synth.export('air_backrooms_23',out,'Original soft electrical hum')
(Path(__file__).parent/'audio_validation_23.json').write_text(json.dumps(report,indent=2)+'\n')
