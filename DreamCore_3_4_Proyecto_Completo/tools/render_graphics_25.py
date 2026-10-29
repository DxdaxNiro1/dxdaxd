"""Render the shipped GLSL and game meshes; these are game views, not concept art."""
from pathlib import Path
import argparse,subprocess,json,os,shutil
root=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--meshes',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();a.output.mkdir(parents=True,exist_ok=True)
catalog=json.loads((root/'tools/music_catalog_24.json').read_text());cameras=json.loads((root/'tools/music_cameras_24.json').read_text());profiles=json.loads((root/'tools/lighting_profiles25.json').read_text());codes={v[0]:v[2] for line in (a.meshes/'catalog_maps.tsv').read_text().splitlines() if (v:=line.split('\t'))}
shader=root/'app/src/main/java/com/dreamcore/game/DreamShaders.kt';atlas=root/'app/src/main/res/drawable-nodpi/dream_materials_25.png'
selected=['dream_02','dream_05','dream_03','echo_09','echo_11','dark_01','dream_rose_island','backrooms_01'];records=[]
for t in catalog:
 world=t['world'];mesh='dream_01' if world=='title'else world;c=cameras[world]
 cmd=['python',str(root/'tools/preview_scene25.py'),str(a.meshes/(mesh+'.f32')),str(shader),str(a.output/(world+'.png')),'--world',codes.get(world,'0'),'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','50','--late-sky','--atlas',str(atlas),'--light',*map(str,profiles.get(mesh,[.5,.68,0])),*c['extra']]
 result=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='480',PREVIEW_HEIGHT='270'));result.check_returncode();rec=json.loads(result.stdout);rec.update(world=world,map=t['map'],quality=1);records.append(rec);print(world,'balanced render OK',flush=True)
 if world in selected:
  target=root/'vistas'/('despues_'+world+'.png');target.parent.mkdir(exist_ok=True);larger=cmd.copy();larger[4]=str(target)
  result=subprocess.run(larger,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='1280',PREVIEW_HEIGHT='720'));result.check_returncode()
  if world in ['dream_02','dream_05','echo_09'] and (root.parent/'DreamCore_2_4_Resonancias').exists():
   before=larger.copy();old=root.parent/'DreamCore_2_4_Resonancias';before[3]=str(old/'app/src/main/java/com/dreamcore/game/DreamShaders.kt');before[4]=str(root/'vistas'/('antes_'+world+'.png'));before[before.index('--atlas')+1]=str(old/'app/src/main/res/drawable-nodpi/dream_materials_21.png')
   subprocess.run(before,check=True,capture_output=True,env=dict(os.environ,PREVIEW_WIDTH='1280',PREVIEW_HEIGHT='720'))
  low=cmd.copy();low[4]=str(a.output/(world+'_low_mediump.png'));low+=['--quality','0','--mediump']
  subprocess.run(low,check=True,capture_output=True,env=dict(os.environ,PREVIEW_WIDTH='480',PREVIEW_HEIGHT='270'))
  records.append(dict(world=world,quality=0,mediump=True,rendered=True))
 if world=='dream_02':
  high=cmd.copy();high[4]=str(a.output/(world+'_high.png'));high+=['--quality','2']
  subprocess.run(high,check=True,capture_output=True,env=dict(os.environ,PREVIEW_WIDTH='960',PREVIEW_HEIGHT='540'));records.append(dict(world=world,quality=2,rendered=True))
(root/'tools/graphics_validation25.json').write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n');print('ALL VIEWS RENDERED',flush=True)
