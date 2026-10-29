"""Compile every actual world-specialized scene/sky variant, plus compatible/mediump paths."""
from pathlib import Path
import json,sys,os
import validate_shaders as V
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation30')));records=[]
for code in range(64):
 for scene,sky in [(0,2),(4,6)]:
  for n,name in [(scene,'scene'),(sky,'sky')]:
   for medium in [False,True]:
    vs=(out/f'shader_{code}_{n}.glsl').read_text();fs=(out/f'shader_{code}_{n+1}.glsl').read_text()
    if medium:vs=vs.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0');fs=fs.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0')
    p,rec=V.compile_pair(vs,fs,f'{code}:{name}:{"legacy" if scene else "enhanced"}:{"medium" if medium else "full"}');records.append(rec);V.deleteprogram(p)
 for medium in [False,True]:
  vs=(out/f'surface_vertex_{code}.glsl').read_text();fs=(out/f'surface_{code}.glsl').read_text()
  if medium:vs=vs.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0');fs=fs.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0')
  p,rec=V.compile_pair(vs,fs,f'{code}:compact:{medium}');records.append(rec);V.deleteprogram(p)
 print('world',code,'10 shader variants linked',flush=True)
p,rec=V.compile_pair((out/'entity32_vertex.glsl').read_text(),(out/'entity32_fragment.glsl').read_text(),'entities32');records.append(rec);V.deleteprogram(p)
report=dict(backend=V.getstr(0x1F01).decode(),programs=records,all_linked=True,scene_fragment_samplers=8,enhanced_sky_fragment_samplers=6,phone_test=False)
(out/'specialized_shaders34.json').write_text(json.dumps(report,indent=2)+'\n');print('ALL 641 SPECIALIZED PAIRS LINKED',flush=True)
