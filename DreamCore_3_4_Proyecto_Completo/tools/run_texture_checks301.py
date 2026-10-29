"""Regression for the reported non-premultiplied Bitmap/Canvas crash, without a phone dependency."""
from pathlib import Path
import os,subprocess,json,re
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',r/'build/validation301'));out.mkdir(parents=True,exist_ok=True)
s=r/'app/src/main/java/com/dreamcore/game';compiler=os.environ.get('KOTLINC','kotlinc');stdlib=os.environ.get('KOTLIN_STDLIB','');base=Path(os.environ.get('DREAMCORE_CORE_JAR',r/'build/validation30/checks.jar'))
cp=str(base)+(os.pathsep+stdlib if stdlib else '');jar=out/'texture301.jar'
args=[compiler,'-jvm-target','17','-classpath',cp,*[str(s/(n+'.kt'))for n in ['DreamPixels301','DreamTexture29','DreamSurfaceTextures']],str(r/'tests/BitmapPixels301Check.kt'),*[str(p)for p in (r/'tests/trace301').glob('*.kt')],'-d',str(jar)]
if not stdlib:args+=['-include-runtime']
subprocess.run(args,check=True);results={}
for name,extra in [('BitmapPixels301CheckKt',[]),('TextureRuntime301CheckKt',[str(r/'app/src/main/res/drawable-nodpi')])]:
 p=subprocess.run(['java','-Xmx1536m','-Djava.awt.headless=true','-cp',str(jar)+os.pathsep+cp,'com.dreamcore.game.'+name,*extra],capture_output=True,text=True);(out/(name+'.log')).write_text(p.stdout+p.stderr);print(p.stdout+p.stderr,flush=True);p.check_returncode();results[name]=int(re.findall(r'PASS (\d+)',p.stdout)[-1])
(out/'texture301.json').write_text(json.dumps(dict(checks=results,total=sum(results.values()),phone_test=False,boundary='Canvas/Bitmap and GL contracts simulated; production loader and real PNGs executed'),indent=2)+'\n')
