"""Controlled GLES2 geometry comparison; timings are desktop Mesa, not phone FPS or thermals."""
from pathlib import Path
import os,subprocess,json
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);records=[]
for world,code,eye,target in [('dark_01',12,[0,1.65,16],[0,8,-64]),('echo_10',39,[-12,2,-30],[-13,8,-150]),('echo_01',30,[0,1.65,0],[0,3,-90]),('echo_09',38,[0,1.65,-2],[0,1.65,-42]),('dream_01',0,[0,1.65,4],[10,3,-20])]:
 pair={}
 for surface in [False,True]:
  name=world+('_compact' if surface else '_effects_program');dest=o/('bench_'+name+'.json')
  cmd=['python',str(r/'tools/preview_scene32.py'),str(o/(world+'.f32')),str(r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(o/'render'/('bench_'+name+'.png')),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--atlas',str(r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),'--world',str(code),'--eye',*map(str,eye),'--target',*map(str,target),'--no-post','--no-shadow','--no-depth','--bench-frames','30','--bench-json',str(dest)]
  if surface:cmd+=['--surface32']
  p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='4',PREVIEW_WIDTH='640',PREVIEW_HEIGHT='360'))
  if p.returncode:raise RuntimeError(p.stdout+p.stderr)
  pair['compact' if surface else 'original_program']=json.loads(dest.read_text())
 pair['world']=world;pair['ratio']=pair['compact']['median_ms']/pair['original_program']['median_ms'];records.append(pair);print(world,round(pair['ratio'],3),flush=True)
(o/'benchmark32.json').write_text(json.dumps(dict(backend='Mesa llvmpipe GLES2, 640x360, four software threads',method='Same 3.1 indexed scene, camera, textures and shading quality; 30 warmed geometry-only samples, no post, sky, compilation, image I/O or entities in timed stage.',phone_fps=False,phone_temperature=False,scenes=records),indent=2)+'\n')
