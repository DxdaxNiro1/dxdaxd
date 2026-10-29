package android.opengl
import java.nio.Buffer
import java.nio.ByteBuffer
object GLES20{
 const val GL_MAX_TEXTURE_SIZE=3379;const val GL_TEXTURE_2D=3553;const val GL_TEXTURE_MIN_FILTER=10241;const val GL_LINEAR_MIPMAP_LINEAR=9987;const val GL_TEXTURE_MAG_FILTER=10240;const val GL_LINEAR=9729;const val GL_TEXTURE_WRAP_S=10242;const val GL_TEXTURE_WRAP_T=10243;const val GL_CLAMP_TO_EDGE=33071;const val GL_RGBA=6408;const val GL_UNSIGNED_BYTE=5121;const val GL_NO_ERROR=0;const val GL_UNPACK_ALIGNMENT=3317
 var limit=4096;var next=1;var bound=0;var error=0;var failAllocation=false;var failSub=false;var failMip=false;val minFilters=HashMap<Int,Int>();var subs=0
 data class Texture(val width:Int,val height:Int,val bytes:ByteArray,val filled:BooleanArray=BooleanArray(32))
 val live=HashMap<Int,Texture>()
 fun reset(){limit=4096;next=1;bound=0;error=0;failAllocation=false;failSub=false;failMip=false;minFilters.clear();subs=0;live.clear();uploads.clear()}
 data class Upload(val width:Int,val height:Int,val bytes:ByteArray)
 val uploads=ArrayList<Upload>()
 fun glGetIntegerv(key:Int,values:IntArray,offset:Int){check(key==GL_MAX_TEXTURE_SIZE);values[offset]=limit}
 fun glGenTextures(n:Int,ids:IntArray,offset:Int){repeat(n){ids[offset+it]=next++}}
 fun glBindTexture(target:Int,id:Int){check(target==GL_TEXTURE_2D);bound=id}
 fun glTexParameteri(target:Int,key:Int,value:Int){check(target==GL_TEXTURE_2D);if(key==GL_TEXTURE_MIN_FILTER)minFilters[bound]=value}
 fun glPixelStorei(key:Int,value:Int){check(key==GL_UNPACK_ALIGNMENT&&value==4)}
 fun glTexImage2D(target:Int,level:Int,internal:Int,width:Int,height:Int,border:Int,format:Int,type:Int,data:Buffer?){
  check(target==GL_TEXTURE_2D&&internal==GL_RGBA&&format==GL_RGBA&&type==GL_UNSIGNED_BYTE)
  if(data==null&&failAllocation){error=1285;return}
  val bytes=ByteArray(width*height*4);val t=Texture(width,height,bytes);live[bound]=t
  if(data!=null){val b=(data as ByteBuffer).duplicate();check(b.remaining()==bytes.size);b.get(bytes);t.filled.fill(true);uploads.add(Upload(width,height,bytes))}
 }
 fun glTexSubImage2D(target:Int,level:Int,x:Int,y:Int,width:Int,height:Int,format:Int,type:Int,data:Buffer){
  if(failSub){error=1285;return};val t=live.getValue(bound);check(x>=0&&y>=0&&x+width<=t.width&&y+height<=t.height)
  val b=(data as ByteBuffer).duplicate();check(b.remaining()==width*height*4);for(row in 0 until height)b.get(t.bytes,((y+row)*t.width+x)*4,width*4)
  t.filled[(y/height)*4+x/width]=true;subs++
 }
 fun glDeleteTextures(n:Int,ids:IntArray,offset:Int){repeat(n){live.remove(ids[offset+it])}}
 fun glGetError():Int{val old=error;error=0;return old}
 fun glGenerateMipmap(target:Int){check(target==GL_TEXTURE_2D);if(failMip)error=1282}
}
