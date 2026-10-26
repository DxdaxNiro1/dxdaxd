"""Real GLES2 FBO/depth/half-resolution AO + FXAA chain, using the shipped shaders."""
import ctypes as C
import numpy as np
from PIL import Image
class Post:
 def __init__(self,g,path):
  self.g=g;self.W=g['W'];self.H=g['H'];self.V=g['V'];self.U=g['U'];self.I=g['I'];self.F=g['F'];self.P=g['P']
  self.depthmask=self.V.gl('glDepthMask',None,[self.V.B]);self.s=self.V.extract_shaders(path)
  self.composite,_=self.V.compile_pair(self.s['vertexShader'],self.s['compositeFragmentShader'],'post FXAA')
  self.ao_program,_=self.V.compile_pair(self.s['aoVertexShader'],self.s['aoFragmentShader'],'post AO')
  self.bloom_program,_=self.V.compile_pair(self.s['vertexShader'],self.s['bloomFragmentShader'],'post bloom')
  self.scene=self.texture(self.W,self.H);self.fbo=self.target(self.scene)
  if g['a'].no_depth:
   self.depth=0;rb=self.U();g['genrb'](1,C.byref(rb));g['bindrb'](0x8D41,rb.value)
   g['rbstore'](0x8D41,0x81A6,self.W,self.H);g['rbattach'](0x8D40,0x8D00,0x8D41,rb.value)
  else:
   self.depth=self.texture(self.W,self.H,True);g['attach'](0x8D40,0x8D00,0x0DE1,self.depth,0)
  assert g['status'](0x8D40)==0x8CD5,'readable depth or real renderbuffer fallback'
  self.ao=self.texture(self.W//2,self.H//2);self.ao_fbo=self.target(self.ao)
  self.bloom=self.texture(self.W//4,self.H//4);self.bloom_fbo=self.target(self.bloom)
  self.blur=self.texture(self.W//4,self.H//4);self.blur_fbo=self.target(self.blur)
 def texture(self,w,h,depth=False):
  g=self.g;t=self.U();g['gentex'](1,C.byref(t));g['activetex'](0x84C0);g['bindtex'](0x0DE1,t.value)
  for p,v in [(0x2801,0x2600 if depth else 0x2601),(0x2800,0x2600 if depth else 0x2601),(0x2802,0x812F),(0x2803,0x812F)]:g['texparam'](0x0DE1,p,v)
  g['teximage'](0x0DE1,0,0x1902 if depth else 0x1908,w,h,0,0x1902 if depth else 0x1908,0x1405 if depth else 0x1401,None)
  return t.value
 def target(self,color):
  g=self.g;fb=self.U();g['genfb'](1,C.byref(fb));g['bindfb'](0x8D40,fb.value);g['attach'](0x8D40,0x8CE0,0x0DE1,color,0)
  assert g['status'](0x8D40)==0x8CD5,'post framebuffer';return fb.value
 def begin(self):
  g=self.g;g['bindfb'](0x8D40,self.fbo);g['viewport'](0,0,self.W,self.H);self.depthmask(True);g['clearcolor'](*g['horizon'],1);g['clear'](0x4000|0x0100)
  # The Android draw path explicitly rebinds the atlases every scene draw.
  for unit,name in [(0,'texture'),(1,'cloudtexture'),(2,'moontexture'),(4,'mtex'),(5,'stex')]:
   g['activetex'](0x84C0+unit);g['bindtex'](0x0DE1,g[name].value)
  if g['a'].mirror:g['activetex'](0x84C3);g['bindtex'](0x0DE1,g['reflection'].value)
  g['activetex'](0x84C0)
 def sample(self,p,name,t,unit):
  g=self.g;g['activetex'](0x84C0+unit);g['bindtex'](0x0DE1,t);g['uniform1i'](g['getuniform'](p,name.encode()),unit)
 def quad(self,p):
  g=self.g;g['bindbuffer'](0x8892,g['skybuf'].value);at=g['attributes'](p,[('aPos',2,0)],8);g['draw'](4,0,6)
  for x in at:g['disattr'](x)
 def capture(self,suffix,w,h):
  g=self.g;im=np.zeros((h,w,4),np.uint8);g['read'](0,0,w,h,0x1908,0x1401,self.P(im.ctypes.data));Image.fromarray(im[::-1]).save(g['a'].output.replace('.png',suffix+'.png'))
 def end(self):
  g=self.g;w=self.W;h=self.H;level=g['a'].quality;uniform=g['uniform'];projection=g['projection']
  g['disable'](0x0B71);g['disable'](0x0BE2);self.depthmask(False)
  if g['a'].save_buffers:self.capture('_scene',w,h)
  ao=not g['a'].no_depth
  if ao:
   g['bindfb'](0x8D40,self.ao_fbo);g['viewport'](0,0,w//2,h//2);p=self.ao_program;g['use'](p)
   self.sample(p,'uDepth',self.depth,0);uniform(p,'uTexel',1/w,1/h);uniform(p,'uProjection',1/projection[0,0],1/projection[1,1],projection[2,3],projection[2,2]);uniform(p,'uTaps',8 if level==2 else 4);self.quad(p)
   if g['a'].save_buffers:self.capture('_ao',w//2,h//2)
  if level==2:
   p=self.bloom_program;g['use'](p);g['viewport'](0,0,w//4,h//4);g['bindfb'](0x8D40,self.bloom_fbo)
   self.sample(p,'uColor',self.scene,0);uniform(p,'uDirection',3/w,0);uniform(p,'uThreshold',1);self.quad(p)
   g['bindfb'](0x8D40,self.blur_fbo);self.sample(p,'uColor',self.bloom,0);uniform(p,'uDirection',0,4/h);uniform(p,'uThreshold',0);self.quad(p)
  g['bindfb'](0x8D40,0);g['viewport'](0,0,w,h);p=self.composite;g['use'](p)
  self.sample(p,'uColor',self.scene,0);self.sample(p,'uAO',self.ao if ao else self.scene,1);self.sample(p,'uBloom',self.blur,2)
  uniform(p,'uTexel',1/w,1/h);uniform(p,'uProjection',1/projection[0,0],1/projection[1,1],projection[2,3],projection[2,2]);uniform(p,'uEffects',1 if ao else 0,.055 if level==2 else 0);self.quad(p)
  self.depthmask(True);g['enable'](0x0B71);g['activetex'](0x84C0)
