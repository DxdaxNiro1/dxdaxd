"""Render actual game geometry + shipped GLES2 stages, including half-size clouds and AO."""
from pathlib import Path
import argparse,subprocess,json,os
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--meshes',type=Path,required=True);p.add_argument('--previous-meshes',type=Path);p.add_argument('--output',type=Path,required=True);a=p.parse_args();a.output.mkdir(parents=True,exist_ok=True)
catalog=json.loads((root/'tools/music_catalog_24.json').read_text());cameras=json.loads((root/'tools/music_cameras_24.json').read_text());profiles=json.loads((root/'tools/lighting_profiles25.json').read_text());codes={v[0]:v[2] for line in (a.meshes/'catalog_maps.tsv').read_text().splitlines() if (v:=line.split('\t'))}
shader=root/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=root/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
selected=['dream_02','dream_05','dream_03','echo_09','echo_11','dark_01','dream_rose_island','backrooms_01','dream_cloud_castle'];records=[]
def run(cmd,w=480,h=270):
 result=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH=str(w),PREVIEW_HEIGHT=str(h)))
 if result.returncode:raise RuntimeError(' '.join(cmd)+'\n'+result.stdout+result.stderr)
 return json.loads(result.stdout)
def before(cmd,name):
 if a.previous_meshes is None:return
 old=root.parent/'DreamCore_2_5_Luz_de_los_Suenos';previous=cmd.copy();previous[1]=str(root/'tools/preview_scene26.py');previous+=['--baseline25'];previous[2]=str(a.previous_meshes/Path(cmd[2]).name);previous[3]=str(old/'app/src/main/java/com/dreamcore/game/DreamShaders.kt');previous[4]=str(root/'vistas'/('antes_'+name+'.png'));previous[previous.index('--atlas')+1]=str(old/'app/src/main/res/drawable-nodpi/dream_materials_25.png');run(previous,1280,720)
for t in catalog:
 world=t['world'];mesh='dream_01' if world=='title'else world;c=cameras[world]
 cmd=['python',str(root/'tools/preview_scene26.py'),str(a.meshes/(mesh+'.f32')),str(shader),str(a.output/(world+'.png')),'--world',codes.get(world,'0'),'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','50','--late-sky','--atlas',str(atlas),'--light',*map(str,profiles.get(mesh,[.5,.68,0])),*c['extra']]
 rec=run(cmd);rec.update(world=world,map=t['map'],quality=1,post=True,cloud_volume_half_size=True);records.append(rec);print(world,'balanced pipeline OK',flush=True)
 if world in selected:
  target=root/'vistas'/('despues_'+world+'.png');target.parent.mkdir(exist_ok=True);larger=cmd.copy();larger[4]=str(target);run(larger,1280,720)
  if world in ['dream_02','dream_05','echo_09']:before(larger,world)
  low=cmd.copy();low[4]=str(a.output/(world+'_low_mediump.png'));low+=['--quality','0','--mediump'];run(low);records.append(dict(world=world,quality=0,mediump=True,post=False,rendered=True))
 if world in ['dream_02','dream_05']:
  high=cmd.copy();high[4]=str(a.output/(world+'_high.png'));high+=['--quality','2'];run(high,960,540);records.append(dict(world=world,quality=2,post=True,bloom=True,rendered=True))
 if world=='dream_02':
  fallback=cmd.copy();fallback[4]=str(a.output/'no_depth_pool.png');fallback+=['--no-depth'];run(fallback);records.append(dict(world=world,quality=1,depth_texture=False,fxaa=True,rendered=True))
for name,world,eye,target,extra in [
 ('pueblo_detalle','dream_05',[77,1.65,25],[75,4,0],[]),
 ('cielo','dream_05',[0,1.65,35],[0,85,-100],['--sky-only']),
 ('agua','dream_02',[0,1.65,-15],[5.45,-.8,-21],[])]:
 cmd=['python',str(root/'tools/preview_scene26.py'),str(a.meshes/(world+'.f32')),str(shader),str(root/'vistas'/('despues_'+name+'.png')),'--world',codes[world],'--eye',*map(str,eye),'--target',*map(str,target),'--time','50','--late-sky','--atlas',str(atlas),'--light',*map(str,profiles[world]),*extra]
 run(cmd,1280,720);before(cmd,name);records.append(dict(detail=name,world=world,quality=1,rendered=True))
(root/'tools/graphics_validation26.json').write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n');print('ALL VIEWS RENDERED',flush=True)
