"""Reference software GLES2 timings, not measurements of phone FPS or temperature."""
from pathlib import Path
import os,json,subprocess
r=Path(__file__).resolve().parents[1];old=r.parent/'DreamCore_2_7_Vigilia';out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation28')));dest=out/'benchmark';dest.mkdir(exist_ok=True)
meshes=Path(os.environ.get('DREAMCORE_BASELINE_DUMPS',str(r.parent/'DreamCore_2_7_Vigilia/build/validation27')));cams=json.loads((old/'tools/music_cameras_27.json').read_text());codes={v[0]:v[2] for l in (meshes/'catalog_maps.tsv').read_text().splitlines() if (v:=l.split('\t'))}
selected=['dream_02','dream_03','dream_05','dream_06','echo_09','echo_11','backrooms_01','dark_01','dark_03','dark_04','dream_sanctuary_clouds','dream_rose_island','dream_flower_city','preview_eyes'];records=[]
for world in selected:
 c=cams[world];pair=[]
 for optimized in [False,True]:
  src=r if optimized else old;name=world+('_28' if optimized else '_27');stats=dest/(name+'.json')
  cmd=['python',str(r/'tools/preview_scene28.py'),str(meshes/(world+'.f32')),str(src/'app/src/main/java/com/dreamcore/game/DreamShaders.kt'),str(dest/(name+'.png')),'--world',codes[world],'--eye',*map(str,c['eye']),'--target',*map(str,c['target']),'--time','40','--atlas',str(src/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),'--bench-frames','24','--bench-json',str(stats),*c.get('extra',[])]
  if world.startswith('dark_'):cmd+=['--lunar-models','--vigil','40']
  if optimized:cmd+=['--optimized','--shader-dir',str(out),'--indexed-dir',str(out/'indexed')]
  q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='640',PREVIEW_HEIGHT='360',LP_NUM_THREADS='4'))
  if q.returncode:raise RuntimeError(name+'\n'+q.stdout+q.stderr)
  pair.append(json.loads(stats.read_text()))
 before,after=pair;rec=dict(world=world,before=before,after=after,geometry_change_percent=100*(after['median_ms']/before['median_ms']-1),cloud_change_percent=100*(after['cloud_median_ms']/before['cloud_median_ms']-1) if before['cloud_median_ms'] else 0)
 records.append(rec);(out/'benchmark_results.json').write_text(json.dumps(records,indent=2)+'\n');print(world,'geometry',round(rec['geometry_change_percent'],1),'cloud',round(rec['cloud_change_percent'],1),'masked',after['cloud_masked_texels'],'/',after['cloud_total_texels'],flush=True)
print('REFERENCE BENCHMARK COMPLETE',flush=True)
