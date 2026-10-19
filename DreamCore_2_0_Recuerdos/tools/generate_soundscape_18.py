"""Original 1.8 compositions and four recurring mystery notes; no sampled video audio."""
from pathlib import Path
import numpy as np,json
import generate_soundscape_16 as synth
RATE=synth.RATE
OUT=Path(__file__).resolve().parents[1]/'app/src/main/res/raw'
synth.OUT=OUT
reports={}
for name,seconds,tonic,instrument,spacing in [('waterpark',52,53,'glass',5.9),('laundry',46,60,'piano',8.1),('train',58,45,'organ',11.3),('dawn',61,62,'harp',7.3)]:
 out=np.zeros((seconds*RATE,2))
 pattern=[0,7,3,10,2,12,7,5] if name=='waterpark' else [0,2,7,3,12,5] if name=='laundry' else [0,7,-2,3,10] if name=='train' else [0,9,7,14,4,12,2]
 for i,start in enumerate(np.arange(3,seconds-7,spacing)):
  midi=tonic+pattern[i%len(pattern)]
  synth.event(out,float(start),midi,instrument,.075,float(np.sin(i*1.7)*.5),10.)
  if name=='waterpark':synth.event(out,float(start)+2.3,midi+12,'piano',.023,-.3,7.)
  if name=='dawn' and i%2==0:synth.event(out,float(start)+1.1,midi-12,'choir',.030,.4,12.)
  if name=='train':synth.event(out,float(start)+.8,midi+7,'glass',.018,.6,12.,True)
 out=synth.finish(out,.075)
 reports[name]=synth.export('music_'+name+'_18',out,f'Original {name}: {instrument}, {seconds}s, independent phrase and timing')
 print(name,flush=True)
air=synth.ambience(28,1801,'room')
t=np.arange(len(air))/RATE
# A quiet diffuse room resonance, far below the musical layer.
air+=np.column_stack((np.sin(2*np.pi*83*t),np.sin(2*np.pi*83*t+.18)))*.004*(.7+.3*np.sin(2*np.pi*t/28))[:,None]
reports['hollow']=synth.export('air_hollow_18',synth.finish(air,.035),'Diffuse hollow indoor air')
radio=np.zeros((32*RATE,2))
for i,m in enumerate([62,65,69,72]):synth.event(radio,3+i*5,m,'piano',.09,(-1)**i*.25,9.)
reports['radio']=synth.export('air_radio_18',synth.finish(radio,.045),'Four mystery notes, distant radio')
for i,m in enumerate([62,65,69,72]):
 tone=np.zeros((int(3.5*RATE),2));synth.event(tone,.04,m,'glass',.25,0.,3.)
 reports['tone'+str(i)]=synth.export('fx_tone'+str(i)+'_18',tone,'Mystery motif note '+str(i+1),False)
(OUT.parents[4]/'tools/audio_validation_18.json').write_text(json.dumps(reports,indent=2)+'\n') if False else None
(Path(__file__).parent/'audio_validation_18.json').write_text(json.dumps(reports,indent=2)+'\n')
print('Validated',len(reports),'new OGG files',flush=True)
