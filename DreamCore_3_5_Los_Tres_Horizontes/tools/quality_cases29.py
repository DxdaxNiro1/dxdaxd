"""High/low, real depth-buffer fallback, odd viewport and interactive/lunar image regressions."""
from pathlib import Path
import os,json,subprocess
from PIL import Image
import numpy as np
r=Path(__file__).resolve().parents[1];old=r.parent/'DreamCore_2_8_Fluidez';out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation29')));dest=out/'quality';dest.mkdir(exist_ok=True)
meshes=Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_8_Fluidez/build/validation28')));cams=json.loads((old/'tools/music_cameras_27.json').read_text());codes={v[0]:v[2] for l in (meshes/'catalog_maps.tsv').read_text().splitlines() if (v:=l.split('\t'))};cases=[]
for world in ['dream_02','dream_05','dark_01','dream_sanctuary_clouds','preview_eyes','backrooms_01']:
 for quality in [0,2]:cases.append((f'{world}_q{quality}',world,640,360,['--quality',str(quality)]+(['--mediump'] if quality==0 else [])))
for world in ['dream_02','dream_05','dream_sanctuary_clouds']:cases.append((world+'_odd',world,481,271,[]))
cases += [('pool_depth_buffer','dream_02',640,360,['--no-depth']),('moon_red','dark_01',640,360,['--vigil','60']),('moon_giant','dark_01',640,360,['--vigil','120']),('moon_blink','dark_01',640,360,['--blink','1']),('moon_back','dark_01',640,360,['--target','0','2','120']),('backrooms_breaker','backrooms_01',640,360,['--circuit','1']),('refuge_photo','pocket_refuge',640,360,['--photo']),('hidden_door_lantern','dream_05',640,360,['--tool','lantern']),('windows_prism','echo_11',640,360,['--tool','prism']),('pool_legacy','dream_02',640,360,['--legacy']),('moon_legacy','dark_01',640,360,['--legacy'])]
report=[]
for name,world,w,h,extra in cases:
 c=cams[world];pixels=[]
 for optimized in [False,True]:
  src=r if optimized else old;image=dest/(name+('_29' if optimized else '_28')+'.png')
  cmd=['python',str(r/'tools/preview_scene28.py'),str(meshes/(world+'.f32')),str(src/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(image),'--world',codes[world],'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','40','--atlas',str(src/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),*c.get('extra',[])]
  if world.startswith('dark_'):cmd+=['--lunar-models']+([] if '--vigil' in extra else ['--vigil','40'])
  cmd+=extra
  if optimized:cmd+=['--optimized','--shader-dir',str(out),'--indexed-dir',str(out/'indexed')]
  q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH=str(w),PREVIEW_HEIGHT=str(h),LP_NUM_THREADS='4'))
  if q.returncode:raise RuntimeError(name+'\n'+q.stdout+q.stderr)
  pixels.append(np.asarray(Image.open(image)).astype(float))
 delta=np.abs(pixels[0][:,:,:3]-pixels[1][:,:,:3]);rec=dict(case=name,world=world,mean_absolute_error=float(delta.mean()),maximum=float(delta.max()),fraction_changed_more_than_16=float(np.mean(delta.max(2)>16)))
 report.append(rec);print(name,round(rec['mean_absolute_error'],5),round(rec['fraction_changed_more_than_16'],6),flush=True)
(out/'quality_differences.json').write_text(json.dumps(report,indent=2)+'\n');print('ALL 26 QUALITY/STATE PAIRS RENDERED',flush=True)
