package com.dreamcore.game

import android.opengl.GLES20 as G
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Optional GLES2 post chain. A depth texture/highp is required only by contact AO. */
class DreamPost26(private val makeProgram:(String,String)->Int,private val allowDepth:Boolean=true) {
    private var width=0;private var height=0;private var failed=false;private var active=false
    private var sceneFbo=0;private var sceneColor=0;private var depth=0;private var depthBuffer=0
    private var aoFbo=0;private var aoColor=0
    private var bloomFbo=0;private var bloomColor=0;private var blurFbo=0;private var blurColor=0
    private var composite=0;private var aoProgram=0;private var bloomProgram=0
    val targetForSky get()=if(active)sceneFbo else 0
    val depthForSky get()=if(active && depthReadable)depth else 0
    private var depthReadable=false
    private val quad=ByteBuffer.allocateDirect(12*4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{
        put(floatArrayOf(-1f,-1f,1f,-1f,-1f,1f,-1f,1f,1f,-1f,1f,1f));position(0)
    }
    private val uniforms=HashMap<Int,HashMap<String,Int>>()
    private fun u(p:Int,n:String)=uniforms.getOrPut(p){HashMap()}.getOrPut(n){G.glGetUniformLocation(p,n)}
    private fun texture(w:Int,h:Int):Int{
        val ids=IntArray(1);G.glGenTextures(1,ids,0);G.glBindTexture(G.GL_TEXTURE_2D,ids[0])
        G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_MIN_FILTER,G.GL_LINEAR)
        G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_MAG_FILTER,G.GL_LINEAR)
        G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_WRAP_S,G.GL_CLAMP_TO_EDGE)
        G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_WRAP_T,G.GL_CLAMP_TO_EDGE)
        G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,w,h,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,null)
        return ids[0]
    }
    private fun target(color:Int):Int{
        val ids=IntArray(1);G.glGenFramebuffers(1,ids,0);G.glBindFramebuffer(G.GL_FRAMEBUFFER,ids[0])
        G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_COLOR_ATTACHMENT0,G.GL_TEXTURE_2D,color,0)
        return ids[0]
    }
    private fun complete()=G.glCheckFramebufferStatus(G.GL_FRAMEBUFFER)==G.GL_FRAMEBUFFER_COMPLETE
    private fun disposeTargets(){
        G.glDeleteFramebuffers(4,intArrayOf(sceneFbo,aoFbo,bloomFbo,blurFbo),0)
        G.glDeleteTextures(5,intArrayOf(sceneColor,depth,aoColor,bloomColor,blurColor),0)
        if(depthBuffer!=0)G.glDeleteRenderbuffers(1,intArrayOf(depthBuffer),0)
        sceneFbo=0;sceneColor=0;depth=0;depthBuffer=0;aoFbo=0;aoColor=0
        bloomFbo=0;bloomColor=0;blurFbo=0;blurColor=0
    }
    private fun allocate(w:Int,h:Int){
        disposeTargets();width=w;height=h;DreamGl29.drain()
        val ext=G.glGetString(G.GL_EXTENSIONS).orEmpty()
        val precision=IntArray(1);G.glGetShaderPrecisionFormat(G.GL_FRAGMENT_SHADER,G.GL_HIGH_FLOAT,IntArray(2),0,precision,0)
        if(composite==0)composite=makeProgram(vertexShader,compositeFragmentShader)
        sceneColor=texture(w,h);sceneFbo=target(sceneColor)
        depthReadable=false
        if(allowDepth && "GL_OES_depth_texture" in ext.split(' ') && precision[0]>=16){
            val ids=IntArray(1);G.glGenTextures(1,ids,0);depth=ids[0];G.glBindTexture(G.GL_TEXTURE_2D,depth)
            G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_MIN_FILTER,G.GL_NEAREST)
            G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_MAG_FILTER,G.GL_NEAREST)
            G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_WRAP_S,G.GL_CLAMP_TO_EDGE)
            G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_WRAP_T,G.GL_CLAMP_TO_EDGE)
            // Do not trade the deep crater's precision for a 16-bit depth texture.
            G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_DEPTH_COMPONENT,w,h,0,G.GL_DEPTH_COMPONENT,G.GL_UNSIGNED_INT,null)
            G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_DEPTH_ATTACHMENT,G.GL_TEXTURE_2D,depth,0)
            depthReadable=complete() && G.glGetError()==G.GL_NO_ERROR
            if(depthReadable)try{if(aoProgram==0)aoProgram=makeProgram(aoVertexShader,aoFragmentShader)}catch(e:Exception){
                android.util.Log.w("DreamCore","Oclusión de contacto no disponible",e);depthReadable=false
            }
        }
        if(!depthReadable){
            G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_DEPTH_ATTACHMENT,G.GL_TEXTURE_2D,0,0)
            if(depth!=0){G.glDeleteTextures(1,intArrayOf(depth),0);depth=0}
            val ids=IntArray(1);G.glGenRenderbuffers(1,ids,0);depthBuffer=ids[0]
            G.glBindRenderbuffer(G.GL_RENDERBUFFER,depthBuffer)
            G.glRenderbufferStorage(G.GL_RENDERBUFFER,if("GL_OES_depth24" in ext.split(' '))0x81A6 else G.GL_DEPTH_COMPONENT16,w,h)
            G.glFramebufferRenderbuffer(G.GL_FRAMEBUFFER,G.GL_DEPTH_ATTACHMENT,G.GL_RENDERBUFFER,depthBuffer)
            if(!complete() || G.glGetError()!=G.GL_NO_ERROR){
                DreamGl29.drain();G.glRenderbufferStorage(G.GL_RENDERBUFFER,G.GL_DEPTH_COMPONENT16,w,h)
            }
        }
        check(complete()){"Destino de escena no compatible"}
        if(depthReadable){aoColor=texture((w/2).coerceAtLeast(1),(h/2).coerceAtLeast(1));aoFbo=target(aoColor);check(complete())}
        // Bloom is allocated lazily when the high preset is actually selected.
        G.glBindFramebuffer(G.GL_FRAMEBUFFER,0)
    }
    fun begin(w:Int,h:Int,level:Int):Boolean{
        active=false
        if(level==0 || failed || w<2 || h<2)return false
        try{
            if(width!=w || height!=h || sceneFbo==0)allocate(w,h)
            G.glBindFramebuffer(G.GL_FRAMEBUFFER,sceneFbo);G.glViewport(0,0,w,h)
            G.glDepthMask(true);G.glClear(G.GL_COLOR_BUFFER_BIT or G.GL_DEPTH_BUFFER_BIT);active=true
        }catch(e:Exception){
            android.util.Log.w("DreamCore","Postprocesado compatible desactivado",e);failed=true
            disposeTargets();G.glBindFramebuffer(G.GL_FRAMEBUFFER,0);G.glViewport(0,0,w,h)
        }
        return active
    }
    private fun sample(p:Int,name:String,id:Int,unit:Int){
        G.glActiveTexture(G.GL_TEXTURE0+unit);G.glBindTexture(G.GL_TEXTURE_2D,id);G.glUniform1i(u(p,name),unit)
    }
    private val quadAttributes=HashMap<Int,Int>()
    private fun draw(p:Int){
        G.glBindBuffer(G.GL_ARRAY_BUFFER,0);val a=quadAttributes.getOrPut(p){G.glGetAttribLocation(p,"aPos")}
        quad.position(0);G.glEnableVertexAttribArray(a);G.glVertexAttribPointer(a,2,G.GL_FLOAT,false,8,quad)
        G.glDrawArrays(G.GL_TRIANGLES,0,6);G.glDisableVertexAttribArray(a)
    }
    fun end(projection:FloatArray,level:Int){
        if(!active)return
        G.glDisable(G.GL_DEPTH_TEST);G.glDepthMask(false);G.glDisable(G.GL_BLEND)
        if(depthReadable){
            G.glBindFramebuffer(G.GL_FRAMEBUFFER,aoFbo);G.glViewport(0,0,width/2,height/2);G.glUseProgram(aoProgram)
            sample(aoProgram,"uDepth",depth,0)
            G.glUniform2f(u(aoProgram,"uTexel"),1f/width,1f/height)
            G.glUniform4f(u(aoProgram,"uProjection"),1f/projection[0],1f/projection[5],projection[14],projection[10])
            G.glUniform1f(u(aoProgram,"uTaps"),if(level==2)8f else 4f);draw(aoProgram)
        }
        var bloomReady=false
        if(level==2)try{
            if(bloomProgram==0)bloomProgram=makeProgram(vertexShader,bloomFragmentShader)
            if(bloomFbo==0){
                bloomColor=texture((width/4).coerceAtLeast(1),(height/4).coerceAtLeast(1));bloomFbo=target(bloomColor);check(complete())
                blurColor=texture((width/4).coerceAtLeast(1),(height/4).coerceAtLeast(1));blurFbo=target(blurColor);check(complete())
            }
            G.glUseProgram(bloomProgram);G.glViewport(0,0,(width/4).coerceAtLeast(1),(height/4).coerceAtLeast(1))
            G.glBindFramebuffer(G.GL_FRAMEBUFFER,bloomFbo);sample(bloomProgram,"uColor",sceneColor,0)
            G.glUniform2f(u(bloomProgram,"uDirection"),3f/width,0f);G.glUniform1f(u(bloomProgram,"uThreshold"),1f);draw(bloomProgram)
            G.glBindFramebuffer(G.GL_FRAMEBUFFER,blurFbo);sample(bloomProgram,"uColor",bloomColor,0)
            G.glUniform2f(u(bloomProgram,"uDirection"),0f,4f/height);G.glUniform1f(u(bloomProgram,"uThreshold"),0f);draw(bloomProgram)
            bloomReady=true
        }catch(e:Exception){android.util.Log.w("DreamCore","Resplandor no disponible",e)}
        G.glBindFramebuffer(G.GL_FRAMEBUFFER,0);G.glViewport(0,0,width,height);G.glUseProgram(composite)
        sample(composite,"uColor",sceneColor,0);sample(composite,"uAO",if(depthReadable)aoColor else sceneColor,1)
        sample(composite,"uBloom",if(bloomReady)blurColor else sceneColor,2)
        G.glUniform2f(u(composite,"uTexel"),1f/width,1f/height)
        G.glUniform4f(u(composite,"uProjection"),1f/projection[0],1f/projection[5],projection[14],projection[10])
        G.glUniform2f(u(composite,"uEffects"),if(depthReadable)1f else 0f,if(bloomReady).055f else 0f)
        draw(composite)
        G.glActiveTexture(G.GL_TEXTURE0);G.glDepthMask(true);G.glEnable(G.GL_DEPTH_TEST);active=false
    }
    companion object {
        val vertexShader = """
            attribute vec2 aPos;
            varying mediump vec2 vUV;
            void main(){vUV=aPos*.5+.5;gl_Position=vec4(aPos,0.0,1.0);}
        """.trimIndent()
        val aoVertexShader = """
            attribute vec2 aPos;
            varying highp vec2 vUV;
            void main(){vUV=aPos*.5+.5;gl_Position=vec4(aPos,0.0,1.0);}
        """.trimIndent()
        val aoFragmentShader = """
            precision highp float;
            uniform highp sampler2D uDepth;
            uniform vec2 uTexel;
            uniform vec4 uProjection;
            uniform float uTaps;
            varying highp vec2 vUV;
            float distanceAt(vec2 uv){float z=texture2D(uDepth,uv).r;return uProjection.z/(z*2.0-1.0+uProjection.w);}
            vec3 pointAt(vec2 uv){uv=(floor(uv/uTexel)+.5)*uTexel;float d=distanceAt(uv);return vec3((uv*2.0-1.0)*uProjection.xy*d,-d);}
            void main(){
                vec2 center=(floor(vUV/uTexel)+.5)*uTexel;float raw=texture2D(uDepth,center).r;vec3 p=pointAt(center);float d=-p.z;
                if(raw>.99995 || d>42.0){gl_FragColor=vec4(1.0,clamp(d/80.0,0.0,1.0),0.0,1.0);return;}
                vec3 r=pointAt(center+vec2(uTexel.x,0.0))-p;
                vec3 l=p-pointAt(center-vec2(uTexel.x,0.0));
                vec3 t=pointAt(center+vec2(0.0,uTexel.y))-p;
                vec3 b=p-pointAt(center-vec2(0.0,uTexel.y));
                vec3 n=normalize(cross(abs(r.z)<abs(l.z)?r:l,abs(t.z)<abs(b.z)?t:b));
                if(dot(n,p)>0.0)n=-n;
                float radius=.72;float occ=0.0;float count=0.0;
                vec2 screenRadius=vec2(radius)/(2.0*uProjection.xy*max(d,.4));
                for(int i=0;i<8;i++){
                    if(float(i)>=uTaps)break;
                    float angle=float(i)*2.399963;float ring=.40+.60*fract(float(i)*.618+.3);
                    vec2 uv=center+vec2(cos(angle),sin(angle))*screenRadius*ring;
                    vec3 delta=pointAt(clamp(uv,vec2(.001),vec2(.999)))-p;
                    float dist=length(delta);
                    float blocked=max(0.0,dot(n,delta)/max(dist,.001)-.10);
                    float range=1.0-smoothstep(radius*.45,radius*1.7,dist);
                    occ+=blocked*range;count+=1.0;
                }
                float fade=1.0-smoothstep(25.0,42.0,d);
                float ao=1.0-clamp(occ/max(count,1.0)*1.65,0.0,.38)*fade;
                gl_FragColor=vec4(ao,clamp(d/80.0,0.0,1.0),0.0,1.0);
            }
        """.trimIndent()
        val compositeFragmentShader = """
            precision mediump float;
            uniform sampler2D uColor;
            uniform sampler2D uAO;
            uniform sampler2D uBloom;
            uniform vec2 uTexel;
            uniform vec4 uProjection;
            uniform vec2 uEffects;
            varying mediump vec2 vUV;
            float luma(vec3 c){return dot(c,vec3(.299,.587,.114));}
            void main(){
                vec3 c=texture2D(uColor,vUV).rgb;
                vec3 nw=texture2D(uColor,vUV+uTexel*vec2(-1.0,1.0)).rgb;
                vec3 ne=texture2D(uColor,vUV+uTexel).rgb;
                vec3 sw=texture2D(uColor,vUV-uTexel).rgb;
                vec3 se=texture2D(uColor,vUV+uTexel*vec2(1.0,-1.0)).rgb;
                float lc=luma(c),a=luma(nw),b=luma(ne),d=luma(sw),e=luma(se);
                float lo=min(lc,min(min(a,b),min(d,e))),hi=max(lc,max(max(a,b),max(d,e)));
                if(hi-lo>max(.035,hi*.10)){
                    vec2 dir=vec2(-(a+b-d-e),a+d-b-e);
                    float reduce=max((a+b+d+e)*.03125,.0078);
                    dir=clamp(dir/(min(abs(dir.x),abs(dir.y))+reduce),vec2(-4.0),vec2(4.0))*uTexel;
                    vec3 aa=.5*(texture2D(uColor,vUV+dir*(-.1667)).rgb+texture2D(uColor,vUV+dir*.1667).rgb);
                    vec3 bb=aa*.5+.25*(texture2D(uColor,vUV-dir*.5).rgb+texture2D(uColor,vUV+dir*.5).rgb);
                    float lb=luma(bb);c=(lb<lo || lb>hi)?aa:bb;
                }
                if(uEffects.x>.5){
                    vec2 center=texture2D(uAO,vUV).rg;float sum=0.0,weight=0.0;
                    for(int i=0;i<4;i++){
                        vec2 offset=vec2(mod(float(i),2.0)*2.0-1.0,floor(float(i)*.5)*2.0-1.0)*uTexel;
                        vec2 q=texture2D(uAO,vUV+offset).rg;
                        float w=1.0/(.01+abs(q.g-center.g)*100.0);sum+=q.r*w;weight+=w;
                    }
                    c*=sqrt(clamp(sum/max(weight,.001),.62,1.0));
                }
                if(uEffects.y>0.0)c+=texture2D(uBloom,vUV).rgb*uEffects.y;
                // One display code value prevents visible bands in long sky gradients.
                float dither=fract(dot(gl_FragCoord.xy,vec2(.06711056,.00583715))*52.9829189)-.5;
                gl_FragColor=vec4(clamp(c+dither/255.0,0.0,1.0),1.0);
            }
        """.trimIndent()
        val bloomFragmentShader = """
            precision mediump float;
            uniform sampler2D uColor;
            uniform vec2 uDirection;
            uniform float uThreshold;
            varying mediump vec2 vUV;
            vec3 fetch(vec2 uv){vec3 c=texture2D(uColor,uv).rgb;return uThreshold>.5?c*smoothstep(.80,1.0,dot(c,vec3(.299,.587,.114))):c;}
            void main(){vec3 c=fetch(vUV)*.28;c+=fetch(vUV+uDirection)*.22;c+=fetch(vUV-uDirection)*.22;c+=fetch(vUV+uDirection*2.0)*.14;c+=fetch(vUV-uDirection*2.0)*.14;gl_FragColor=vec4(c,1.0);}
        """.trimIndent()
    }
}
