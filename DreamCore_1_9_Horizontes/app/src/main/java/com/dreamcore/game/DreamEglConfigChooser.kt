package com.dreamcore.game

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/** Prefer a precise depth buffer for the deep shaft; retain an ES2 fallback. */
class DreamEglConfigChooser : GLSurfaceView.EGLConfigChooser {
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        for (spec in arrayOf(intArrayOf(8,8,8,24),intArrayOf(8,8,8,16),intArrayOf(5,6,5,16))) {
            val attributes = intArrayOf(
                EGL10.EGL_RED_SIZE,spec[0], EGL10.EGL_GREEN_SIZE,spec[1],
                EGL10.EGL_BLUE_SIZE,spec[2], EGL10.EGL_DEPTH_SIZE,spec[3],
                0x3040,4, // EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT
                EGL10.EGL_NONE
            )
            val found = arrayOfNulls<EGLConfig>(1)
            val count = IntArray(1)
            if (egl.eglChooseConfig(display,attributes,found,1,count) && count[0]>0) {
                val config=found[0]
                if (config!=null) return config
            }
        }
        error("No se encontro una configuracion OpenGL ES 2 con profundidad de 16 bits")
    }
}
