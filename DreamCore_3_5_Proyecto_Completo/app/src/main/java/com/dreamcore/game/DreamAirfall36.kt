package com.dreamcore.game
import kotlin.math.*

/** Real edge departure and gravity; eyelids reset position only while fully closed. */
class DreamAirfall36 {
    var x=0f;private set;var z=6f;private set;var feet=0f;private set
    var speedY=0f;private set;var grounded=true;private set
    var eyes=0f;private set;var wind=0f;private set;var returned=false;private set
    private var world="";private var closing=0f
    private var anchorX=0f;private var anchorZ=6f;private var anchorY=0f
    fun reset(id:String,px:Float,pz:Float,py:Float){world=id;x=px;z=pz;feet=py;speedY=0f;grounded=DreamHorizons36.land(id,x,z)?.let{abs(it-py)<.6f}==true;eyes=0f;wind=0f;closing=0f;val a=DreamHorizons36.spawn(id);anchorX=a.x;anchorZ=a.z;anchorY=DreamHorizons36.height(id,a.x,a.z)}
    fun step(id:String,vx:Float,vz:Float,dt:Float,jump:Boolean,clear:(Float,Float,Float)->Boolean={_,_,_->true}){
        if(world!=id){val p=DreamHorizons36.spawn(id);reset(id,p.x,p.z,DreamHorizons36.height(id,p.x,p.z))}
        returned=false;val d=dt.coerceIn(0f,.12f)
        if(closing>0f){closing+=d;eyes=when{closing<.32f->closing/.32f;closing<.62f->1f;else->1f-(closing-.62f)/.42f}.coerceIn(0f,1f)
            if(closing>=.40f&&closing-d<.40f){x=anchorX;z=anchorZ;feet=anchorY;speedY=0f;grounded=true;returned=true}
            if(closing>=1.04f){closing=0f;eyes=0f};wind*=exp(-d*4f);return}
        if(jump&&grounded){speedY=6.8f;grounded=false}
        val steps=maxOf(1,ceil(d/.012f).toInt());val h=d/steps
        repeat(steps){
            val control=if(grounded)1f else .62f;val nx=x+vx*h*control;val nz=z+vz*h*control;val floor=DreamHorizons36.land(id,nx,nz)
            // A rise is a solid step/rock side; an absent surface is free air.
            if((!grounded||floor==null||floor-feet<.56f)&&clear(nx,(if(grounded&&floor!=null)maxOf(floor,feet)else feet)+(if(grounded).42f else 0f),nz)){x=nx;z=nz}
            val support=DreamHorizons36.land(id,x,z)
            if(grounded&&support!=null&&feet-support<.65f){feet=support;speedY=0f}
            else{grounded=false;val previous=feet;speedY=maxOf(-58f,speedY-15f*h);feet+=speedY*h
                if(support!=null&&speedY<=0f&&previous>=support-.04f&&feet<=support){feet=support;speedY=0f;grounded=true}}
        }
        if(grounded){val floor=DreamHorizons36.land(id,x,z);if(floor!=null&&abs(x)<180f&&abs(z)<190f){anchorX=x;anchorZ=z;anchorY=floor}}
        wind+=(abs(speedY)/16f-wind)*(1f-exp(-d*6f));wind=wind.coerceIn(0f,1f)
        val limit=if(id==DreamHorizons36.RED)-25f else DreamHorizons36.sea(id)+2.0f
        if(!grounded&&speedY< -2f&&feet<limit){closing=.001f;eyes=0f}
    }
    fun anchor(id:String):DreamChronicle.Checkpoint=DreamChronicle.Checkpoint(id,anchorX,anchorY+1.65f,anchorZ,0f,0f)
}
