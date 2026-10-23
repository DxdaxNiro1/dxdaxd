package com.dreamcore.game

import android.content.Context
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Ordered disk writes, a previous valid snapshot, and a bounded lifecycle flush. */
class DreamSaveStore(context:Context) {
    private val prefs=context.getSharedPreferences("dreamcore_journey_18",Context.MODE_PRIVATE)
    private val writer=Executors.newSingleThreadExecutor()
    fun hasProgress()=DreamChronicle.decode(prefs.getString("current",null))!=null || DreamChronicle.decode(prefs.getString("previous",null))!=null
    fun replace(snapshot:String){
        require(DreamChronicle.decode(snapshot)!=null)
        writer.submit{if(!prefs.edit().putString("current",snapshot).putString("previous",snapshot).commit())Log.w("DreamCore","No se pudo iniciar el nuevo guardado")}
    }
    fun load()=DreamChronicle.recover(prefs.getString("current",null),prefs.getString("previous",null))
    fun save(snapshot:String,flush:Boolean) {
        val future=writer.submit {
            try {
                val old=prefs.getString("current",null)
                val edit=prefs.edit()
                if(DreamChronicle.decode(old)!=null)edit.putString("previous",old)
                if(!edit.putString("current",snapshot).commit())Log.w("DreamCore","No se pudo guardar el progreso")
            } catch(problem:Exception){Log.w("DreamCore","Guardado pendiente",problem)}
        }
        if(flush)try{future.get(700,TimeUnit.MILLISECONDS)}catch(problem:Exception){Log.w("DreamCore","El guardado continúa en segundo plano",problem)}
    }
    fun close(){writer.shutdown()}
}
