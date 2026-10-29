"""Run the complete regression suite and actual gameplay-state/save tests on the JVM."""
from pathlib import Path
import os,subprocess,json,re
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',r/'build/validation30'));out.mkdir(parents=True,exist_ok=True)
compiler=os.environ.get('KOTLINC','kotlinc');stdlib=os.environ.get('KOTLIN_STDLIB','')
sources=[str(f) for f in (r/'app/src/main/java/com/dreamcore/game').glob('*.kt') if 'import android.' not in f.read_text()]
names=['Journey19Check','CraterPhysicsCheck','RadialFallCheck','MoonTravel17Check','Ecos18Check','Horizons19Check','Voyage20Check','References21Check','Miradas22Check','Desvios23Check','Resonancias24Check','Graphics26Check','Vigilia27Check','Fluidez28Check','Compatibility29Check','UltimoTren30Check','Terror32Check','Memoria33Check','Walking33Check','ChapterVisual33Check','Ecos34Check','Furia35Check']
cmd=[compiler,*sources,*[str(r/'tests'/(n+'.kt')) for n in names],'-jvm-target','17','-d',str(out/'checks.jar')]
cmd+=['-classpath',stdlib] if stdlib else ['-include-runtime']
subprocess.run(cmd,check=True)
counts={};failures=[]
for n in names:
 p=subprocess.run(['java','-Xmx1536m','-Ddreamcore.output='+str(out),'-Ddreamcore.baseline='+str(out),'-cp',str(out/'checks.jar')+(':'+stdlib if stdlib else ''),'com.dreamcore.game.'+n+'Kt'],capture_output=True,text=True)
 (out/(n+'.log')).write_text(p.stdout+p.stderr)
 if p.returncode:print(p.stdout+p.stderr);failures.append(n);continue
 counts[n]=int(re.findall(r'PASS (\d+)',p.stdout)[-1]);print(n,counts[n],flush=True)
(out/'core_checks30.json').write_text(json.dumps(dict(checks=counts,total=sum(counts.values())),indent=2)+'\n')

if failures:raise SystemExit("Failed: "+", ".join(failures))
