package com.swipegallery.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MediaDetails(
    val name: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val mimeType: String?,
    val width: Int,
    val height: Int,
    val megapixels: String,
    val dateTaken: String,
    val relativePath: String?,
    val uriString: String,
    val isVideo: Boolean,
    val durationFormatted: String?,
    // EXIF metadata
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensModel: String? = null,
    val aperture: String? = null,
    val exposureTime: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
    val flash: String? = null,
    val whiteBalance: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val software: String? = null,
    // Video metadata
    val frameRate: String? = null,
    val bitrate: String? = null,
    val orientation: String? = null,
    val hasExifData: Boolean = false,
)

object MediaDetailsHelper {

    suspend fun getDetails(context: Context, photo: Photo): MediaDetails = withContext(Dispatchers.IO) {
        var sizeBytes = 0L
        var mimeType: String? = null
        var dateModified = photo.date * 1000
        var width = 0
        var height = 0

        // Query MediaStore for additional metadata
        try {
            val projection = arrayOf(
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT
            )
            context.contentResolver.query(photo.uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    val mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                    val modCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                    val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                    val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

                    if (sizeCol >= 0) sizeBytes = cursor.getLong(sizeCol)
                    if (mimeCol >= 0) mimeType = cursor.getString(mimeCol)
                    if (modCol >= 0 && cursor.getLong(modCol) > 0) dateModified = cursor.getLong(modCol) * 1000
                    if (widthCol >= 0) width = cursor.getInt(widthCol)
                    if (heightCol >= 0) height = cursor.getInt(heightCol)
                }
            }
        } catch (_: Exception) {}

        // Fallback for file size
        if (sizeBytes <= 0) {
            try {
                context.contentResolver.openInputStream(photo.uri)?.use { stream ->
                    sizeBytes = stream.available().toLong()
                }
            } catch (_: Exception) {}
        }

        // If resolution is not available from MediaStore, read from file
        if (width <= 0 || height <= 0) {
            if (photo.isVideo) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, photo.uri)
                    val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                    val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                    if (w != null && h != null) {
                        width = w
                        height = h
                    }
                    retriever.release()
                } catch (_: Exception) {}
            } else {
                try {
                    context.contentResolver.openInputStream(photo.uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(stream, null, options)
                        width = options.outWidth
                        height = options.outHeight
                        if (mimeType == null) mimeType = options.outMimeType
                    }
                } catch (_: Exception) {}
            }
        }

        val megapixels = if (width > 0 && height > 0) {
            val mp = (width * height).toDouble() / 1_000_000.0
            String.format(Locale.getDefault(), "%.1f MP (%d × %d)", mp, width, height)
        } else {
            "Unknown"
        }

        val dateFormatted = SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.getDefault()).format(Date(dateModified))

        if (photo.isVideo) {
            var frameRate: String? = null
            var bitrate: String? = null
            var rotation: String? = null
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, photo.uri)
                val rate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                val br = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()
                val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                frameRate = if (rate != null) "$rate fps" else null
                bitrate = if (br != null) "${br / 1000} kbps" else null
                rotation = if (rot != null) "$rot°" else null
                retriever.release()
            } catch (_: Exception) {}

            MediaDetails(
                name = photo.name,
                sizeBytes = sizeBytes,
                formattedSize = formatFileSize(sizeBytes),
                mimeType = mimeType ?: "video/*",
                width = width,
                height = height,
                megapixels = megapixels,
                dateTaken = dateFormatted,
                relativePath = photo.relativePath,
                uriString = photo.uri.toString(),
                isVideo = true,
                durationFormatted = if (photo.durationMs > 0) formatDuration(photo.durationMs) else null,
                frameRate = frameRate,
                bitrate = bitrate,
                orientation = rotation,
                hasExifData = false
            )
        } else {
            // Read EXIF tags for images
            var cameraMake: String? = null
            var cameraModel: String? = null
            var lensModel: String? = null
            var aperture: String? = null
            var exposureTime: String? = null
            var iso: String? = null
            var focalLength: String? = null
            var flash: String? = null
            var whiteBalance: String? = null
            var latitude: Double? = null
            var longitude: Double? = null
            var altitude: Double? = null
            var software: String? = null
            var hasExif = false

            try {
                context.contentResolver.openInputStream(photo.uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE)
                    cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL)
                    lensModel = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)
                    software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)

                    val fNumber = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)
                    if (fNumber > 0) aperture = "f/$fNumber"

                    val exp = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                    if (exp != null) {
                        val expD = exp.toDoubleOrNull()
                        exposureTime = if (expD != null && expD < 1.0 && expD > 0) {
                            "1/${(1.0 / expD).toInt()}s"
                        } else {
                            "${exp}s"
                        }
                    }

                    val isoVal = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                        ?: exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
                    if (isoVal != null) iso = "ISO $isoVal"

                    val focal = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
                    if (focal > 0) focalLength = "${focal.toInt()} mm"

                    val flashVal = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1)
                    if (flashVal != -1) {
                        flash = if ((flashVal and 1) != 0) "Fired" else "Did not fire"
                    }

                    val wb = exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, -1)
                    if (wb != -1) {
                        whiteBalance = if (wb == ExifInterface.WHITE_BALANCE_AUTO.toInt()) "Auto" else "Manual"
                    }

                    val latLong = exif.latLong
                    if (latLong != null && latLong.size >= 2) {
                        latitude = latLong[0]
                        longitude = latLong[1]
                    }
                    val alt = exif.getAltitude(0.0)
                    if (alt != 0.0) altitude = alt

                    hasExif = cameraMake != null || cameraModel != null || aperture != null ||
                            exposureTime != null || iso != null || focalLength != null ||
                            latitude != null || software != null
                }
            } catch (_: Exception) {}

            MediaDetails(
                name = photo.name,
                sizeBytes = sizeBytes,
                formattedSize = formatFileSize(sizeBytes),
                mimeType = mimeType ?: "image/*",
                width = width,
                height = height,
                megapixels = megapixels,
                dateTaken = dateFormatted,
                relativePath = photo.relativePath,
                uriString = photo.uri.toString(),
                isVideo = false,
                durationFormatted = null,
                cameraMake = cameraMake,
                cameraModel = cameraModel,
                lensModel = lensModel,
                aperture = aperture,
                exposureTime = exposureTime,
                iso = iso,
                focalLength = focalLength,
                flash = flash,
                whiteBalance = whiteBalance,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                software = software,
                hasExifData = hasExif
            )
        }
    }

    /**
     * Strips all EXIF/metadata from an image.
     * Rewrites the image bitmap cleanly without metadata to remove all traces of EXIF/GPS/device data.
     */
    suspend fun stripAllMetadata(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First decode bitmap
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext Result.failure(Exception("Could not read image bitmap"))

            // Try writing out clean image
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                val format = if (uri.toString().endsWith(".png", ignoreCase = true)) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }
                bitmap.compress(format, 95, out)
                out.flush()
            } ?: return@withContext Result.failure(Exception("Could not open output stream to overwrite file"))

            // Also try clearing any ExifInterface tags if file descriptor is accessible
            try {
                context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                    val exif = ExifInterface(pfd.fileDescriptor)
                    val tagsToClear = arrayOf(
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_LATITUDE_REF,
                        ExifInterface.TAG_GPS_LONGITUDE_REF,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE_REF,
                        ExifInterface.TAG_GPS_TIMESTAMP,
                        ExifInterface.TAG_GPS_DATESTAMP,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD,
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
                        ExifInterface.TAG_SOFTWARE,
                        ExifInterface.TAG_ARTIST,
                        ExifInterface.TAG_COPYRIGHT,
                        ExifInterface.TAG_USER_COMMENT,
                        ExifInterface.TAG_DATETIME,
                        ExifInterface.TAG_DATETIME_ORIGINAL,
                        ExifInterface.TAG_DATETIME_DIGITIZED,
                        ExifInterface.TAG_EXPOSURE_TIME,
                        ExifInterface.TAG_F_NUMBER,
                        ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                        ExifInterface.TAG_FOCAL_LENGTH,
                        ExifInterface.TAG_LENS_MAKE,
                        ExifInterface.TAG_LENS_MODEL
                    )
                    tagsToClear.forEach { tag -> exif.setAttribute(tag, null) }
                    exif.saveAttributes()
                }
            } catch (_: Exception) {}

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Renames media in MediaStore.
     */
    suspend fun renameMedia(context: Context, photo: Photo, newName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val extension = photo.name.substringAfterLast('.', "")
            val finalName = if (extension.isNotEmpty() && !newName.endsWith(".$extension", ignoreCase = true)) {
                "$newName.$extension"
            } else {
                newName
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, finalName)
            }
            val rows = context.contentResolver.update(photo.uri, values, null, null)
            if (rows > 0) {
                Result.success(finalName)
            } else {
                Result.failure(Exception("Failed to update media name"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
