"""1.7 neutral carved face. Animated eyes, lids, brows and teeth are added by GLES2."""
from pathlib import Path
import numpy as np
from PIL import Image,ImageFilter
ROOT=Path(__file__).resolve().parents[1]
N=1024
q=(np.arange(N)+.5)/N*2-1
x,y=np.meshgrid(q,-q);r2=x*x+y*y
rng=np.random.default_rng(1707)
inside=r2<.986**2
z=np.sqrt(np.maximum(0,1-r2))
h=.045*z
# An original broad, sleeping stone face occupies most of the visible disk.
def g(cx,cy,sx,sy):return np.exp(-((x-cx)/sx)**2-((y-cy)/sy)**2)
face=np.exp(-((x/.63)**6+((y+.02)/.84)**6))
h+=face*(.105*g(0,.16,.45,.64)+.068*g(0,-.57,.30,.18))
for side in [-1,1]:
 h-=.080*g(side*.265,.17,.20,.125)  # sockets
 h+=.069*g(side*.27,.32,.23,.070)  # brows
 h+=.058*g(side*.34,-.17,.19,.23) # cheeks
 h+=.035*g(side*.16,-.16,.085,.065)
 h-=.025*g(side*.12,-.21,.05,.025)
h+=.145*g(0,.045,.077,.29)+.10*g(0,-.13,.115,.12)
h-=.028*g(0,-.275,.06,.07)
# Eyelids and lips are rendered in the shader, leaving neutral carved sockets here.
h+=.035*g(0,-.44,.24,.12)
noise=np.zeros((N,N),float)
for freq,amp in [(4,.010),(8,.007),(16,.004),(32,.0025),(64,.0015),(128,.0009)]:
 small=rng.random((freq,freq)).astype('float32')
 field=np.asarray(Image.fromarray(small,mode='F').resize((N,N),Image.Resampling.BICUBIC))-.5
 noise+=amp*field
h+=noise
# Craters avoid the central facial features and vary in size and depth.
for i in range(135):
 cx,cy=rng.uniform(-.94,.94,2)
 if cx*cx+cy*cy>.88:continue
 size=rng.uniform(.018,.090)
 if abs(cx)<.48 and -.65<cy<.50:size*=.28
 d=np.sqrt((x-cx)**2+(y-cy)**2)/size
 h+=.007*np.exp(-((d-1)/.30)**2)-.009*np.exp(-(d/.76)**4)
# Normal from sphere + displacement. All asset lighting remains original.
hx=np.gradient(h,axis=1)/(2/N);hy=-np.gradient(h,axis=0)/(2/N)
nx=x-hx*1.15;ny=y-hy*1.15;nz=z
length=np.sqrt(nx*nx+ny*ny+nz*nz)+1e-8
light=np.maximum(0,(-.42*nx+.43*ny+.80*nz)/length)
base=.35+.61*light
albedo=.90+noise*4
color=np.stack([base*albedo*.91,base*albedo*.94,base*albedo],axis=-1)
# Tactile stone shadows add depth to lids and mouth without drawing black lines.
color=np.clip(color,0,1)
alpha=np.clip((.986-np.sqrt(r2))/.006,0,1)
img=np.concatenate([color,alpha[...,None]],axis=-1)
out=ROOT/'app/src/main/res/drawable-nodpi/dream_moon_face.png'
out.parent.mkdir(parents=True,exist_ok=True)
Image.fromarray(np.uint8(img*255),'RGBA').save(out,optimize=True)
print(out)
