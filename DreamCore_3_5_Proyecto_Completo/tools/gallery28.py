"""Export direct before/after frames at the same resolution; no illustrated replacement images."""
from pathlib import Path
import os,json,subprocess
r=Path(__file__).resolve().parents[1];old=r.parent/'DreamCore_2_7_Vigilia';out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation28')));gallery=r/'vistas';gallery.mkdir(exist_ok=True)
cams=json.loads((old/'tools/music_cameras_27.json').read_text());codes={v[0]:v[2] for l in (Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_7_Vigilia/build/validation27')))/'catalog_maps.tsv').read_text().splitlines() if (v:=l.split('\t'))}
for name,world,extra in [('piscinas','dream_02',[]),('pueblo','dream_05',[]),('backrooms','backrooms_01',[]),('lunas','dark_01',['--lunar-models','--vigil','60']),('luna_gigante','dark_01',['--lunar-models','--vigil','120']),('isla','dream_sanctuary_clouds',[])]:
 c=cams[world]
 for optimized in [False,True]:
  src=r if optimized else old;image=gallery/(('despues_' if optimized else 'antes_')+name+'.png')
  cmd=['python',str(r/'tools/preview_scene28.py'),str(Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_7_Vigilia/build/validation27')))/(world+'.f32')),str(src/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(image),'--world',codes[world],'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','40','--atlas',str(src/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),*c.get('extra',[]),*extra]
  if optimized:cmd+=['--optimized','--shader-dir',str(out),'--indexed-dir',str(out/'indexed')]
  q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='1280',PREVIEW_HEIGHT='720',LP_NUM_THREADS='4'))
  if q.returncode:raise RuntimeError(q.stdout+q.stderr)
 print(name,'before/after exported',flush=True)
print('GALLERY COMPLETE',flush=True)
