package com.dreamcore.game

import kotlin.math.hypot
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Geometría sin recursos externos: se genera una vez y se envía a la GPU. */
object DreamWorlds {
    const val STATION = "dream_01"
    const val POOLS = "dream_02"
    const val CRATER = "dream_03"
    const val GARDEN = "dream_04"
    const val MEADOW = "dream_05"
    const val AQUARIUM = "dream_06"
    const val HOUSE_MEADOW = "dream_07"
    const val HOUSE_CRATER = "dream_08"

    data class Scene(val vertices: FloatArray) {
        val vertexCount: Int get() = vertices.size / 10
    }

    private class Builder {
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

        fun finish() = Scene(data.copyOf(used))
    }

    private fun rgb(r: Float,g: Float,b: Float) = floatArrayOf(r,g,b)

    // Colinas suaves. Cerca de la estación queda nivelado para caminar.
    fun groundHeight(x: Float, z: Float): Float {
        val distance = kotlin.math.max(kotlin.math.abs(x) - 9f, kotlin.math.abs(z + 8f) - 22f)
        val flatten = (distance / 8f).coerceIn(0f, 1f)
        return flatten * (0.70f*sin(x*.095f)*cos(z*.067f) + 0.22f*sin(z*.19f))
    }

    fun createStation(): Scene {
        val b=Builder()
        val grass=rgb(.47f,.79f,.52f)
        val lilac=rgb(.75f,.61f,.85f)
        val cream=rgb(.99f,.89f,.79f)
        val pink=rgb(.95f,.61f,.74f)
        val plum=rgb(.35f,.27f,.57f)
        val gold=rgb(.99f,.86f,.61f)
        // Terreno ondulado, mosaico de verdes no idénticos.
        val step=2.0f
        var x=-56f
        while(x<56f) {
            var z=-70f
            while(z<50f) {
                val wobble=(.5f+.5f*sin(x*.73f+z*.61f))*.095f
                val tone=rgb(grass[0]+wobble, grass[1]-wobble*.42f, grass[2]+wobble*.22f)
                val a=floatArrayOf(x,groundHeight(x,z)-.16f,z)
                val q=floatArrayOf(x+step,groundHeight(x+step,z)-.16f,z)
                val c=floatArrayOf(x+step,groundHeight(x+step,z+step)-.16f,z+step)
                val d=floatArrayOf(x,groundHeight(x,z+step)-.16f,z+step)
                b.quad(a,d,c,q,tone,4f)
                z+=step
            }
            x+=step
        }

        // Sendero crema y plataforma.
        b.box(0f,-.09f,-6f,1.45f,.045f,11.6f,rgb(.98f,.94f,.88f),8f)
        b.box(0f,-.04f,-17f,4.8f,.18f,3.9f,rgb(.92f,.77f,.85f),11f)
        b.box(0f,.14f,-17f,4.65f,.035f,3.75f,rgb(.97f,.85f,.89f),11f)
        for (xx in floatArrayOf(-4f,4f)) for(zz in floatArrayOf(-13.5f,-20.5f)) {
            b.box(xx,2.25f,zz,.22f,2.18f,.22f,cream,15f)
            b.box(xx,.28f,zz,.40f,.22f,.40f,lilac)
            b.box(xx,4.25f,zz,.35f,.15f,.35f,pink)
        }
        b.box(0f,4.52f,-17f,4.85f,.20f,4.1f,pink,11f)
        b.box(0f,4.76f,-17f,4.55f,.085f,3.8f,rgb(.99f,.82f,.88f),11f)
        b.box(0f,3.77f,-13.23f,4.5f,.11f,.12f,cream)
        // Fachada trasera; hueco central para el portal.
        b.box(-2.70f,2.1f,-20.74f,1.35f,2.05f,.15f,cream,5f)
        b.box(2.70f,2.1f,-20.74f,1.35f,2.05f,.15f,cream,5f)
        b.box(0f,3.83f,-20.74f,1.35f,.33f,.15f,cream)
        // Portal con material pulsante (tipo 2).
        b.box(0f,1.68f,-20.55f,1.00f,1.69f,.06f,plum,2f)
        for (xx in floatArrayOf(-1.12f,1.12f)) b.box(xx,1.75f,-20.32f,.11f,1.78f,.16f,lilac)
        b.box(0f,3.58f,-20.32f,1.23f,.12f,.16f,lilac)
        // Marquesina, banco, poste, reloj nostálgico.
        b.box(-1.7f,3.64f,-13.0f,1.95f,.28f,.13f,rgb(.75f,.52f,.70f))
        b.box(-1.7f,3.65f,-12.83f,1.75f,.19f,.03f,cream)
        b.box(2.3f,.59f,-14.2f,1.3f,.13f,.42f,lilac)
        b.box(2.3f,.98f,-14.55f,1.3f,.39f,.09f,lilac)
        for (xx in floatArrayOf(1.25f,3.35f)) b.box(xx,.30f,-14.2f,.10f,.30f,.30f,cream)
        b.box(-5.7f,2.5f,-11.8f,.09f,2.5f,.09f,rgb(.81f,.76f,.81f))
        b.ellipsoid(-5.7f,5.05f,-11.8f,.34f,.34f,.34f,gold,8,12)
        b.box(-3f,3.35f,-20.50f,.42f,.42f,.15f,cream)
        b.ring(-3f,3.35f,-20.3f,.32f,.065f,plum)
        // Árboles redondeados alejados del camino.
        val trees=arrayOf(
            floatArrayOf(-14f,-15f),floatArrayOf(14f,-26f),floatArrayOf(-22f,4f),
            floatArrayOf(19f,12f),floatArrayOf(-14f,24f),floatArrayOf(27f,-39f)
        )
        for ((i,p) in trees.withIndex()) {
            val h=groundHeight(p[0],p[1]); val sy=2.6f+i%3*.25f
            b.box(p[0],h+sy*.5f,p[1],.26f,sy*.5f,.26f,rgb(.65f,.52f,.53f),15f)
            b.ellipsoid(p[0],h+sy+.65f,p[1],1.5f+i%2*.3f,1.55f,1.5f,rgb(.57f,.78f,.66f),9,12,12f)
        }

        // Portal turquesa: tercer mundo, separado de la puerta original de piscinas.
        b.box(-9.2f,1.7f,-17.5f,1.07f,1.7f,.09f,rgb(.46f,.83f,.82f),3f)
        for (xx in floatArrayOf(-10.39f,-8.01f))
            b.box(xx,1.8f,-17.5f,.13f,1.86f,.20f,cream)
        b.box(-9.2f,3.68f,-17.5f,1.33f,.13f,.20f,pink)
        // Cartel flotante y escalones suaves hacia el segundo portal.
        b.box(-9.2f,4.10f,-17.5f,1.22f,.21f,.16f,rgb(.95f,.91f,.74f))
        // 0.7: puerta dorada a EL JARDIN DE LA LLUVIA DETENIDA.
        // Se encuentra a la derecha de la estación, sin tapar las otras.
        b.box(9.2f,1.73f,-17.5f,1.08f,1.73f,.08f,rgb(.88f,.73f,.87f),2f)
        for (xx in floatArrayOf(8.00f,10.40f)) {
            b.box(xx,1.84f,-17.5f,.16f,1.86f,.22f,rgb(.98f,.86f,.64f),6f)
            b.ellipsoid(xx,3.95f,-17.5f,.23f,.24f,.24f,rgb(1f,.93f,.71f),7,9)
        }
        b.box(9.2f,3.75f,-17.5f,1.36f,.16f,.23f,rgb(.98f,.86f,.64f),6f)
        b.box(9.2f,4.30f,-17.5f,1.27f,.30f,.16f,rgb(.96f,.94f,.90f),5f)
        // 1.0: nueva puerta al Atlas. Estacion y sus tres entradas previas intactas.
        val blueGlass=rgb(.31f,.64f,.80f)
        b.box(-18f,1.77f,-5.5f,1.07f,1.73f,.09f,blueGlass,3f)
        for (xx in floatArrayOf(-19.25f,-16.75f))
            b.box(xx,1.85f,-5.5f,.16f,1.91f,.22f,cream,5f)
        b.box(-18f,3.78f,-5.5f,1.36f,.17f,.22f,gold,6f)
        for (i in 0..7) {
            val x=-3f-i*1.93f
            b.box(x,groundHeight(x,-5.5f)+.035f,-5.5f,.63f,.035f,.74f,cream,10f)
        }
        // 0.8: la estación es un sueño con nubes monumentales, no simples esferas.
        // Cada nube tiene un volumen enorme, varios lóbulos y sombras pastel.
        // No usa nuevas texturas ni transparencias, compatible con GLES 2.0.
        buildImpossibleClouds(b)
        // La galería es pequeña por fuera, inmensa por dentro.
        buildImpossibleGallery(b)
        // 1.1: capas oníricas de algodón enormes, muy alejadas y alargadas.
        // Su volumen se renderiza con normales suaves (kind 20).
        val cloudWhite=rgb(.94f,.93f,.98f)
        val cloudRose=rgb(.99f,.86f,.95f)
        for(i in 0..6) {
            val t=i.toFloat()
            val cx=-145f+t*49f
            val cz=-120f - 12f*sin(t*.78f)
            val cy=51f+15f*sin(t*.63f)
            b.ellipsoid(cx,cy,cz,30f+(i%3)*10f,9f+(i%2)*3f,26f,
                if(i%3==0)cloudRose else cloudWhite,9,14,20f)
        }
        return b.finish()
    }

    // Un tipo de material específico (kind=20) evita usar el atlas de paredes.
    // Los volúmenes quedan en el cielo y no afectan las colisiones del jugador.
    private fun buildImpossibleClouds(b: Builder) {
        val ivory = rgb(1.00f,.98f,.96f)
        val palePink = rgb(.99f,.88f,.96f)
        val blue = rgb(.82f,.89f,1.00f)
        val lavender = rgb(.90f,.84f,1.00f)
        val colors = arrayOf(ivory,palePink,blue,lavender)
        // 9 bancos inmensos alrededor de la estación, alturas surrealistas.
        // Mantener los objetos entre 25 y 145 m para verlos también sin mirar verticalmente.
        val banks = arrayOf(
            floatArrayOf(-23f,34f,-44f,19f, 0f),
            floatArrayOf( 28f,31f,-50f,21f, 1f),
            floatArrayOf(  4f,68f,-97f,32f, 2f),
            floatArrayOf(-67f,42f,-25f,24f, 3f),
            floatArrayOf( 59f,44f,-12f,25f, 0f),
            floatArrayOf(-28f,40f, 56f,31f, 1f),
            floatArrayOf( 39f,43f, 64f,30f, 2f),
            floatArrayOf(-95f,64f,-98f,40f, 3f),
            floatArrayOf(101f,73f,-112f,39f, 0f)
        )
        for ((bank,p) in banks.withIndex()) {
            val x=p[0]; val y=p[1]; val z=p[2]; val wide=p[3]
            val shade=colors[p[4].toInt()]
            // Base horizontal de nube: su anchura supera varios edificios.
            b.ellipsoid(x,y,z,wide*1.02f,wide*.38f,wide*.69f,shade,14,20,20f)
            for(i in -2..2) {
                val k=i.toFloat()
                val sweep=(1f-kotlin.math.abs(k)*.16f)
                val xx=x+k*wide*.41f
                val zz=z + 3.0f*sin(bank*.7f+k*1.1f)
                val yy=y + wide*(.04f+.10f*cos(k*1.8f+bank*.8f))
                b.ellipsoid(xx,yy,zz,wide*.39f*sweep,wide*.36f*sweep,
                    wide*.45f, colors[(bank+i+12)%4],12,17,20f)
            }
            // Volúmenes altos, como montañas de algodón flotantes.
            for(i in 0..2) {
                val xx=x+(i-1)*wide*.53f
                b.ellipsoid(xx,y+wide*(.21f+.07f*i),z-wide*.15f,
                    wide*(.24f+.05f*i),wide*.30f,wide*.31f,
                    colors[(bank+i+1)%4],12,17,20f)
            }
        }
        // Tres masas casi sobre la cámara: el cielo parece desproporcionado.
        for (i in 0..2) {
            val x=-19f+i*20f
            b.ellipsoid(x,83f+(i%2)*7f,-7f,23f,9f,18f,
                colors[(i+1)%4],9,12,20f)
        }
    }

    private fun buildImpossibleGallery(b: Builder) {
        val porcelain=rgb(.95f,.96f,.91f)
        val lilac=rgb(.81f,.73f,.93f)
        val rose=rgb(.96f,.78f,.85f)
        val metal=rgb(.75f,.78f,.89f)
        val velvet=rgb(.26f,.32f,.53f)
        val green=rgb(.74f,.87f,.79f)
        // Galería a la derecha de la estación. Se entra por x≈30,z≈-8.
        // Es más larga por dentro de lo que su fachada deja imaginar.
        val cx=30f
        val entranceZ=-8.5f
        // Camino de pequeñas baldosas que guía a la galería desde la estación.
        for (i in 0..10) {
            val px=10.0f+i*1.9f
            val pz=-7.2f-i*.13f
            val py=groundHeight(px,pz)
            b.box(px,py+.032f,pz,.77f,.035f,.69f,
                if (i%2==0) porcelain else lilac,10f)
        }
        val floor0=groundHeight(cx,entranceZ)
        // Primera fachada de casita, abierta en su centro (sin romper portales).
        for (x in floatArrayOf(26.1f,33.9f)) {
            b.box(x,floor0+3.15f,entranceZ,.56f,3.1f,.52f,porcelain,5f)
        }
        b.box(cx,floor0+6.30f,entranceZ,4.45f,.45f,.66f,rose,13f)
        b.box(cx,floor0+6.90f,entranceZ,3.55f,.27f,.78f,lilac,13f)
        // Suelo por segmentos ajustado a la altura de las colinas.
        for (i in 0..13) {
            val z=-9.5f-i*2.60f
            val h=groundHeight(cx,z)
            b.box(cx,h-.08f,z,3.70f,.13f,1.33f,porcelain,10f)
            b.box(cx,h+.065f,z,3.20f,.01f,.035f,rose,11f)
        }
        // Dos muros y techo imposiblemente largos, con arcos que se repiten.
        for (i in 0..12) {
            val z=-10.0f-i*2.85f
            val h=groundHeight(cx,z)
            for (side in floatArrayOf(-1f,1f)) {
                val x=cx+side*3.95f
                b.box(x,h+3.42f,z,.30f,3.3f,1.49f,porcelain,5f)
                b.box(x-side*.28f,h+4.20f,z,.055f,1.14f,.83f,
                    if(i%4==0) green else velvet,21f)
                b.box(x-side*.35f,h+4.20f,z,.095f,1.3f,.10f,metal,6f)
            }
            b.box(cx,h+6.75f,z,4.10f,.22f,1.46f,porcelain,10f)
            if(i%2==0) {
                b.box(cx,h+6.18f,z,3.95f,.12f,.20f,lilac,13f)
                b.ellipsoid(cx,h+5.91f,z,.21f,.21f,.21f,rose,6,8)
            }
        }
        // Al fondo hay una puerta que parece mostrar otro cielo.
        val farZ=-46.3f
        val y=groundHeight(cx,farZ)
        b.box(cx-2.90f,y+3.1f,farZ,.85f,3.0f,.38f,porcelain,5f)
        b.box(cx+2.90f,y+3.1f,farZ,.85f,3.0f,.38f,porcelain,5f)
        b.box(cx,y+5.95f,farZ,3.6f,.28f,.46f,rose,13f)
        b.box(cx,y+2.42f,farZ,.95f,2.30f,.09f,velvet,21f)
        b.box(cx,y+4.94f,farZ,1.2f,.16f,.12f,green,6f)
        // Señales de escala sin textos: un espejo muestra un cielo de otro color.
        b.box(cx,y+.22f,farZ+2.6f,2.6f,.12f,1.4f,lilac,13f)
    }

    fun createPools(): Scene {
        val b=Builder()
        val wall=rgb(.90f,.76f,.90f)
        val sky=rgb(.82f,.83f,.99f)
        val pink=rgb(.96f,.70f,.82f)
        val ivory=rgb(.99f,.94f,.90f)
        val aqua=rgb(.38f,.79f,.86f)
        val turquoise=rgb(.62f,.91f,.88f)
        // Sala surrealista amplia con piso claro.
        b.box(0f,-.25f,-11f,15.5f,.19f,25f,ivory,10f)
        b.box(-15.5f,5f,-11f,.32f,5.4f,25f,wall,11f)
        b.box(15.5f,5f,-11f,.32f,5.4f,25f,wall,11f)
        b.box(0f,5f,-36f,15.5f,5.4f,.32f,wall,11f)
        b.box(0f,10.25f,-11f,15.5f,.16f,25f,sky,10f)
        // Techo con franjas que sugieren claraboyas.
        for (i in 0..7) b.box(0f,10.05f,5f-i*6f,15.3f,.10f,.19f,pink)
        // Dos canales de agua con puente central.
        for (side in floatArrayOf(-1f,1f)) {
            val center=side*5.45f
            b.box(center,-.12f,-17f,3.5f,.09f,12f,aqua,1f)
            b.box(side*1.85f,.24f,-17f,.16f,.30f,12.1f,pink)
            b.box(side*9.04f,.24f,-17f,.16f,.30f,12.1f,pink)
            b.box(center,.05f,-29.2f,3.60f,.23f,.19f,pink)
            b.box(center,.05f,-4.8f,3.60f,.23f,.19f,pink)
        }
        b.box(0f,-.015f,-17f,1.70f,.08f,12.1f,ivory,10f)
        for (i in 0..18) {
            val z = -28.5f + i * 1.25f
            b.box(0f,.035f,z,1.63f,.018f,.045f,pink)
        }
        // Columnas, luces esféricas y arcos de techo.
        for (i in 0..6) {
            val z= -5f-i*4f
            for (xx in floatArrayOf(-11.5f,11.5f)) {
                b.box(xx,4.5f,z,.30f,4.3f,.30f,ivory,15f)
                b.box(xx,8.7f,z,.45f,.18f,.45f,pink)
                b.ellipsoid(xx,8.05f,z,.35f,.35f,.35f,turquoise,7,10)
            }
            b.box(0f,9.15f,z,11.6f,.22f,.21f,pink)
        }
        // Plataforma del fondo y tótem inexplicable.
        b.box(0f,.10f,-32f,3.3f,.24f,2.5f,pink)
        b.box(0f,1.0f,-32f,.65f,.85f,.65f,wall)
        b.ellipsoid(0f,2.55f,-32f,1.03f,1.03f,1.03f,turquoise,12,16)
        // Portal de retorno, detrás de la aparición.
        b.box(0f,1.70f,7.6f,1.05f,1.70f,.12f,rgb(.30f,.27f,.57f),2f)
        for (xx in floatArrayOf(-1.18f,1.18f)) b.box(xx,1.78f,7.55f,.12f,1.79f,.22f,ivory)
        b.box(0f,3.62f,7.55f,1.28f,.12f,.22f,ivory)
        // 1.0: pasaje desde las piscinas originales hacia el acuario de las horas.
        val newPortalColor=rgb(.54f,.83f,.91f)
        b.box(10f,1.74f,6.5f,1.07f,1.73f,.09f,newPortalColor,3f)
        for (xx in floatArrayOf(8.75f,11.25f))
            b.box(xx,1.85f,6.5f,.15f,1.88f,.20f,ivory,5f)
        b.box(10f,3.75f,6.5f,1.36f,.16f,.20f,pink,11f)
        // Banco de descanso junto al regreso.
        b.box(-4f,.72f,4.8f,1.55f,.15f,.45f,pink)
        b.box(-4f,1.2f,5.2f,1.55f,.45f,.13f,pink)
        for (xx in floatArrayOf(-5.2f,-2.8f)) b.box(xx,.35f,4.8f,.11f,.34f,.12f,wall)
        return b.finish()
    }



    // ===============================================
    // MUNDO 03: DESCENSO AL ABISMO
    // ===============================================
    // La parte alta todavía recuerda un pueblo imposible,
    // pero al bajar el lugar se vuelve menos reconocible.
    const val CRATER_CENTER_Z = -102f
    const val CRATER_RIM_RADIUS = 92f
    const val CRATER_DEPTH = 180f
    const val CRATER_WATER_Y = -178.8f
    const val CRATER_ISLAND_Y = -176f
    const val CRATER_HOUSE_FRONT_Z = -97.3f
    private const val CRATER_Z = CRATER_CENTER_Z
    private const val RIM_RADIUS = CRATER_RIM_RADIUS
    private const val CRATER_WADING_Y = CRATER_WATER_Y - .55f

    private fun smooth01(t: Float): Float {
        val a = t.coerceIn(0f, 1f)
        return a * a * (3f - 2f * a)
    }

    /** Original terrain height: the cliff stays exactly vertical, never a ramp. */
    fun craterHeight(x: Float, z: Float): Float {
        val r = hypot(x, z - CRATER_Z)
        if (r < RIM_RADIUS) return -CRATER_DEPTH
        val blend = smooth01((r - RIM_RADIUS) / 12f)
        return (.55f*sin(x*.048f)*cos(z*.060f) + .22f*sin((x-z)*.032f))*blend
    }

    /** Feet height for gravity: rim, shallow wading, then a continuous island shore. */
    fun craterWalkSurface(x: Float, z: Float): Float {
        val r = hypot(x, z - CRATER_Z)
        if (r >= RIM_RADIUS) return craterHeight(x,z)
        if (r <= 12f) return CRATER_ISLAND_Y
        val shore = smooth01((r - 12f) / 7f)
        return CRATER_ISLAND_Y + (CRATER_WADING_Y - CRATER_ISLAND_Y) * shore
    }

    // Narrow grass rings are real ledges: choose the nearest one below the feet.
    fun craterShelfSurface(x:Float,z:Float,feetY:Float):Float? {
        val r=hypot(x,z-CRATER_Z)
        if(r < RIM_RADIUS-1.0f || r >= RIM_RADIUS)return null
        val level=kotlin.math.ceil((-feetY-.06f)/7.5f).toInt().coerceAtLeast(1)
        if(level>23)return null
        return -level*7.5f
    }

    fun craterIsWater(x: Float, z: Float): Boolean =
        hypot(x,z-CRATER_Z) < RIM_RADIUS && craterWalkSurface(x,z) < CRATER_WATER_Y - .02f

    /** Hollow central house; its +Z facade has a genuinely open 2.6 m doorway. */
    fun craterBodyBlocked(x: Float, z: Float, feetY: Float): Boolean {
        if (feetY >= CRATER_ISLAND_Y + 5.0f || feetY + 1.7f < CRATER_ISLAND_Y) return false
        val dz = z - CRATER_Z
        val ax = kotlin.math.abs(x)
        if (ax in 3.29f..4.31f && kotlin.math.abs(dz) < 5.05f) return true
        if (dz in -5.05f..-4.15f && ax < 4.31f) return true
        if (dz in 4.15f..5.05f && ax in 1.01f..4.31f) return true
        return false
    }

    /** Sloped roof support, tested only while descending; never teleport feet up. */
    fun craterRoofSurface(x: Float, z: Float): Float? {
        val ax=kotlin.math.abs(x)
        if(ax > 4.2f || kotlin.math.abs(z-CRATER_Z) > 5.08f) return null
        return CRATER_ISLAND_Y + 5.1f + 3f * (1f-ax/4.2f)
    }

    /** Approach from the shore, or walk into the open doorway, to return home. */
    fun craterReturnDoor(x: Float, z: Float, feetY: Float): Boolean =
        kotlin.math.abs(x) < 1.04f && kotlin.math.abs(z - CRATER_HOUSE_FRONT_Z) < 1.65f &&
        feetY in (CRATER_ISLAND_Y - .5f)..(CRATER_ISLAND_Y + 2.2f)

    private fun littleHouse(b: Builder, x: Float, y: Float, z: Float, scale: Float, style: Int,
                            glitch: Boolean = false) {
        val white = rgb(.95f,.96f,.98f)
        val wallKind = if (glitch) 23f else 5f
        val roofKind = if (glitch) 23f else 6f
        val edges = rgb(.79f,.84f,.86f)
        val roof = if (style % 3 == 0) rgb(.79f,.82f,.88f)
                   else if (style % 3 == 1) rgb(.82f,.83f,.87f)
                   else rgb(.75f,.81f,.90f)
        val dark = rgb(.58f,.74f,.84f)
        val sx = 1.75f * scale
        val sz = 1.47f * scale
        // Planta principal de la casa: materiales reales del atlas.
        b.box(x, y + 1.34f*scale, z, sx, 1.34f*scale, sz, white,wallKind)
        b.box(x, y + .10f*scale, z, sx*1.07f, .14f*scale, sz*1.07f, edges,15f)
        // Frontón triangular y techo a dos aguas (textura de tejas).
        val eave = y + 2.68f * scale
        val ridge = y + 4.12f * scale
        val left = x - sx*1.16f
        val right = x + sx*1.16f
        val front = z + sz*1.12f
        val back = z - sz*1.12f
        fun v(px: Float, py: Float, pz: Float) = floatArrayOf(px,py,pz)
        b.triangle(v(x-sx,eave,front),v(x+sx,eave,front),v(x,ridge,front),white,wallKind)
        b.triangle(v(x+sx,eave,back),v(x-sx,eave,back),v(x,ridge,back),white,wallKind)
        b.quad(v(left,eave,front),v(x,ridge,front),v(x,ridge,back),v(left,eave,back),roof,roofKind)
        b.quad(v(x,ridge,front),v(right,eave,front),v(right,eave,back),v(x,ridge,back),roof,roofKind)
        // Marcos de las ventanas y puertas.
        for (side in floatArrayOf(-.78f,.78f)) {
            b.box(x+side*scale, y+1.72f*scale, z+sz+.039f,
                .36f*scale,.53f*scale,.05f*scale,edges,15f)
            b.box(x+side*scale, y+1.72f*scale,z+sz+.094f,
                .27f*scale,.44f*scale,.045f*scale,dark,13f)
            b.box(x+side*scale,y+1.72f*scale,z+sz+.141f,
                .028f*scale,.45f*scale,.02f*scale,white,15f)
        }
        b.box(x,y+.83f*scale,z+sz+.071f,.40f*scale,.80f*scale,.07f*scale,roof,9f)
        b.box(x+.21f*scale,y+.86f*scale,z+sz+.148f,.035f*scale,.035f*scale,.018f*scale,white)
        // Una ventana lateral visible al mirar desde los costados del cráter.
        b.box(x+sx+.032f,y+1.65f*scale,z,.04f*scale,.39f*scale,.34f*scale,dark,13f)
        b.box(x-sx-.032f,y+1.65f*scale,z,.04f*scale,.39f*scale,.34f*scale,dark,13f)
        // Chimenea repetida sólo en algunas casas.
        if(style % 4 == 0) {
            b.box(x+sx*.52f,y+3.33f*scale,z-sz*.35f,.23f*scale,.71f*scale,.23f*scale,edges,7f)
        }
    }

    fun createCrater(): Scene {
        val b = Builder()
        val grass = rgb(.38f,.62f,.29f)
        val ivory = rgb(.91f,.90f,.83f)
        val rose = rgb(.85f,.63f,.73f)
        val lavender = rgb(.64f,.62f,.77f)
        val path = rgb(.81f,.79f,.72f)
        val chalk = rgb(.76f,.82f,.78f)
        val water = rgb(.31f,.60f,.66f)
        val stone = rgb(.38f,.47f,.53f)
        val warmLight = rgb(1f,.86f,.64f)
        val sectors = 128
        fun point(r: Float, angle: Float, y: Float) =
            floatArrayOf(r*cos(angle),y,CRATER_Z+r*sin(angle))
        fun top(r: Float, angle: Float, lift: Float = .0f): FloatArray {
            val x=r*cos(angle); val z=CRATER_Z+r*sin(angle)
            return point(r,angle,craterHeight(x,z)+lift)
        }

        // A low tessellation annulus leaves the opening unobstructed. The old map
        // spent 14,592 triangles on grass alone; these 2,560 cover the same area.
        for (i in 0 until sectors) {
            val a=(2.0*PI*i/sectors).toFloat()
            val n=(2.0*PI*(i+1)/sectors).toFloat()
            for (j in 0 until 10) {
                val r0=RIM_RADIUS+j*16f
                val r1=r0+16f
                val t=.012f*sin(i*.61f+j*.83f)
                val color=rgb(grass[0]+t,grass[1]+t*.8f,grass[2]+t)
                b.quad(top(r0,a),top(r0,n),top(r1,n),top(r1,a),color,4f)
            }
            // Green vertical strata and narrow concentric grass terraces,
            // inspired by the user's reference, with no sloping bowl profile.
            for (level in 0 until 24) {
                val y0=-level*7.5f
                val y1=-(level+1)*7.5f
                val depth=level/23f
                val face=rgb(.36f-.10f*depth,.48f-.12f*depth,.22f-.05f*depth)
                b.quad(point(RIM_RADIUS,a,y0),point(RIM_RADIUS,a,y1),
                    point(RIM_RADIUS,n,y1),point(RIM_RADIUS,n,y0),face,27f)
                if(level>0) {
                    b.quad(point(RIM_RADIUS-1.0f,a,y0),point(RIM_RADIUS-1.0f,n,y0),
                        point(RIM_RADIUS,n,y0),point(RIM_RADIUS,a,y0),
                        rgb(.53f-.13f*depth,.68f-.15f*depth,.29f-.06f*depth),4f)
                    // A soil lip gives each ring a legible edge without thin-line alias.
                    b.quad(point(RIM_RADIUS-1.0f,a,y0),point(RIM_RADIUS-1.0f,a,y0-.36f),
                        point(RIM_RADIUS-1.0f,n,y0-.36f),point(RIM_RADIUS-1.0f,n,y0),face,27f)
                }
            }
            b.triangle(floatArrayOf(0f,-CRATER_DEPTH,CRATER_Z),
                point(RIM_RADIUS,n,-CRATER_DEPTH),point(RIM_RADIUS,a,-CRATER_DEPTH),stone,14f)
            // Water stops at the island instead of cutting through its dry top.
            b.quad(point(14f,a,CRATER_WATER_Y),point(14f,n,CRATER_WATER_Y),
                point(RIM_RADIUS-.07f,n,CRATER_WATER_Y),point(RIM_RADIUS-.07f,a,CRATER_WATER_Y),water,1f)
        }

        // Circular walking trail: a calm ivory line makes the immense drop legible.
        for(i in 0 until sectors) {
            val a=(2.0*PI*i/sectors).toFloat()
            val n=(2.0*PI*(i+1)/sectors).toFloat()
            b.quad(top(97f,a,.035f),top(97f,n,.035f),top(100.3f,n,.035f),
                top(100.3f,a,.035f),path,8f)
            // A low border intentionally leaves the player free to step and jump off.
            b.quad(top(92.12f,a,.045f),top(92.12f,n,.045f),
                top(92.65f,n,.045f),top(92.65f,a,.045f),grass,4f)
        }
        // Arrival avenue and the original return door stay in their known positions.
        b.box(0f,.03f,12f,1.6f,.03f,20f,path,8f)
        b.box(-7.8f,.045f,24f,2.6f,.035f,3.4f,ivory,15f)
        for(k in 0 until 9) {
            val z=8f-k*1.8f
            b.box(0f,.075f,z,1.42f,.012f,.08f,ivory,15f)
        }

        // Low cost opaque gradient patches provide stable soft contact shadows.
        // Sample terrain for every vertex so patches follow hills without floating.
        fun groundShadow(cx:Float,cz:Float,sx:Float,sz:Float,onIsland:Boolean=false) {
            val rings=4
            val count=16
            fun p(r:Float,a:Float):FloatArray {
                var x=cx+sx*r*cos(a);var z=cz+sz*r*sin(a)
                if(!onIsland) {
                    val distance=hypot(x,z-CRATER_Z)
                    if(distance < RIM_RADIUS+.3f) {
                        val stretch=(RIM_RADIUS+.3f)/distance
                        x*=stretch;z=CRATER_Z+(z-CRATER_Z)*stretch
                    }
                }
                val y=if(onIsland) craterWalkSurface(x,z) else craterHeight(x,z)
                return floatArrayOf(x,y+.018f,z)
            }
            val base=if(onIsland) chalk else grass
            for(j in 0 until rings) {
                val inside=j/rings.toFloat()
                val outside=(j+1f)/rings
                val shade=.70f+.30f*((j+.5f)/rings)
                val color=rgb(base[0]*shade,base[1]*shade,base[2]*shade)
                for(k in 0 until count) {
                    val a=(2.0*PI*k/count).toFloat()
                    val n=(2.0*PI*(k+1)/count).toFloat()
                    if(j==0) b.triangle(p(0f,a),p(outside,n),p(outside,a),color)
                    else b.quad(p(inside,a),p(inside,n),p(outside,n),p(outside,a),color)
                }
            }
        }

        // Homes have several scales and gaps; no billboards or downloaded models.
        // Shadows extend opposite the pale sun, not straight under each roof.
        for(i in 0 until 30) {
            val angle=(2.0*PI*(i+.17)/30.0).toFloat()
            val radius=112f+11f*sin(i*1.73f)+((i%3)-1)*4f
            val hx=radius*cos(angle);val hz=CRATER_Z+radius*sin(angle)
            if(hz>1f && kotlin.math.abs(hx)<29f) continue
            val scale=1.42f+(i%4)*.17f
            groundShadow(hx+scale*1.6f,hz+scale,3.7f*scale,2.9f*scale)
            littleHouse(b,hx,craterHeight(hx,hz),hz,scale,i,i%11==7)
            if(i%4==0) {
                b.box(hx-3.3f*scale,craterHeight(hx,hz)+.31f,hz,
                    .55f*scale,.31f,.55f*scale,ivory,15f)
                b.ellipsoid(hx-3.3f*scale,craterHeight(hx,hz)+1.3f,hz,
                    .76f,.9f,.76f,rgb(.67f,.76f,.66f),6,8,12f)
            }
        }

        // Architectural islands seem torn out of the village and pinned to the
        // vertical walls. Shadow strips, floating steps and broken bands sell depth.
        for(i in 0 until 24) {
            val angle=(2.0*PI*(i+.27)/24.0).toFloat()
            val radius=89.5f-(i%3)*2.7f
            val hx=radius*cos(angle);val hz=CRATER_Z+radius*sin(angle)
            val base=-13f-(i%8)*20.4f
            val scale=1.22f+(i%3)*.22f
            b.box(hx,base-.22f,hz,3.2f*scale,.22f,2.8f*scale,chalk,15f)
            b.box(hx+.45f,base+.014f,hz+.4f,2.1f*scale,.008f,1.75f*scale,
                rgb(.56f,.63f,.66f))
            littleHouse(b,hx,base,hz,scale,i,i%5==2)
            // A few complete houses and detached slices flicker together.
            for(slice in 0 until 3) {
                val tint=if(slice%2==0) rgb(.83f,.59f,.75f) else rgb(.47f,.80f,.83f)
                b.box(hx+2.5f+slice*.55f,base+1f+slice*.97f,hz+.3f,
                    1.65f,.10f,1.2f,tint,23f)
            }
            if(i%5==0) {
                b.box(hx,base+1.45f,hz+1.6f*scale,.3f,.42f,.06f,warmLight,24f)
            }
        }

        // The island has a real continuous shore; no teleport or lethal landing.
        // Geometry follows the same function used by player gravity/collision.
        val islandSegments=64
        for(i in 0 until islandSegments) {
            val a=(2.0*PI*i/islandSegments).toFloat()
            val n=(2.0*PI*(i+1)/islandSegments).toFloat()
            for(j in 0 until 7) {
                val r0=j*3f;val r1=(j+1)*3f
                fun shore(r:Float,t:Float):FloatArray {
                    val x=r*cos(t);val z=CRATER_Z+r*sin(t)
                    return floatArrayOf(x,craterWalkSurface(x,z),z)
                }
                val c=if(j<4) chalk else rgb(.62f,.73f,.69f)
                if(j==0) b.triangle(shore(0f,a),shore(r1,n),shore(r1,a),c,15f)
                else b.quad(shore(r0,a),shore(r0,n),shore(r1,n),shore(r1,a),c,15f)
            }
        }
        groundShadow(1.7f,CRATER_Z+1.5f,6.3f,7.1f,true)
        // Inlaid walkway plus rings are visible from the rim and lead from shore.
        b.box(0f,CRATER_ISLAND_Y+.025f,CRATER_Z+8.3f,1.45f,.018f,3.65f,ivory,10f)
        for(i in 0 until 16) {
            val a=(2.0*PI*i/16).toFloat()
            val x=10.2f*cos(a);val z=CRATER_Z+10.2f*sin(a)
            b.box(x,CRATER_ISLAND_Y+.055f,z,.30f,.05f,.30f,ivory,15f)
            if(i%2==0) {
                b.box(x,CRATER_ISLAND_Y+.52f,z,.07f,.48f,.07f,lavender,15f)
                b.ellipsoid(x,CRATER_ISLAND_Y+1.08f,z,.23f,.25f,.23f,warmLight,6,8,24f)
            }
        }

        // Central house has walls, a floor and a real doorway, not a solid box.
        val base=CRATER_ISLAND_Y
        val front=CRATER_HOUSE_FRONT_Z
        val back=CRATER_Z-4.7f
        b.box(-3.8f,base+2.55f,CRATER_Z,.18f,2.55f,4.7f,ivory,5f)
        b.box(3.8f,base+2.55f,CRATER_Z,.18f,2.55f,4.7f,ivory,5f)
        b.box(0f,base+2.55f,back,3.8f,2.55f,.18f,ivory,5f)
        b.box(-2.55f,base+2.55f,front,1.25f,2.55f,.18f,ivory,5f)
        b.box(2.55f,base+2.55f,front,1.25f,2.55f,.18f,ivory,5f)
        b.box(0f,base+4.4f,front,1.3f,.7f,.18f,ivory,5f)
        b.box(0f,base-.035f,CRATER_Z,3.8f,.035f,4.7f,ivory,15f)
        val eave=base+5.1f
        val ridge=base+8.1f
        fun v(x:Float,y:Float,z:Float)=floatArrayOf(x,y,z)
        b.triangle(v(-3.8f,eave,front),v(3.8f,eave,front),v(0f,ridge,front),ivory,5f)
        b.triangle(v(3.8f,eave,back),v(-3.8f,eave,back),v(0f,ridge,back),ivory,5f)
        b.quad(v(-4.2f,eave,front+.38f),v(0f,ridge,front+.38f),
            v(0f,ridge,back-.38f),v(-4.2f,eave,back-.38f),rose,6f)
        b.quad(v(0f,ridge,front+.38f),v(4.2f,eave,front+.38f),
            v(4.2f,eave,back-.38f),v(0f,ridge,back-.38f),rose,6f)
        // Door glows from inside its opening. It is the safe exit from the basin.
        b.box(0f,base+1.68f,front-.22f,1.20f,1.68f,.06f,warmLight,24f)
        for(x in floatArrayOf(-1.35f,1.35f)) {
            b.box(x,base+1.75f,front+.04f,.10f,1.75f,.16f,lavender,15f)
        }
        b.box(0f,base+3.55f,front+.04f,1.45f,.11f,.16f,lavender,15f)
        b.box(0f,base+.04f,front+1.12f,1.4f,.04f,1.2f,rgb(.92f,.86f,.72f),24f)
        for(x in floatArrayOf(-2.55f,2.55f)) {
            b.box(x,base+2.5f,front+.21f,.62f,.84f,.05f,lavender,15f)
            b.box(x,base+2.5f,front+.27f,.49f,.69f,.03f,rgb(.69f,.85f,.86f),24f)
            b.box(x,base+2.5f,front+.31f,.045f,.71f,.025f,ivory,15f)
        }
        b.box(1.8f,base+6.8f,CRATER_Z-2.1f,.42f,1.5f,.46f,lavender,7f)
        // Large warm attic window provides a readable landmark through the mist.
        b.ellipsoid(0f,base+6.25f,front+.11f,.53f,.64f,.075f,warmLight,8,12,24f)

        // Existing rim interior and station portal remain reachable without falling.
        val houseX=-24f;val houseZ=10f
        val houseGround=craterHeight(houseX,houseZ)
        groundShadow(houseX+1.5f,houseZ+1f,4.2f,3.4f)
        littleHouse(b,houseX,houseGround,houseZ,1.15f,3)
        b.box(houseX,houseGround+1.6f,houseZ+1.76f,.88f,1.45f,.07f,rgb(.63f,.75f,.80f),22f)
        b.box(houseX,houseGround+.04f,houseZ+3.5f,1.35f,.035f,2.1f,ivory,10f)
        b.box(-7.2f,1.8f,24f,1f,1.75f,.08f,rgb(.40f,.76f,.78f),3f)
        for(px in floatArrayOf(-8.33f,-6.07f)) b.box(px,1.87f,24f,.13f,1.88f,.19f,ivory)
        b.box(-7.2f,3.70f,24f,1.26f,.14f,.19f,ivory)
        // Lamp avenue points to the edge; the unguarded center remains traversable.
        for(k in 0..6) {
            val x=-24f+k*8f;val z=6.5f
            val y=craterHeight(x,z)
            b.box(x,y+1.58f,z,.09f,1.54f,.09f,lavender,15f)
            b.ellipsoid(x,y+3.26f,z,.34f,.31f,.34f,warmLight,7,10,24f)
        }
        // Distant, monumental clouds soften the rim and frame the upright walls.
        for(i in 0 until 7) {
            val a=(2.0*PI*(i+.3)/7.0).toFloat()
            val x=172f*cos(a);val z=CRATER_Z+172f*sin(a)
            b.ellipsoid(x,35f+(i%3)*8f,z,25f+(i%2)*12f,8f,22f,
                if(i%2==0) rgb(.91f,.87f,.94f) else rgb(.91f,.94f,.95f),7,12,20f)
        }
        return b.finish()
    }


    // ===============================================================
    // MUNDO 04: EL JARDIN DE LA LLUVIA DETENIDA
    // ===============================================================
    // Cuarto mundo original: mar sin horizonte, paseos de porcelana,
    // arcos desproporcionados, jardines floridos y un edificio silencioso.
    // La geometria es estatica para funcionar en OpenGL ES 2.0.
    fun createGarden(): Scene {
        val b = Builder()
        val ivory = rgb(.98f,.93f,.87f)
        val marble = rgb(.82f,.89f,.89f)
        val rose = rgb(.94f,.72f,.78f)
        val peach = rgb(.97f,.81f,.66f)
        val lavender = rgb(.73f,.70f,.90f)
        val aqua = rgb(.48f,.77f,.79f)
        val paleAqua = rgb(.77f,.91f,.84f)
        val velvet = rgb(.31f,.38f,.49f)
        val brass = rgb(.97f,.87f,.60f)
        val white = rgb(.98f,.97f,.94f)
        val blueWater = rgb(.53f,.73f,.81f)

        // La inmensidad es una ilusion controlada: el mar llega a la niebla.
        b.box(0f,-1.10f,-95f,182f,.08f,230f,blueWater,1f)

        // Plataforma de llegada y avenida principal.
        b.box(0f,-.20f,18f,12f,.20f,18f,ivory,10f)
        b.box(0f,-.19f,-78f,4.3f,.19f,81f,ivory,10f)
        for (xx in floatArrayOf(-4.40f,4.40f)) {
            b.box(xx,-.08f,-78f,.19f,.16f,81f,brass,6f)
        }
        for (i in 0..51) {
            val z=0f-i*3.05f
            b.box(0f,.018f,z,4.02f,.012f,.045f,marble,11f)
        }
        // Una plaza octogonal simulada por plataformas rectangulares.
        b.box(0f,-.19f,-73f,15.0f,.19f,15.0f,ivory,10f)
        b.box(0f,-.16f,-73f,12.7f,.13f,12.7f,rgb(.93f,.91f,.88f),10f)
        for (xx in floatArrayOf(-13.8f,13.8f))
            b.box(xx,.03f,-73f,.24f,.24f,13.4f,marble,11f)
        for (zz in floatArrayOf(-86.8f,-59.2f))
            b.box(0f,.03f,zz,13.4f,.24f,.24f,marble,11f)

        // Puentes para visitar los dos jardines laterales.
        for (side in floatArrayOf(-1f,1f)) {
            b.box(side*25f,-.18f,-73f,12.1f,.18f,3.15f,ivory,10f)
            b.box(side*36f,-.19f,-73f,12.0f,.19f,12f,ivory,10f)
            for (zz in floatArrayOf(-76.28f,-69.72f))
                b.box(side*25f,.17f,zz,12.0f,.27f,.13f,brass,6f)
            // Jardin cubierto de flores circulares en el suelo.
            for (i in 0..11) {
                val angle=(2.0*PI*i/12.0).toFloat()
                val r=if(i%3==0)7.7f else 5.7f
                val px=side*36f + r*cos(angle)
                val pz=-73f+r*sin(angle)
                b.ellipsoid(px,.12f,pz,.7f,.13f,.65f,if(i%2==0)rose else peach,5,8)
                b.ellipsoid(px,.18f,pz,.24f,.17f,.24f,brass,5,8)
            }
            // Arboles color menta: las hojas parecen nubes bajas.
            for (i in 0..3) {
                val px=side*(32f+((i%2)*7f))
                val pz=-79f+((i/2)*12f)
                b.box(px,2.0f,pz,.22f,2.0f,.22f,marble,15f)
                b.ellipsoid(px,4.35f,pz,1.6f,1.26f,1.55f,paleAqua,8,11,12f)
            }
            // Banco de porcelana al lado del agua.
            b.box(side*36f,.51f,-73f,1.5f,.12f,.47f,rose,5f)
        }

        // Tramo derecho secreto, conectado con una pequena plataforma solitaria.
        b.box(16.35f,-.18f,-126f,12.70f,.18f,2.45f,ivory,10f)
        b.box(29f,-.19f,-126f,9.1f,.19f,9.1f,ivory,10f)
        b.box(29f,.09f,-126f,4.5f,.18f,4.5f,lavender,13f)
        // Un marco sin puerta, sin pared y sin explicacion.
        for (xx in floatArrayOf(26.8f,31.2f))
            b.box(xx,3.0f,-126f,.33f,3f,.33f,white,5f)
        b.box(29f,6.05f,-126f,2.50f,.30f,.35f,white,5f)
        b.box(29f,1.95f,-126f,.81f,1.78f,.10f,velvet,2f)

        // Gran casa a la que conduce la avenida, con espacio para explorar.
        b.box(0f,-.2f,-178f,18.0f,.2f,19.0f,ivory,10f)
        b.box(0f,.025f,-178f,12f,.05f,13.5f,marble,11f)
        // El edificio no esta cerrado: la entrada es amplia y accesible.
        for (xx in floatArrayOf(-7.0f,7.0f))
            b.box(xx,5.2f,-178f,.45f,5.1f,11.7f,ivory,5f)
        b.box(0f,9.95f,-178f,8.0f,.26f,12.0f,peach,13f)
        b.box(0f,10.5f,-178f,6.0f,.34f,10.3f,lavender,13f)
        b.box(0f,5.0f,-190.5f,7.1f,5.0f,.35f,marble,11f)
        for (xx in floatArrayOf(-5.2f,5.2f))
            b.box(xx,4.8f,-165.7f,1.8f,4.6f,.35f,marble,11f)
        b.box(0f,9.3f,-165.7f,7.15f,.44f,.36f,marble,11f)
        // Gran ventana oscura en la pared del fondo.
        b.box(0f,5.2f,-190.05f,2.5f,3.0f,.14f,velvet,15f)
        for (yy in floatArrayOf(2.1f,8.2f))
            b.box(0f,yy,-189.85f,2.8f,.10f,.08f,brass,6f)
        for (xx in floatArrayOf(-2.7f,2.7f))
            b.box(xx,5.2f,-189.85f,.10f,3.13f,.08f,brass,6f)
        // Techo / escalones invertidos suspendidos a lo alto.
        for (i in 0..6) {
            val k=i.toFloat()
            b.box(0f,13.5f+k*.80f,-178f+k*.9f,6.5f-k*.55f,.25f,5.6f-k*.42f,peach,13f)
        }
        b.ellipsoid(0f,22.0f,-173f,2.25f,2.25f,2.25f,brass,10,14)

        // Arcos enormes a lo largo de la avenida. Su ritmo se vuelve irreal.
        for (i in 0..10) {
            val z=-16f-i*13f
            val height=6.6f + (i%3)*1.45f
            val tone=if(i%3==0) ivory else if(i%3==1) marble else rose
            for (xx in floatArrayOf(-5.4f,5.4f)) {
                b.box(xx,height*.5f,z,.38f,height*.5f,.50f,tone,5f)
                b.box(xx,.33f,z,.65f,.33f,.70f,brass,6f)
            }
            b.box(0f,height,z,5.85f,.41f,.59f,tone,5f)
            b.ellipsoid(0f,height+.55f,z,.54f,.55f,.54f,brass,7,9)
            // Cada ciertos arcos flota una ventana sin muro.
            if(i%3==2) {
                for (xx in floatArrayOf(-8.5f,8.5f))
                    b.box(xx,13.3f,z,.15f,2.50f,.20f,white,5f)
                b.box(0f,15.8f,z,8.65f,.15f,.2f,white,5f)
            }
        }

        // Columnas distantes medio hundidas en el agua: mundo "sin fin".
        for (i in 0 until 28) {
            val t=(2.0 * PI * i / 28.0).toFloat()
            val radius=105f + (i%4)*13f
            val px=radius*cos(t)
            val pz=-90f+radius*sin(t)
            val h=8f +(i%5)*3.5f
            b.box(px,h*.5f-.7f,pz,.55f,h*.5f,.55f,marble,11f)
            if(i%3==0)
                b.ellipsoid(px,h,pz,1.45f,.60f,1.45f,peach,7,10)
        }

        // Escaleras imposibles flotantes sobre el mar, puramente escenograficas.
        for (i in 0..11) {
            val k=i.toFloat()
            b.box(-26f+k*1.25f,7.8f+k*.74f,-122f,.86f,.26f,2.25f,
                if(i%2==0) lavender else ivory,13f)
        }
        for (i in 0..9) {
            val k=i.toFloat()
            b.box(36f,12.1f+k*.7f,-38f-k*2.5f,2.8f,.27f,.85f,
                if(i%2==0) peach else marble,13f)
        }

        // Esferas luminosas flotantes; su repeticion produce una falsa perspectiva.
        for (i in 0..23) {
            val side=if(i%2==0) -1f else 1f
            val z=-6f-i*6.25f
            val y=13f + (i%4)*1.75f
            b.ellipsoid(side*(9f+(i%3)*2.6f),y,z,.58f,.55f,.58f,
                if(i%3==0) peach else white,7,9)
        }

        // Puerta de regreso en la plaza de llegada.
        b.box(-8f,1.72f,22f,1.05f,1.72f,.08f,rgb(.69f,.60f,.82f),2f)
        for (xx in floatArrayOf(-9.2f,-6.8f))
            b.box(xx,1.83f,22f,.17f,1.86f,.22f,brass,6f)
        b.box(-8f,3.75f,22f,1.37f,.17f,.25f,brass,6f)
        b.box(-8f,4.20f,22f,1.20f,.18f,.16f,white,5f)
        return b.finish()
    }

    private fun inGardenRect(x: Float, z: Float, cx: Float, cz: Float, rx: Float, rz: Float): Boolean {
        return kotlin.math.abs(x-cx) <= rx && kotlin.math.abs(z-cz) <= rz
    }

    // Colisiones sencillas sobre las losas, para no caminar "sobre el agua".
    fun canWalkGarden(x: Float, z: Float): Boolean {
        val walkway = inGardenRect(x,z,0f,-78f,4.1f,81f)
        val spawn = inGardenRect(x,z,0f,18f,11.85f,17.8f)
        val plaza = inGardenRect(x,z,0f,-73f,14.7f,14.7f)
        val sideBridge = inGardenRect(x,z,-25f,-73f,12.0f,3.0f) ||
                         inGardenRect(x,z,25f,-73f,12.0f,3.0f)
        val sideGardens = inGardenRect(x,z,-36f,-73f,11.8f,11.8f) ||
                          inGardenRect(x,z,36f,-73f,11.8f,11.8f)
        val secretBridge = inGardenRect(x,z,16.35f,-126f,12.55f,2.30f)
        val secretPlaza = inGardenRect(x,z,29f,-126f,8.9f,8.9f)
        val museum = inGardenRect(x,z,0f,-178f,17.7f,19.0f)
        val hasFloor = walkway || spawn || plaza || sideBridge || sideGardens ||
                       secretBridge || secretPlaza || museum
        if (!hasFloor) return false
        // Colisiones en paredes interiores del pabellon. La entrada queda libre.
        if (inGardenRect(x,z,-7f,-178f,.74f,11.8f)) return false
        if (inGardenRect(x,z,7f,-178f,.74f,11.8f)) return false
        if (inGardenRect(x,z,0f,-190.5f,7.2f,.58f)) return false
        // El portal no es una pared atravesable.
        if (hitsRect(x,z,-9.08f,-6.92f,21.84f,22.16f)) return false
        return true
    }


    // =============================================================
    // MUNDO 05: LA PRADERA DEL ÚLTIMO SOL
    // Horizonte azul imposible, una casa lejana, cercas blancas,
    // barrio silencioso, aparcamiento rosado y ruta al mundo 06.
    // =============================================================
    fun meadowHeight(x: Float, z: Float): Float {
        val d = kotlin.math.abs(x)
        val pathBlend = smooth01((d-7f)/8f)
        return (0.58f * sin(x*.040f) * cos(z*.045f) +
                0.22f * sin(z*.13f)) * pathBlend
    }

    private fun villageHouse(b: Builder, x: Float, z: Float, blue: Boolean, size: Float) {
        val y=meadowHeight(x,z)
        val cream=if(blue) rgb(.76f,.90f,.98f) else rgb(.96f,.94f,.89f)
        val roof=if(blue) rgb(.64f,.66f,.82f) else rgb(.82f,.36f,.30f)
        val black=rgb(.27f,.29f,.38f)
        b.box(x,y+size*2.05f,z,4.2f*size,2.05f*size,3.0f*size,cream,5f)
        // Roof ridge, deliberately large and asymmetrical.
        b.box(x,y+size*4.15f,z,4.50f*size,.30f*size,3.30f*size,roof,13f)
        b.box(x,y+size*4.55f,z,3.14f*size,.25f*size,3.29f*size,roof,13f)
        b.box(x,y+size*4.89f,z,1.65f*size,.20f*size,3.22f*size,roof,13f)
        b.box(x,y+size*5.18f,z,.45f*size,.15f*size,3.1f*size,roof,13f)
        for(dx in floatArrayOf(-2.15f,2.15f))
            b.box(x+dx*size,y+2.50f*size,z+3.07f*size,.52f*size,.74f*size,.10f*size,black,21f)
        b.box(x,y+1.13f*size,z+3.10f*size,.64f*size,1.05f*size,.12f*size,black,21f)
    }

    fun createMeadow(): Scene {
        val b=Builder()
        val grassy=rgb(.45f,.78f,.46f)
        val sage=rgb(.55f,.83f,.54f)
        val pavement=rgb(.69f,.72f,.73f)
        val white=rgb(.98f,.96f,.92f)
        val rose=rgb(.99f,.70f,.79f)
        val gold=rgb(.98f,.83f,.62f)
        val aqua=rgb(.48f,.80f,.81f)
        val dark=rgb(.34f,.42f,.52f)
        // Grass to the blue horizon; coarse tiles are an intentional mobile optimization.
        val step=5f
        var x=-210f
        while (x<210f) {
            var z=-235f
            while (z<180f) {
                val wave=(.5f+.5f*sin(x*.28f+z*.32f))*.15f
                val color=rgb(grassy[0]+wave*.35f,grassy[1]-wave*.12f,grassy[2]+wave*.25f)
                fun pt(px:Float,pz:Float)=floatArrayOf(px,meadowHeight(px,pz)-.12f,pz)
                b.quad(pt(x,z),pt(x,z+step),pt(x+step,z+step),pt(x+step,z),color,4f)
                z+=step
            }
            x+=step
        }
        // Slender immaculate path to a very distant house.
        for (i in 0..84) {
            val z=24f-i*2.32f
            b.box(0f,.015f,z,2.1f,.045f,1.18f,if(i%7==0) white else rgb(.89f,.87f,.78f),10f)
        }
        for (side in floatArrayOf(-1f,1f)) {
            // White fence grows across the fields, eventually swallowed by fog.
            for (i in 0..65) {
                val z=20f-i*3.0f
                val hx=side*(15f+2.0f*sin(i*.14f))
                b.box(hx,meadowHeight(hx,z)+.71f,z,.105f,.71f,.105f,white,5f)
                b.box(hx,meadowHeight(hx,z)+.75f,z-1.4f,.11f,.08f,1.5f,white,5f)
            }
        }
        // A lonely red-roofed home at the end of the road; walk around it.
        villageHouse(b,0f,-137f,false,1.55f)
        b.box(0f,.06f,-137f,6.80f,.07f,5.7f,rgb(.93f,.87f,.80f),10f)
        for (i in 0 until 20) {
            val z=-48f-i*5f
            val sx=if(i%2==0) -1f else 1f
            val hx=sx*(33f+(i%4)*3.9f)
            if(i%3==0) villageHouse(b,hx,z,i%4==0,.56f+(i%4)*.11f)
        }
        // Glowing quiet pink parking lot and a service station by impossible clouds.
        b.box(62f,-.17f,-72f,32f,.17f,27f,pavement,14f)
        b.box(62f,-.12f,-72f,30f,.07f,25f,rgb(.76f,.72f,.79f),14f)
        b.box(31f,.09f,-72f,30f,.11f,3.15f,white,10f) // link from main path
        for (i in 0..12) {
            val z=-94f+i*3.7f
            b.box(53f,.0f,z,.09f,.025f,1.35f,gold,6f)
            b.box(73f,.0f,z,.09f,.025f,1.35f,gold,6f)
        }
        for (xx in floatArrayOf(44f,81f)) {
            b.box(xx,4.25f,-84f,.21f,4.2f,.21f,white,5f)
            b.ellipsoid(xx,8.62f,-84f,.72f,.23f,.72f,rose,8,12,20f)
        }
        b.box(64f,3.75f,-102f,10f,3.7f,.55f,white,5f)
        b.box(64f,7.58f,-102f,12f,.28f,2.0f,rose,13f)
        for(xx in floatArrayOf(59f,69f))
            b.box(xx,2.6f,-101.4f,1.2f,1.8f,.075f,aqua,21f)
        // Pond like a piece of another sky, with pale stairs to its edge.
        b.box(-48f,-.19f,-94f,20f,.15f,15f,aqua,1f)
        b.box(-22f,.07f,-94f,23f,.10f,2.3f,white,10f)
        for(i in 0..12) {
            val px=-64f+i*2.7f
            b.box(px,.045f,-111f,1.05f,.05f,.20f,rose,11f)
        }
        // Trees are exaggerated and cast pastel silhouettes in the mist.
        for (i in 0..44) {
            val angle=(i*2.39996).toFloat()
            val radius=24f+(i%9)*12.8f
            val px=radius*cos(angle)
            val pz=-67f+radius*sin(angle)
            if(kotlin.math.abs(px)<10f || (px>25f && px<98f && pz in -111f..-38f)) continue
            val h=meadowHeight(px,pz)
            b.box(px,h+2.0f,pz,.27f,2.0f,.27f,rgb(.62f,.54f,.52f),15f)
            b.ellipsoid(px,h+5.1f,pz,2.55f,2.0f,2.35f,if(i%4==0) rose else sage,8,12,20f)
        }
        // A cloud continent seems to float above the whole landscape.
        for(i in 0..12) {
            val px=-120f+i*22f
            val pz=-166f+16f*sin(i*.63f)
            val height=45f + (i%3)*12f
            b.ellipsoid(px,height,pz,24f+(i%4)*5f,10f,16f,
                if(i%3==0) rose else white,11,17,20f)
        }
        // Portal return at arrival plaza.
        b.box(-11f,1.72f,27f,1.05f,1.72f,.085f,rgb(.44f,.70f,.88f),3f)
        for(xx in floatArrayOf(-12.2f,-9.8f)) b.box(xx,1.8f,27f,.15f,1.86f,.20f,white,5f)
        b.box(-11f,3.73f,27f,1.37f,.16f,.20f,gold,6f)
        // 1.1: La puerta de la casa roja conduce a una casa mucho mayor por dentro.
        b.box(0f,1.54f,-132.3f, .88f,1.54f,.08f,rgb(.60f,.72f,.84f),22f)
        b.box(-.99f,1.64f,-132.3f,.12f,1.70f,.12f,white,5f)
        b.box(.99f,1.64f,-132.3f,.12f,1.70f,.12f,white,5f)
        b.box(0f,3.30f,-132.3f,1.12f,.13f,.12f,rose,13f)
        // Un cartel silencioso que permite encontrar la entrada.
        b.box(2.7f,1.60f,-130.8f,.12f,1.60f,.12f,white,5f)
        b.box(2.7f,3.2f,-130.8f,.70f,.26f,.19f,rose,13f)
        // Portal to the time aquarium, through the lonely house and beyond.
        b.box(0f,1.8f,-165f,1.10f,1.78f,.095f,rgb(.60f,.86f,.90f),3f)
        for(xx in floatArrayOf(-1.27f,1.27f)) b.box(xx,1.88f,-165f,.14f,1.95f,.20f,white,5f)
        b.box(0f,3.94f,-165f,1.43f,.17f,.20f,rose,6f)
        return b.finish()
    }

    // =============================================================
    // MUNDO 06: EL ACUARIO DE LAS HORAS
    // Arquitectura mojada y absurda, pasillos, galerías de azulejos
    // rosas, estanques y una esfera que flota sobre el agua.
    // =============================================================
    fun createAquarium(): Scene {
        val b=Builder()
        val ivory=rgb(.94f,.96f,.94f)
        val teal=rgb(.48f,.83f,.85f)
        val tile=rgb(.90f,.65f,.75f)
        val salmon=rgb(.98f,.75f,.61f)
        val mauve=rgb(.64f,.59f,.75f)
        val dark=rgb(.30f,.42f,.58f)
        val gold=rgb(.97f,.88f,.68f)
        // Surreal submerged foundation beyond the traversable section.
        b.box(0f,-.90f,-110f,122f,.08f,183f,teal,1f)
        // Welcome gallery, center spine and dry platforms.
        b.box(0f,-.19f,21f,19.0f,.19f,14f,ivory,10f)
        b.box(0f,-.19f,-85f,3.75f,.19f,102f,ivory,11f)
        for(side in floatArrayOf(-1f,1f)) {
            b.box(side*14.0f,-.19f,-86f,3.60f,.19f,99f,tile,11f)
            b.box(side*8.8f,-.35f,-88f,6f,.09f,90f,teal,1f)
            b.box(side*10.65f,-.10f,-85f,.14f,.20f,99f,ivory,5f)
            // Buildings seems supported by absurdly many thin columns.
            for (i in 0..23) {
                val z=10f-i*8f
                b.box(side*19.5f,4.8f,z,.28f,4.85f,.32f,ivory,5f)
                b.ellipsoid(side*19.5f,9.64f,z,.48f,.28f,.5f,salmon,6,9,20f)
            }
        }
        // Side plazas with bridges; crossing the great pools is possible.
        for(z in floatArrayOf(-39f,-97f,-154f)) {
            b.box(0f,-.18f,z,21f,.19f,3.6f,ivory,10f)
            b.box(0f,.015f,z,20.7f,.03f,.11f,gold,6f)
        }
        b.box(0f,-.19f,-167f,21f,.19f,16f,ivory,10f)
        for (side in floatArrayOf(-1f,1f)) {
            for (i in 0..15) {
                val z=4f-i*11f
                val h=8.2f+(i%4)*1.3f
                b.box(side*20f,h*.50f,z,.34f,h*.50f,.34f,ivory,5f)
                b.box(side*20f,h+.27f,z,.75f,.25f,.75f,tile,13f)
                b.box(side*14f,h,z,6.40f,.27f,.40f,if(i%2==0) salmon else mauve,13f)
            }
        }
        // Inverted hall arches and loops, drawing the eye down the canal.
        for (i in 0..13) {
            val z=-9f-i*11.7f
            val h=8.7f+(i%4)*.80f
            for(side in floatArrayOf(-1f,1f)) {
                b.box(side*5f,h*.52f,z,.35f,h*.50f,.34f,tile,11f)
                b.box(side*10f,h+.6f,z,.42f,.6f,.43f,salmon,13f)
            }
            b.box(0f,h+.25f,z,5.2f,.32f,.50f,tile,11f)
            b.ellipsoid(0f,h+.48f,z,.54f,.54f,.54f,gold,7,9,20f)
        }
        // Wide lateral halls with open views through the building.
        for (side in floatArrayOf(-1f,1f)) {
            b.box(side*33f,-.18f,-74f,10f,.18f,17f,ivory,10f)
            b.box(side*26f,-.18f,-74f,6.9f,.18f,2.7f,ivory,10f)
            for(i in 0..5) {
                val z=-88f+i*5.6f
                b.box(side*42f,4.8f,z,.30f,4.6f,.45f,tile,11f)
                b.box(side*33f,9.35f,z,10.2f,.27f,.55f,salmon,13f)
            }
            // Nested windows, suggesting one room fits inside another.
            for(i in 0..3) {
                val xx=side*(31f+i*2.5f)
                b.box(xx,3.25f,-89.5f,1.07f,1.95f,.09f,mauve,21f)
            }
        }
        // Atrium without a reasonable ceiling: one floating enormous sphere.
        b.ellipsoid(0f,13.2f,-132f,7.5f,7.5f,7.5f,salmon,15,24,20f)
        b.ellipsoid(0f,13.9f,-132f,4.95f,5.7f,5.1f,rgb(.98f,.80f,.78f),12,18,20f)
        for(i in 0..8) {
            val a=(i*2.0*PI/9.0).toFloat()
            val x=23f*cos(a)
            val z=-132f+23f*sin(a)
            b.box(x,6.5f,z,.43f,6.4f,.43f,ivory,5f)
        }
        // Distant pastel pool beyond last station.
        b.box(0f,-.33f,-193f,17.0f,.09f,15.8f,teal,1f)
        b.box(0f,-.18f,-186f,4.1f,.18f,11.8f,ivory,10f)
        // Two entrance portals, keeping user close to both.
        b.box(-11f,1.72f,23f,1.08f,1.72f,.08f,rgb(.35f,.71f,.84f),3f)
        b.box(11f,1.72f,23f,1.08f,1.72f,.08f,rgb(.75f,.57f,.88f),2f)
        for(xx in floatArrayOf(-12.25f,-9.75f,9.75f,12.25f))
            b.box(xx,1.85f,23f,.14f,1.9f,.20f,ivory,5f)
        for(xx in floatArrayOf(-11f,11f)) b.box(xx,3.72f,23f,1.40f,.16f,.20f,gold,6f)
        return b.finish()
    }

    private fun within(x:Float,z:Float,cx:Float,cz:Float,rx:Float,rz:Float):Boolean =
        kotlin.math.abs(x-cx)<rx && kotlin.math.abs(z-cz)<rz

    fun canWalkMeadow(x:Float,z:Float):Boolean {
        if (x !in -205f..205f || z !in -230f..175f) return false
        if (within(x,z,-48f,-94f,20.1f,15.1f)) return false // pond
        // Keep the long bridge across the pink parking lot walkable.
        return true
    }

    fun canWalkAquarium(x:Float,z:Float):Boolean {
        val arrival=within(x,z,0f,21f,18.6f,13.5f)
        val center=within(x,z,0f,-85f,3.6f,102f)
        val sideLanes=within(x,z,-14f,-86f,3.4f,98f) ||
            within(x,z,14f,-86f,3.4f,98f)
        val crossing=within(x,z,0f,-39f,20.7f,3.4f) ||
            within(x,z,0f,-97f,20.7f,3.4f) ||
            within(x,z,0f,-154f,20.7f,3.4f)
        val plaza=within(x,z,0f,-167f,20.5f,15.5f)
        val side=within(x,z,-33f,-74f,9.6f,16.5f) ||
                 within(x,z,33f,-74f,9.6f,16.5f)
        val bridges=within(x,z,-26f,-74f,7f,2.65f) ||
                    within(x,z,26f,-74f,7f,2.65f)
        val finale=within(x,z,0f,-186f,3.8f,11.4f)
        return arrival || center || sideLanes || crossing || plaza || side || bridges || finale
    }

    private fun hitsRect(x: Float, z: Float, minX: Float, maxX: Float, minZ: Float, maxZ: Float): Boolean {
        val nearestX=x.coerceIn(minX,maxX)
        val nearestZ=z.coerceIn(minZ,maxZ)
        val dx=x-nearestX; val dz=z-nearestZ
        return dx*dx+dz*dz < .35f*.35f
    }


    // =============================================================
    // 1.1: DOS CASAS CON ARQUITECTURA IMPOSIBLE
    // Son escenas propias (no se dibujan mientras recorres los exteriores),
    // por eso una casa pequeña puede contener cien metros de pasillos.
    // =============================================================
    private fun roomDoor(b:Builder,x:Float,z:Float,color:FloatArray) {
        b.box(x,1.65f,z,1.0f,1.65f,.095f,color,22f)
        for(dx in floatArrayOf(-1.18f,1.18f))
            b.box(x+dx,1.75f,z,.13f,1.78f,.17f,rgb(.96f,.94f,.90f),5f)
        b.box(x,3.54f,z,1.30f,.13f,.18f,rgb(.88f,.78f,.86f),11f)
    }

    fun createHouseMeadow():Scene {
        val b=Builder()
        val pearl=rgb(.93f,.90f,.89f)
        val mint=rgb(.76f,.88f,.84f)
        val pink=rgb(.94f,.75f,.83f)
        val lavender=rgb(.70f,.66f,.84f)
        val cream=rgb(.99f,.94f,.82f)
        val midnight=rgb(.28f,.39f,.61f)
        // Un pasillo de casi cien metros; sus laterales abren a salones.
        b.box(0f,-.22f,-48f,4.1f,.20f,60f,pearl,10f)
        b.box(0f,8.12f,-48f,4.15f,.18f,60f,lavender,11f)
        for(side in floatArrayOf(-1f,1f)) {
            // Dejamos huecos reales para entrar en los salones laterales.
            val sections=if(side>0f)
                arrayOf(floatArrayOf(-108f,-91.4f),floatArrayOf(-72.6f,-35.4f),floatArrayOf(-16.6f,12f))
                else arrayOf(floatArrayOf(-108f,-64.4f),floatArrayOf(-45.6f,12f))
            for(section in sections) {
                val za=section[0]; val zb=section[1]
                b.box(side*4.07f,4.0f,(za+zb)*.5f,.18f,4.0f,(zb-za)*.5f,mint,5f)
            }
            for(i in 0..24) {
                val z=7f-i*4.5f
                b.box(side*3.78f,5.8f,z,.12f,1.16f,1.15f,midnight,21f)
                b.box(side*3.72f,3.86f,z,.14f,.045f,1.30f,cream,6f)
                b.box(side*3.70f,7.08f,z,.15f,.08f,1.28f,pink,13f)
            }
        }
        // Los salones aparecen sin quitar el pasillo: más espacio de lo posible.
        val rooms=floatArrayOf(-26f,-55f,-82f)
        for((i,z) in rooms.withIndex()) {
            val shift=if(i%2==0)1f else -1f
            b.box(shift*14f,-.20f,z,10.1f,.20f,9.4f,if(i%2==0)cream else mint,10f)
            b.box(shift*14f,9.25f,z,10.1f,.16f,9.4f,lavender,11f)
            b.box(shift*24f,4.5f,z,.2f,4.5f,9.35f,pink,5f)
            b.box(shift*14f,4.5f,z-9.2f,10f,4.5f,.19f,pearl,5f)
            b.box(shift*14f,4.5f,z+9.2f,10f,4.5f,.19f,pearl,5f)
            for(k in 0..3) {
                val xx=shift*(8f+k*4f)
                b.box(xx,.43f,z,.7f,.46f,.58f,cream,6f)
                b.ellipsoid(xx,1.6f,z,.66f,.62f,.75f,pink,7,10,20f)
            }
            b.box(shift*16f,4.1f,z-9.00f,3.1f,2.6f,.13f,midnight,21f)
            b.box(shift*14f,6.7f,z,2f,.16f,2.0f,pink,11f)
        }
        for(i in 0..16) {
            val z=6f-i*6.0f
            b.box(0f,7.74f,z,3.75f,.18f,.18f,cream,6f)
            b.ellipsoid(0f,7.25f,z,.31f,.32f,.31f,rgb(.99f,.93f,.75f),7,10)
            b.box(0f,-.012f,z,3.60f,.015f,.095f,if(i%2==0)pink else mint,11f)
        }
        // Una puerta respira al final, sin sustos repentinos.
        b.box(0f,3.75f,-108f,4.0f,3.8f,.25f,pearl,5f)
        roomDoor(b,0f,-107.7f,midnight)
        roomDoor(b,0f,8f,rgb(.45f,.70f,.76f))
        return b.finish()
    }

    fun createHouseCrater():Scene {
        val b=Builder()
        val stone=rgb(.77f,.84f,.79f)
        val white=rgb(.94f,.95f,.94f)
        val dim=rgb(.52f,.60f,.66f)
        val sky=rgb(.63f,.78f,.88f)
        val gold=rgb(.95f,.82f,.61f)
        // Salas repetidas bajo la montaña; algunos muebles cuelgan del techo.
        b.box(0f,-.22f,-36f,7.5f,.20f,44f,stone,10f)
        b.box(0f,11.65f,-36f,7.5f,.18f,44f,white,11f)
        for(side in floatArrayOf(-1f,1f)) {
            b.box(side*7.5f,5.72f,-36f,.2f,5.72f,44f,stone,5f)
            for(i in 0..10) {
                val z=5f-i*8.1f
                b.box(side*7.18f,4.5f,z,.10f,1.6f,1.32f,sky,21f)
                b.box(side*6.90f,9.9f,z,.40f,1.25f,.40f,white,5f)
                b.box(side*5.65f,9.0f,z,1.10f,.12f,1.08f,dim,6f)
                b.box(side*5.65f,8.28f,z,1.08f,.65f,.12f,dim,5f)
            }
        }
        for(i in 0..9) {
            val z=5f-i*8.0f
            b.box(0f,9.25f,z,7.1f,.20f,.32f,white,5f)
            // Columnas a los lados: eje central libre para avanzar.
            for(xx in floatArrayOf(-4.9f,4.9f)) {
                b.box(xx,2.15f,z,.32f,2.15f,.36f,dim,5f)
                b.box(xx,4.31f,z,.54f,.19f,.42f,gold,6f)
            }
            b.box(0f,.03f,z-3f,6.3f,.04f,.08f,dim,10f)
        }
        // Un sol falso atrapado dentro de la casa.
        b.ellipsoid(0f,8.1f,-48f,3.6f,3.6f,3.6f,sky,13,17,20f)
        b.box(0f,4.5f,-80f,7.3f,4.5f,.22f,dim,5f)
        roomDoor(b,0f,-79.7f,gold)
        roomDoor(b,0f,7.5f,sky)
        return b.finish()
    }

    fun canWalkHouseMeadow(x:Float,z:Float):Boolean {
        if(z !in -108f..9f) return false
        val hall=kotlin.math.abs(x)<3.68f
        val rightRoom=within(x,z,14f,-26f,9.75f,9.1f) ||
                      within(x,z,14f,-82f,9.75f,9.1f)
        val leftRoom=within(x,z,-14f,-55f,9.75f,9.1f)
        return hall || rightRoom || leftRoom
    }

    fun canWalkHouseCrater(x:Float,z:Float):Boolean =
        kotlin.math.abs(x)<7.15f && z in -79.7f..7.9f

    fun canWalk(worldId: String, x: Float, z: Float): Boolean {
        if(DreamObjects.blocked(worldId,x,z))return false
        if(DreamHorizons.contains(worldId))return !DreamJourney.frameBlocked(worldId,x,z)&&DreamHorizons.canWalk(worldId,x,z)
        if(worldId==DreamWaterpark.ID)return !DreamJourney.frameBlocked(worldId,x,z) && DreamWaterpark.canWalk(x,z)
        if(DreamPocket.contains(worldId))return DreamPocket.canWalk(worldId,x,z)
        if(DreamJourney.frameBlocked(worldId,x,z))return false
        if(DreamLands.contains(worldId))return DreamLands.canWalk(worldId,x,z)
        if(worldId==DreamJourney.DARK)return DreamDarkMap.canWalk(x,z)
        if(worldId==STATION)return DreamJourney.canWalkHub(x,z)
        if(DreamExpansion.contains(worldId)) return DreamExpansion.canWalk(worldId,x,z)
        if(worldId==STATION) {
            if(x !in -52f..52f || z !in -65f..45f) return false
            if(DreamExpansion.stationBlocked(x,z)) return false
            for(xx in floatArrayOf(-4f,4f)) for(zz in floatArrayOf(-13.5f,-20.5f)) {
                if(hitsRect(x,z,xx-.25f,xx+.25f,zz-.25f,zz+.25f)) return false
            }
            if(hitsRect(x,z,1f,3.6f,-14.8f,-13.6f)) return false // banco
            if(hitsRect(x,z,-4.1f,-1.1f,-20.95f,-20.52f)) return false
            if(hitsRect(x,z,1.1f,4.1f,-20.95f,-20.52f)) return false
            if(hitsRect(x,z,-1.0f,1.0f,-20.70f,-20.52f)) return false // puerta: usar INTERACTUAR
            // Muros laterales de la nueva galería: el pasillo queda libre.
            if(z in -46.8f..-8.8f && (x in 25.65f..26.65f || x in 33.35f..34.35f)) return false
            if(z in -46.9f..-45.9f && x in 26.6f..33.4f) return false
            return true
        }
        if (worldId == HOUSE_MEADOW) return canWalkHouseMeadow(x,z)
        if (worldId == HOUSE_CRATER) return canWalkHouseCrater(x,z)
        if (worldId == GARDEN) return canWalkGarden(x,z)
        if (worldId == MEADOW) return canWalkMeadow(x,z)
        if (worldId == AQUARIUM) return canWalkAquarium(x,z)
        if (worldId == CRATER) {
            // Vertical support is resolved by craterWalkSurface + renderer gravity.
            // Crossing the rim is permitted; side-wall collisions depend on feet Y.
            return x in -164f..164f && z in -254f..78f
        }
        if(x !in -14.6f..14.6f || z !in -35.4f..12.8f) return false
        // Agua en los canales: el protagonista camina por puente y bordes.
        if(z in -29.35f..-4.6f && kotlin.math.abs(x) > 1.54f && kotlin.math.abs(x) < 9.30f) return false
        if(hitsRect(x,z,-.8f,.8f,-32.7f,-31.3f)) return false // tótem
        return true
    }
}
