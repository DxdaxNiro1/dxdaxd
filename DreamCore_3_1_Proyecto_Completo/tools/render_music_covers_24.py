"""Render actual map thumbnails from the geometry dumped by run_core_checks_24.py."""
from pathlib import Path
import argparse,json,subprocess,os,shutil
root=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser();parser.add_argument('--meshes',type=Path,default=root/'build/validation24');args=parser.parse_args()
catalog=json.loads((root/'tools/music_catalog_24.json').read_text());cameras=json.loads((root/'tools/music_cameras_24.json').read_text())
codes={a:float(c) for a,b,c in (line.split('\t') for line in (args.meshes/'catalog_maps.tsv').read_text().splitlines())}
resources=root/'app/src/main/res/drawable-nodpi';album=root/'Banda_sonora/Miniaturas';album.mkdir(parents=True,exist_ok=True)
report=[]
for track in catalog:
    world=track['world'];mesh='dream_01' if world=='title' else world;camera=cameras[world];dest=resources/(track['cover']+'.png')
    run=subprocess.run(['python',str(root/'tools/preview_scene23.py'),str(args.meshes/(mesh+'.f32')),str(root/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(dest),'--world',str(codes.get(world,0)),'--eye',*map(str,camera['eye']),'--target',*map(str,camera['target']),'--time','50','--late-sky','--atlas',str(resources/'dream_materials_21.png'),*camera['extra']],env=dict(os.environ,PREVIEW_WIDTH='480',PREVIEW_HEIGHT='270'),capture_output=True,text=True)
    if run.returncode:print(run.stdout+run.stderr);run.check_returncode()
    item=json.loads(run.stdout);item.update(world=world,number=track['number'],map=track['map']);item['image']=str(dest.relative_to(root));report.append(item)
    shutil.copyfile(dest,album/dest.name);print(world,flush=True)
(root/'tools/music_cover_validation_24.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
