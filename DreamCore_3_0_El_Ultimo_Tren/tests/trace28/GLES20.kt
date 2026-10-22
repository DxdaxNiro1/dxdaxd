package android.opengl

import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/** Test-only GL boundary: replay actual uploaded buffers and ushort draw calls. Never part of the APK. */
object GLES20 {
 const val GL_ARRAY_BUFFER=34962;const val GL_ELEMENT_ARRAY_BUFFER=34963;const val GL_STATIC_DRAW=35044
 const val GL_FLOAT=5126;const val GL_UNSIGNED_SHORT=5123;const val GL_TRIANGLES=4
 private var next=1;private var vertex=0;private var index=0
 val vertices=HashMap<Int,FloatArray>();val indices=HashMap<Int,ShortArray>();val live=HashSet<Int>();val deletedPrograms=ArrayList<Int>()
 val output=ArrayList<Float>()
 fun glGenBuffers(n:Int,ids:IntArray,offset:Int){repeat(n){val id=next++;ids[offset+it]=id;live.add(id)}}
 fun glBindBuffer(target:Int,id:Int){check(id==0||id in live);if(target==GL_ARRAY_BUFFER)vertex=id else if(target==GL_ELEMENT_ARRAY_BUFFER)index=id else error("target")}
 fun glBufferData(target:Int,size:Int,data:Buffer,usage:Int){check(usage==GL_STATIC_DRAW)
  if(target==GL_ARRAY_BUFFER){val source=(data as FloatBuffer).duplicate();check(size==source.remaining()*4);vertices[vertex]=FloatArray(source.remaining()).also{source.get(it)}}
  else{val source=(data as ShortBuffer).duplicate();check(size==source.remaining()*2);indices[index]=ShortArray(source.remaining()).also{source.get(it)}}
 }
 fun glVertexAttribPointer(location:Int,size:Int,type:Int,normalized:Boolean,stride:Int,offset:Int){check(location>=0&&type==GL_FLOAT&&!normalized&&stride==40&&offset in listOf(0,12,24,36));check(size==if(offset==36)1 else 3);check(vertex in vertices)}
 fun glDrawElements(mode:Int,count:Int,type:Int,offset:Int){check(mode==GL_TRIANGLES&&type==GL_UNSIGNED_SHORT&&count%3==0&&offset%2==0)
  val v=vertices.getValue(vertex);val ids=indices.getValue(index);check(offset/2+count<=ids.size)
  for(i in offset/2 until offset/2+count){val at=(ids[i].toInt()and 65535)*10;check(at+10<=v.size);for(j in 0..9)output.add(v[at+j])}
 }
 fun glDeleteBuffers(n:Int,ids:IntArray,offset:Int){repeat(n){val id=ids[offset+it];if(id!=0){check(live.remove(id));vertices.remove(id);indices.remove(id)}}}
 fun glDeleteProgram(id:Int){deletedPrograms.add(id)}
}
