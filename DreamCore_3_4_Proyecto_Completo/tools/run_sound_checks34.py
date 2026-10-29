from pathlib import Path
import os,subprocess,json,re
r=Path(__file__).resolve().parents[1];o=Path(os.environ['DREAMCORE_VALIDATION_OUT']);compiler=os.environ.get('KOTLINC','kotlinc');cp=str(o/'checks.jar')+os.pathsep+os.environ['KOTLIN_STDLIB'];jar=o/'sound34.jar'
subprocess.run([compiler,str(r/'app/src/main/java/com/dreamcore/game/DreamFootsteps.kt'),*[str(x)for x in (r/'tests/audio34').glob('*.kt')],'-classpath',cp,'-d',str(jar)],check=True)
p=subprocess.run(['java','-cp',str(jar)+os.pathsep+cp,'com.dreamcore.game.SoundLifecycle34Kt'],capture_output=True,text=True);print(p.stdout+p.stderr);p.check_returncode();(o/'sound_lifecycle34.json').write_text(json.dumps(dict(assertions=int(re.findall(r'PASS (\d+)',p.stdout)[-1]),boundary='Production SoundPool owner executed with recorded Android boundary; not a physical-device playback test'),indent=2)+'\n')
