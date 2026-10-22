"""GLES2 shader/capability and depth-AO regressions on a real Mesa context."""
from pathlib import Path
import os,json,subprocess,sys
import numpy as np
from PIL import Image
import validate_shaders as V
root=Path(__file__).resolve().parents[1];out=Path(sys.argv[1] if len(sys.argv)>1 else root/'build/graphics26_checks');out.mkdir(parents=True,exist_ok=True)
source=root/'app/src/main/java/com/dreamcore/game';records=[]
for name,pairs in [('DreamShaders.kt',[('vertexShader','fragmentShader'),('skyVertexShader','skyFragmentShader'),('shadowVertexShader','shadowFragmentShader')]),('DreamLegacyShaders.kt',[('vertexShader','fragmentShader'),('skyVertexShader','skyFragmentShader')]),('DreamPost26.kt',[('aoVertexShader','aoFragmentShader'),('vertexShader','compositeFragmentShader'),('vertexShader','bloomFragmentShader')])]:
 shaders=V.extract_shaders(source/name)
 for vertex,fragment in pairs:
  for medium in ([False,True] if name!='DreamPost26.kt' else [False]):
   vs=shaders[vertex];fs=shaders[fragment]
   if medium:vs=vs.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0');fs=fs.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0')
   program,rec=V.compile_pair(vs,fs,name+':'+fragment+(':mediump' if medium else ''));records.append(rec);V.deleteprogram(program)
def tri(a,b,c,n=(0,1,0)):
 return [value for p in (a,b,c) for value in (*p,.65,.65,.65,*n,10)]
def quad(a,b,c,d,n):return tri(a,b,c,n)+tri(a,c,d,n)
a=(-50,0,-80);b=(50,0,-80);c=(50,0,30);d=(-50,0,30)
flat=tri(a,b,c)+tri(a,c,d);np.asarray(flat,dtype='<f4').tofile(out/'flat.f32')
box=[]
for n,points in [((0,0,1),[(-1,0,-8),(1,0,-8),(1,2,-8),(-1,2,-8)]),((0,1,0),[(-1,2,-8),(1,2,-8),(1,2,-10),(-1,2,-10)]),((1,0,0),[(1,0,-8),(1,0,-10),(1,2,-10),(1,2,-8)]),((-1,0,0),[(-1,0,-10),(-1,0,-8),(-1,2,-8),(-1,2,-10)])]:box+=quad(*points,n)
np.asarray(flat+box,dtype='<f4').tofile(out/'contact.f32')
results={}
for name in ['flat','contact']:
 cmd=['python',str(root/'tools/preview_scene26.py'),str(out/(name+'.f32')),str(source/'DreamShaders.kt'),str(out/(name+'.png')),'--world','38','--eye','0','1.65','5','--target','0','0','-10','--atlas',str(root/'app/src/main/res/drawable-nodpi/dream_materials_25.png'),'--save-buffers']
 q=subprocess.run(cmd,capture_output=True,text=True,env=dict(os.environ,PREVIEW_WIDTH='640',PREVIEW_HEIGHT='360'))
 if q.returncode:raise RuntimeError(q.stdout+q.stderr)
 red=np.asarray(Image.open(out/(name+'_ao.png')))[:,:,0]
 results[name]=dict(minimum=int(red.min()),mean=float(red.mean()),fraction_below_250=float(np.mean(red<250)))
assert results['flat']['minimum']>=253,'Flat plane must not self-occlude'
assert results['contact']['minimum']<235,'Box/floor contact must produce contact occlusion'
assert results['contact']['fraction_below_250']<.25,'Contact darkening must remain localized'
report=dict(renderer=V.getstr(0x1F01).decode(),programs=records,ao=results,depth_type='GL_UNSIGNED_INT; highp sampler; texel-centered reconstruction',phone_test=False)
(root/'tools/gpu_checks26.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
