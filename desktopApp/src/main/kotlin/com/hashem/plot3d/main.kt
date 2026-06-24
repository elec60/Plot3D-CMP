package com.hashem.plot3d

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plot3D",
    ) {
        App()
    }
}