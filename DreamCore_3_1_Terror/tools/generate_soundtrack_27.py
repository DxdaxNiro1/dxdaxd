"""Three original additions. Existing 49 soundtrack recordings are never rewritten."""
from pathlib import Path
import json,tempfile,wave,subprocess
import numpy as np
from generate_soundtrack_24 import compose,decode_metrics,RATE
r=Path(__file__).resolve().parents[1]; report={}
for c in json.loads((r/'tools/music_catalog_27.json').read_text()):
 out=compose(c)
 with tempfile.TemporaryDirectory() as td:
  wav=Path(td)/'score.wav'
  with wave.open(str(wav),'wb') as f:
   f.setnchannels(2);f.setsampwidth(2);f.setframerate(RATE);f.writeframes((np.clip(out,-.99,.99)*32767).astype('<i2').tobytes())
  dest=r/'app/src/main/res/raw'/(c['resource']+'.ogg')
  subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','1','-metadata','title='+c['title'],'-metadata','artist=NiroDxdax - música procedural original',str(dest)],check=True)
 m=decode_metrics(dest);assert m['clipped']==0 and m['peak']<.9 and m['seam']<.012
 report[c['resource']]=m;print(c['title'],m['seconds'],flush=True)
(r/'tools/audio_validation_27.json').write_text(json.dumps(report,indent=2)+'\n')
