package com.dreamcore.game

import kotlin.math.*

fun main(){
    var checks=0
    fun verify(v:Boolean,label:String){check(v){label};checks++}
    for(q in 0..2)for(heat in listOf(24,30,60))for(power in 0..2){
        val level=DreamGraphics26.level(q,heat,power)
        verify(level in 0..q,"effective quality respects selection")
        verify(heat>24&&power!=2 || level==0,"heat/save mode bypass expensive passes")
        verify(heat!=30 || level<=1,"warm high preset cannot force full effects")
    }
    verify(DreamGraphics26.cloudSteps(0)==0f,"low uses compatible clouds")
    verify(DreamGraphics26.cloudSteps(1)<DreamGraphics26.cloudSteps(2),"cloud samples scale")
    verify(DreamGraphics26.waterPlane(DreamWorlds.POOLS)==-.12f,"pool plane matches geometry/reflection")
    val p=DreamPools26.create().vertices
    verify(p.all{it.isFinite()}&&p.size%30==0,"pool triangle layout")
    verify(p.size/30<15000,"bounded pool geometry")
    verify(DreamPerformance.translucent(116f),"pool water draws after basin depth")
    val water=(p.indices step 10).filter{p[it+9]==116f}
    verify(water.all{p[it+7]>.99f},"water normals face out of the basin")
    verify(water.size==12 && water.all{abs(p[it+1]-DreamGraphics26.POOL_WATER)<.00001f},"two thin, coplanar water surfaces")
    verify((p.indices step 10).any{p[it+1]< -1.4f&&p[it+9]==9f},"mosaic basins have real depth")
    verify((p.indices step 10).any{p[it+9]==2f&&abs(p[it+2]-7.6f)<.2f},"return portal retained")
    verify((p.indices step 10).any{p[it+9]==3f&&p[it] in 8.8f..11.2f},"forward portal retained")
    val b=DreamMesh();DreamBotany26.tree(b,0f,0f,0f,1f,17);val a=b.finish().vertices
    val b2=DreamMesh();DreamBotany26.tree(b2,0f,0f,0f,1f,17)
    verify(a.contentEquals(b2.finish().vertices),"vegetation deterministic across reloads")
    verify(a.size/30<900,"tree cheaper than old ellipsoid crown")
    var leaves=0
    for(i in a.indices step 10)if(a[i+9]==111f){
        leaves++
        verify(a[i+3] in 0f..1f&&a[i+4] in 0f..1f,"leaf UV belongs to its cell")
        verify(a[i+5] in 0f..11f,"valid botanical variant")
        val n=sqrt(a[i+6]*a[i+6]+a[i+7]*a[i+7]+a[i+8]*a[i+8]);verify(abs(n-1f)<.001f,"unit leaf normal")
    }
    verify(leaves==22*6,"complete cutout crown")
    val house=DreamMesh();DreamSculpt.house(house,0f,0f,0f,floatArrayOf(.8f,.7f,.6f))
    val hd=house.finish().vertices
    verify((hd.indices step 10).filter{hd[it+9]==112f||hd[it+9]==113f}.all{hd[it+7]>0f},"both shared roof slopes face the sky")
    val village=DreamLands.create(DreamWorlds.MEADOW).vertices
    verify((village.indices step 10).filter{village[it+9]==112f||village[it+9]==113f}.all{village[it+7]>0f},"both village roof slopes face the sky")
    verify(DreamLands.canWalk(DreamWorlds.MEADOW,0f,0f),"central village route remains open")
    verify(DreamLands.canWalk(DreamWorlds.MEADOW,-40f,-47.8f),"open house entrance retained")
    verify(!DreamLands.canWalk(DreamWorlds.MEADOW,23f,14f),"new near tree blocks its trunk")
    println("PASS $checks graphics checks")
}
