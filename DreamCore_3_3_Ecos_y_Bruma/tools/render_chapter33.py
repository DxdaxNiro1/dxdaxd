"""Production GLES2 scenes and modeled story objects; no illustrations and no physical-phone claims."""
from pathlib import Path
import os,subprocess,json,shutil
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);d=o/'render33';d.mkdir(exist_ok=True);records=[]
shader=r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
views=[('dream_01',0,'umbral',[0,2,7],[-4,1.8,1]),('dream_archive33',62,'archivo',[-12,2,-58],[0,3,-70]),('dream_archive33',62,'archivista',[0,2,-100],[-5,2,-109]),('dream_archive33',62,'mesa',[-9,.55,-30],[0,.7,-36]),('dream_archive33',62,'armario',[9,1.87,-48],[9,1.87,-37]),('pocket_refuge',22,'recuerdo',[0,2,0],[3,1.7,-16]),('dream_05',4,'ventana',[66,4,-49],[66.4,3.3,-63]),('dream_library30',52,'biblioteca',[3,2,-29],[9,2.5,-38]),('pocket_train',15,'tren',[0,1.7,-10],[-2.5,1.3,-20]),('dream_06',5,'gigante',[0,3,-75],[40,38,-190])]
state={'archivista':'archivo','mesa':'archivo','armario':'archivo'}
def render(world,code,name,eye,target,mode,key):
 extra=['--legacy','--mediump'] if mode=='esencial' else ['--no-depth'] if mode=='estable' else []
 output=d/(name+'_'+mode+'.png')
 cmd=['python',str(r/'tools/preview_scene33.py'),str(o/(world+'.f32')),str(shader),str(output),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--surface32','--entities32',str(o/('entities33_'+key+'.json')),'--props30',str(o/('instances33_'+key+'.json')),'--state33',str(o/('state33_'+key+'.json')),'--atlas',str(atlas),'--world',str(code),'--eye',*map(str,eye),'--target',*map(str,target),'--time','40','--quality','1',*extra]
 p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='4',PREVIEW_WIDTH='800',PREVIEW_HEIGHT='450'))
 if p.returncode:raise RuntimeError(name+' '+mode+'\n'+p.stdout+p.stderr)
 rgb=np.asarray(Image.open(output).convert('RGB'));assert len(np.unique(rgb.reshape(-1,3),axis=0))>32,(name,mode)
 records.append(dict(world=world,name=name,mode=mode,eye=eye,target=target,real_gles=True));print(name,mode,'OK',flush=True)
 if name=='archivista' and mode=='completo':shutil.copyfile(output,r/'app/src/main/res/drawable-nodpi/music_cover_63_33.png')
for args in views:
 for mode in ['completo','estable','esencial']:render(*args,mode,state.get(args[2],args[2]))
for row in (o/'catalog_maps.tsv').read_text().splitlines():
 world,_,code=row.split('\t')
 for mode in ['estable','esencial']:render(world,int(float(code)),'map_'+world,[0,2,2],[0,2,-30],mode,'map_'+world)
(o/'views33.json').write_text(json.dumps(dict(backend='OpenGL ES2 / Mesa llvmpipe',phone_test=False,views=records),indent=2)+'\n')
