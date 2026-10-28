"""Actual GLES2 maps, independent prop transforms and preserved lunar attention states."""
from pathlib import Path
import subprocess,os,json,shutil
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);dest=o/'render';dest.mkdir(exist_ok=True);records=[]
base=['python',str(r/'tools/preview_scene30.py')];shader=r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
def render(world,code,name,eye,target,extra=(),props=False):
 cmd=base+[str(o/(world+'.f32')),str(shader),str(dest/(name+'.png')),'--atlas',str(atlas),'--world',str(code),'--eye',*map(str,eye),'--target',*map(str,target),'--time','40','--quality','1',*extra]
 if props:cmd+=['--props30',str(o/('instances30_'+world+'.json'))]
 q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='4',PREVIEW_WIDTH='640',PREVIEW_HEIGHT='360'))
 if q.returncode:raise RuntimeError(name+'\n'+q.stdout+q.stderr)
 rgb=np.asarray(Image.open(dest/(name+'.png')).convert('RGB'));colors=len(np.unique(rgb.reshape(-1,3),axis=0));assert colors>64
 records.append(dict(name=name,world=world,colors=colors,options=list(extra),real_gles=True));print(name,'OK',flush=True)
 return dest/(name+'.png')
views=[('dream_library30',52,'biblioteca',[0,2,2],[-5,2,-30]),('dream_library30',52,'piano',[-8,2,-12],[-12,1.5,-19]),('dream_library30',52,'proyeccion',[16,2,-39],[25,2,-48]),('dream_library30',52,'maqueta',[-8,2,-52],[-12,1.6,-58]),('dream_library30',52,'marco',[0,1.65,-94],[0,2,-124]),('dream_fair30',53,'feria',[8,2,-35],[0,4,-69]),('dream_fair30',53,'deposito',[-15,2,-33],[-25,-2,-48]),('dream_fair30',53,'telescopio',[8,2,-125],[12,2,-134]),('dream_station30',54,'anden_secreto',[0,2,2],[0,8,-50]),('pocket_refuge',22,'refugio',[0,2,0],[10,2,-12]),('pocket_train',15,'tren',[0,1.65,6],[0,2,-12])]
for w,c,n,e,t in views:
 for mode,extra in [('completo',[]),('estable',['--no-depth']),('esencial',['--legacy','--mediump'])]:
  f=render(w,c,n+'_'+mode,e,t,extra,True)
  if mode=='completo' and n in ['biblioteca','feria','anden_secreto']:
   num={'biblioteca':53,'feria':54,'anden_secreto':55}[n];shutil.copyfile(f,r/'app/src/main/res/drawable-nodpi'/f'music_cover_{num}_30.png')
for n,extra in [('luna_azul',[]),('luna_roja_60s',['--vigil','60']),('luna_parpadeo',['--blink','1']),('luna_gigante_120s',['--vigil','120']),('luna_cercana',['--fov','36','--sky-only','--only-main-moon']),('luna_creciente',['--moon-variant','6','--only-main-moon','--sky-only','--fov','36'])]:
 render('dark_01',12,n,[0,1.65,12],[8,53,-74],['--lunar-models',*extra])
for w,c,n,eye,target in [('dark_01',12,17,[0,1.65,12],[8,53,-74]),('dark_02',19,18,[0,2,2],[8,43,-80]),('dark_03',49,50,[0,2,-80],[9,43,-190]),('dark_04',50,51,[0,2,-150],[4,34,-260])]:
 f=render(w,c,'dark_cover_'+str(n),eye,target,['--lunar-models']);shutil.copyfile(f,r/'app/src/main/res/drawable-nodpi'/('music_cover_%02d_%d.png'%(n,24 if n<50 else 27)))
(o/'views30.json').write_text(json.dumps(dict(backend='GLES2 Mesa llvmpipe',phone_test=False,views=records),indent=2)+'\n')
