package com.dreamcore.game

/** Shared sky radiance is used by the dome and ocean in all three GLES2 paths. */
object DreamHorizonShaders36 {
    val common="""
        uniform mediump float uPanorama36;
        float hNoise36(vec2 p){return texture2D(uClouds,p).r;}
        float hFbm36(vec2 p){return hNoise36(p)*.57+hNoise36(p*2.03+vec2(.27,.13))*.28+hNoise36(p*4.01+vec2(.63,.31))*.15;}
        vec3 horizonSky36(vec3 ray){
            if(uPanorama36>.5){float angle=atan(ray.x,-ray.z);float longitude=abs(angle)<1.047198?abs(angle)*.401070: .42+(abs(angle)-1.047198)*.038197;float horizon=uWorld>65.5?.535:.56;float latitude=asin(clamp(ray.y,-1.0,1.0));float vertical=latitude<.0?latitude*.31831:(latitude<.6?latitude*.80:.48+(latitude-.6)*(horizon-.48)/.970796);vec2 uv=vec2(.5+sign(angle)*longitude,horizon-vertical);return texture2D(uMoon,uv).rgb;}
            bool red=uWorld>64.5&&uWorld<65.5;bool blue=uWorld>65.5;
            vec3 sun=dreamNormalize(blue?vec3(-.72,.18,-.68):vec3(0.0,.075,-1.0));
            vec3 low=red?vec3(.50,.065,.025):(blue?vec3(.78,.87,.98):vec3(1.0,.69,.34));
            vec3 high=red?vec3(.035,.003,.008):(blue?vec3(.045,.22,.58):vec3(.045,.37,.36));
            vec3 sky=mix(low,high,smoothstep(-.06,.65,ray.y));
            float solar=max(0.0,dot(ray,sun));sky+=(red?vec3(.15,.015,.003):vec3(.65,.35,.08))*dreamPow(solar,18.0);
            if(!red)sky=mix(sky,vec3(1.0,.98,.75),smoothstep(.99955,.99985,solar));
            // Rounded cloud banks with a sampled density edge and directional warm rims.
            for(int i=0;i<12;i++){
                float k=float(i);float az=k*2.399;float elev=.05+mod(k,3.0)*.115;
                vec3 centre=dreamNormalize(vec3(sin(az),elev,-cos(az)));
                float facing=dot(ray,centre);float size=.17+mod(k*.37,1.0)*.13;
                vec3 right=dreamNormalize(cross(centre,vec3(0.0,1.0,0.0)));vec3 up=cross(right,centre);
                vec2 q=vec2(dot(ray,right),dot(ray,up))/size;
                if(facing>.75&&dot(q,q)<1.7){
                    float n=hFbm36(q*.22+vec2(k*.17,uTime*.00016));
                    float d=length(q*vec2(.78,1.0));float edge=.73+n*.42;
                    float opacity=(1.0-smoothstep(edge-.07,edge+.045,d))*smoothstep(-.04,.025,ray.y);
                    float depth=sqrt(max(0.0,1.0-dot(q*.70,q*.70)));
                    float lit=clamp(.28+depth*.28+q.x*.12+n*.35+sin(q.x*13.0+q.y*9.0)*.08+cos(q.y*17.0-q.x*6.0)*.06,0.0,1.0);
                    vec3 shade=red?vec3(.14,.014,.018):(blue?vec3(.47,.57,.78):vec3(.62,.37,.47));
                    vec3 bright=red?vec3(.85,.16,.055):(blue?vec3(1.0,.95,.84):vec3(1.0,.79,.54));
                    sky=mix(sky,mix(shade,bright,lit),opacity*.96);
                }
            }
            if(red||blue){
                vec3 dir=dreamNormalize(red?vec3(.26,.84,-.61):vec3(.54,.26,-.80));float radius=red?.76:.19;
                vec3 right=dreamNormalize(cross(dir,vec3(0.0,1.0,0.0)));vec3 up=cross(right,dir);
                vec2 q=vec2(dot(ray,right),dot(ray,up))/radius;float rr=dot(q,q);float face=dot(ray,dir);
                if(face>0.0&&rr<1.0){
                    vec3 n=vec3(q,sqrt(max(0.0,1.0-rr)));vec2 coord=q*.55+vec2(n.z*.17,uTime*.00002);
                    float field=hFbm36(coord*.9);vec2 delta=q-vec2(.28,-.24);float vortex=exp(-dot(delta*vec2(1.0,2.8),delta*vec2(1.0,2.8))*9.0);float twist=atan(delta.y*2.0,delta.x)+vortex*3.0;float latitude=q.y+vortex*sin(twist)*.09;float band=sin(latitude*99.0+sin(q.x*3.0+q.y*9.0)*2.5+field*2.0)*.75+sin(latitude*36.0+q.x*5.0)*.25;
                    float storm=length((q-vec2(.28,-.24))*vec2(1.0,2.8));
                    vec3 surface=red?mix(vec3(.17,.025,.012),vec3(.88,.39,.18),.5+.5*band):mix(vec3(.013,.035,.12),vec3(.15,.40,.66),field);
                    if(red)surface=mix(surface,vec3(.35,.055,.025),(.5+.5*cos(storm*45.0+field*8.0))*exp(-storm*storm*9.0)*.72);
                    else{float crack=dreamPow(max(0.0,1.0-abs(sin(q.x*26.0+q.y*17.0+field*20.0))),18.0);surface+=vec3(.02,.22,.56)*crack;}
                    float light=.35+.65*max(0.0,dot(n,dreamNormalize(vec3(-.72,.43,.58))));surface*=light;
                    surface+=(red?vec3(.80,.14,.04):vec3(.06,.25,.68))*dreamPow(1.0-n.z,3.0)*.45;
                    sky=mix(sky,surface,1.0-smoothstep(.985,1.0,rr));
                }
                if(blue){
                    // Distant suspended rock silhouettes belong to the vista, not near collision meshes.
                    for(int i=0;i<9;i++){float k=float(i);vec3 d=dreamNormalize(vec3(-.12+k*.13,.16+mod(k*1.37,1.0)*.20,-1.0));
                        vec3 r=dreamNormalize(cross(d,vec3(0.0,1.0,0.0)));vec3 up=cross(r,d);vec2 p=vec2(dot(ray,r),dot(ray,up));
                        float w=.008+mod(k*.37,1.0)*.009;float taper=max(.0,1.0-abs(p.y)/.09);float shape=step(abs(p.x),w*taper*(.75+.25*sin(p.y*245.0+k)))*step(abs(p.y),.09)*step(.8,dot(ray,d));
                        sky=mix(sky,vec3(.08,.18,.33)+max(p.x,0.0)*9.0,shape*.92);
                    }
                }
                vec3 moonDir=dreamNormalize(vec3(-.55,.47,-.80));float moon=length(ray-moonDir);if(moon<.035)sky=mix(vec3(.11,.035,.025),vec3(.58,.24,.12),smoothstep(0.0,.035,moon));
            }
            return clamp(sky,0.0,1.0);
        }
        vec3 horizonWater36(vec3 position,vec3 eye){
            vec2 p=mod(position.xz,128.0);float t=uTime;
            float a=sin(p.x*.43+p.y*.26+t*.48)+sin(p.y*.71-p.x*.23-t*.31)*.44;
            float b=cos(p.y*.49+p.x*.31-t*.37)+sin(p.x*.83+t*.42)*.32;
            float wave=uWorld>65.5?.0025:.009;vec3 n=dreamNormalize(vec3(a*wave,1.0,b*wave));vec3 view=dreamNormalize(eye-position+vec3(0.0,.001,0.0));vec3 reflected=reflect(-view,n);
            float fresnel=.20+.80*dreamPow(1.0-max(0.0,dot(view,n)),4.0);
            vec3 clear=uWorld<64.5?vec3(.015,.58,.39):vec3(.015,.19,.37);
            vec3 colour=mix(clear,horizonSky36(reflected),(uWorld>65.5?.82:.48)+fresnel*(uWorld>65.5?.16:.50));
            vec3 sun=dreamNormalize(uWorld>65.5?vec3(-.72,.18,-.68):vec3(0.0,.075,-1.0));
            colour+=vec3(.85,.58,.22)*dreamPow(max(0.0,dot(reflected,sun)),240.0)*(uWorld>65.5?.55:(.8+.2*sin(p.x*3.0+p.y*2.0)));
            float ripple=sin(p.x*2.8+sin(p.y*1.7+t*.3))*sin(p.y*3.5+sin(p.x*1.8-t*.26));
            float shore=1.0-smoothstep(24.0,48.0,length(position.xz-vec2(0.0,-8.0)));colour+=vec3(.07,.18,.09)*dreamPow(max(0.0,ripple),7.0)*shore*(1.0-fresnel);
            return clamp(colour,0.0,1.0);
        }
    """.trimIndent()
    fun scene(source:String):String{
        val marker="vec3 environmentColor(vec3 ray) {"
        var s=source.replace(marker,common+"\n"+marker+"\n if(uWorld>63.5)return horizonSky36(ray);")
        val water="""
            if(uWorld>63.5&&((vKind>3.5&&vKind<4.5)||(vKind>5.5&&vKind<6.5))){
                vec3 n=dreamNormalize(vNormal);vec2 q=abs(n.y)>.6?vPosition.xz:(abs(n.x)>abs(n.z)?vPosition.zy:vPosition.xy);
                q=1.0-abs(fract(q*.10)*2.0-1.0);vec3 rock=texture2D(uAtlas,(vec2(2.02,.02)+q*.96)/vec2(4.0,8.0)).rgb;vec3 grass=texture2D(uAtlas,(vec2(.02,.02)+q*.96)/vec2(4.0,8.0)).rgb;
                bool red=uWorld>64.5&&uWorld<65.5;bool blue=uWorld>65.5;float patch=sin(vPosition.x*.37+sin(vPosition.z*.23))*cos(vPosition.z*.39);
                float rocky=vKind>5.5?1.0:clamp((blue?.72:.18)+patch*.45+(1.0-abs(n.y))*.6,0.0,1.0);
                vec2 boulder=(vPosition.xz-vec2(-11.0,-4.0))*.25;float rocks=exp(-dot(boulder,boulder));boulder=(vPosition.xz-vec2(10.0,3.0))*.25;rocks=max(rocks,exp(-dot(boulder,boulder)));boulder=(vPosition.xz-vec2(3.0,-15.0))*.25;rocks=max(rocks,exp(-dot(boulder,boulder)));if(!red&&!blue)rocky=max(rocky,smoothstep(.16,.50,rocks));
                vec3 albedo=mix(grass,rock,rocky);if(red)albedo*=vec3(1.5,.21,.12);
                vec3 sun=dreamNormalize(blue?vec3(-.72,.18,-.68):vec3(0.0,.18,-1.0));float direct=max(0.0,dot(n,sun));
                vec3 ambient=red?vec3(.56,.10,.065):(blue?vec3(.40,.52,.75):vec3(.64,.55,.33));vec3 radiance=ambient+vec3(1.0,.86,.56)*direct*.8;
                vec3 colour=sqrt(max(albedo*albedo*radiance,vec3(0.0)));float dist=length(vEye100)*100.0;colour=mix(colour,red?vec3(.36,.028,.012):vec3(.69,.78,.86),smoothstep(150.0,520.0,dist)*.55);
                gl_FragColor=vec4(colour,1.0);return;
            }
            if(uWorld>63.5&&vKind>137.5&&vKind<138.5){
                if(uMirrorPass>.5)discard;
                vec3 water=horizonWater36(vPosition,vPosition+vEye100*100.0);
                if(uMirrorReady>.5){vec2 uv=vReflection.xy/max(vReflection.z,.001);if(min(uv.x,uv.y)>.005&&max(uv.x,uv.y)<.995)water=mix(water,texture2D(uReflection,uv).rgb,.20);}
                gl_FragColor=vec4(water,1.0);return;
            }
        """.trimIndent()
        s=s.replace("void main() {","void main() {\n"+water)
        return s
    }
    fun sky(source:String):String=source.replace("void main() {",common+"\nvoid main() {\n"+"""
        if(uWorld>63.5){
            vec3 r=dreamNormalize(vRay);vec3 c;
            if(uWorld<64.5||uWorld>65.5){if(r.y<-.0001){float level=uWorld<64.5?-8.0:0.0;float distance=(level-uCloudCamera.y)/r.y;vec3 hit=uCloudCamera+r*max(0.0,distance);c=horizonWater36(hit,uCloudCamera);}else c=horizonSky36(r);}
            else c=horizonSky36(r);
            gl_FragColor=vec4(c,1.0);return;
        }
    """.trimIndent())
}
