"""Actual final-quality GLES2 views plus compatible paths; no illustration substituted for game geometry."""
from pathlib import Path
import os,subprocess,json,shutil
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);d=o/'render34';d.mkdir(exist_ok=True);records=[]
shader=r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
views=[('dream_rainward34',63,'calle',[0,1.8,2],[0,3,-65],.72),('dream_rainward34',63,'inquilino',[-2,1.8,-51],[-6.8,2.5,-60],.72),('dream_rainward34',63,'corredor',[0,1.8,-97],[0,2,-138],0),('dream_rainward34',63,'plaza',[0,2.6,-151],[3,2,-185],.72),('dream_01',0,'umbral',[0,2,6],[-15,2,-24],0),('horror_forest32',58,'bosque',[0,2,-18],[-13,14,-65],.30)]
def render(world,code,name,eye,target,rain,mode):
 extra=['--legacy','--mediump'] if mode=='esencial' else ['--no-depth'] if mode=='estable' else []
 output=d/f'{name}_{mode}.png'
 cmd=['python',str(r/'tools/preview_scene34.py'),str(o/(world+'.f32')),str(shader),str(output),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--surface32','--entities32',str(o/('entities33_map_'+world+'.json')),'--props30',str(o/('instances33_map_'+world+'.json')),'--state33',str(o/('state33_map_'+world+'.json')),'--atlas',str(atlas),'--world',str(code),'--eye',*map(str,eye),'--target',*map(str,target),'--time','37','--quality','1','--rain34',str(rain),*extra]
 p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='4',PREVIEW_WIDTH='800',PREVIEW_HEIGHT='450'))
 if p.returncode:raise RuntimeError(name+' '+mode+'\n'+p.stdout+p.stderr)
 rgb=np.asarray(Image.open(output).convert('RGB'));assert len(np.unique(rgb.reshape(-1,3),axis=0))>48,(name,mode)
 records.append(dict(world=world,name=name,mode=mode,eye=eye,target=target,rain=rain,actual_gles=True));print(name,mode,'OK',flush=True)
 if name=='calle'and mode=='completo':shutil.copyfile(output,r/'app/src/main/res/drawable-nodpi/music_cover_64_34.png')
for args in views:
 for mode in ['completo','estable','esencial']:render(*args,mode)
for row in ([] if os.environ.get("SHOWCASE_ONLY34")=="1" else (o/'catalog_maps.tsv').read_text().splitlines()):
 world,_,code=row.split('\t')
 for mode in ['estable','esencial']:render(world,int(float(code)),'map_'+world,[0,2,2],[0,2,-30],.72 if world=='dream_rainward34'else .30 if world=='horror_forest32'else 0,mode)
if os.environ.get('SHOWCASE_ONLY34')=='1':
 for mode in ['estable','esencial']:render('dream_rainward34',63,'map_dream_rainward34',[0,2,2],[0,2,-30],.72,mode)
if os.environ.get('SHOWCASE_ONLY34')=='1':
 old=json.loads((o/'views34.json').read_text())['views'];keys={(x['name'],x['mode'])for x in records};records=[x for x in old if (x['name'],x['mode'])not in keys]+records
(o/'views34.json').write_text(json.dumps(dict(backend='OpenGL ES2 / Mesa llvmpipe',physical_phone=False,views=records),indent=2)+'\n')
