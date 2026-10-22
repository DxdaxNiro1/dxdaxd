"""Execute the actual context probe against recorded GL success/failure cases."""
from pathlib import Path
import subprocess,os,shutil
r=Path(__file__).resolve().parents[1];o=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation29')))
compiler=shutil.which('kotlinc') or shutil.which('kotlinc.bat')
if not compiler:raise SystemExit('Install Kotlin 1.9.24 and Java 17 first.')
cp=str(o/'checks.jar')
if os.environ.get('KOTLIN_STDLIB'):cp+=os.pathsep+os.environ['KOTLIN_STDLIB']
subprocess.run([compiler,'-classpath',cp,str(r/'app/src/main/java/com/dreamcore/game/DreamGl29.kt'),*[str(p)for p in (r/'tests/trace29').glob('*.kt')],'-d',str(o/'trace29.jar')],check=True)
subprocess.run(['java','-cp',str(o/'trace29.jar')+os.pathsep+cp,'com.dreamcore.game.GlRuntime29CheckKt'],check=True)
