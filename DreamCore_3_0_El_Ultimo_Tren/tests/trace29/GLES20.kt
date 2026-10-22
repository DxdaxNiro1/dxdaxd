package android.opengl
import java.nio.Buffer
import java.nio.ByteBuffer

object GLES20 {
 const val GL_ARRAY_BUFFER=34962
 const val GL_BLEND=3042
 const val GL_CLAMP_TO_EDGE=33071
 const val GL_COLOR_ATTACHMENT0=36064
 const val GL_COLOR_BUFFER_BIT=16384
 const val GL_DEPTH_ATTACHMENT=36096
 const val GL_DEPTH_BUFFER_BIT=256
 const val GL_DEPTH_COMPONENT=6402
 const val GL_DEPTH_TEST=2929
 const val GL_DITHER=3024
 const val GL_EXTENSIONS=7939
 const val GL_FLOAT=5126
 const val GL_FRAGMENT_SHADER=35632
 const val GL_FRAMEBUFFER=36160
 const val GL_FRAMEBUFFER_COMPLETE=36053
 const val GL_HIGH_FLOAT=36338
 const val GL_MAX_RENDERBUFFER_SIZE=34024
 const val GL_MAX_TEXTURE_IMAGE_UNITS=34930
 const val GL_MAX_TEXTURE_SIZE=3379
 const val GL_MAX_VARYING_VECTORS=36348
 const val GL_MAX_VIEWPORT_DIMS=3386
 const val GL_NEAREST=9728
 const val GL_NO_ERROR=0
 const val GL_RENDERER=7937
 const val GL_RGBA=6408
 const val GL_TEXTURE0=33984
 const val GL_TEXTURE_2D=3553
 const val GL_TEXTURE_MAG_FILTER=10240
 const val GL_TEXTURE_MIN_FILTER=10241
 const val GL_TEXTURE_WRAP_S=10242
 const val GL_TEXTURE_WRAP_T=10243
 const val GL_TRIANGLES=4
 const val GL_UNSIGNED_BYTE=5121
 const val GL_UNSIGNED_INT=5125
 const val GL_VERSION=7938
 var corruptColor=false;var depthSupported=true;var corruptDepth=false;var depthAttached=0;var sampled=0;var colorAttached=0
 var currentProgram=0;var next=10;var error=0;var renderer="Adreno (TM) 619";var extensions="GL_OES_depth_texture GL_OES_depth24";var depthClear=1f
 val liveTextures=HashSet<Int>();val liveFrames=HashSet<Int>();var depthDraws=0;var restored=false
 fun reset(){corruptColor=false;depthSupported=true;corruptDepth=false;depthAttached=0;sampled=0;colorAttached=0;currentProgram=0;error=0;depthDraws=0;restored=false;check(liveTextures.isEmpty()&&liveFrames.isEmpty())}
 fun glGetError():Int{val e=error;error=0;return e}
 fun glGetIntegerv(key:Int,out:IntArray,offset:Int){out[offset]=when(key){GL_MAX_TEXTURE_IMAGE_UNITS->8;GL_MAX_VARYING_VECTORS->16;else->4096};if(key==GL_MAX_VIEWPORT_DIMS)out[offset+1]=4096}
 fun glGetShaderPrecisionFormat(type:Int,kind:Int,range:IntArray,offset:Int,p:IntArray,po:Int){p[po]=23}
 fun glGetString(key:Int)=when(key){GL_RENDERER->renderer;GL_EXTENSIONS->extensions;else->"OpenGL ES 3.2"}
 fun glGenTextures(n:Int,out:IntArray,offset:Int){repeat(n){val id=next++;out[offset+it]=id;liveTextures.add(id)}}
 fun glGenFramebuffers(n:Int,out:IntArray,offset:Int){repeat(n){val id=next++;out[offset+it]=id;liveFrames.add(id)}}
 fun glBindFramebuffer(target:Int,id:Int){if(id==0)restored=true else check(id in liveFrames)}
 fun glActiveTexture(unit:Int){check(unit==GL_TEXTURE0)}
 fun glBindTexture(target:Int,id:Int){check(id==0||id in liveTextures);sampled=id}
 fun glTexParameteri(target:Int,name:Int,value:Int){}
 fun glTexImage2D(target:Int,level:Int,internal:Int,w:Int,h:Int,border:Int,format:Int,type:Int,p:Buffer?){if(format==GL_DEPTH_COMPONENT&&!depthSupported)error=1280}
 fun glFramebufferTexture2D(target:Int,attachment:Int,textureTarget:Int,id:Int,level:Int){if(attachment==GL_DEPTH_ATTACHMENT)depthAttached=id else colorAttached=id}
 fun glCheckFramebufferStatus(target:Int)=if(depthAttached!=0&&!depthSupported)36061 else GL_FRAMEBUFFER_COMPLETE
 fun glViewport(x:Int,y:Int,w:Int,h:Int){}
 fun glDisable(name:Int){};fun glEnable(name:Int){}
 fun glClearColor(r:Float,g:Float,b:Float,a:Float){};fun glClear(mask:Int){}
 fun glReadPixels(x:Int,y:Int,w:Int,h:Int,format:Int,type:Int,p:Buffer){
  val q=p as ByteBuffer;val c=if(currentProgram==0)intArrayOf(51,if(corruptColor)255 else 153,204,255)else intArrayOf(if(corruptDepth)0 else 96,96,96,255)
  repeat(w*h){i->for(j in 0..3)q.put(i*4+j,c[j].toByte())}
 }
 fun glDepthMask(value:Boolean){};fun glClearDepthf(value:Float){depthClear=value}
 fun glUseProgram(id:Int){currentProgram=id};fun glUniform1i(location:Int,value:Int){}
 fun glGetUniformLocation(id:Int,name:String)=0;fun glGetAttribLocation(id:Int,name:String)=0
 fun glBindBuffer(target:Int,id:Int){};fun glEnableVertexAttribArray(a:Int){}
 fun glVertexAttribPointer(a:Int,size:Int,type:Int,normalized:Boolean,stride:Int,p:Buffer){}
 fun glDrawArrays(mode:Int,first:Int,count:Int){check(depthAttached==0&&sampled!=colorAttached){"depth or color framebuffer feedback"};depthDraws++}
 fun glDisableVertexAttribArray(a:Int){};fun glDeleteProgram(id:Int){}
 fun glDeleteFramebuffers(n:Int,ids:IntArray,offset:Int){repeat(n){val id=ids[offset+it];if(id!=0)check(liveFrames.remove(id))}}
 fun glDeleteTextures(n:Int,ids:IntArray,offset:Int){repeat(n){val id=ids[offset+it];if(id!=0)check(liveTextures.remove(id))}}
}
