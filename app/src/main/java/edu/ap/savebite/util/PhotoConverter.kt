package edu.ap.savebite.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Photos as Base64 text (assignment, "Foto's als Base64"): scale down to at most 800 px,
 * compress as JPEG with quality 70, and back. Everything runs on Dispatchers.Default.
 */
class PhotoConverter(private val contentResolver: ContentResolver) {

    /** Reads a photo from the gallery, already scaled down. */
    suspend fun load(uri: Uri): Bitmap = withContext(Dispatchers.Default) {
        scaleDown(decode(uri))
    }

    /** Scales a photo (e.g. from the camera) down to at most [MAX_SIDE] px. */
    suspend fun prepare(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        scaleDown(bitmap)
    }

    suspend fun toBase64(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    /** Returns null when the text is not a valid photo. */
    suspend fun fromBase64(base64: String): Bitmap? = withContext(Dispatchers.Default) {
        try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun decode(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder also turns photos that were taken sideways the right way up
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, info, _ ->
                val scale = MAX_SIDE.toFloat() / max(info.size.width, info.size.height)
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceAtLeast(1),
                        (info.size.height * scale).roundToInt().coerceAtLeast(1)
                    )
                }
                // A software bitmap, so it can be compressed to JPEG
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            // Read only the size first, then load a smaller version to save memory
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
            var sampleSize = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_SIDE) sampleSize *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
                ?: throw IllegalArgumentException("Not an image: $uri")
        }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= MAX_SIDE) return bitmap
        val scale = MAX_SIDE.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    }

    private companion object {
        const val MAX_SIDE = 800
        const val JPEG_QUALITY = 70
    }
}
