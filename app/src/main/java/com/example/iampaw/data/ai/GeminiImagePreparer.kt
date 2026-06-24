package com.example.iampaw.data.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.iampaw.data.local.GeminiImagePayload
import java.io.ByteArrayOutputStream
import kotlin.math.max

/** Reduce tamaño de imagen antes de enviarla a Gemini (límite de payload y latencia). */
internal object GeminiImagePreparer {

    private const val MAX_EDGE_PX = 1280
    private const val JPEG_QUALITY = 85
    private const val MAX_BYTES = 4 * 1024 * 1024

    fun prepare(bytes: ByteArray, mimeType: String): GeminiImagePayload {
        if (bytes.isEmpty()) return GeminiImagePayload(bytes, mimeType)
        if (bytes.size <= MAX_BYTES && mimeType == "image/jpeg") {
            return GeminiImagePayload(bytes, mimeType)
        }

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return GeminiImagePayload(bytes, mimeType)

        val scaled = scaleDown(bitmap)
        if (scaled !== bitmap) bitmap.recycle()

        val jpegBytes = ByteArrayOutputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            stream.toByteArray()
        }
        scaled.recycle()
        return GeminiImagePayload(jpegBytes, "image/jpeg")
    }

    private fun scaleDown(source: Bitmap): Bitmap {
        val longest = max(source.width, source.height)
        if (longest <= MAX_EDGE_PX) return source
        val scale = MAX_EDGE_PX.toFloat() / longest
        val targetW = (source.width * scale).toInt().coerceAtLeast(1)
        val targetH = (source.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }
}
