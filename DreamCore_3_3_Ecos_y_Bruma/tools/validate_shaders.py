#!/usr/bin/env python3
"""Compile/link embedded GLES2 shaders on real headless EGL/Mesa, no packages required."""
import os,ctypes as C,re,textwrap,json,sys
from pathlib import Path
os.environ.setdefault('EGL_PLATFORM','surfaceless')
os.environ.setdefault('LIBGL_ALWAYS_SOFTWARE','1')
os.environ.setdefault('MESA_GLES_VERSION_OVERRIDE','2.0')
os.environ.setdefault('MESA_SHADER_CACHE_DIR','/workspace/dreamcore-validation/mesa-cache')
E=C.CDLL('libEGL.so.1')
I=C.c_int; U=C.c_uint; P=C.c_void_p; B=C.c_ubyte

def egl(name,result,args):
 f=getattr(E,name); f.restype=result; f.argtypes=args; return f
getdisplay=egl('eglGetDisplay',P,[P]); init=egl('eglInitialize',U,[P,C.POINTER(I),C.POINTER(I)])
bind=egl('eglBindAPI',U,[U]); choose=egl('eglChooseConfig',U,[P,C.POINTER(I),C.POINTER(P),I,C.POINTER(I)])
createctx=egl('eglCreateContext',P,[P,P,P,C.POINTER(I)])
createsurf=egl('eglCreatePbufferSurface',P,[P,P,C.POINTER(I)])
makecurrent=egl('eglMakeCurrent',U,[P,P,P,P]); getproc=egl('eglGetProcAddress',P,[C.c_char_p])
geterror=egl('eglGetError',I,[])

def check(value,label):
 if not value: raise RuntimeError(f'{label} failed: EGL {geterror():#x}')
 return value

display=check(getdisplay(None),'get display'); major=I(); minor=I()
check(init(display,C.byref(major),C.byref(minor)),'initialize'); check(bind(0x30A0),'bind GLES')
attrs=(I*15)(0x3033,1,0x3040,4,0x3024,8,0x3023,8,0x3022,8,0x3021,8,0x3025,24,0x3038)
config=P(); count=I(); check(choose(display,attrs,C.byref(config),1,C.byref(count)),'choose config')
if count.value<1: raise RuntimeError('No GLES2 pbuffer configuration')
context=check(createctx(display,config,None,(I*3)(0x3098,2,0x3038)),'create ES2 context')
surface=check(createsurf(display,config,(I*5)(0x3057,int(os.environ.get('PREVIEW_WIDTH','960')),0x3056,int(os.environ.get('PREVIEW_HEIGHT','540')),0x3038)),'create pbuffer')
check(makecurrent(display,surface,surface,context),'make current')

def gl(name,result,args):
 p=getproc(name.encode()); check(p,name)
 return C.CFUNCTYPE(result,*args)(p)
getstr=gl('glGetString',C.c_char_p,[U])
createshader=gl('glCreateShader',U,[U]); source=gl('glShaderSource',None,[U,I,C.POINTER(C.c_char_p),C.POINTER(I)])
compileshader=gl('glCompileShader',None,[U]); shaderiv=gl('glGetShaderiv',None,[U,U,C.POINTER(I)])
shaderlog=gl('glGetShaderInfoLog',None,[U,I,C.POINTER(I),C.c_char_p]); deleteshader=gl('glDeleteShader',None,[U])
createprogram=gl('glCreateProgram',U,[]); attach=gl('glAttachShader',None,[U,U]); link=gl('glLinkProgram',None,[U])
programiv=gl('glGetProgramiv',None,[U,U,C.POINTER(I)]); programlog=gl('glGetProgramInfoLog',None,[U,I,C.POINTER(I),C.c_char_p])
deleteprogram=gl('glDeleteProgram',None,[U])

def statuslog(obj,iv,log):
 status=I(); size=I(); iv(obj,0x8B84,C.byref(size))
 buf=C.create_string_buffer(max(1,size.value)); log(obj,len(buf),None,buf)
 return buf.value.decode(errors='replace')

def extract_shaders(path):
 path=Path(path); kotlin=path.read_text()
 if 'DreamShaders.vertexShader' in kotlin: kotlin=(path.parent/'DreamShaders.kt').read_text()
 return {name:textwrap.dedent(text).strip() for name,text in re.findall(r'(?:private\s+)?val (\w+) = \"\"\"(.*?)\"\"\"\.trimIndent\(\)',kotlin,re.S)}

def compile_one(text,kind,label):
 shader=createshader(kind); encoded=text.encode(); ptr=C.c_char_p(encoded)
 source(shader,1,C.byref(ptr),None); compileshader(shader)
 status=I(); shaderiv(shader,0x8B81,C.byref(status)); message=statuslog(shader,shaderiv,shaderlog)
 if not status.value: raise RuntimeError(f'{label} compile failed:\n{message}')
 return shader,message

def compile_pair(vertex,fragment,label):
 vs,vlog=compile_one(vertex,0x8B31,label+' vertex'); fs,flog=compile_one(fragment,0x8B30,label+' fragment')
 program=createprogram(); attach(program,vs); attach(program,fs); link(program)
 status=I(); programiv(program,0x8B82,C.byref(status)); message=statuslog(program,programiv,programlog)
 deleteshader(vs); deleteshader(fs)
 if not status.value: raise RuntimeError(f'{label} link failed:\n{message}')
 return program,{'pair':label,'linked':True,'log':vlog+flog+message}

if __name__=='__main__':
 if len(sys.argv)!=2: raise SystemExit('Usage: validate_shaders.py DreamRenderer.kt')
 path=Path(sys.argv[1]); kotlin=path.read_text()
 shaders=extract_shaders(path)
 result={'source':str(path),'EGL':f'{major.value}.{minor.value}',
 'GL_VERSION':getstr(0x1F02).decode(),'GL_RENDERER':getstr(0x1F01).decode(),'programs':[]}
 for vertex,fragment,label in [('vertexShader','fragmentShader','scene'),('skyVertexShader','skyFragmentShader','sky')]:
  program,record=compile_pair(shaders[vertex],shaders[fragment],label)
  result['programs'].append(record); deleteprogram(program)
 print(json.dumps(result,indent=2))
