"""Execute the actual shader math on GLES2, including signed inputs and mediump paths."""
from pathlib import Path
import ctypes as C,json,os,math
import validate_shaders as V
r=Path(__file__).resolve().parents[1];out=Path(os.environ.get('DREAMCORE_VALIDATION_OUT',str(r/'build/validation29')));out.mkdir(parents=True,exist_ok=True)
U=V.U;I=V.I;F=C.c_float;P=V.P
use=V.gl('glUseProgram',None,[U]);attrib=V.gl('glGetAttribLocation',I,[U,C.c_char_p]);uniloc=V.gl('glGetUniformLocation',I,[U,C.c_char_p]);uniform=V.gl('glUniform1f',None,[I,F]);enable=V.gl('glEnableVertexAttribArray',None,[U]);pointer=V.gl('glVertexAttribPointer',None,[U,I,U,V.B,I,P]);draw=V.gl('glDrawArrays',None,[U,I,I]);viewport=V.gl('glViewport',None,[I,I,I,I]);read=V.gl('glReadPixels',None,[I,I,I,I,U,U,P]);error=V.gl('glGetError',U,[])
vertex='attribute vec2 aPos;void main(){gl_Position=vec4(aPos,0.0,1.0);}'
source=V.extract_shaders(r/'app/src/main/java/com/dreamcore/game/DreamShaders.kt')['fragmentShader'];helpers=source[source.index('float dreamPow'):source.index('vec3 planetAlbedo')]
vertices=(F*6)(-1,-1,3,-1,-1,3);pixels=(C.c_ubyte*4)();records=[]
for precision in ['highp','mediump']:
 fs='precision '+precision+' float;'+helpers+'''uniform float uX;uniform float uY;void main(){float d=dreamPow(uX,uY);vec3 n=dreamNormalize(vec3(uX,0.0,-uX));gl_FragColor=vec4(clamp(d/4096.0,0.0,1.0),n.x*.5+.5,n.z*.5+.5,1.0);}'''
 program,_=V.compile_pair(vertex,fs,'math '+precision);use(program);a=attrib(program,b'aPos');enable(a);pointer(a,2,0x1406,False,0,C.cast(vertices,P));viewport(0,0,1,1)
 for x in [-10000.,-1000.,-64.,-10.,-2.,-.1,0.,.1,2.,10.,64.,1000.,10000.]:
  for y in [2.,3.,10.,88.]:
   uniform(uniloc(program,b'uX'),x);uniform(uniloc(program,b'uY'),y);draw(4,0,3);read(0,0,1,1,0x1908,0x1401,C.cast(pixels,P))
   assert error()==0
   # Large positive powers may exceed the minimum mediump range; test the bounded
   # signed square and the zero/nonpositive bases that caused undefined pow before.
   q=min(64.,max(-64.,x));value=q*q if y==2 else (4096. if x>1. else max(0.,x)**y)
   n=math.copysign(math.sqrt(.5),x) if x else 0.
   expected=[round(min(1.,value/4096)*255),round((n*.5+.5)*255),round((-n*.5+.5)*255)]
   if y==2 or x<=1:
    assert max(abs(pixels[i]-expected[i])for i in range(3))<=2,(precision,x,y,list(pixels),expected)
    records.append(dict(precision=precision,x=x,exponent=y,rgb=list(pixels)[:3],expected=expected))
 V.deleteprogram(program)
(out/'math_probe29.json').write_text(json.dumps(dict(backend=V.getstr(0x1F01).decode(),cases=len(records),all_passed=True,phone_test=False,results=records),indent=2)+'\n');print('GLES2 MATH PROBE PASSED',len(records),'cases')
