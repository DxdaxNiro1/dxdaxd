package com.dreamcore.game

/** Deterministic policy: content stays intact; automatic resolution responds to OS heat signals. */
object DreamPower {
    fun cap(status:Int)=if(status>=3)24 else if(status>=2)30 else 60
    fun scale(mode:Int,thermalCap:Int):Float=when(mode){
        1->1f
        2->if(thermalCap<60).75f else .85f
        else->if(thermalCap<=24).75f else if(thermalCap<=30).88f else 1f
    }
    fun fps(requested:Int,thermalCap:Int,mode:Int)=minOf(requested,thermalCap,if(mode==2)24 else 60).coerceIn(15,60)
    fun indoor(id:String)=id==DreamWorld30.LIBRARY||DreamReferences.indoor(id)||id in listOf(DreamVoyage.REFUGE,DreamVoyage.LETTER,DreamPocket.LAUNDRY,DreamPocket.TRAIN,DreamPocket.DAWN,DreamMystery.HOUSE,DreamBackrooms.ID)
    fun shadowNeeded(id:String)=!indoor(id)
    fun mirrorInterval(moving:Boolean,requested:Int,thermalCap:Int)=if(!moving)1f else if(thermalCap<60).4f else if(requested<=30).25f else .12f
}
