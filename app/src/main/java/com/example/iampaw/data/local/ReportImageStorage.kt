package com.example.iampaw.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportImageStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val imagesDir: File
        get() = File(context.filesDir, "report_images").also { it.mkdirs() }

    /**
     * Copia content:// o cache temporal a filesDir/report_images/{reportId}.jpg
     * para que la foto sobreviva al cerrar la app.
     */
    fun persistReportImage(sourceUri: String, reportId: String): String {
        if (sourceUri.isBlank()) return ""
        if (sourceUri.startsWith("http://") || sourceUri.startsWith("https://")) return sourceUri

        val dest = File(imagesDir, "$reportId.jpg")
        if (dest.exists() && dest.absolutePath == sourceUri.removePrefix("file://")) {
            return dest.absolutePath
        }

        if (sourceUri.startsWith("file:")) {
            val sourcePath = Uri.parse(sourceUri).path ?: return ""
            val source = File(sourcePath)
            if (source.exists() && source.absolutePath != dest.absolutePath) {
                source.copyTo(dest, overwrite = true)
                return dest.absolutePath
            }
            if (dest.exists()) return dest.absolutePath
        }

        return try {
            context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            if (dest.exists()) dest.absolutePath else ""
        } catch (_: Exception) {
            ""
        }
    }

    /** Elimina la imagen local asociada a un reporte (si existe). */
    fun deleteReportImage(reportId: String) {
        File(imagesDir, "$reportId.jpg").delete()
    }

    /** Lee bytes de una imagen local (content://, file:// o path absoluto). */
    fun readImageBytes(storedPath: String): ByteArray? {
        if (storedPath.isBlank() || storedPath.startsWith("http")) return null
        return try {
            when {
                storedPath.startsWith("content:") ->
                    context.contentResolver.openInputStream(Uri.parse(storedPath))?.use { it.readBytes() }
                storedPath.startsWith("file:") -> {
                    val path = Uri.parse(storedPath).path ?: return null
                    File(path).takeIf { it.exists() }?.readBytes()
                }
                else -> File(storedPath).takeIf { it.exists() }?.readBytes()
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Coil puede cargar File, http o content. */
    fun modelForDisplay(storedPath: String): Any? = when {
        storedPath.isBlank() -> null
        storedPath.startsWith("http") -> storedPath
        storedPath.startsWith("content:") -> storedPath
        else -> {
            val file = File(storedPath)
            if (file.exists()) file else null
        }
    }
}
