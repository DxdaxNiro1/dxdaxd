package com.dreamcore.game

import kotlin.math.*

/** Shared mesh builder: position/color/normal/material, GLES2 layout unchanged. */
class DreamMesh {
    private var data = FloatArray(65536)
    private var used = 0

    private fun append(value: Float) {
        if (used >= data.size) data = data.copyOf(data.size * 2)
        data[used++] = value
    }

    private fun vertex(
        p: FloatArray, c: FloatArray,
        nx: Float, ny: Float, nz: Float, kind: Float
    ) {
        append(p[0]); append(p[1]); append(p[2])
        append(c[0]); append(c[1]); append(c[2])
        append(nx); append(ny); append(nz); append(kind)
    }

    fun triangle(a: FloatArray, b: FloatArray, c: FloatArray, color: FloatArray, kind: Float = 0f) {
        val ux = b[0] - a[0]
        val uy = b[1] - a[1]
        val uz = b[2] - a[2]
        val vx = c[0] - a[0]
        val vy = c[1] - a[1]
        val vz = c[2] - a[2]
        var nx = uy * vz - uz * vy
        var ny = uz * vx - ux * vz
        var nz = ux * vy - uy * vx
        val length = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.00001f)
        nx /= length; ny /= length; nz /= length
        vertex(a, color, nx, ny, nz, kind)
        vertex(b, color, nx, ny, nz, kind)
        vertex(c, color, nx, ny, nz, kind)
    }

    fun quad(a: FloatArray, b: FloatArray, c: FloatArray, d: FloatArray, color: FloatArray, kind: Float = 0f) {
        triangle(a, b, c, color, kind)
        triangle(a, c, d, color, kind)
    }

    fun box(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, c: FloatArray, kind: Float = 0f) {
        val l=x-sx; val r=x+sx; val lo=y-sy; val hi=y+sy; val f=z+sz; val bk=z-sz
        fun v(a: Float, b: Float, d: Float) = floatArrayOf(a,b,d)
        quad(v(l,lo,f),v(r,lo,f),v(r,hi,f),v(l,hi,f),c,kind)
        quad(v(r,lo,bk),v(l,lo,bk),v(l,hi,bk),v(r,hi,bk),c,kind)
        quad(v(l,lo,bk),v(l,lo,f),v(l,hi,f),v(l,hi,bk),c,kind)
        quad(v(r,lo,f),v(r,lo,bk),v(r,hi,bk),v(r,hi,f),c,kind)
        quad(v(l,hi,f),v(r,hi,f),v(r,hi,bk),v(l,hi,bk),c,kind)
        quad(v(l,lo,bk),v(r,lo,bk),v(r,lo,f),v(l,lo,f),c,kind)
    }

    // Normales suaves en nubes y objetos redondos: brillo continuo, sin facetas.
    private fun ellipsoidTriangle(a: FloatArray, b: FloatArray, c: FloatArray,
                                 centerX:Float,centerY:Float,centerZ:Float,
                                 rx:Float,ry:Float,rz:Float,color:FloatArray,kind:Float) {
        fun push(p:FloatArray) {
            val nx=(p[0]-centerX)/(rx*rx)
            val ny=(p[1]-centerY)/(ry*ry)
            val nz=(p[2]-centerZ)/(rz*rz)
            val len=sqrt(nx*nx+ny*ny+nz*nz).coerceAtLeast(.000001f)
            vertex(p,color,nx/len,ny/len,nz/len,kind)
        }
        push(a);push(b);push(c)
    }

    fun ellipsoid(x: Float, y: Float, z: Float, rx: Float, ry: Float, rz: Float, color: FloatArray,
                  latSegments: Int = 9, lonSegments: Int = 12, kind: Float = 0f) {
        fun point(i: Int, j: Int): FloatArray {
            val phi = (PI * i / latSegments).toFloat()
            val theta = (2.0 * PI * j / lonSegments).toFloat()
            return floatArrayOf(
                x + rx * sin(phi) * cos(theta),
                y + ry * cos(phi),
                z + rz * sin(phi) * sin(theta)
            )
        }
        for (i in 0 until latSegments) {
            for (j in 0 until lonSegments) {
                val a=point(i,j); val b=point(i+1,j)
                val c=point(i+1,j+1); val d=point(i,j+1)
                if (i != 0) ellipsoidTriangle(a,b,d,x,y,z,rx,ry,rz,color,kind)
                if (i != latSegments - 1) ellipsoidTriangle(b,c,d,x,y,z,rx,ry,rz,color,kind)
            }
        }
    }

    fun ring(cx: Float, cy: Float, cz: Float, radius: Float, thickness: Float, c: FloatArray) {
        val n = 24
        for (i in 0 until n) {
            val a = (2.0 * PI * i / n).toFloat()
            val b = (2.0 * PI * (i+1) / n).toFloat()
            val mx = cx + radius * cos((a+b)*.5f)
            val my = cy + radius * sin((a+b)*.5f)
            val dx = cos((a+b)*.5f) * thickness
            val dy = sin((a+b)*.5f) * thickness
            box(mx,my,cz, max(0.035f, kotlin.math.abs(dx)), max(0.035f,kotlin.math.abs(dy)), .055f,c)
        }
    }

    fun finish() = DreamWorlds.Scene(data.copyOf(used))
}
