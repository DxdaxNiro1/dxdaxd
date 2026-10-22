package com.dreamcore.game

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import kotlin.math.abs

/** Choose a window-compatible RGB target explicitly; never accept the driver's arbitrary first match. */
class DreamEglConfigChooser : GLSurfaceView.EGLConfigChooser {
    override fun chooseConfig(egl:EGL10,display:EGLDisplay):EGLConfig {
        for(bits in arrayOf(intArrayOf(8,8,8),intArrayOf(5,6,5))){
            val spec=intArrayOf(EGL10.EGL_RED_SIZE,bits[0],EGL10.EGL_GREEN_SIZE,bits[1],EGL10.EGL_BLUE_SIZE,bits[2],EGL10.EGL_DEPTH_SIZE,16,EGL10.EGL_SURFACE_TYPE,EGL10.EGL_WINDOW_BIT,0x3040,4,EGL10.EGL_NONE)
            val count=IntArray(1)
            if(!egl.eglChooseConfig(display,spec,null,0,count)||count[0]<=0)continue
            val found=arrayOfNulls<EGLConfig>(count[0]);if(!egl.eglChooseConfig(display,spec,found,found.size,count))continue
            val value=IntArray(1)
            fun get(c:EGLConfig,key:Int)=if(egl.eglGetConfigAttrib(display,c,key,value))value[0] else 0
            return found.filterNotNull().minByOrNull{c->
                abs(get(c,EGL10.EGL_RED_SIZE)-bits[0])*1000+abs(get(c,EGL10.EGL_GREEN_SIZE)-bits[1])*1000+abs(get(c,EGL10.EGL_BLUE_SIZE)-bits[2])*1000+
                    get(c,EGL10.EGL_SAMPLES)*10000+abs(get(c,EGL10.EGL_DEPTH_SIZE)-24)*10+abs(get(c,EGL10.EGL_ALPHA_SIZE)-8)+get(c,EGL10.EGL_STENCIL_SIZE)
            }?:continue
        }
        error("No se encontró una superficie OpenGL ES 2 con profundidad de 16 bits")
    }
}
