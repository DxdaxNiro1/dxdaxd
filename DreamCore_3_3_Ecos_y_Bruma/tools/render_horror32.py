"""Actual GLES2 screenshots of the production material streams, modeled entities and profiles."""
from pathlib import Path
import os,subprocess,json,shutil
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);d=o/'render';d.mkdir(exist_ok=True);records=[]
views=[('horror_atrium32',55,'coro',[-1,4,-20],[-7,8.5,-40]),('horror_garden32',56,'antenas',[9,2,-72],[0,26,-128]),('horror_road32',57,'calle',[0,2,-15],[0,5,-44]),('horror_road32',57,'testigo',[0,2,-83],[0,16,-131]),('horror_forest32',58,'eclipse',[0,2,-18],[-13,14,-65]),('horror_screens32',59,'pantallas',[2,2,-20],[10,6,-52]),('horror_house32',60,'casa',[110,120,105],[0,-22,-91]),('horror_pool32',61,'costa',[-25,2,-28],[2,2,-85]),('echo_09',38,'sillas',[0,1.65,-2],[0,1.65,-42]),('echo_10',39,'atrium',[-12,2,-30],[-13,8,-150]),('echo_01',30,'barrio',[0,1.65,0],[0,3,-90]),('dark_01',12,'castillo',[0,1.65,16],[0,15,-64]),('dream_01',0,'umbral',[0,1.65,4],[10,3,-20]),('echo_11',40,'oficina',[0,1.65,8],[-5,2,-28]),('echo_02',31,'cine',[0,2,-18],[0,4,-144]),('horror_garden32',56,'estrella',[5,2,-41],[8,4,-52])]
shader=r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
def render(world,code,name,eye,target,mode='completo'):
 extra=['--legacy','--mediump'] if mode=='esencial' else ['--no-depth'] if mode=='estable' else []
 cmd=['python',str(r/'tools/preview_scene32.py'),str(o/(world+'.f32')),str(shader),str(d/(name+'_'+mode+'.png')),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--surface32','--entities32',str(o/('entities32_'+world+'.json')),'--atlas',str(atlas),'--world',str(code),'--eye',*map(str,eye),'--target',*map(str,target),'--time','40','--quality','1',*extra]
 p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='4',PREVIEW_WIDTH='800',PREVIEW_HEIGHT='450'))
 if p.returncode:raise RuntimeError(name+'\n'+p.stdout+p.stderr)
 f=d/(name+'_'+mode+'.png');rgb=np.asarray(Image.open(f).convert('RGB'));assert np.isfinite(rgb).all();assert len(np.unique(rgb.reshape(-1,3),axis=0))>64
 records.append(dict(world=world,name=name,mode=mode,eye=eye,target=target,real_gles=True));print(name,mode,'OK',flush=True)
 if world.startswith('horror_') and mode=='completo' and name not in ['testigo','estrella']:
  n=56+code-55;shutil.copyfile(f,r/'app/src/main/res/drawable-nodpi'/f'music_cover_{n}_32.png')
for args in views:
 for mode in ['completo','estable','esencial']:render(*args,mode)
# Every pre-existing and new map also gets a stable/essential check at its entry.
seen={v[0] for v in views}
for row in (o/'catalog_maps.tsv').read_text().splitlines():
 world,_,code=row.split('\t')
 if world in seen:continue
 for mode in ['estable','esencial']:render(world,float(code),'map_'+world,[0,2,2],[0,2,-30],mode)
(o/'views32.json').write_text(json.dumps(dict(backend='OpenGL ES2 / Mesa llvmpipe',phone_test=False,views=records),indent=2)+'\n')
