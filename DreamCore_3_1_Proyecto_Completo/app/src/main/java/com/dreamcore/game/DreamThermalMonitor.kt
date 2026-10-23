package com.dreamcore.game

import android.content.Context
import android.os.Build
import android.os.PowerManager

/** Uses the OS heat signal where available; no assumptions about a phone's chipset. */
class DreamThermalMonitor(context:Context,cap:(Int)->Unit) {
    private var dispose:(()->Unit)?=null
    init {if(Build.VERSION.SDK_INT>=29){
        val power=context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if(power!=null){val listener=PowerManager.OnThermalStatusChangedListener{status->cap(DreamPower.cap(status))}
            power.addThermalStatusListener(context.mainExecutor,listener)
            cap(DreamPower.cap(power.currentThermalStatus))
            dispose={power.removeThermalStatusListener(listener)}}
    }}
    fun close(){dispose?.invoke();dispose=null}
}
