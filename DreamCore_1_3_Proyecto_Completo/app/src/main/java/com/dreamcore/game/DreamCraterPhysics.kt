package com.dreamcore.game

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot

/** Fixed substeps keep falls and edge collisions stable at different frame rates. */
class DreamCraterPhysics {
    var x = 0f; private set
    var z = 0f; private set
    var feetY = 0f; private set
    var verticalSpeed = 0f; private set
    var grounded = true; private set
    var landedThisStep = false; private set
    var landingSpeed = 0f; private set

    fun reset(x: Float, z: Float, feet: Float) {
        this.x = x; this.z = z; feetY = feet
        verticalSpeed = 0f
        grounded = abs(feet - DreamWorlds.craterWalkSurface(x,z)) < .04f
        landedThisStep = false; landingSpeed = 0f
    }

    private fun canMove(nx: Float, nz: Float): Boolean {
        if (!DreamWorlds.canWalk(DreamWorlds.CRATER,nx,nz)) return false
        // A capsule cannot leave the vertical shaft through its wall.
        val r = hypot(nx,nz-DreamWorlds.CRATER_CENTER_Z)
        val currentRadius = hypot(x,z-DreamWorlds.CRATER_CENTER_Z)
        if (feetY < -1f && r > DreamWorlds.CRATER_RIM_RADIUS-.38f &&
            r >= currentRadius) return false
        val surface = DreamWorlds.craterWalkSurface(nx,nz)
        if (surface > feetY + .42f) return false
        return !DreamWorlds.craterBodyBlocked(nx,nz,feetY)
    }

    fun step(dt: Float, velocityX: Float, velocityZ: Float, jump: Boolean) {
        landedThisStep = false
        if (dt <= 0f) return
        if (jump && grounded) { verticalSpeed = 5.6f; grounded = false }
        val bounded = dt.coerceIn(0f,.1f)
        val steps = ceil(bounded / (1f/120f)).toInt().coerceAtLeast(1)
        val h = bounded/steps
        repeat(steps) {
            val nx = x + velocityX*h
            if (canMove(nx,z)) x = nx
            val nz = z + velocityZ*h
            if (canMove(x,nz)) z = nz
            val ground = DreamWorlds.craterWalkSurface(x,z)
            val roof = DreamWorlds.craterRoofSurface(x,z)
            // Apoyar una caída sobre el techo sin levantar al jugador desde dentro.
            val floor = if (roof != null && (feetY >= roof-.03f ||
                    (grounded && abs(roof-feetY) <= .42f))) roof else ground
            if (grounded && feetY-floor > .08f) grounded = false
            if (!grounded) {
                verticalSpeed = (verticalSpeed - 14.2f*h).coerceAtLeast(-32f)
                val nextFeet = feetY + verticalSpeed*h
                if (nextFeet <= floor && verticalSpeed <= 0f) {
                    landingSpeed = -verticalSpeed
                    landedThisStep = true
                    feetY = floor; verticalSpeed = 0f; grounded = true
                } else feetY = nextFeet
            } else {
                feetY = floor
            }
        }
        // Guard corrupted state without allowing a permanent fall below the map.
        if (!feetY.isFinite() || feetY < -DreamWorlds.CRATER_DEPTH-12f) {
            reset(0f,DreamWorlds.CRATER_CENTER_Z+15f,
                DreamWorlds.craterWalkSurface(0f,DreamWorlds.CRATER_CENTER_Z+15f))
        }
    }
}
