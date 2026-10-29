"""Render the shipped geometry and attack shaders through actual GLES2/Mesa."""
from pathlib import Path
import json,os,subprocess,concurrent.futures
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);d=o/'render35';d.mkdir(exist_ok=True)
shader=r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
profiles=json.loads((o/'attack35_styles.json').read_text());jobs=[]
for p in profiles:
 name=p['name'];height={'observatory':9.4,'root-eye':11.,'mushroom':3.,'water-eye':1.4,'gentleman':4.1,'tenant':4.34,'seraph':3.6,'cone':4.25}.get(name,4.8)
 fixture=o/('pose35_'+name+'.json');fixture.write_text(json.dumps([dict(name=name,position=[0,0,-10],scale=1,yaw=0,phase=0)]))
 for progress in [0.,.62]:jobs.append(dict(name=name,progress=progress,world='dream_rainward34',code=63,eye=[0,height-.2,-8.3],target=[0,height,-10],fixture=fixture))
jobs+= [dict(name='umbral_plano',progress=0,world='dream_01',code=0,eye=[0,61,18],target=[0,0,-15]),dict(name='umbral_entrada',progress=0,world='dream_01',code=0,eye=[0,2.5,6],target=[0,2,-26]),dict(name='pradera',progress=0,world='preview_eyes',code=41,eye=[-5,5,-15],target=[-16,5,-31],fixture=o/'entities33_map_preview_eyes.json')]
def render(job):
 name=job['name'];progress=job['progress'];output=d/(name+('_furia'if progress else '_reposo')+'.png')
 cmd=['python',str(r/'tools/preview_scene35.py'),str(o/(job['world']+'.f32')),str(shader),str(output),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--surface32','--atlas',str(atlas),'--world',str(job['code']),'--eye',*map(str,job['eye']),'--target',*map(str,job['target']),'--time','11','--quality','1']
 if 'fixture'in job:cmd+=['--entities32',str(job['fixture']),'--attack35',str(o/'attack35_styles.json'),'--progress35',str(progress)]
 if name.startswith('umbral'):cmd+=['--props30',str(o/'instances33_map_dream_01.json')]
 p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='2',PREVIEW_WIDTH='800',PREVIEW_HEIGHT='450'))
 if p.returncode:raise RuntimeError(name+'\n'+p.stdout+p.stderr)
 data=np.asarray(Image.open(output).convert('RGB'));assert len(np.unique(data.reshape(-1,3),axis=0))>40
 print(name,progress,'OK',flush=True);return dict(name=name,progress=progress,file=output.name,actual_gles=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=2)as pool:rows=list(pool.map(render,jobs))
for p in profiles:
 name=p['name'];a=np.asarray(Image.open(d/(name+'_reposo.png')).convert('RGB')).astype(float);b=np.asarray(Image.open(d/(name+'_furia.png')).convert('RGB')).astype(float)
 changed=int(np.sum(np.max(np.abs(a-b),axis=2)>5));assert changed>60,(name,changed)
 p['changed_pixels']=changed
(o/'renders35.json').write_text(json.dumps(dict(backend='OpenGL ES2 / Mesa llvmpipe',physical_phone=False,views=rows,animated_profiles=profiles),indent=2)+'\n')
