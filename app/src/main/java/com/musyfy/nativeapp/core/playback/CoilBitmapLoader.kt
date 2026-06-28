package com.musyfy.nativeapp.core.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture

class CoilBitmapLoader(private val context: Context) : BitmapLoader {
    private val imageLoader = ImageLoader(context)

    override fun supportsMimeType(mimeType: String): Boolean {
        return true
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        val request = ImageRequest.Builder(context)
            .data(data)
            .allowHardware(false) // Required to convert to bitmap
            .target(object : coil.target.Target {
                override fun onSuccess(result: Drawable) {
                    if (result is BitmapDrawable) {
                        future.set(result.bitmap)
                    } else {
                        future.setException(Exception("Decoded drawable is not a BitmapDrawable"))
                    }
                }

                override fun onError(error: Drawable?) {
                    future.setException(Exception("Failed to decode bitmap"))
                }
            })
            .build()
        imageLoader.enqueue(request)
        return future
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        val request = ImageRequest.Builder(context)
            .data(uri)
            .allowHardware(false) // Required to convert to bitmap
            .target(object : coil.target.Target {
                override fun onSuccess(result: Drawable) {
                    if (result is BitmapDrawable) {
                        future.set(result.bitmap)
                    } else {
                        future.setException(Exception("Loaded drawable is not a BitmapDrawable"))
                    }
                }

                override fun onError(error: Drawable?) {
                    future.setException(Exception("Failed to load bitmap from uri"))
                }
            })
            .build()
        imageLoader.enqueue(request)
        return future
    }
}
