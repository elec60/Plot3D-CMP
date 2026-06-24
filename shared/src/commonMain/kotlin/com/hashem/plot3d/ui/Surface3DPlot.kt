package com.hashem.plot3d.ui


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.hashem.plot3d.models.Camera
import com.hashem.plot3d.models.Surface3D
import com.hashem.plot3d.models.ColorMap
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * An animated 3D surface plot in pure Compose.
 *
 * @param surface the mesh generator (z = f(x, y, time))
 * @param animate if true, `time` advances each frame and faces rebuild
 * @param wireframe draw mesh edges on top of filled faces
 */
@Composable
fun Surface3DPlot(
    surface: Surface3D,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    wireframe: Boolean = true,
    background: Color = Color(0xFF101015),
) {
    var camera by remember { mutableStateOf(Camera()) }
    var time by remember { mutableStateOf(0f) }

    // Animation clock.
    LaunchedEffect(animate) {
        if (!animate) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) time += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }

    val gestureMod = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            camera = camera.copy(
                azimuth = camera.azimuth - pan.x * 0.01f,
                elevation = (camera.elevation + pan.y * 0.01f)
                    .coerceIn((-PI / 2 + 0.05).toFloat(), (PI / 2 - 0.05).toFloat()),
                distance = (camera.distance / zoom).coerceIn(1.5f, 12f),
            )
        }
    }

    Canvas(modifier = modifier.then(gestureMod)) {
        drawRect(background, size = size)
        val w = size.width; val h = size.height

        val faces = surface.buildFaces(time)

        // Painter's algorithm: project, sort back-to-front by mean depth.
        data class Drawn(val path: Path, val depth: Float, val t: Float)
        val drawn = faces.map { face ->
            val pa = camera.project(face.a, w, h)
            val pb = camera.project(face.b, w, h)
            val pc = camera.project(face.c, w, h)
            val pd = camera.project(face.d, w, h)
            val path = Path().apply {
                moveTo(pa.screen.x, pa.screen.y)
                lineTo(pb.screen.x, pb.screen.y)
                lineTo(pc.screen.x, pc.screen.y)
                lineTo(pd.screen.x, pd.screen.y)
                close()
            }
            val depth = (pa.depth + pb.depth + pc.depth + pd.depth) / 4f
            Drawn(path, depth, face.t)
        }.sortedByDescending { it.depth }

        for (d in drawn) {
            drawPath(d.path, ColorMap.color(d.t, alpha = 0.92f))
            if (wireframe) {
                drawPath(d.path, Color(0f, 0f, 0f, 0.25f), style = Stroke(width = 1f))
            }
        }
    }
}


@Composable
fun Demo() {
    val surface = remember {
        Surface3D(resolution = 48) { x, y, t ->
            val r = sqrt(x * x + y * y)
            sin(r * 2f - t * 2f) / (r + 1f)
        }
    }
    Surface3DPlot(
        surface = surface,
        animate = true,
        wireframe = true,
        modifier = Modifier.fillMaxSize(),
    )
}

