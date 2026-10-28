"""Pixel comparison with the original effect program on independent feature fixtures."""
from pathlib import Path
import os,subprocess,json
import numpy as np
from PIL import Image
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);records=[]
for k in [91,93,108,110]:
 images=[]
 for compact in [False,True]:
  path=o/'render'/f'feature_{k}_{int(compact)}.png'
  cmd=['python',str(r/'tools/preview_scene32.py'),str(o/f'feature32_{k}.f32'),str(r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(path),'--optimized','--shader-dir',str(o),'--indexed-dir',str(o/'indexed'),'--atlas',str(r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),'--world','0','--eye','0','4','3','--target','0','2' if k==110 else '0','-8','--no-post','--no-shadow','--no-depth']
  if compact:cmd+=['--surface32']
  p=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,LP_NUM_THREADS='4',PREVIEW_WIDTH='640',PREVIEW_HEIGHT='360'))
  if p.returncode:raise RuntimeError(p.stdout+p.stderr)
  images.append(np.asarray(Image.open(path).convert('RGB'),dtype=np.float32))
 error=np.abs(images[0]-images[1]);mean=float(error.mean());p99=float(np.percentile(error,99));assert mean<3 and p99<16,(k,mean,p99)
 records.append(dict(kind=k,mean_absolute_error_8bit=mean,p99_absolute_error_8bit=p99));print(k,mean,p99,flush=True)
(o/'surface_features32.json').write_text(json.dumps(dict(backend='actual GLES2 Mesa',fixtures=records,threshold_mean_8bit=3,threshold_p99_8bit=16,phone_test=False),indent=2)+'\n')
