"""Render both engines on identical meshes/cameras; report numerical image differences."""
from pathlib import Path
import os,json,subprocess
from PIL import Image
import numpy as np
r=Path(__file__).resolve().parents[1];old=r.parent/'DreamCore_2_7_Vigilia';out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation28')));frames=out/'comparison';frames.mkdir(exist_ok=True)
meshes=Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_7_Vigilia/build/validation27')));cams=json.loads((old/'tools/music_cameras_27.json').read_text());catalog=json.loads((old/'tools/music_catalog_24.json').read_text())+json.loads((old/'tools/music_catalog_27.json').read_text());codes={v[0]:v[2] for l in (meshes/'catalog_maps.tsv').read_text().splitlines() if (v:=l.split('\t'))};records=[]
for t in catalog:
 world=t['world'];mesh='dream_01' if world=='title' else world;c=cams[world];images=[]
 for optimized in [False,True]:
  source=r if optimized else old;image=frames/(world+('_28' if optimized else '_27')+'.png')
  cmd=['python',str(r/'tools/preview_scene28.py'),str(meshes/(mesh+'.f32')),str(source/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(image),'--world',codes.get(world,'0'),'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','40','--atlas',str(source/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),*c.get('extra',[])]
  if world.startswith('dark_'):cmd+=['--lunar-models','--vigil','40']
  if optimized:cmd+=['--optimized','--shader-dir',str(out),'--indexed-dir',str(out/'indexed')]
  q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='480',PREVIEW_HEIGHT='270',LP_NUM_THREADS='4'))
  if q.returncode:raise RuntimeError(world+'\n'+q.stdout+q.stderr)
  images.append(np.asarray(Image.open(image)).astype(float))
 delta=np.abs(images[0]-images[1]);rgb=delta[:,:,:3];rec=dict(world=world,mean_absolute_error=float(rgb.mean()),maximum=float(rgb.max()),fraction_changed_more_than_2=float(np.mean(rgb.max(2)>2)),fraction_changed_more_than_16=float(np.mean(rgb.max(2)>16)),exact_pixels=float(np.mean(rgb.max(2)==0)))
 records.append(rec);print(world,round(rec['mean_absolute_error'],4),round(rec['fraction_changed_more_than_16'],4),flush=True)
(out/'frame_differences.json').write_text(json.dumps(records,indent=2)+'\n');print('ALL 52 FRAME PAIRS RENDERED',flush=True)
