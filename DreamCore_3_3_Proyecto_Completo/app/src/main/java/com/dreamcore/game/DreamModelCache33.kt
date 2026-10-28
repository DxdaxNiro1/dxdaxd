package com.dreamcore.game

/** Repeated furniture and vegetation are tessellated once, then copied into the existing spatial draw batches. */
object DreamModelCache33 {
    private val meshes=LinkedHashMap<String,FloatArray>(32,.75f,true)
    var hits=0;private set
    var builds=0;private set
    @Synchronized fun model(key:String,create:()->FloatArray):FloatArray{
        meshes[key]?.let{hits++;return it}
        val data=create();meshes[key]=data;builds++
        while(meshes.size>32){val it=meshes.entries.iterator();it.next();it.remove()}
        return data
    }
    @Synchronized fun clear(){meshes.clear();hits=0;builds=0}
    @Synchronized fun bytes()=meshes.values.sumOf{it.size*4}
}
