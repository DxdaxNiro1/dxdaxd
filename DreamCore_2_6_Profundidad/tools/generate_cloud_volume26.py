"""Generate numerical cumulus density: RGB columns/gradient, alpha 64 volume slices.

The 1024² RGBA lookup packs a periodic 128x128x64 density field into an 8x8
slice atlas. These are linear data, not sRGB artwork or premultiplied opacity.
"""
from pathlib import Path
import json
import numpy as np
from PIL import Image
root=Path(__file__).resolve().parents[1]
rng=np.random.default_rng(260650)
N=128;Z=64
z,x=np.mgrid[:N,:N].astype(np.float32)/N
y=np.linspace(0,1,Z,dtype=np.float32)[:,None,None]
field=np.full((Z,N,N),-1,dtype=np.float32)
for cloud in range(14):
 cx,cz=rng.random(2);width=rng.uniform(.045,.10)
 for lobe in range(7):
  lx=cx+rng.normal(0,width*.50);lz=cz+rng.normal(0,width*.50)
  dx=(x-lx+.5)%1-.5;dz=(z-lz+.5)%1-.5
  rx=width*rng.uniform(.45,1.15);rz=width*rng.uniform(.45,1.1)
  cy=rng.uniform(.26,.60);ry=rng.uniform(.18,.34)
  distance=np.sqrt((dx[None]/rx)**2+(dz[None]/rz)**2+((y-cy)/ry)**2)
  field=np.maximum(field,1-distance)
# Periodic 3D low-pass noise gently erodes silhouettes and breaks perfect lobes.
noise=rng.standard_normal(field.shape).astype(np.float32)
fy=np.fft.fftfreq(Z)*Z;fz=np.fft.fftfreq(N)*N;fx=np.fft.rfftfreq(N)*N
spectrum=np.fft.rfftn(noise)
filter=np.exp(-((fy[:,None,None]/10)**2+(fz[None,:,None]/22)**2+(fx[None,None,:]/22)**2))
noise=np.fft.irfftn(spectrum*filter,s=field.shape).real
noise=noise/max(noise.std(),1e-6)
def smooth(a,b,t):
 v=np.clip((t-a)/(b-a),0,1);return v*v*(3-2*v)
density=smooth(-.035,.32,field+noise*.025)
density*=smooth(.0,.11,y)*(1-smooth(.85,1,y))
# Column opacity is also usable by the inexpensive compatible sky and reflections.
columns=1-np.exp(-density.sum(axis=0)*.13)
columns=np.asarray(Image.fromarray(columns.astype(np.float32),'F').resize((1024,1024),Image.Resampling.BILINEAR))
du=(np.roll(columns,-1,1)-np.roll(columns,1,1))*512
dv=(np.roll(columns,-1,0)-np.roll(columns,1,0))*512
rgba=np.empty((1024,1024,4),dtype=np.uint8);rgba[:,:,0]=np.rint(np.clip(columns,0,1)*255)
rgba[:,:,1]=np.rint(128+127*np.clip(du/64,-1,1));rgba[:,:,2]=np.rint(128+127*np.clip(dv/64,-1,1))
for i in range(Z):rgba[(i//8)*N:(i//8+1)*N,(i%8)*N:(i%8+1)*N,3]=np.rint(density[i]*255)
path=root/'app/src/main/res/drawable-nodpi/dream_cloud_volume_26.png'
Image.fromarray(rgba,'RGBA').save(path,optimize=True)
assert np.isfinite(density).all() and density.max()>.9 and np.mean(density>.1)<.35
report=dict(dimensions=[N,Z,N],atlas=[1024,1024],slices=64,coverage=float(np.mean(columns>.1)),occupied_voxels=float(np.mean(density>.1)),rgba_channels='R=column density; GB=column derivatives; A=8x8 slices, straight numerical data')
(root/'tools/cloud_volume26.json').write_text(json.dumps(report,indent=2)+'\n');print(report)
