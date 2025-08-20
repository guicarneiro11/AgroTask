package com.guicarneirodev.agrotask

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform