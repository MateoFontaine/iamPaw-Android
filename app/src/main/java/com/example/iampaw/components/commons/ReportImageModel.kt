package com.example.iampaw.components.commons

import java.io.File

/** Resuelve rutas locales, content:// o URLs para Glide. */
fun reportImageModel(storedPath: String): Any? = when {
    storedPath.isBlank() -> null
    storedPath.startsWith("http://") || storedPath.startsWith("https://") -> storedPath
    storedPath.startsWith("content:") -> storedPath
    else -> File(storedPath).takeIf { it.exists() } ?: storedPath
}
