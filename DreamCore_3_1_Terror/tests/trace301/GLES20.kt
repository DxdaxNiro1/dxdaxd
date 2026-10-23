package android.opengl
import java.nio.Buffer
import java.nio.ByteBuffer
object GLES20{
 const val GL_MAX_TEXTURE_SIZE=3379;const val GL_TEXTURE_2D=3553;const val GL_TEXTURE_MIN_FILTER=10241;const val GL_LINEAR_MIPMAP_LINEAR=9987;const val GL_TEXTURE_MAG_FILTER=10240;const val GL_LINEAR=9729;const val GL_TEXTURE_WRAP_S=10242;const val GL_TEXTURE_WRAP_T=10243;const val GL_CLAMP_TO_EDGE=33071;const val GL_RGBA=6408;const val GL_UNSIGNED_BYTE=5121;const val GL_NO_ERROR=0;const val GL_UNPACK_ALIGNMENT=3317
 var limit=4096;var next=1
 data class Upload(val width:Int,val height:Int,val bytes:ByteArray)
 val uploads=ArrayList<Upload>()
 fun glGetIntegerv(key:Int,values:IntArray,offset:Int){check(key==GL_MAX_TEXTURE_SIZE);values[offset]=limit}
 fun glGenTextures(n:Int,ids:IntArray,offset:Int){repeat(n){ids[offset+it]=next++}}
 fun glBindTexture(target:Int,id:Int){check(target==GL_TEXTURE_2D)}
 fun glTexParameteri(target:Int,key:Int,value:Int){check(target==GL_TEXTURE_2D)}
 fun glPixelStorei(key:Int,value:Int){check(key==GL_UNPACK_ALIGNMENT&&value==4)}
 fun glTexImage2D(target:Int,level:Int,internal:Int,width:Int,height:Int,border:Int,format:Int,type:Int,data:Buffer){check(target==GL_TEXTURE_2D&&internal==GL_RGBA&&format==GL_RGBA&&type==GL_UNSIGNED_BYTE);val b=(data as ByteBuffer).duplicate();val bytes=ByteArray(b.remaining());b.get(bytes);check(bytes.size==width*height*4);uploads.add(Upload(width,height,bytes))}
 fun glGetError()=GL_NO_ERROR
 fun glGenerateMipmap(target:Int){check(target==GL_TEXTURE_2D)}
}
