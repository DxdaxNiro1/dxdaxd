"""Replay actual GPU uploads, draw ranges and lifetimes against a recording GL boundary."""
from pathlib import Path
import subprocess,os,shutil
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation28')));s=r/'app/src/main/java/com/dreamcore/game'
compiler=shutil.which('kotlinc') or shutil.which('kotlinc.bat')
if not compiler:raise SystemExit('Install the Kotlin command-line compiler 1.9.24 and Java 17 first.')
classpath=str(out/'checks.jar')
if os.environ.get('KOTLIN_STDLIB'):classpath+=os.pathsep+os.environ['KOTLIN_STDLIB']
args=[compiler,'-classpath',classpath,*[str(s/(n+'.kt')) for n in ['DreamGpuMesh28','DreamPrograms28']],*[str(p) for p in (r/'tests/trace28').glob('*.kt')],'-d',str(out/'gpu_trace.jar')]
subprocess.run(args,check=True);subprocess.run(['java','-Xmx512m','-cp',str(out/'gpu_trace.jar')+os.pathsep+classpath,'com.dreamcore.game.GpuTrace28CheckKt'],check=True)
