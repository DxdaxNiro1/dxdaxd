package com.dreamcore.game

/** The title screen never advances or saves a journey that has not been started. */
class DreamSession(private val storedProgress:Boolean){
    var atTitle=true;private set
    var started=false;private set
    val canContinue get()=storedProgress||started
    val maySave get()=started
    fun home(){atTitle=true}
    fun continueGame():Boolean{if(!canContinue)return false;started=true;atTitle=false;return true}
    fun newGame(){started=true;atTitle=false}
}
