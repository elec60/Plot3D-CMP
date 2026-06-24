package com.hashem.plot3d.models

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

data class Camera(
    val azimuth: Float = 0.6f,
    val elevation: Float = 0.5f,
    val distance: Float = 4f,
    val fov: Float = 1.2f, // focal length factor
) {
    /** Rotate a model point into camera space (depth in .z, larger = farther). */
    fun toCameraSpace(p: Vector3D): Vector3D {
        // Rotate around Z (azimuth) then around X (elevation).
        val ca = cos(azimuth); val sa = sin(azimuth)
        val x1 = p.x * ca - p.y * sa
        val y1 = p.x * sa + p.y * ca
        val z1 = p.z

        val ce = cos(elevation); val se = sin(elevation)
        val y2 = y1 * ce - z1 * se
        val z2 = y1 * se + z1 * ce

        // Push the whole scene away from the camera along view axis (y2).
        return Vector3D(x1, y2, z2 + 0f).let { Vector3D(it.x, it.z, y2 + distance) }
    }

    /** Project a model point to screen pixels. width/height in px. */
    fun project(p: Vector3D, width: Float, height: Float): Projected {
        val c = toCameraSpace(p)
        val depth = c.z.coerceAtLeast(0.01f)
        val scale = (fov * minOf(width, height)) / depth
        val sx = width / 2f + c.x * scale
        val sy = height / 2f - c.y * scale
        return Projected(Offset(sx, sy), depth)
    }
}

data class Projected(val screen: Offset, val depth: Float)