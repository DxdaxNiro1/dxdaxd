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
p.add_argument('--near',type=float,default=.18)
p.add_argument('--far',type=float,default=520.)
p.add_argument('--fov',type=float,default=66.)
a=p.parse_args()
W=int(os.environ.get('PREVIEW_WIDTH','960')); H=int(os.environ.get('PREVIEW_HEIGHT','540'))
U=V.U; I=V.I; P=V.P; F=C.c_float
geterr=V.gl('glGetError',U,[])
viewport=V.gl('glViewport',None,[I,I,I,I]); clearcolor=V.gl('glClearColor',None,[F,F,F,F]); clear=V.gl('glClear',None,[U])
enable=V.gl('glEnable',None,[U]); disable=V.gl('glDisable',None,[U]); use=V.gl('glUseProgram',None,[U])
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
 if len(values)==3:uniform3f(loc,*values)
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
if a.mediump:shaders={k:v.replace('#ifdef GL_FRAGMENT_PRECISION_HIGH','#if 0') for k,v in shaders.items()}
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
top,horizon,fog,fognear,fogfar=presets.get(int(a.world),((.34,.46,.75),(.94,.80,.86),(.48,.54,.69),105,420))
sun=(.10,.65,-.80) if int(a.world) in (3,5,7,11,12,19,21) else (-.40,.82,.40)
cloudpath=Path(a.clouds) if a.clouds else Path(a.shaders).parents[4]/'res/drawable-nodpi/dream_cloud_density.png'
if not cloudpath.exists():
 candidates=list(Path(a.shaders).parents[4].glob('res/drawable*/dream_cloud_density.png'))
 if candidates:cloudpath=candidates[0]
if 'uClouds' in shaders['skyFragmentShader']:
 if not cloudpath.exists(): raise RuntimeError(f'Cloud density PNG not found: {cloudpath}; use --clouds')
 cloudimage=Image.open(cloudpath).convert('RGBA'); cloudpixels=np.asarray(cloudimage,dtype=np.uint8).copy()
 cloudtexture=U();gentex(1,C.byref(cloudtexture));activetex(0x84C1);bindtex(0x0DE1,cloudtexture.value)
 for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x2901),(0x2803,0x2901)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,cloudimage.width,cloudimage.height,0,0x1908,0x1401,P(cloudpixels.ctypes.data))
 generatemipmap(0x0DE1)
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
 source=Path(a.shaders).parents[4]/'res/drawable-nodpi'/resource
 im=Image.open(source).convert('RGBA').resize(dimensions,Image.Resampling.BILINEAR);data=np.asarray(im,dtype=np.uint8).copy()
 if int(a.world)==22 and unit==7:data[:]=[46,49,63,255] # same empty wall atlas as Android Canvas
 tex=U();gentex(1,C.byref(tex));activetex(0x84C0+unit);bindtex(0x0DE1,tex.value)
 for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
 teximage(0x0DE1,0,0x1908,*dimensions,0,0x1908,0x1401,P(data.ctypes.data));generatemipmap(0x0DE1)
 for prog in (scene,sky):
  use(prog);uniform1i(getuniform(prog,name.encode()),unit);uniform(prog,'uUniverse',3271%89);uniform(prog,'uEclipse',0)
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
elif int(a.world) in [14,15,16,22,23,31,38,39,40,43,48]:light=[.48,.08,.15]
elif int(a.world) in [3,5,7,11,12,19,21]:light=[.24,.40,1.]
else:light=[.50,.68,0.]
uniform4f(getuniform(scene,b'uLook'),*light,float(a.quality))
if a.atlas: atlas=Image.open(a.atlas).convert('RGBA').resize((atlas_size,atlas_size),Image.Resampling.BILINEAR)
else:
 import io
 apk=next(Path('/workspace/attachments').glob('*/app-debug.apk'))
 with zipfile.ZipFile(apk) as z:atlas=Image.open(io.BytesIO(z.read('res/drawable-nodpi-v4/dreamcore_atlas.png'))).convert('RGBA')
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
rough=[.95,.86,.90,.89,.83,.27,.31,.20,.72,.98,.68,.82,.92,.95,.88,.32]
tile_size=atlas_size//4
for row in range(4):
 for col in range(4):
  tile=height[row*tile_size:(row+1)*tile_size,col*tile_size:(col+1)*tile_size]
  gx=np.clip((np.roll(tile,-1,1)-np.roll(tile,1,1))*2,-1,1)
  gy=np.clip((np.roll(tile,-1,0)-np.roll(tile,1,0))*2,-1,1)
  dest=material[row*tile_size:(row+1)*tile_size,col*tile_size:(col+1)*tile_size]
  dest[:,:,0]=(gx*.5+.5)*255;dest[:,:,1]=(gy*.5+.5)*255;dest[:,:,2]=rough[row*4+col]*255;dest[:,:,3]=tile*255
mtex=U();gentex(1,C.byref(mtex));activetex(0x84C4);bindtex(0x0DE1,mtex.value)
for parameter,value in [(0x2801,0x2703),(0x2800,0x2601),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
teximage(0x0DE1,0,0x1908,atlas_size,atlas_size,0,0x1908,0x1401,P(material.ctypes.data));generatemipmap(0x0DE1)
uniform1i(getuniform(scene,b'uMaterial'),4)
activetex(0x84C0);bindtex(0x0DE1,texture.value);generatemipmap(0x0DE1);texparam(0x0DE1,0x2801,0x2703)
uniform4f(getuniform(scene,b'uStory'),1,0,0,0);uniform(scene,'uLamp',-40,2.7,-44.5)
geometry=np.fromfile(a.geometry,dtype='<f4') if not a.sky_only else np.zeros(0,dtype='<f4')
triangles=geometry.reshape((-1,30));kinds=triangles[:,9]
transparent=((kinds>=49.5)&(kinds<=50.5))|((kinds>=51.5)&(kinds<=54.5))|((kinds>=67.5)&(kinds<=70.5))|((kinds>=91.5)&(kinds<=92.5))|((kinds>=98.5)&(kinds<=99.5))|((kinds>=104.5)&(kinds<=105.5))
geometry=np.concatenate([triangles[~transparent].ravel(),triangles[transparent].ravel()]);opaque=int(np.count_nonzero(~transparent))*3
buf=upload(geometry)
uniform4f(getuniform(scene,b'uTools'),1 if a.tool=='lantern' else 0,1 if a.tool=='prism' else 0,0,0);uniform(scene,'uTorchForward',*forward)
t=a.time*.011;cx=60*math.sin(t);cy=23+1.2*math.sin(t*.7);cz=-240+112*math.cos(t);yaw=-math.atan2(math.cos(t)*60,-math.sin(t)*112)
uniform4f(getuniform(scene,b'uCreature'),cx,cy,cz,yaw)
if geometry.size%10:raise RuntimeError('Geometry must contain 10 floats per vertex')
attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
if a.presence:
 uniform(scene,'uActorYaw',a.actor_yaw)
 uniform(scene,'uEntity',*a.entity);uniform(scene,'uPresence',1)
# Directional colour-encoded shadow map with a depth attachment, as in DreamShadowMap.
shadow,_=V.compile_pair(shaders['shadowVertexShader'],shaders['shadowFragmentShader'],'shadow')
genfb=V.gl('glGenFramebuffers',None,[I,C.POINTER(U)]);bindfb=V.gl('glBindFramebuffer',None,[U,U])
attach=V.gl('glFramebufferTexture2D',None,[U,U,U,U,I]);status=V.gl('glCheckFramebufferStatus',U,[U])
genrb=V.gl('glGenRenderbuffers',None,[I,C.POINTER(U)]);bindrb=V.gl('glBindRenderbuffer',None,[U,U]);rbstore=V.gl('glRenderbufferStorage',None,[U,U,I,I]);rbattach=V.gl('glFramebufferRenderbuffer',None,[U,U,U,U])
stex=U();gentex(1,C.byref(stex));activetex(0x84C5);bindtex(0x0DE1,stex.value)
for parameter,value in [(0x2801,0x2600),(0x2800,0x2600),(0x2802,0x812F),(0x2803,0x812F)]:texparam(0x0DE1,parameter,value)
teximage(0x0DE1,0,0x1908,1024,1024,0,0x1908,0x1401,None)
fb=U();genfb(1,C.byref(fb));bindfb(0x8D40,fb.value);attach(0x8D40,0x8CE0,0x0DE1,stex.value,0)
depth=U();genrb(1,C.byref(depth));bindrb(0x8D41,depth.value);rbstore(0x8D41,0x81A5,1024,1024);rbattach(0x8D40,0x8D00,0x8D41,depth.value)
assert status(0x8D40)==0x8CD5
night=int(a.world) in (3,5,7,11,12,19,21)
light_direction=np.array([.1,.65,-.80] if night else [-.40,.82,.40]);light_direction/=np.linalg.norm(light_direction)
centre=eye.copy();centre[1]=np.clip(centre[1],-170,45)
light_eye=centre+light_direction*110;lf=-light_direction;lr=np.cross(lf,[0,1,0]);lr/=np.linalg.norm(lr);lu=np.cross(lr,lf)
lv=np.eye(4);lv[0,:3]=lr;lv[1,:3]=lu;lv[2,:3]=-lf;lv[:3,3]=-(lv[:3,:3]@light_eye)
lp=np.eye(4);lp[0,0]=lp[1,1]=1/78;lp[2,2]=-2/249;lp[2,3]=-251/249
lightvp=np.asarray((lp@lv).T,dtype=np.float32).copy()
viewport(0,0,1024,1024);clearcolor(1,1,1,1);clear(0x4000|0x0100);use(shadow)
uniformmat(getuniform(shadow,b'uLightVP'),1,False,lightvp.ctypes.data_as(C.POINTER(F)))
bindbuffer(0x8892,buf.value);shattrs=attributes(shadow,[('aPos',3,0),('aKind',1,36)],40)
enable(0x8037);polygon=V.gl('glPolygonOffset',None,[F,F]);polygon(1.2,2)
draw(4,0,len(geometry)//10);disable(0x8037)
for at in shattrs:disattr(at)
bindfb(0x8D40,0);viewport(0,0,W,H);use(scene)
uniformmat(getuniform(scene,b'uLightVP'),1,False,lightvp.ctypes.data_as(C.POINTER(F)))
uniform1i(getuniform(scene,b'uShadow'),5);uniform(scene,'uShadowReady',0 if int(a.world) in (31,38,39,40,43,48) else 1);uniform(scene,'uShadowTexel',1/1024)
activetex(0x84C5);bindtex(0x0DE1,stex.value);activetex(0x84C0)
attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)

if a.mirror_y is not None:a.mirror=[0.,-130.]
uniform(scene,'uWaterReflection',1 if a.mirror_y is not None else 0);uniform(scene,'uWaterPlane',.05)
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
 uniform(sky,'uForward',*reflected_forward);uniform(sky,'uRight',*reflected_side);uniform(sky,'uUp',*reflected_up)
 bindbuffer(0x8892,skybuf.value);skyattrs=attributes(sky,[('aXY',2,0)],8);draw(4,0,6)
 for at in skyattrs:disattr(at)
 clear(0x0100);enable(0x0B71);use(scene);bindbuffer(0x8892,buf.value)
 attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
 bindtex(0x0DE1,0);uniform1i(getuniform(scene,b'uReflection'),3)
 uniform(scene,'uMirrorPass',1);uniform(scene,'uMirrorZ',a.mirror[1]);uniform(scene,'uMirrorCenter',a.mirror[0],0,a.mirror[1])
 uniform(scene,'uCamera',a.eye[0],2*a.mirror_y-a.eye[1] if a.mirror_y is not None else a.eye[1],a.eye[2] if a.mirror_y is not None else 2*a.mirror[1]-a.eye[2])
 uniformmat(getuniform(scene,b'uVP'),1,False,reflectedvp.ctypes.data_as(C.POINTER(F)))
 draw(4,0,opaque)
 if opaque<len(geometry)//10:
  depthmask=V.gl('glDepthMask',None,[V.B]);blend=V.gl('glBlendFunc',None,[U,U]);enable(0x0BE2);blend(0x0302,0x0303);depthmask(False)
  draw(4,opaque,len(geometry)//10-opaque);depthmask(True);disable(0x0BE2)
 bindfb(0x8D40,0);viewport(0,0,W,H)
 use(scene);uniformmat(getuniform(scene,b'uVP'),1,False,vp.ctypes.data_as(C.POINTER(F)))
 uniformmat(getuniform(scene,b'uMirrorVP'),1,False,reflectedvp.ctypes.data_as(C.POINTER(F)))
 uniform(scene,'uMirrorPass',0);uniform(scene,'uMirrorReady',1);uniform(scene,'uCamera',*a.eye)
 activetex(0x84C3);bindtex(0x0DE1,reflection.value);activetex(0x84C0)
 use(sky);uniform(sky,'uForward',*forward);uniform(sky,'uRight',*side);uniform(sky,'uUp',*up);use(scene)
draw(4,0,opaque)
if a.late_sky:
 depthmask=V.gl('glDepthMask',None,[V.B]);depthfunc=V.gl('glDepthFunc',None,[U]);depthmask(False);depthfunc(0x0203)
 use(sky);bindbuffer(0x8892,skybuf.value);skyattrs=attributes(sky,[('aXY',2,0)],8);draw(4,0,6)
 for at in skyattrs:disattr(at)
 depthmask(True);depthfunc(0x0201)
 use(scene);bindbuffer(0x8892,buf.value);attrs=attributes(scene,[('aPos',3,0),('aColor',3,12),('aNormal',3,24),('aKind',1,36)],40)
if opaque<len(geometry)//10:
 depthmask=V.gl('glDepthMask',None,[V.B]);blend=V.gl('glBlendFunc',None,[U,U]);enable(0x0BE2);blend(0x0302,0x0303);depthmask(False)
 draw(4,opaque,len(geometry)//10-opaque);depthmask(True);disable(0x0BE2)
finish()

error=geterr()
if error:raise RuntimeError(f'OpenGL error {error:#x}')
image=np.zeros((H,W,4),dtype=np.uint8);read(0,0,W,H,0x1908,0x1401,P(image.ctypes.data));Image.fromarray(image[::-1]).save(a.output)
print(json.dumps({'image':a.output,'vertices':len(geometry)//10,'eye':a.eye,'target':a.target,'GLES':V.getstr(0x1F02).decode()}))
