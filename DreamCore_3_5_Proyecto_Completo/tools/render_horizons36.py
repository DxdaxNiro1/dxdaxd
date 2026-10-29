"""Direct GLES2/Mesa views of the production meshes and compiled shader strings."""
from pathlib import Path
import os,json,subprocess,concurrent.futures,shutil
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);d=o/'render36';d.mkdir(exist_ok=True)
views=[('horizon_doors36',64,'puertas',[0,6.8,10],[0,8,-100]),('horizon_doors36',64,'escaleras',[18,6,-4],[25,16,-38]),('horizon_doors36',64,'orilla',[26,2,-5],[45,-8,-25]),('horizon_crimson36',65,'gigante',[0,33,12],[0,65,-95]),('horizon_crimson36',65,'valle',[18,48,-20],[-9,44,-125]),('horizon_blue36',66,'oceano',[0,78,2],[0,84,-110]),('horizon_blue36',66,'roca',[38,106,45],[0,62,-8]),('horizon_blue36',66,'abajo',[10,78,-8],[8,6,-9])]
def render(args):
 (world,code,name,eye,target),mode=args;output=d/(name+'_'+mode+'.png')
 cmd=['python',str(r/'tools/preview_scene35.py'),str(o/(world+'.f32')),str(r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(output),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--surface32','--atlas',str(r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),'--world',str(code),'--eye',*map(str,eye),'--target',*map(str,target),'--time','37','--quality','1']
 if mode=='estable':cmd+=['--no-depth']
 if mode=='esencial':cmd+=['--legacy','--mediump','--quality','0']
 p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='2',PREVIEW_WIDTH='960',PREVIEW_HEIGHT='540'))
 if p.returncode:raise RuntimeError(name+' '+mode+'\n'+p.stdout+p.stderr)
 data=np.asarray(Image.open(output).convert('RGB'));assert len(np.unique(data.reshape(-1,3),axis=0))>80
 print(name,mode,'OK',flush=True);return dict(world=world,name=name,mode=mode,eye=eye,target=target,file=output.name,actual_gles=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=2)as pool:rows=list(pool.map(render,[(v,mode)for v in views for mode in ['completo','estable','esencial']]))
for n,name in [(65,'puertas'),(66,'gigante'),(67,'oceano')]:shutil.copyfile(d/(name+'_completo.png'),r/'app/src/main/res/drawable-nodpi'/f'music_cover_{n}_36.png')
(o/'views36.json').write_text(json.dumps(dict(backend='GLES2 / Mesa llvmpipe',phone_tested=False,views=rows),indent=2)+'\n')
