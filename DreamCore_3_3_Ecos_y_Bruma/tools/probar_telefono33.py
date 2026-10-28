"""Collect a physical-device session after using Ajustes > Diagnóstico. Requires adb and a debug build."""
import argparse,subprocess,time,json,re
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--serial',required=True);p.add_argument('--seconds',type=int,default=180);p.add_argument('--output',type=Path,default=Path('telefono33.json'));a=p.parse_args()
def adb(*args,required=True):
 r=subprocess.run(['adb','-s',a.serial,*args],capture_output=True,text=True)
 if required:r.check_returncode()
 return r.stdout.strip()
assert adb('get-state')=='device','No connected physical device for this serial.'
model=adb('shell','getprop','ro.product.model');manufacturer=adb('shell','getprop','ro.product.manufacturer');samples=[]
print('Inicia la medición en el juego, cierra el diagnóstico y recorre los mapas que se traban. Al terminar guarda el informe desde el juego.',flush=True)
end=time.monotonic()+max(10,min(a.seconds,3600))
while time.monotonic()<end:
 battery=adb('shell','dumpsys','battery');m=re.search(r'temperature:\s*(-?\d+)',battery)
 samples.append(dict(monotonic_seconds=time.monotonic(),battery_c=int(m.group(1))/10 if m else None,plugged=bool(re.search(r'(AC|USB|Wireless) powered: true',battery))))
 time.sleep(min(10,max(0,end-time.monotonic())))
report=adb('exec-out','run-as','com.dreamcore.game','cat','files/dream_diagnostic33.json',required=False)
try:game=json.loads(report)
except (ValueError,TypeError):game=None
result=dict(physical_device_connected=True,model=model,manufacturer=manufacturer,battery_samples=samples,in_game_report=game,complete=bool(game and game.get('samples')),note='Battery temperature is not SoC temperature. If no in-game report, save/share it inside the app and repeat collection. Graphics dumps do not measure this custom GL renderer reliably.')
a.output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n');print(a.output,'complete=',result['complete'])
