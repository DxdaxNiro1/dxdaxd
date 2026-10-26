package android.graphics.drawable
import android.graphics.*
abstract class Drawable{val bounds=Rect(0,0,800,450);fun invalidateSelf(){};abstract fun draw(canvas:Canvas);open fun getIntrinsicWidth()=0;open fun getIntrinsicHeight()=0;abstract fun setAlpha(alpha:Int);abstract fun setColorFilter(filter:ColorFilter?);abstract fun getOpacity():Int}
