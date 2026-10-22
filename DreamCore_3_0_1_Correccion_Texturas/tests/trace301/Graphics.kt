package android.graphics

import javax.imageio.ImageIO
import java.io.File
import android.content.res.Resources

/** Android bitmap boundary contract. No Android device or native Skia is simulated beyond this contract. */
class Bitmap(val width:Int,val height:Int,val pixels:IntArray,val hasAlpha:Boolean,val premultiplied:Boolean){
 enum class Config{ARGB_8888}
 var recycled=false
 fun getPixels(out:IntArray,offset:Int,stride:Int,x:Int,y:Int,w:Int,h:Int){check(!recycled);for(row in 0 until h)pixels.copyInto(out,offset+row*stride,(y+row)*width+x,(y+row)*width+x+w)}
 fun recycle(){check(!recycled);recycled=true;recycledCount++}
 companion object{
  var scaleCalls=0;var recycledCount=0
  fun createScaledBitmap(source:Bitmap,w:Int,h:Int,filter:Boolean):Bitmap{scaleCalls++;if(source.hasAlpha&&!source.premultiplied)throw RuntimeException("Canvas: trying to use a non-premultiplied bitmap");error("Unexpected Canvas scaling in data-texture path")}
 }
}
class ColorSpace {enum class Named{SRGB};companion object{fun get(name:Named)=ColorSpace()}}
object BitmapFactory {
 class Options {var inScaled=true;var inPreferredConfig=Bitmap.Config.ARGB_8888;var inPremultiplied=true;var inPreferredColorSpace:ColorSpace?=null}
 fun decodeResource(resources:Resources,id:Int,options:Options):Bitmap{
  check(!options.inScaled&&options.inPreferredConfig==Bitmap.Config.ARGB_8888)
  val image=ImageIO.read(File(resources.folder,if(id==1)"dream_materials_25.png" else "dream_botany_26.png"));val data=image.getRGB(0,0,image.width,image.height,null,0,image.width)
  if(id==2)data[0]=0x0097c154 // Sentinel: meaningful color data under zero alpha must not be destroyed.
  return Bitmap(image.width,image.height,data,image.colorModel.hasAlpha(),options.inPremultiplied)
 }
}
