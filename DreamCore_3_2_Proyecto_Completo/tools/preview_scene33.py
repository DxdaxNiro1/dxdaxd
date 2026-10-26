#!/usr/bin/env python3
"""Render the actual float32 scene, GLSL and APK atlas via GLES2/Mesa."""
from pathlib import Path
import sys,os,ctypes as C,math,json,zipfile,argparse
import numpy as np
from PIL import Image
import validate_shaders as V
p=argparse.ArgumentParser()
p.add_argument('geometry'); p.add_argument('shaders'); p.add_argument('output')
p.add_argument('--eye',nargs=3,type=float,default=[0,4,-2])
p.add_argument('--target',nargs=3,type=float,default=[0,-160,-120])
p.add_argument('--light',nargs=3,type=float)
p.add_argument('--quality',type=int,choices=[0,1,2],default=1)
p.add_argument('--mediump',action='store_true')
p.add_argument('--no-post',action='store_true')
p.add_argument('--no-shadow',action='store_true')
p.add_argument('--legacy',action='store_true')
p.add_argument('--baseline25',action='store_true')
p.add_argument('--debug-cloud',action='store_true')
p.add_argument('--no-depth',action='store_true')
p.add_argument('--save-buffers',action='store_true')
p.add_argument('--atlas'); p.add_argument('--time',type=float,default=8.0)
p.add_argument('--world',type=float,default=2.0)
p.add_argument('--clouds')
p.add_argument('--sky-only',action='store_true')
p.add_argument('--mirror',nargs=2,type=float)
p.add_argument('--mirror-y',type=float)
p.add_argument('--presence',action='store_true')
p.add_argument('--entity-art',action='store_true')
p.add_argument('--actor-yaw',type=float,default=0.)
p.add_argument('--circuit',type=float,default=0.)
p.add_argument('--train-offset',type=float,default=0.)
p.add_argument('--up',nargs=3,type=float)
p.add_argument('--entity',nargs=3,type=float,default=[22,0,-80])
p.add_argument('--late-sky',action='store_true')
p.add_argument('--tool',choices=['none','lantern','prism'],default='none')
p.add_argument('--photo',action='store_true')
p.add_argument('--moon-state')
p.add_argument('--lunar-models',action='store_true')
p.add_argument('--optimized',action='store_true')
p.add_argument('--shader-dir',type=Path)
p.add_argument('--indexed-dir',type=Path)
p.add_argument('--bench-frames',type=int,default=0)
p.add_argument('--no-mask',action='store_true')
p.add_argument('--bench-json',type=Path)
p.add_argument('--vigil',type=float,default=0.)
p.add_argument('--blink',type=float,default=0.)
p.add_argument('--moon-variant',type=int)
p.add_argument('--only-main-moon',action='store_true')
p.add_argument('--surface32',action='store_true')
p.add_argument('--entities32',type=Path)
p.add_argument('--props30',type=Path)
p.add_argument('--state33',type=Path)
p.add_argument('--phase30',type=float,default=0.)
p.add_argument('--near',type=float,default=.18)
p.add_argument('--far',type=float,default=520.)
p.add_argument('--fov',type=float,default=66.)
a=p.parse_args();a.late_sky=True
if a.baseline25:a.no_post=True
W=int(os.environ.get('PREVIEW_WIDTH','960')); H=int(os.environ.get('PREVIEW_HEIGHT','540'))
U=V.U; I=V.I; P=V.P; F=C.c_float
geterr=V.gl('glGetError',U,[])
viewport=V.gl('glViewport',None,[I,I,I,I]); clearcolor=V.gl('glClearColor',None,[F,F,F,F]); clear=V.gl('glClear',None,[U])
enable=V.gl('glEnable',None,[U]); disable=V.gl('glDisable',None,[U]); use=V.gl('glUseProgram',None,[U])
uniform2f=V.gl('glUniform2f',None,[I,F,F])
uniform4f=V.gl('glUniform4f',None,[I,F,F,F,F])
getuniform=V.gl('glGetUniformLocation',I,[U,C.c_char_p]); getattribute=V.gl('glGetAttribLocation',I,[U,C.c_char_p])
uniform1f=V.gl('glUniform1f',None,[I,F]); uniform1i=V.gl('glUniform1i',None,[I,I]); uniform3f=V.gl('glUniform3f',None,[I,F,F,F])
uniformmat=V.gl('glUniformMatrix4fv',None,[I,I,V.B,C.POINTER(F)])
genbuffers=V.gl('glGenBuffers',None,[I,C.POINTER(U)]); bindbuffer=V.gl('glBindBuffer',None,[U,U]); bufferdata=V.gl('glBufferData',None,[U,C.c_ssize_t,P,U])
enattr=V.gl('glEnableVertexAttribArray',None,[U]); disattr=V.gl('glDisableVertexAttribArray',None,[U]); attrptr=V.gl('glVertexAttribPointer',None,[U,I,U,V.B,I,P])
draw=V.gl('glDrawArrays',None,[U,I,I]); read=V.gl('glReadPixels',None,[I,I,I,I,U,U,P]); finish=V.gl('glFinish',None,[])
gentex=V.gl('glGenTextures',None,[I,C.POINTER(U)]); bindtex=V.gl('glBindTexture',None,[U,U]); activetex=V.gl('glActiveTexture',None,[U])
teximage=V.gl('glTexImage2D',None,[U,I,I,I,I,I,U,U,P]); texparam=V.gl('glTexParameteri',None,[U,U,I])
generatemipmap=V.gl('glGenerateMipmap',None,[U])

def uniform(program,name,*values):
 loc=getuniform(program,name.encode())
 if loc<0:return
 if len(values)==4:uniform4f(loc,*values)
 elif len(values)==3:uniform3f(loc,*values)
 elif len(values)==2:uniform2f(loc,*values)
 else:uniform1f(loc,values[0])

def upload(data):
 buf=U();genbuffers(1,C.byref(buf));bindbuffer(0x8892,buf.value)
 bufferdata(0x8892,data.nbytes,P(data.ctypes.data),0x88E4)
 return buf

def attributes(program,items,stride):
 active=[]
 for name,size,offset in items:
  loc=getattribute(program,name.encode())
  if loc>=0:enattr(loc);attrptr(loc,size,0x1406,False,stride,P(offset));active.append(loc)
 return active

shaders=V.extract_shaders(a.shaders)
if a.optimized:
 for i,k in enumerate(['vertexShader','fragmentShader','skyVertexShader','skyFragmentShader']):shaders[k]=(a.shader_dir/f'shader_{int(a.world)}_{i}.glsl').read_text()
if a.legacy:
 shaders.update(V.extract_shaders(Path(a.shaders).with_name('DreamLegacyShaders.kt')));a.no_post=True
 if a.optimized:
  for i,k in enumerate(['vertexShader','fragmentShader','skyVertexShader','skyFragmentShader']):shaders[k]=(a.shader_dir/f'shader_{int(a.world)}_{i+4}.glsl').read_text()
if a.mediump:shaders={k:v.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0') for k,v in shaders.items()}
if a.debug_cloud:shaders['skyFragmentShader']=shaders['skyFragmentShader'].replace('vec3 ray = normalize(vRay);','vec3 ray = normalize(vRay);gl_FragColor=vec4(cloudDensity(vec3(vRay.x*.5+.5,.4,vRay.y*.5+.5)),texture2D(uClouds,(vRay.xy*.3+.5)).r,texture2D(uClouds,(vRay.xy*.3+.5)).a,1.0);return;')
scene,_=V.compile_pair(shaders['vertexShader'],shaders['fragmentShader'],'scene')
sky,_=V.compile_pair(shaders['skyVertexShader'],shaders['skyFragmentShader'],'sky')
eye=np.array(a.eye); target=np.array(a.target); forward=(target-eye);forward/=np.linalg.norm(forward)
vertical=np.array(a.up) if a.up else (np.array([0.,1.,0.]) if abs(forward[1])<.99 else np.array([0.,0.,1.]))
side=np.cross(forward,vertical);side/=np.linalg.norm(side); up=np.cross(side,forward)
presets={
 3:((.025,.035,.12),(.22,.24,.40),(.22,.24,.40),125,315),
 4:((.15,.48,.94),(.89,.79,.90),(.78,.81,.88),125,315),
 5:((.025,.035,.12),(.22,.24,.40),(.22,.24,.40),125,315),
 6:((.30,.39,.55),(.68,.62,.77),(.78,.81,.88),45,180),
 7:((.025,.035,.12),(.22,.24,.40),(.22,.24,.40),45,180),
 12:((.008,.012,.04),(.065,.050,.13),(.055,.065,.115),60,240),
 2:((.34,.46,.75),(.94,.80,.86),(.77,.82,.76),105,420),
 8:((.23,.43,.70),(.91,.84,.77),(.72,.78,.85),100,340),
 9:((.35,.48,.68),(.77,.85,.89),(.48,.62,.69),38,160),
 10:((.42,.43,.70),(.98,.73,.80),(.79,.65,.78),100,340),
 11:((.36,.34,.67),(.98,.67,.77),(.79,.62,.79),100,340),
}
for code in (13,14,15,16):presets[code]=((.30,.39,.55),(.68,.62,.77),(.88,.78,.80),56,240)
presets.update({17:((.04,.31,.70),(.66,.80,.94),(.88,.78,.80),56,240),18:((.22,.31,.54),(.94,.70,.52),(.88,.78,.80),56,240),19:((.008,.018,.055),(.08,.13,.25),(.055,.065,.115),60,240),20:((.08,.15,.30),(.55,.57,.65),(.88,.78,.80),56,240),21:((.008,.018,.055),(.08,.13,.25),(.88,.78,.80),56,240)})
for code in range(30,41):presets[code]=((.10,.43,.77),(.78,.88,.94),(.72,.79,.83),135,420)
presets[31]=((.005,.025,.065),(.08,.30,.48),(.17,.19,.23),55,240)
presets[32]=((.51,.62,.65),(.80,.86,.84),(.72,.79,.83),135,420)
presets[36]=((.45,.40,.64),(1.,.67,.46),(.72,.79,.83),135,420)
for code in (38,40):presets[code]=((.10,.43,.77),(.78,.88,.94),(.17,.19,.23),55,240)
presets[39]=((.025,.028,.035),(.05,.055,.065),(.17,.19,.23),55,240)
presets[41]=((.003,.005,.007),(.003,.005,.007),(.025,.04,.03),125,300)
presets[42]=((.15,.57,.76),(.58,.80,.84),(.43,.74,.78),180,520)
presets[43]=((.30,.39,.55),(.68,.62,.77),(.38,.35,.18),25,125)
presets[44]=((.025,.08,.18),(.22,.38,.46),(.69,.79,.84),135,410)
for code in (45,46,47):presets[code]=((.08,.40,.78),(.73,.84,.90),(.69,.79,.84),135,410)
presets[48]=((.30,.39,.55),(.68,.62,.77),(.20,.23,.26),25,125)
presets[49]=presets[50]=((.008,.018,.055),(.08,.13,.25),(.055,.065,.11),95,380)
presets[51]=((.13,.49,.83),(.80,.90,.96),(.69,.82,.91),220,520)
for code in range(55,62):
 night=code in (57,58)
 presets[code]=((.006,.012,.035),(.09,.10,.16),(.045,.055,.09),65,270) if night else ((.15,.26,.31),(.46,.54,.55),(.30,.38,.42),100,420)
spread28=.649408*(abs(float(side[1]))*W/H+abs(float(up[1])))
low28=float(forward[1])-spread28;high28=float(forward[1])+spread28
if int(a.world) in (10,33,45,47,51):cloud_intersection28=(a.eye[1]<190 and high28>0) or (a.eye[1]>-135 and low28<0)
else:cloud_intersection28=high28>0 if a.eye[1]<85 else low28<0 if a.eye[1]>190 else True
top,horizon,fog,fognear,fogfar=presets.get(int(a.world),((.34,.46,.75),(.94,.80,.86),(.48,.54,.69),105,420))
sun=(.10,.65,-.80) if int(a.world) in (3,5,7,11,12,19,21,41,49,50,57,58) else (-.40,.82,.40)
cloud_name='dream_cloud_density.png' if a.baseline25 else 'dream_cloud_volume_26.png'
cloudpath=Path(a.clouds) if a.clouds else Path(a.shaders).parents[4]/'res/drawable-nodpi'/cloud_name
if not cloudpath.exists():
 candidates=list(Path(a.shaders).parents[4].glob('res/drawable*/dream_cloud_volume_26.png'))
 if candidates:cloudpath=candidates[0]
if 'uClouds' in shaders['skyFragmentShader']:
 if not cloudpath.exists(): raise RuntimeError(f'Cloud density PNG not found: {cloudpath}; use --clouds')
 cloudimage=Image.open(cloudpath).convert('RGBA'); cloudpixels=np.asarray(cloudimage,dtype=np.uint8).copy()
 cloudtexture=U();gentex(1,C.byref(cloudtexture));activetex(0x84C1);bindtex(0x0DE1,cloudtexture.value)
 for parameter,value in [(0x2801,0x2601),(0x2800,0x2601),(0x2802,0x2901),(0x2803,0x2901)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,cloudimage.width,cloudimage.height,0,0x1908,0x1401,P(cloudpixels.ctypes.data))
 for prog in (scene,sky):
  use(prog);uniform1i(getuniform(prog,b'uClouds'),1)
 activetex(0x84C0)
moonpath=Path(a.shaders).parents[4]/'res/drawable-nodpi'/('dream_entities_22.png' if a.entity_art or int(a.world)==41 else 'dream_moon_face.png')
if 'uMoon' in shaders['skyFragmentShader']:
 moonimage=Image.open(moonpath).convert('RGBA').resize((1024,1024),Image.Resampling.BILINEAR);moonpixels=np.asarray(moonimage,dtype=np.uint8).copy()
 moontexture=U();gentex(1,C.byref(moontexture));activetex(0x84C2);bindtex(0x0DE1,moontexture.value)
 for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,moonimage.width,moonimage.height,0,0x1908,0x1401,P(moonpixels.ctypes.data))
 generatemipmap(0x0DE1)
 for prog in (scene,sky):
  use(prog);uniform1i(getuniform(prog,b'uMoon'),2)
 activetex(0x84C0)
if a.moon_state:
 state=json.loads(Path(a.moon_state).read_text());a.time=state['time']
 for prog in (scene,sky):
  use(prog)
  for key,name in [('mood','uMoonMood'),('eyes','uEyeTarget'),('giant','uGiant')]:
   uniform4f(getuniform(prog,name.encode()),*state[key])
  uniform(prog,'uGiantDirection',*state['direction'])
for resource,unit,name,dimensions in [('dream_planets_19.png',6,'uPlanets',(1024,512)),('dream_weeping_face_19.png',7,'uWeeping',(1024,1024))]:
 if unit==7 and int(a.world) in (41,51):resource='dream_guardians_27.png'
 source=Path(a.shaders).parents[4]/'res/drawable-nodpi'/resource
 im=Image.open(source).convert('RGBA').resize(dimensions,Image.Resampling.BILINEAR);data=np.asarray(im,dtype=np.uint8).copy()
 if int(a.world)==22 and unit==7:data[:]=[46,49,63,255] # same empty wall atlas as Android Canvas
 tex=U();gentex(1,C.byref(tex));activetex(0x84C0+unit);bindtex(0x0DE1,tex.value)
 for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,*dimensions,0,0x1908,0x1401,P(data.ctypes.data));generatemipmap(0x0DE1)
 for prog in (scene,sky):
  use(prog);uniform1i(getuniform(prog,name.encode()),unit);uniform(prog,'uUniverse',3271%89);uniform(prog,'uEclipse',0)
for prog in (scene,sky):
 use(prog);uniform(prog,'uModeledMoons',1 if a.lunar_models else 0);uniform(prog,'uLunarBlood',1 if a.vigil>=60 else 0);uniform(prog,'uCloudCamera',*a.eye)
 uniform(prog,'uCloudParams',1/(1200 if int(a.world)==35 else 650),85,105,(0 if int(a.world) in [14,15,16,22,23,31,38,39,40,43,48] else [0,16,24][a.quality]))
 uniform(prog,'uCloudDrift',(a.time*.0011)%1,(a.time*.00032)%1)
state33=json.loads(a.state33.read_text()) if a.state33 else {}
profiles33=json.loads((a.shader_dir/'profiles33.json').read_text()) if a.shader_dir and (a.shader_dir/'profiles33.json').exists() else {}
for prog in (scene,sky):
 use(prog);uniform(prog,'uArtProfile33',*state33.get('art',profiles33.get(str(int(a.world)),[0,0,0,0])));uniform(prog,'uChapter33',*state33.get('chapter',[0,0,0,0]))
activetex(0x84C0)
use(scene);uniform4f(getuniform(scene,b'uScene'),1,1,1,0);uniform(scene,'uPhoto',1 if a.photo else 0)
viewport(0,0,W,H); clearcolor(.48,.54,.69,1);clear(0x4000|0x0100);disable(0x0B71)
use(sky);uniform(sky,'uTop',*top);uniform(sky,'uHorizon',*horizon)
uniform(sky,'uWorld',a.world);uniform(sky,'uTime',a.time)
uniform(sky,'uForward',*forward);uniform(sky,'uRight',*side);uniform(sky,'uUp',*up)
uniform(sky,'uAspect',W/H);uniform(sky,'uSun',-.34,.82,.46)
skydata=np.array([-1,-1,1,-1,-1,1,-1,1,1,-1,1,1],dtype=np.float32);skybuf=upload(skydata)
attrs=attributes(sky,[('aXY',2,0)],8)
if not a.late_sky:draw(4,0,6)
for at in attrs: disattr(at)
clear(0x0100); enable(0x0B71);disable(0x0B44)
use(scene)
view=np.eye(4); view[0,:3]=side; view[1,:3]=up; view[2,:3]=-forward
view[:3,3]=-(view[:3,:3]@eye)
f=1/math.tan(math.radians(a.fov)/2); near=a.near; far=a.far
projection=np.zeros((4,4));projection[0,0]=f/(W/H);projection[1,1]=f
projection[2,2]=(far+near)/(near-far);projection[2,3]=2*far*near/(near-far);projection[3,2]=-1
vp=np.asarray((projection@view).T,dtype=np.float32).copy()
uniformmat(getuniform(scene,b'uVP'),1,False,vp.ctypes.data_as(C.POINTER(F)))
uniform(scene,'uCircuit',a.circuit);uniform(scene,'uTrainOffset',a.train_offset)
uniform(scene,'uMirrorReady',0);uniform(scene,'uMirrorPass',0);uniform(scene,'uPresence',0)
uniform(scene,'uMirrorCenter',9999,0,9999)
uniform(scene,'uCamera',*a.eye); uniform(scene,'uWorld',a.world);uniform(scene,'uTime',a.time);uniform(scene,'uAnomaly',0)
uniform(scene,'uFogColor',*fog);uniform(scene,'uSun',*sun)
uniform(scene,'uFogNear',fognear);uniform(scene,'uFogFar',fogfar)
atlas_size=[512,1024,2048][a.quality]
if a.light:light=a.light
elif int(a.world)==41:light=[.14,.18,1.]
elif int(a.world) in [14,15,16,22,23,31,38,39,40,43,48,52,55]:light=[.48,.08,.15]
elif int(a.world) in [3,5,7,11,12,19,21,49,50,57,58]:light=[.24,.40,1.]
else:light=[.50,.68,0.]
uniform4f(getuniform(scene,b'uLook'),*light,float(a.quality))
if a.atlas: atlas=Image.open(a.atlas).convert('RGBA').resize((atlas_size,atlas_size),Image.Resampling.BILINEAR)
else:
 import io
 apk=next(Path('/workspace/attachments').glob('*/app-debug.apk'))
 with zipfile.ZipFile(apk) as z:atlas=Image.open(io.BytesIO(z.read('res/drawable-nodpi-v4/dreamcore_atlas.png'))).convert('RGBA')
if not a.baseline25:
 botany=Image.open(Path(a.shaders).parents[4]/'res/drawable-nodpi/dream_botany_26.png').convert('RGBA').resize((atlas_size,atlas_size),Image.Resampling.BILINEAR)
 combined=np.concatenate([np.asarray(atlas),np.asarray(botany)],axis=0)
 atlas=Image.fromarray(combined)
# Android Bitmap rows upload in their original order (no vertical flip).
pixels=np.asarray(atlas,dtype=np.uint8).copy(); texture=U();gentex(1,C.byref(texture));activetex(0x84C0);bindtex(0x0DE1,texture.value)
for parameter,value in [(0x2801,0x2601),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
teximage(0x0DE1,0,0x1908,atlas.width,atlas.height,0,0x1908,0x1401,P(pixels.ctypes.data))
uniform1i(getuniform(scene,b'uAtlas'),0)
# Reproduce the engine's tile-local slope/roughness map, uploaded as raw RGBA data.
height=(pixels[:,:,0]*.2126+pixels[:,:,1]*.7152+pixels[:,:,2]*.0722)/255
tile_size=atlas_size//4
cloth=pixels[tile_size*2:tile_size*3,tile_size:tile_size*2,:3].mean(axis=(0,1))/255.0
uniform(scene,"uClothMean",*cloth)
material=np.zeros_like(pixels)
rough=[.95,.86,.90,.89,.83,.27,.31,.20,.72,.98,.68,.82,.92,.95,.88,.32,.72,.86,.88,.80]+[.95]*12
tile_size=atlas_size//4
for row in range(4 if a.baseline25 else 8):
 for col in range(4):
  tile=height[row*tile_size:(row+1)*tile_size,col*tile_size:(col+1)*tile_size]
  gx=np.clip((np.roll(tile,-1,1)-np.roll(tile,1,1))*2,-1,1)
  gy=np.clip((np.roll(tile,-1,0)-np.roll(tile,1,0))*2,-1,1)
  dest=material[row*tile_size:(row+1)*tile_size,col*tile_size:(col+1)*tile_size]
  dest[:,:,0]=(gx*.5+.5)*255;dest[:,:,1]=(gy*.5+.5)*255;dest[:,:,2]=rough[row*4+col]*255;dest[:,:,3]=tile*255
mtex=U();gentex(1,C.byref(mtex));activetex(0x84C4);bindtex(0x0DE1,mtex.value)
for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
teximage(0x0DE1,0,0x1908,atlas.width,atlas.height,0,0x1908,0x1401,P(material.ctypes.data));generatemipmap(0x0DE1)
uniform1i(getuniform(scene,b'uMaterial'),4)
activetex(0x84C0);bindtex(0x0DE1,texture.value);generatemipmap(0x0DE1);texparam(0x0DE1,0x2801,0x2703)
uniform4f(getuniform(scene,b'uStory'),1,0,0,0);uniform(scene,'uLamp',-40,2.7,-44.5)
geometry=np.fromfile(a.geometry,dtype='<f4') if not a.sky_only else np.zeros(0,dtype='<f4')
triangles=geometry.reshape((-1,30));kinds=triangles[:,9]
transparent=((kinds>=49.5)&(kinds<=50.5))|((kinds>=51.5)&(kinds<=54.5))|((kinds>=67.5)&(kinds<=70.5))|((kinds>=91.5)&(kinds<=92.5))|((kinds>=104.5)&(kinds<=105.5))|((kinds>=115.5)&(kinds<=116.5))
geometry=np.concatenate([triangles[~transparent].ravel(),triangles[transparent].ravel()]);opaque=int(np.count_nonzero(~transparent))*3
buf=upload(geometry)
elements=V.gl('glDrawElements',None,[U,I,U,P])
index_pages=[]
layer_pages32=[]
surface32=None
if a.surface32 and not a.legacy:
 surface32,_=V.compile_pair((a.shader_dir/f'surface_vertex_{int(a.world)}.glsl').read_text(),(a.shader_dir/f'surface_{int(a.world)}.glsl').read_text(),'compact surface32')
if a.optimized and a.indexed_dir and not a.sky_only:
 mesh=Path(a.geometry).stem;at=0
 for file in sorted(a.indexed_dir.glob(mesh+'_*.f32'),key=lambda v:int(v.stem.split('_')[-1])):
  data=np.fromfile(file,dtype='<f4');idx=np.fromfile(file.with_suffix('.u16'),dtype='<u2');cast=np.fromfile(file.with_suffix('.shadow.u16'),dtype='<u2')
  vb=upload(data)
  def ib(data):
   b=U();genbuffers(1,C.byref(b));bindbuffer(0x8893,b.value);bufferdata(0x8893,data.nbytes,P(data.ctypes.data),0x88E4);return b
  index_pages.append((vb,ib(idx),ib(cast),len(idx),len(cast),at));at+=len(idx)
 assert at==len(geometry)//10,(mesh,at,len(geometry)//10)
 if surface32:
  for file,(vb,*_) in zip(sorted(a.indexed_dir.glob(mesh+'_*.f32'),key=lambda v:int(v.stem.split('_')[-1])),index_pages):
   a32=np.fromfile(file.with_suffix('.surface.u16'),dtype='<u2');b32=np.fromfile(file.with_suffix('.fx.u16'),dtype='<u2');layer_pages32.append((vb,ib(a32),len(a32),ib(b32),len(b32)))
 bindbuffer(0x8893,0)
def copy_uniforms32(source,dest):
 getp=V.gl('glGetProgramiv',None,[U,U,C.POINTER(I)])
 active=V.gl('glGetActiveUniform',None,[U,U,I,C.POINTER(I),C.POINTER(I),C.POINTER(U),C.c_void_p])
 getf=V.gl('glGetUniformfv',None,[U,I,C.POINTER(F)]);geti=V.gl('glGetUniformiv',None,[U,I,C.POINTER(I)])
 number=I();getp(source,0x8B86,C.byref(number));use(dest)
 for index in range(number.value):
  name=C.create_string_buffer(256);length=I();size=I();kind=U();active(source,index,256,C.byref(length),C.byref(size),C.byref(kind),name)
  loc=getuniform(dest,name.value)
  if loc<0:continue
  origin=getuniform(source,name.value)
  if kind.value in (0x1404,0x8B5E,0x8B56):
   val=I();geti(source,origin,C.byref(val));uniform1i(loc,val.value)
  else:
   val=(F*16)();getf(source,origin,val)
   if kind.value==0x8B5C:uniformmat(loc,1,False,val)
   elif kind.value==0x8B52:uniform4f(loc,*val[:4])
   elif kind.value==0x8B51:uniform3f(loc,*val[:3])
   elif kind.value==0x8B50:uniform2f(loc,*val[:2])
   elif kind.value==0x1406:uniform1f(loc,val[0])
def layer32(prog,solid):
 use(prog)
 for vb,idx,n,fx,fn in layer_pages32:
  count=n if solid else fn
  if not count:continue
  bindbuffer(0x8892,vb.value);attributes(prog,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
  bindbuffer(0x8893,idx.value if solid else fx.value);elements(4,count,0x1403,None)
 bindbuffer(0x8893,0)
def draw_scene(prog,first,count,shadow_pass=False):
 if prog==scene and first==0 and count==opaque and surface32 and not shadow_pass:
  copy_uniforms32(scene,surface32);layer32(surface32,True);layer32(scene,False);return
 if not index_pages:
  bindbuffer(0x8892,buf.value);attributes(prog,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40);draw(4,first,count);return
 for vb,idx,cast,n,sn,base in index_pages:
  start=0 if shadow_pass else max(0,first-base);end=sn if shadow_pass else min(n,first+count-base)
  if end<=start:continue
  bindbuffer(0x8892,vb.value);attributes(prog,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
  bindbuffer(0x8893,cast.value if shadow_pass else idx.value);elements(4,end-start,0x1403,P(start*2))
 bindbuffer(0x8893,0)

uniform4f(getuniform(scene,b'uTools'),1 if a.tool=='lantern' else 0,1 if a.tool=='prism' else 0,0,0);uniform(scene,'uTorchForward',*forward)
t=a.time*.011;cx=60*math.sin(t);cy=23+1.2*math.sin(t*.7);cz=-240+112*math.cos(t);yaw=-math.atan2(math.cos(t)*60,-math.sin(t)*112)
uniform4f(getuniform(scene,b'uCreature'),cx,cy,cz,yaw)
if geometry.size%10:raise RuntimeError('Geometry must contain 10 floats per vertex')
attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
if a.presence:
 uniform(scene,'uActorYaw',a.actor_yaw)
 uniform(scene,'uEntity',*a.entity);uniform(scene,'uPresence',1)
# Directional colour-encoded shadow map with a depth attachment, as in DreamShadowMap.
shadow_size=1024 if a.quality==2 else 512
shadow,_=V.compile_pair(shaders['shadowVertexShader'],shaders['shadowFragmentShader'],'shadow')
genfb=V.gl('glGenFramebuffers',None,[I,C.POINTER(U)]);bindfb=V.gl('glBindFramebuffer',None,[U,U])
attach=V.gl('glFramebufferTexture2D',None,[U,U,U,U,I]);status=V.gl('glCheckFramebufferStatus',U,[U])
genrb=V.gl('glGenRenderbuffers',None,[I,C.POINTER(U)]);bindrb=V.gl('glBindRenderbuffer',None,[U,U]);rbstore=V.gl('glRenderbufferStorage',None,[U,U,I,I]);rbattach=V.gl('glFramebufferRenderbuffer',None,[U,U,U,U])
stex=U();gentex(1,C.byref(stex));activetex(0x84C5);bindtex(0x0DE1,stex.value)
for parameter,value in [(0x2801,0x2600),(0x2800,0x2600),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
teximage(0x0DE1,0,0x1908,shadow_size,shadow_size,0,0x1908,0x1401,None)
fb=U();genfb(1,C.byref(fb));bindfb(0x8D40,fb.value);attach(0x8D40,0x8CE0,0x0DE1,stex.value,0)
depth=U();genrb(1,C.byref(depth));bindrb(0x8D41,depth.value);rbstore(0x8D41,0x81A5,shadow_size,shadow_size);rbattach(0x8D40,0x8D00,0x8D41,depth.value)
assert status(0x8D40)==0x8CD5
night=int(a.world) in (3,5,7,11,12,19,21,41,49,50,57,58)
light_direction=np.array([.1,.65,-.80] if night else [-.40,.82,.40]);light_direction/=np.linalg.norm(light_direction)
centre=eye.copy();centre[1]=np.clip(centre[1],-170,45)
light_eye=centre+light_direction*110;lf=-light_direction;lr=np.cross(lf,[0,1,0]);lr/=np.linalg.norm(lr);lu=np.cross(lr,lf)
lv=np.eye(4);lv[0,:3]=lr;lv[1,:3]=lu;lv[2,:3]=-lf;lv[:3,3]=-(lv[:3,:3]@light_eye)
lp=np.eye(4);lp[0,0]=lp[1,1]=1/(78 if a.quality==2 else 60);lp[2,2]=-2/249;lp[2,3]=-251/249
lightvp=np.asarray((lp@lv).T,dtype=np.float32).copy()
viewport(0,0,shadow_size,shadow_size);clearcolor(1,1,1,1);clear(0x4000|0x0100);use(shadow)
uniformmat(getuniform(shadow,b'uLightVP'),1,False,lightvp.ctypes.data_as(C.POINTER(F)))
bindbuffer(0x8892,buf.value);shattrs=attributes(shadow,[('aPos',3,0),('aColor',3,12),('aKind',1,36)],40)
activetex(0x84C0);bindtex(0x0DE1,texture.value);uniform1i(getuniform(shadow,b'uAtlas'),0)
enable(0x8037);polygon=V.gl('glPolygonOffset',None,[F,F]);polygon(1.2,2)
draw_scene(shadow,0,len(geometry)//10,True);disable(0x8037)
for at in shattrs:disattr(at)
bindfb(0x8D40,0);viewport(0,0,W,H);use(scene)
uniformmat(getuniform(scene,b'uLightVP'),1,False,lightvp.ctypes.data_as(C.POINTER(F)))
uniform1i(getuniform(scene,b'uShadow'),5);uniform(scene,'uShadowReady',0 if a.no_shadow or a.quality==0 or int(a.world) in (14,15,16,22,23,31,38,39,40,43,48) else 1);uniform(scene,'uShadowTexel',1/shadow_size)
activetex(0x84C5);bindtex(0x0DE1,stex.value);activetex(0x84C0)
attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)

if not a.baseline25 and int(a.world)==1 and a.quality>0 and a.mirror_y is None:a.mirror_y=-.12
if a.mirror_y is not None:a.mirror=[0.,-17. if int(a.world)==1 else -130.]
uniform(scene,'uWaterReflection',1 if a.mirror_y is not None else 0);uniform(scene,'uWaterPlane',a.mirror_y or .05)
if a.mirror:
 genfb=V.gl('glGenFramebuffers',None,[I,C.POINTER(U)]);bindfb=V.gl('glBindFramebuffer',None,[U,U])
 attach=V.gl('glFramebufferTexture2D',None,[U,U,U,U,I]);status=V.gl('glCheckFramebufferStatus',U,[U])
 genrb=V.gl('glGenRenderbuffers',None,[I,C.POINTER(U)]);bindrb=V.gl('glBindRenderbuffer',None,[U,U]);rbstore=V.gl('glRenderbufferStorage',None,[U,U,I,I]);rbattach=V.gl('glFramebufferRenderbuffer',None,[U,U,U,U])
 reflection=U();gentex(1,C.byref(reflection));activetex(0x84C3);bindtex(0x0DE1,reflection.value)
 for parameter,value in [(0x2801,0x2601),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1907,512,512,0,0x1907,0x1401,None)
 fb=U();genfb(1,C.byref(fb));bindfb(0x8D40,fb.value);attach(0x8D40,0x8CE0,0x0DE1,reflection.value,0)
 depth=U();genrb(1,C.byref(depth));bindrb(0x8D41,depth.value);rbstore(0x8D41,0x81A5,512,512);rbattach(0x8D40,0x8D00,0x8D41,depth.value)
 assert status(0x8D40)==0x8CD5
 transform=np.eye(4)
 if a.mirror_y is not None:transform[1,1]=-1;transform[1,3]=2*a.mirror_y
 else:transform[2,2]=-1;transform[2,3]=2*a.mirror[1]
 reflectedvp=np.asarray((projection@view@transform).T,dtype=np.float32).copy()
 viewport(0,0,512,512);clearcolor(*horizon,1);clear(0x4000|0x0100)
 # Sky ray reflection matches the negative-determinant scene view.
 disable(0x0B71);use(sky)
 reflected_forward=forward.copy();reflected_forward[1 if a.mirror_y is not None else 2]*=-1
 reflected_side=side.copy();reflected_side[1 if a.mirror_y is not None else 2]*=-1
 reflected_up=up.copy();reflected_up[1 if a.mirror_y is not None else 2]*=-1
 uniform(sky,'uCloudCamera',a.eye[0],2*a.mirror_y-a.eye[1] if a.mirror_y is not None else a.eye[1],a.eye[2]);uniform(sky,'uForward',*reflected_forward);uniform(sky,'uRight',*reflected_side);uniform(sky,'uUp',*reflected_up)
 bindbuffer(0x8892,skybuf.value);skyattrs=attributes(sky,[('aXY',2,0)],8);draw(4,0,6)
 for at in skyattrs:disattr(at)
 clear(0x0100);enable(0x0B71);use(scene);bindbuffer(0x8892,buf.value)
 attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
 bindtex(0x0DE1,0);uniform1i(getuniform(scene,b'uReflection'),3)
 uniform(scene,'uMirrorPass',1);uniform(scene,'uMirrorZ',a.mirror[1]);uniform(scene,'uMirrorCenter',a.mirror[0],0,a.mirror[1])
 uniform(scene,'uCamera',a.eye[0],2*a.mirror_y-a.eye[1] if a.mirror_y is not None else a.eye[1],a.eye[2] if a.mirror_y is not None else 2*a.mirror[1]-a.eye[2])
 uniformmat(getuniform(scene,b'uVP'),1,False,reflectedvp.ctypes.data_as(C.POINTER(F)))
 draw_scene(scene,0,opaque)
 if opaque<len(geometry)//10:
  depthmask=V.gl('glDepthMask',None,[V.B]);blend=V.gl('glBlendFunc',None,[U,U]);enable(0x0BE2);blend(0x0302,0x0303);depthmask(False)
  draw_scene(scene,opaque,len(geometry)//10-opaque);depthmask(True);disable(0x0BE2)
 bindfb(0x8D40,0);viewport(0,0,W,H)
 use(scene);uniformmat(getuniform(scene,b'uVP'),1,False,vp.ctypes.data_as(C.POINTER(F)))
 uniformmat(getuniform(scene,b'uMirrorVP'),1,False,reflectedvp.ctypes.data_as(C.POINTER(F)))
 uniform(scene,'uMirrorPass',0);uniform(scene,'uMirrorReady',1);uniform(scene,'uCamera',*a.eye)
 activetex(0x84C3);bindtex(0x0DE1,reflection.value);activetex(0x84C0)
 use(sky);uniform(sky,'uCloudCamera',*a.eye);uniform(sky,'uForward',*forward);uniform(sky,'uRight',*side);uniform(sky,'uUp',*up);use(scene)
bindings28={}
getinteger=V.gl('glGetIntegerv',None,[U,C.POINTER(I)])
for unit in range(8):
 activetex(0x84C0+unit);value=I();getinteger(0x8069,C.byref(value));bindings28[unit]=value.value
activetex(0x84C0)
post=None
if not a.no_post and a.quality>0:
 import post_preview26
 post=post_preview26.Post(globals(),Path(a.shaders).with_name('DreamPost26.kt'))
 post.begin()
 use(scene);bindbuffer(0x8892,buf.value);attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
draw_scene(scene,0,opaque)
if a.entities32:
 entity_program,_=V.compile_pair((a.entities32.parent/'entity32_vertex.glsl').read_text(),(a.entities32.parent/'entity32_fragment.glsl').read_text(),'modeled entities32')
 im=Image.open(Path(a.shaders).parents[4]/'res/drawable-nodpi/dream_entities_32.png').convert('RGBA').resize((1024,1024),Image.Resampling.BILINEAR);epixels=np.asarray(im).copy();etex=U();gentex(1,C.byref(etex));activetex(0x84C0);bindtex(0x0DE1,etex.value)
 for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,1024,1024,0,0x1908,0x1401,P(epixels.ctypes.data));generatemipmap(0x0DE1)
 use(entity_program);uniform1i(getuniform(entity_program,b'uAtlas'),0);uniformmat(getuniform(entity_program,b'uVP'),1,False,vp.ctypes.data_as(C.POINTER(F)));uniform(entity_program,'uEye',*eye);uniform(entity_program,'uClipY',-10000.);uniform(entity_program,'uNight',float(light[2]>.5))
 for item in json.loads(a.entities32.read_text()):
  name=item['name'];pos=np.asarray(item['position'],dtype=float);scale=item['scale'];phase=item['phase'];dist=np.linalg.norm(pos[[0,2]]-eye[[0,2]]);lod=1 if dist/scale>40 else 0
  data=np.fromfile(a.entities32.parent/f'entity32_{name}_{lod}.f32',dtype='<f4')
  yaw=np.clip(math.atan2(eye[0]-pos[0],eye[2]-pos[2]),-1.15,1.15)+item['yaw'];c=math.cos(yaw);ss=math.sin(yaw)
  model=np.array([[c,0,ss,0],[0,1,0,0],[-ss,0,c,0],[0,0,0,1]],np.float32);model[:3,:3]*=scale;model[:3,3]=pos
  if name in ['choir','mask','crimson','gold']:model[1,3]+=math.sin(a.time*.35+phase)*.12
  model=np.ascontiguousarray(model.T);uniformmat(getuniform(entity_program,b'uModel'),1,False,model.ctypes.data_as(C.POINTER(F)))
  speed={'choir':.42,'mask':.15,'crimson':.62,'antler':.22,'gold':.31,'star':.18}.get(name,.38)
  uniform(entity_program,'uArt33',math.sin(a.time*speed+phase)*.10,math.sin(a.time*.23+phase)*.045,.5+.5*math.sin(a.time*.19+phase),math.exp(-(((a.time+phase)%6.7-.22)**2)/.006));uniform(entity_program,'uGesture33',0.)
  uniform(entity_program,'uTime',a.time+phase);uniform(entity_program,'uOpen',float(np.clip((math.sin(a.time*.12+phase)-.2)/.8,0,1)) if name=='witness' else 0.)
  eb=upload(data);attributes(entity_program,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40);draw(4,0,len(data)//10)
 for unit,tid in bindings28.items():activetex(0x84C0+unit);bindtex(0x0DE1,tid)
 activetex(0x84C0);use(scene)
if a.props30:
 use(scene);uniform(scene,'uExposure30',1.,a.phase30,0.,40.);uniform(scene,'uPlay30',a.eye[0],a.eye[2],40.,0.)
 enable(0x0BE2);V.gl('glBlendFunc',None,[U,U])(0x0302,0x0303)
 for item in json.loads(a.props30.read_text()):
  name=item['name']
  if math.hypot(item['position'][0]-a.eye[0],item['position'][2]-a.eye[2])>105 and name!='sky-sign':continue
  data=np.fromfile(a.props30.parent/('prop30_'+name+'.f32'),dtype='<f4')
  angle=math.radians(item['angle']);tilt=math.radians(item['tilt']);c=math.cos(angle);s=math.sin(angle);ct=math.cos(tilt);st=math.sin(tilt)
  ry=np.array([[c,0,s,0],[0,1,0,0],[-s,0,c,0],[0,0,0,1]],np.float32);rx=np.array([[1,0,0,0],[0,ct,-st,0],[0,st,ct,0],[0,0,0,1]],np.float32)
  model=ry@rx;model[:3,:3]*=item['scale'];model[:3,3]=item['position'];model=np.ascontiguousarray(model.T)
  uniform(scene,'uUseModel30',1.);uniformmat(getuniform(scene,b'uModel30'),1,False,model.ctypes.data_as(C.POINTER(F)))
  propbuf=upload(data);attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40);draw(4,0,len(data)//10)
 uniform(scene,'uUseModel30',0.);disable(0x0BE2)
 bindbuffer(0x8892,buf.value);attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
if a.late_sky:
 depthmask=V.gl('glDepthMask',None,[V.B]);depthfunc=V.gl('glDepthFunc',None,[U]);depthmask(False);depthfunc(0x0203)
 use(sky);bindbuffer(0x8892,skybuf.value);skyattrs=attributes(sky,[('aXY',2,0)],8)
 if (not a.optimized or cloud_intersection28) and not a.baseline25 and not a.legacy and a.quality>0 and int(a.world) not in ([6,7,12,14,15,16,19,22,23,31,38,39,40,41,43,44,48,49,50,52,62] if a.optimized else [6,7,12,14,15,16,19,22,23,31,38,39,40,41,43,44,48]):
  saved=self_fb=post.fbo if post else 0
  cloudcolor=U();gentex(1,C.byref(cloudcolor));activetex(0x84C3);bindtex(0x0DE1,cloudcolor.value)
  for parameter,value in [(0x2801,0x2601),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
  teximage(0x0DE1,0,0x1908,W//2,H//2,0,0x1908,0x1401,None)
  cloudfb=U();genfb(1,C.byref(cloudfb));bindfb(0x8D40,cloudfb.value);attach(0x8D40,0x8CE0,0x0DE1,cloudcolor.value,0);assert status(0x8D40)==0x8CD5
  bindtex(0x0DE1,0);uniform1i(getuniform(sky,b'uCloudVolume'),3);uniform(sky,'uCloudReady',0);uniform(sky,'uCloudPass',1)
  if a.optimized and post and post.depth and not a.no_mask:
   activetex(0x84C5);bindtex(0x0DE1,post.depth);uniform1i(getuniform(sky,b'uSkyDepth'),5);uniform(sky,'uSkyDepthReady',1);uniform(sky,'uSkyDepthTexel',1/W,1/H);uniform(sky,'uCloudViewport',W//2,H//2)
  viewport(0,0,W//2,H//2);disable(0x0B71);draw(4,0,6)
  if a.optimized:
   uniform(sky,'uSkyDepthReady',0);activetex(0x84C5);bindtex(0x0DE1,stex.value);activetex(0x84C3)

  bindfb(0x8D40,saved);viewport(0,0,W,H);enable(0x0B71);bindtex(0x0DE1,cloudcolor.value)
  uniform(sky,'uCloudPass',0);uniform(sky,'uCloudReady',1);uniform(sky,'uCloudViewport',W,H)
 draw(4,0,6)
 for at in skyattrs:disattr(at)
 depthmask(True);depthfunc(0x0201)
 if a.mirror:activetex(0x84C3);bindtex(0x0DE1,reflection.value);activetex(0x84C0)
 use(scene);bindbuffer(0x8892,buf.value);attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
if a.lunar_models and int(a.world) in (12,19,49,50):
 lunar_shaders=V.extract_shaders(Path(a.shaders).with_name('DreamLunarShaders27.kt'))
 lunar,_=V.compile_pair(lunar_shaders['vertexShader'],lunar_shaders['fragmentShader'],'3D lunar models')
 use(lunar);uniformmat(getuniform(lunar,b'uVP'),1,False,vp.ctypes.data_as(C.POINTER(F)))
 uniform(lunar,'uCamera',*a.eye)
 im=Image.open(Path(a.shaders).parents[4]/'res/drawable-nodpi/dream_lunar_faces_27.png').convert('RGBA').resize((1024,2048),Image.Resampling.BILINEAR)
 pixels=np.asarray(im,dtype=np.uint8).copy();tex=U();gentex(1,C.byref(tex));activetex(0x84C0);bindtex(0x0DE1,tex.value)
 for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,1024,2048,0,0x1908,0x1401,P(pixels.ctypes.data));generatemipmap(0x0DE1);uniform1i(getuniform(lunar,b'uAtlas'),0)
 variants={12:(0,6),19:(5,3),49:(1,4),50:(2,7)}[int(a.world)]
 if a.moon_variant is not None:variants=(a.moon_variant,variants[1])
 models=[(variants[0],[.08,.50,-.86],320,86),(variants[1],[-.60,.48,-.63],340,34)]
 if a.only_main_moon:models=models[:1]
 if a.vigil>=120:models.append((7,list(forward),210,128))
 for variant,direction,distance,radius in models:
  direction=np.array(direction);direction/=np.linalg.norm(direction)
  data=np.fromfile(Path(a.geometry).parent/('moon_crescent27.f32' if variant==6 else 'moon_round27.f32'),dtype='<f4')
  moonbuf=upload(data);moonattrs=attributes(lunar,[('aPosition',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
  uniform(lunar,'uDirection',*direction);uniform(lunar,'uDistance',distance);uniform(lunar,'uRadius',radius);uniform(lunar,'uMood',a.blink,min(1,a.vigil/42))
  uniform(lunar,'uEyeAim',.025,.007);uniform(lunar,'uVariant',variant);uniform(lunar,'uRed',1 if a.vigil>=60 and variant in (0,4,5) else 0)
  if a.optimized and a.indexed_dir:
   name='moon_crescent27' if variant==6 else 'moon_round27'
   for mf in sorted(a.indexed_dir.glob(name+'_*.f32')):
    mv=np.fromfile(mf,dtype='<f4');mi=np.fromfile(mf.with_suffix('.u16'),dtype='<u2');mb=upload(mv);moonattrs=attributes(lunar,[('aPosition',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
    ibo=U();genbuffers(1,C.byref(ibo));bindbuffer(0x8893,ibo.value);bufferdata(0x8893,mi.nbytes,P(mi.ctypes.data),0x88E4);elements(4,len(mi),0x1403,None);bindbuffer(0x8893,0)
  else:draw(4,0,len(data)//10)
  for at in moonattrs:disattr(at)
 # Restore scene sampler and uniforms for transparent surfaces.
 activetex(0x84C0);bindtex(0x0DE1,texture.value)
 use(scene);bindbuffer(0x8892,buf.value);attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)

if opaque<len(geometry)//10:
 depthmask=V.gl('glDepthMask',None,[V.B]);blend=V.gl('glBlendFunc',None,[U,U]);enable(0x0BE2);blend(0x0302,0x0303);depthmask(False)
 draw_scene(scene,opaque,len(geometry)//10-opaque);depthmask(True);disable(0x0BE2)
if post:post.end()
finish()
if a.bench_frames:
 import time,statistics
 metrics=[]
 def benchmark_frame():
  bindfb(0x8D40,0);viewport(0,0,W,H);enable(0x0B71);disable(0x0BE2)
  depthmask=V.gl('glDepthMask',None,[V.B]);depthmask(True);clear(0x4000|0x0100)
  for unit,texture_id in bindings28.items():activetex(0x84C0+unit);bindtex(0x0DE1,texture_id)
  use(scene);draw_scene(scene,0,opaque)
  if opaque<len(geometry)//10:
   enable(0x0BE2);blend=V.gl('glBlendFunc',None,[U,U]);blend(0x0302,0x0303);depthmask(False);draw_scene(scene,opaque,len(geometry)//10-opaque);depthmask(True);disable(0x0BE2)
  finish()
 for i in range(a.bench_frames+6):
  start=time.perf_counter_ns();benchmark_frame();elapsed_ms=(time.perf_counter_ns()-start)/1e6
  if i>=6:metrics.append(elapsed_ms)
 report=dict(world=int(a.world),optimized=a.optimized,frames=len(metrics),median_ms=statistics.median(metrics),p95_ms=float(np.percentile(metrics,95)),minimum_ms=min(metrics),stage='actual geometry only; no post, sky, compilation or image I/O',renderer=V.getstr(0x1F01).decode(),phone_measurement=False)
 cloud_times=[];masked=0;mask_total=0
 if 'cloudfb' in globals():
  def cloud_frame(program=sky):
   bindfb(0x8D40,cloudfb.value);viewport(0,0,W//2,H//2);disable(0x0B71)
   use(program);bindbuffer(0x8892,skybuf.value);attributes(program,[('aXY',2,0)],8)
   for unit,texture_id in bindings28.items():activetex(0x84C0+unit);bindtex(0x0DE1,texture_id)
   activetex(0x84C3);bindtex(0x0DE1,0);uniform1i(getuniform(program,b'uCloudVolume'),3)
   uniform(program,'uCloudPass',1);uniform(program,'uCloudReady',0)
   if a.optimized and post and post.depth and not a.no_mask:
    activetex(0x84C5);bindtex(0x0DE1,post.depth);uniform1i(getuniform(program,b'uSkyDepth'),5);uniform(program,'uSkyDepthReady',1);uniform(program,'uSkyDepthTexel',1/W,1/H);uniform(program,'uCloudViewport',W//2,H//2)
   draw(4,0,6);finish()
  for i in range(a.bench_frames+4):
   started=time.perf_counter_ns();cloud_frame();ms=(time.perf_counter_ns()-started)/1e6
   if i>=4:cloud_times.append(ms)
  if a.optimized and post and post.depth:
   fs=shaders['skyFragmentShader'].replace('if(buried){gl_FragColor=vec4(.0);return;}','if(buried){gl_FragColor=vec4(1.0,.0,.0,1.0);return;}').replace('gl_FragColor=cloudVolume(ray,sunDir,base,height);','gl_FragColor=vec4(.0,1.0,.0,1.0);return;')
   debug,_=V.compile_pair(shaders['skyVertexShader'],fs,'mask coverage diagnostics')
   # Copy the same uniforms needed for the sky rays and depth test.
   use(debug);uniform(debug,'uCloudCamera',*a.eye);uniform(debug,'uCloudParams',1/(1200 if int(a.world)==35 else 650),85,105,[0,16,24][a.quality]);uniform(debug,'uForward',*forward);uniform(debug,'uRight',*side);uniform(debug,'uUp',*up);uniform(debug,'uAspect',W/H)
   cloud_frame(debug);pixels=np.zeros((H//2,W//2,4),np.uint8);read(0,0,W//2,H//2,0x1908,0x1401,P(pixels.ctypes.data));masked=int(np.count_nonzero(pixels[:,:,0]>200));mask_total=(W//2)*(H//2)
  bindfb(0x8D40,0);viewport(0,0,W,H);enable(0x0B71)
 report.update(cloud_median_ms=statistics.median(cloud_times) if cloud_times else 0.,cloud_p95_ms=float(np.percentile(cloud_times,95)) if cloud_times else 0.,cloud_pass_present=bool(cloud_times),cloud_masked_texels=masked,cloud_total_texels=mask_total)
 if a.bench_json:a.bench_json.write_text(json.dumps(report,indent=2)+'\n')

error=geterr()
if error:raise RuntimeError(f'OpenGL error {error:#x}')
image=np.zeros((H,W,4),dtype=np.uint8);read(0,0,W,H,0x1908,0x1401,P(image.ctypes.data));Image.fromarray(image[::-1]).save(a.output)
print(json.dumps({'image':a.output,'vertices':len(geometry)//10,'eye':a.eye,'target':a.target,'GLES':V.getstr(0x1F02).decode()}))
