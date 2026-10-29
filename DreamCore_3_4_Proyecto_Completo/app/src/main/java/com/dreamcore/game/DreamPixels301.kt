package com.dreamcore.game

/** Resample numerical ARGB channels independently: no Canvas and no alpha association. */
object DreamPixels301 {
    fun resizeInto(source:IntArray,sw:Int,sh:Int,target:IntArray,offset:Int,dw:Int,dh:Int){
        require(sw>0&&sh>0&&dw>0&&dh>0)
        require(sw.toLong()*sh<=source.size&&offset>=0&&offset.toLong()+dw.toLong()*dh<=target.size)
        if(sw==dw&&sh==dh){source.copyInto(target,offset,0,sw*sh);return}
        val left=IntArray(dw);val right=IntArray(dw);val weight=FloatArray(dw)
        for(x in 0 until dw){val at=((x+.5f)*sw/dw-.5f).coerceIn(0f,(sw-1).toFloat());left[x]=at.toInt();right[x]=minOf(left[x]+1,sw-1);weight[x]=at-left[x]}
        for(y in 0 until dh){
            val at=((y+.5f)*sh/dh-.5f).coerceIn(0f,(sh-1).toFloat());val top=at.toInt()*sw;val bottom=minOf(at.toInt()+1,sh-1)*sw;val wy=at-at.toInt()
            for(x in 0 until dw){
                val a=source[top+left[x]];val b=source[top+right[x]];val c=source[bottom+left[x]];val d=source[bottom+right[x]];val wx=weight[x]
                var color=0
                for(shift in 0..24 step 8){
                    val aa=(a ushr shift)and 255;val bb=(b ushr shift)and 255;val cc=(c ushr shift)and 255;val dd=(d ushr shift)and 255
                    val upper=aa+(bb-aa)*wx;val lower=cc+(dd-cc)*wx
                    val value=(upper+(lower-upper)*wy+.5f).toInt().coerceIn(0,255)
                    color=color or(value shl shift)
                }
                target[offset+y*dw+x]=color
            }
        }
    }
}
