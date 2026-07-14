package com.musyfy.nativeapp.feature.appearance.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object WallpaperProcessor {

    /**
     * Decodes a Uri into a Bitmap safely, correcting rotation based on EXIF metadata.
     */
    fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            // Query EXIF orientation
            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exifInterface = ExifInterface(stream)
                    exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            } ?: ExifInterface.ORIENTATION_NORMAL

            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotationAngle != 0f) {
                val matrix = Matrix().apply { postRotate(rotationAngle) }
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle()
                }
                rotatedBitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resizes the bitmap (longest side max 1080px), encodes as lossy WEBP (85% quality), 
     * deletes the old wallpaper, and saves it locally.
     */
    fun processAndSaveWallpaper(context: Context, bitmap: Bitmap): File? {
        return try {
            val themeDir = File(context.filesDir, "theme")
            if (!themeDir.exists()) {
                themeDir.mkdirs()
            }

            val targetFile = File(themeDir, "custom_background.webp")
            if (targetFile.exists()) {
                targetFile.delete()
            }

            // Define resizing bounds
            val maxDimension = 1080
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height

            val (targetWidth, targetHeight) = if (originalWidth > originalHeight) {
                if (originalWidth > maxDimension) {
                    val ratio = maxDimension.toFloat() / originalWidth
                    maxDimension to (originalHeight * ratio).toInt()
                } else {
                    originalWidth to originalHeight
                }
            } else {
                if (originalHeight > maxDimension) {
                    val ratio = maxDimension.toFloat() / originalHeight
                    (originalWidth * ratio).toInt() to maxDimension
                } else {
                    originalWidth to originalHeight
                }
            }

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

            // Compress to webp (85%)
            FileOutputStream(targetFile).use { out ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    resizedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, out)
                } else {
                    @Suppress("DEPRECATION")
                    resizedBitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
                }
            }

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
