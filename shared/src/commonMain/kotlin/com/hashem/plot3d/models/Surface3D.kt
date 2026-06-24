package com.hashem.plot3d.models

import androidx.compose.ui.graphics.Color

class Surface3D(
    val resolution: Int = 40,
    val xRange: ClosedFloatingPointRange<Float> = -3f..3f,
    val yRange: ClosedFloatingPointRange<Float> = -3f..3f,
    val zScale: Float = 0.9f,
    val f: (Float, Float, Float) -> Float, // (x, y, time) -> z
) {
    fun buildFaces(time: Float): List<Face> {
        val n = resolution
        val xs = FloatArray(n + 1) { xRange.start + (xRange.endInclusive - xRange.start) * it / n }
        val ys = FloatArray(n + 1) { yRange.start + (yRange.endInclusive - yRange.start) * it / n }

        // Evaluate grid, track min/max for normalization.
        val zs = Array(n + 1) { i -> FloatArray(n + 1) { j -> f(xs[i], ys[j], time) } }
        var zMin = Float.MAX_VALUE; var zMax = -Float.MAX_VALUE
        for (row in zs) for (z in row) { if (z < zMin) zMin = z; if (z > zMax) zMax = z }
        val span = (zMax - zMin).takeIf { it > 1e-6f } ?: 1f

        fun norm(v: Float, lo: Float, hi: Float) = (v - lo) / (hi - lo) * 2f - 1f
        fun vec(i: Int, j: Int): Vector3D = Vector3D(
            norm(xs[i], xRange.start, xRange.endInclusive),
            norm(ys[j], yRange.start, yRange.endInclusive),
            (zs[i][j] - zMin) / span * 2f * zScale - zScale
        )

        val faces = ArrayList<Face>(n * n)
        for (i in 0 until n) for (j in 0 until n) {
            val tMid = ((zs[i][j] + zs[i + 1][j] + zs[i][j + 1] + zs[i + 1][j + 1]) / 4f - zMin) / span
            faces += Face(vec(i, j), vec(i + 1, j), vec(i + 1, j + 1), vec(i, j + 1), tMid)
        }
        return faces
    }
}
object ColorMap {
    private val stops = arrayOf(
        Triple(0.267f, 0.005f, 0.329f),
        Triple(0.283f, 0.141f, 0.458f),
        Triple(0.254f, 0.265f, 0.530f),
        Triple(0.207f, 0.372f, 0.553f),
        Triple(0.164f, 0.471f, 0.558f),
        Triple(0.128f, 0.567f, 0.551f),
        Triple(0.135f, 0.659f, 0.518f),
        Triple(0.267f, 0.749f, 0.441f),
        Triple(0.478f, 0.821f, 0.318f),
        Triple(0.741f, 0.873f, 0.150f),
        Triple(0.993f, 0.906f, 0.144f),
    )
    fun color(t0: Float, alpha: Float = 1f): Color {
        val t = t0.coerceIn(0f, 1f) * (stops.size - 1)
        val i = t.toInt().coerceAtMost(stops.size - 2)
        val frac = t - i
        val (r1, g1, b1) = stops[i]; val (r2, g2, b2) = stops[i + 1]
        return Color(
            r1 + (r2 - r1) * frac,
            g1 + (g2 - g1) * frac,
            b1 + (b2 - b1) * frac,
            alpha
        )
    }
}