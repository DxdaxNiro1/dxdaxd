"""Direct GLES2 renders showing complete, stable and essential visual paths."""
from pathlib import Path
import json,os,subprocess
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation29')));meshes=Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_8_Fluidez/build/validation28')));dest=r/'vistas';dest.mkdir(exist_ok=True)
cams=json.loads((r/'tools/music_cameras_27.json').read_text());codes={v[0]:v[2]for l in (meshes/'catalog_maps.tsv').read_text().splitlines()if(v:=l.split('\t'))}
for label,world in [('piscinas','dream_02'),('ojos','preview_eyes'),('lunas','dark_01'),('isla','dream_sanctuary_clouds')]:
 c=cams[world]
 for mode,extra in [('completo',[]),('estable',['--no-depth']),('esencial',['--legacy','--mediump'])]:
  cmd=['python',str(r/'tools/preview_scene28.py'),str(meshes/(world+'.f32')),str(r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(dest/(label+'_'+mode+'.png')),'--world',codes[world],'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','40','--atlas',str(r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),*c.get('extra',[]),*extra,'--optimized','--shader-dir',str(out),'--indexed-dir',str(out/'indexed')]
  if world.startswith('dark_'):cmd+=['--lunar-models','--vigil','60']
  q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='1280',PREVIEW_HEIGHT='720',LP_NUM_THREADS='4'))
  if q.returncode:raise RuntimeError(label+' '+mode+q.stdout+q.stderr)
  print(label,mode,'exported',flush=True)
print('ALL 12 GALLERY VIEWS EXPORTED',flush=True)
