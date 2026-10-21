package com.dreamcore.game

/** Exact ten-float vertex reuse. No quantization, welding tolerance, lost UV or triangle removal. */
object DreamIndexed28 {
    const val LIMIT=65535
    data class Page(val vertices:FloatArray,val indices:ShortArray,val opaqueIndices:Int,val shadow:ShortArray) {
        val bytes get()=vertices.size*4+(indices.size+shadow.size)*2
    }
    data class Mesh(val pages:List<Page>,val originalVertices:Int) {
        val bytes get()=pages.sumOf{it.bytes}
        val uniqueVertices get()=pages.sumOf{it.vertices.size/10}
    }
    // Same predicate as the existing shadow fragment shader, evaluated once instead of every shadow pass.
    fun castsShadow(k:Float)=!((k>.5f&&k<3.5f)||(k>19.5f&&k<20.5f)||(k>27.5f&&k<32.5f)||(k>49.5f&&k<110.5f)||k>115.5f)
    fun encode(data:FloatArray,opaque:Int=data.size/10,includeShadow:Boolean=true):Mesh {
        require(data.size%30==0 && opaque%3==0 && opaque in 0..data.size/10)
        val pages=ArrayList<Page>();var begin=0
        while(begin<data.size/10){
            // An open-addressed table avoids allocating one key and ten boxed floats per vertex.
            val table=IntArray(1 shl 17){-1};val unique=FloatArray(minOf(LIMIT,(data.size/10)-begin)*10)
            val index=ShortArray(minOf(data.size/10-begin,LIMIT*6));val shadow=if(includeShadow)ShortArray(index.size)else ShortArray(0)
            var count=0;var used=0;var shadows=0;var opaqueCount=0;var cursor=begin
            fun find(at:Int):Int {
                var hash=0x811c9dc5.toInt()
                for(j in 0..9)hash=(hash xor data[at+j].toRawBits())*16777619
                var slot=(hash xor (hash ushr 16))and(table.size-1)
                while(true){val candidate=table[slot]
                    if(candidate<0)return -slot-1
                    var same=true
                    for(j in 0..9)if(unique[candidate*10+j].toRawBits()!=data[at+j].toRawBits()){same=false;break}
                    if(same)return candidate
                    slot=(slot+1)and(table.size-1)
                }
            }
            while(cursor<data.size/10 && used+3<=index.size){
                // End a page on a triangle boundary; reserve space for three distinct corners.
                if(count>LIMIT-3)break
                val caster=includeShadow && castsShadow(data[cursor*10+9])
                for(corner in 0..2){val at=(cursor+corner)*10;var n=find(at)
                    if(n<0){val slot=-n-1;n=count++;data.copyInto(unique,n*10,at,at+10);table[slot]=n}
                    index[used++]=n.toShort();if(caster)shadow[shadows++]=n.toShort()
                }
                if(cursor<opaque)opaqueCount+=3
                cursor+=3
            }
            check(cursor>begin)
            pages.add(Page(unique.copyOf(count*10),index.copyOf(used),opaqueCount,shadow.copyOf(shadows)));begin=cursor
        }
        return Mesh(pages,data.size/10)
    }
    fun expand(mesh:Mesh):FloatArray {
        val out=FloatArray(mesh.originalVertices*10);var at=0
        for(p in mesh.pages)for(i in p.indices){val index=(i.toInt()and 65535)*10;p.vertices.copyInto(out,at,index,index+10);at+=10}
        return out
    }
}
