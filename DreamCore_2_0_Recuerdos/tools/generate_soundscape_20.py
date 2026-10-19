"""Original quiet scores for the refuge, gravity corridor, letter room and four photographs."""
from pathlib import Path
import numpy as np,json
import generate_soundscape_16 as synth
RATE=synth.RATE
synth.OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
reports={}
patterns=[('refuge',59,57,'piano',[0,7,9,4]),('gravity',67,47,'glass',[0,2,7,1,12]),('letter',61,52,'harp',[0,3,10,7]),('photo_house',57,60,'choir',[0,4,11,7]),('photo_sea',69,43,'glass',[0,7,2,9]),('photo_window',63,55,'piano',[0,1,8,5]),('photo_planet',71,62,'flute',[0,9,14,7])]
for name,seconds,tonic,kind,notes in patterns:
 out=np.zeros((seconds*RATE,2))
 for i,start in enumerate(np.arange(4,seconds-12,9.7+(len(name)%4))):
  synth.event(out,float(start),tonic+notes[i%len(notes)],kind,.055,np.sin(i*1.3)*.5,12.)
  if i%2==0:synth.event(out,float(start)+2.8,tonic-12,'choir',.020,-.3,14.)
 out=synth.finish(out,.065)
 reports[name]=synth.export('music_'+name+'_20',out,f'Original {kind}, phrase {notes}, {seconds}s')
 print(name,flush=True)
t=np.arange(4*RATE)/RATE
wave=np.sin(2*np.pi*(63*t-2.5*t*t+.3*np.sin(t*1.5)))+.22*np.sin(2*np.pi*125*t)
env=np.sin(np.pi*t/4)**2
out=np.column_stack((wave*env,wave*env))*.07
reports['whale']=synth.export('fx_whale_20',out,'Original low suspended-sea creature call',False)
(Path(__file__).parent/'audio_validation_20.json').write_text(json.dumps(reports,indent=2)+'\n')
