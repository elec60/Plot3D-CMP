package com.hashem.plot3d

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform