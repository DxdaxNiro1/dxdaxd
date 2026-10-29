"""Run the shipped pure Kotlin regression checks with Kotlin 1.9.24 + Java 17 installed."""
from pathlib import Path
import shutil,subprocess,re,json
root=Path(__file__).resolve().parents[1]
compiler=shutil.which('kotlinc') or shutil.which('kotlinc.bat')
if not compiler:raise SystemExit('Install the Kotlin command-line compiler 1.9.24 and Java 17 first.')
out=root/'build/validation29';out.mkdir(parents=True,exist_ok=True)
sources=[str(f) for f in (root/'app/src/main/java/com/dreamcore/game').glob('*.kt') if 'import android.' not in f.read_text()]
names=['Journey19Check','CraterPhysicsCheck','RadialFallCheck','MoonTravel17Check','Ecos18Check','Horizons19Check','Voyage20Check','References21Check','Miradas22Check','Desvios23Check','Resonancias24Check','Graphics26Check','Vigilia27Check','Fluidez28Check','Compatibility29Check']
subprocess.run([compiler,*sources,*[str(root/'tests'/(n+'.kt')) for n in names],'-include-runtime','-d',str(out/'checks.jar')],check=True)
counts={}
for name in names:
 result=subprocess.run(['java','-Xmx1g','-Ddreamcore.output='+str(out),'-Ddreamcore.baseline='+str(out),'-cp',str(out/'checks.jar'),'com.dreamcore.game.'+name+'Kt'],capture_output=True,text=True)
 (out/(name+'.log')).write_text(result.stdout+result.stderr);result.check_returncode()
 matches=re.findall(r'PASS (\d+)',result.stdout)
 if not matches:raise RuntimeError('Missing executed checks: '+name)
 counts[name]=int(matches[-1]);print(name,counts[name],flush=True)
print(json.dumps({'checks':counts,'total':sum(counts.values())},indent=2))
