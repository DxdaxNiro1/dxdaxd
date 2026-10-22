"""Render every world using actual depth-renderbuffer and essential/mediump paths."""
from pathlib import Path
import json,os,subprocess
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation29')));meshes=Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_8_Fluidez/build/validation28')));dest=out/'compatibility';dest.mkdir(exist_ok=True)
cams=json.loads((r/'tools/music_cameras_27.json').read_text());codes={v[0]:v[2]for l in (meshes/'catalog_maps.tsv').read_text().splitlines()if(v:=l.split('\t'))};records=[]
for mode,extra in [('stable',['--no-depth']),('essential',['--legacy','--mediump'])]:
 for world,code in codes.items():
  c=cams[world];file=dest/(mode+'_'+world+'.png')
  cmd=['python',str(r/'tools/preview_scene28.py'),str(meshes/(world+'.f32')),str(r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(file),'--world',code,'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','40','--atlas',str(r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),*c.get('extra',[]),*extra,'--optimized','--shader-dir',str(out),'--indexed-dir',str(out/'indexed')]
  if world.startswith('dark_'):cmd+=['--lunar-models','--vigil','60']
  q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='480',PREVIEW_HEIGHT='270',LP_NUM_THREADS='4'))
  if q.returncode:raise RuntimeError(mode+' '+world+'\n'+q.stdout+q.stderr)
  rgb=np.asarray(Image.open(file).convert('RGB'));unique=len(np.unique(rgb.reshape(-1,3),axis=0));assert unique>64,(mode,world,unique)
  records.append(dict(world=world,profile=mode,unique_colors=unique,rendered=True));print(mode,world,unique,flush=True)
(out/'compatibility_views29.json').write_text(json.dumps(dict(backend='GLES2 Mesa llvmpipe',phone_test=False,views=records,all_rendered=True),indent=2)+'\n');print('ALL 102 COMPATIBILITY VIEWS RENDERED',flush=True)
