"""Real GLES2 frame validation: all registered worlds, new models and attention states."""
from pathlib import Path
import argparse,subprocess,json,os
r=Path(__file__).resolve().parents[1];p=argparse.ArgumentParser();p.add_argument('--meshes',required=True,type=Path);p.add_argument('--output',required=True,type=Path);a=p.parse_args();a.output.mkdir(exist_ok=True,parents=True)
s=r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=r/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
catalog=json.loads((r/'tools/music_catalog_24.json').read_text())+json.loads((r/'tools/music_catalog_27.json').read_text());cams=json.loads((r/'tools/music_cameras_24.json').read_text());codes={v[0]:v[2] for line in (a.meshes/'catalog_maps.tsv').read_text().splitlines() if (v:=line.split('\t'))}
cams.update({'dark_03':dict(eye=[0,1.65,19],target=[8,46,-85],extra=[]),'dark_04':dict(eye=[0,1.65,-150],target=[4,34,-260],extra=[]),'dream_sanctuary_clouds':dict(eye=[20,74,-20],target=[30,133,-200],extra=[])})
(r/'tools/music_cameras_27.json').write_text(json.dumps(cams,indent=2)+'\n')
records=[];gallery=r/'vistas';gallery.mkdir(exist_ok=True)
def render(world,name,eye,target,extra=[],w=480,h=270):
 mesh='dream_01' if world=='title' else world
 cmd=['python',str(r/'tools/preview_scene27.py'),str(a.meshes/(mesh+'.f32')),str(s),str(a.output/(name+'.png')),'--world',codes.get(world,'0'),'--eye',*map(str,eye),'--target',*map(str,target),'--atlas',str(atlas),'--time','40',*extra]
 if world.startswith('dark_') and name!='moon_legacy_fallback':cmd+=['--lunar-models'];cmd+=[] if '--vigil' in extra else ['--vigil','40']
 q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH=str(w),PREVIEW_HEIGHT=str(h)))
 if q.returncode:raise RuntimeError(name+'\n'+q.stdout+q.stderr)
 rec=json.loads(q.stdout);rec.update(world=world,quality=int(extra[extra.index('--quality')+1]) if '--quality' in extra else 1,extra=extra);records.append(rec);print(name,'OK',flush=True)
 return a.output/(name+'.png')
for t in catalog:
 c=cams[t['world']];render(t['world'],t['world'],c['eye'],c['target'],c.get('extra',[]))
 # Three live in-game album thumbnails, rendered from their real meshes.
 if t['number']>=50:
  import shutil
  shutil.copyfile(a.output/(t['world']+'.png'),r/'app/src/main/res/drawable-nodpi'/(t['cover']+'.png'))
views=[
 ('luna_azul','dark_01',[0,1.65,12],[8,53,-74],[]),
 ('luna_roja_60s','dark_01',[0,1.65,12],[8,53,-74],['--vigil','60']),
 ('luna_parpadeo','dark_01',[0,1.65,12],[8,53,-74],['--blink','1']),
 ('luna_gigante_120s','dark_01',[0,1.65,12],[8,53,-74],['--vigil','120']),
 ('pueblo_escarlata','dark_03',[0,1.65,-80],[9,43,-190],[]),
 ('castillo_palido','dark_04',[0,1.65,-150],[4,34,-260],[]),
 ('isla_y_observador','dream_sanctuary_clouds',[20,74,-20],[30,133,-200],[]),
 ('isla_flotante','dream_sanctuary_clouds',[100,132,40],[0,92,-58],[]),
 ('angel_distante','dream_sanctuary_clouds',[-20,74,-20],[52,228,-245],[]),
 ('los_cuatro','preview_eyes',[-8,4,-12],[0,4,-43],[]),
 ('observador_sentado','preview_eyes',[-11,3,-22],[-16,3.5,-31],[]),
 ('caballero_del_ojo','preview_eyes',[15,3,-30],[13,3.1,-39],[]),
 ('arbol_que_mira','preview_eyes',[26,5,-72],[26,9,-92],[]),
 ('alas_en_reposo','preview_eyes',[-34,3,-53],[-34,4,-65],[])]
import shutil
for name,world,eye,target,extra in views:
 # Last occurrence of --vigil must be the explicit state requested for this view.
 path=render(world,name,eye,target,extra,960,540);shutil.copyfile(path,gallery/(name+'.png'))
for variant in range(8):render('dark_01','lunar_variant_'+str(variant),[0,1.65,12],[8,53,-74],['--moon-variant',str(variant),'--only-main-moon'])
for world in ['dark_01','dark_03','dark_04','dream_sanctuary_clouds','preview_eyes']:
 c=cams[world];render(world,world+'_low',c['eye'],c['target'],['--quality','0','--mediump'])
render('dark_01','moon_legacy_fallback',[0,1.65,12],[8,53,-74],['--legacy'])
# In fallback preview the 3D path remains supported; the classic path is separately tested without the flag.
render('echo_09','legacy_interior',[0,1.65,0],[0,1.8,-50],['--legacy'])
render('dream_02','no_depth_pool',[0,1.65,-15],[5.45,-.8,-21],['--no-depth'])
(r/'tools/graphics_validation27.json').write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n');print('ALL VIEWS RENDERED',flush=True)
