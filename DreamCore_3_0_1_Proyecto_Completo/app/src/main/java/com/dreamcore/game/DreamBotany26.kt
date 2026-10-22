package com.dreamcore.game

import kotlin.math.*

/** Cutout foliage uses the existing albedo sampler and ordinary opaque batches. */
object DreamBotany26 {
    fun spray(b:DreamMesh,x:Float,y:Float,z:Float,w:Float,h:Float,yaw:Float,variant:Int,lean:Float=0f){
        require(variant in 0..11)
        val rx=cos(yaw)*w*.5f;val rz=sin(yaw)*w*.5f
        val a=floatArrayOf(x-rx,y,z-rz);val c=floatArrayOf(x+rx,y,z+rz)
        val d=floatArrayOf(x+rx+sin(yaw)*lean,y+h,z+rz-cos(yaw)*lean)
        val e=floatArrayOf(x-rx+sin(yaw)*lean,y+h,z-rz-cos(yaw)*lean)
        val n=floatArrayOf(-sin(yaw),0f,cos(yaw));val t=variant.toFloat()
        val uvA=floatArrayOf(0f,1f,t);val uvC=floatArrayOf(1f,1f,t);val uvD=floatArrayOf(1f,0f,t);val uvE=floatArrayOf(0f,0f,t)
        b.mappedTriangle(a,c,d,uvA,uvC,uvD,n,n,n,111f)
        b.mappedTriangle(a,d,e,uvA,uvD,uvE,n,n,n,111f)
    }
    fun tree(b:DreamMesh,x:Float,y:Float,z:Float,scale:Float=1f,seed:Int=0){
        val rng=java.util.Random(seed.toLong()*131L+x.toBits().toLong()*17+z.toBits())
        val wood=floatArrayOf(.34f,.28f,.21f)
        DreamSculpt.cylinder(b,x,y,z,.36f*scale,4.7f*scale,wood,43f,10)
        for(i in 0..4){
            val a=i*2.399f;val dx=cos(a)*1.8f*scale;val dz=sin(a)*1.8f*scale
            DreamShapes.tube(b,listOf(floatArrayOf(x,y+2.5f*scale,z),floatArrayOf(x+dx*.5f,y+3.5f*scale,z+dz*.5f),floatArrayOf(x+dx,y+(4.2f+i*.2f)*scale,z+dz)),.085f*scale,wood,43f,6)
        }
        for(i in 0 until 22){
            val a=i*2.399f;val ring=if(i<8).6f else 1.9f
            val xx=x+cos(a)*ring*scale;val zz=z+sin(a)*ring*scale
            val yy=y+(3.1f+rng.nextFloat()*1.8f)*scale
            spray(b,xx,yy,zz,(2.2f+rng.nextFloat())*scale,(2.3f+rng.nextFloat()*1.1f)*scale,a,i%3,.20f*scale)
        }
    }
    fun shrub(b:DreamMesh,x:Float,y:Float,z:Float,scale:Float=1f,variant:Int=5){
        for(i in 0..3)spray(b,x+sin(i*2.399f)*.18f*scale,y,z+cos(i*2.399f)*.18f*scale,1.25f*scale,1.35f*scale,i*1.2f,variant,.12f*scale)
    }
    fun tuft(b:DreamMesh,x:Float,y:Float,z:Float,seed:Int){
        val size=.34f+((seed*17)and 31)*.009f
        val variant=8+abs(seed%4)
        spray(b,x,y,z,size*1.5f,size,(seed%31)*.21f,variant,.03f)
        spray(b,x,y,z,size*1.5f,size,(seed%31)*.21f+1.57f,variant,.03f)
    }
}
