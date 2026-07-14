package com.musyfy.nativeapp.feature.appearance.presentation.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CropScreen(
    bitmap: Bitmap,
    onCropDone: (Bitmap) -> Unit,
    onCancel: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val targetRatio = screenWidth / screenHeight

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        // Crop Area containing the image with pan/zoom gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { containerSize = it.size }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = Offset(
                            x = offset.x + pan.x,
                            y = offset.y + pan.y
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val imageWidth = bitmap.width.toFloat()
                val imageHeight = bitmap.height.toFloat()
                val imageRatio = imageWidth / imageHeight

                // Find initial drawing size to fit within screen
                val drawSize = if (imageRatio > targetRatio) {
                    val h = size.height
                    val w = size.height * imageRatio
                    Offset((size.width - w) / 2f, 0f) to IntSize(w.toInt(), h.toInt())
                } else {
                    val w = size.width
                    val h = size.width / imageRatio
                    Offset(0f, (size.height - h) / 2f) to IntSize(w.toInt(), h.toInt())
                }

                val initialOffset = drawSize.first
                val size = drawSize.second

                withTransform({
                    translate(initialOffset.x + offset.x, initialOffset.y + offset.y)
                    scale(scale, scale, pivot = Offset(size.width / 2f, size.height / 2f))
                }) {
                    drawImage(
                        image = bitmap.asImageBitmap(),
                        dstSize = size
                    )
                }
            }
        }

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Crop Wallpaper",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable {
                        scale = 1f
                        offset = Offset.Zero
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Reset",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Navigation Actions
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E20))
                    .clickable { onCancel() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor)
                    .clickable {
                        val cropped = cropBitmap(bitmap, scale, offset, containerSize, targetRatio)
                        if (cropped != null) {
                            onCropDone(cropped)
                        } else {
                            onCancel()
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Next",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Renders the visible region of the user's viewport to a 1080px wide cropped Bitmap.
 */
private fun cropBitmap(
    src: Bitmap,
    zoom: Float,
    pan: Offset,
    containerSize: IntSize,
    targetRatio: Float
): Bitmap? {
    return try {
        val targetWidth = 1080
        val targetHeight = (1080 / targetRatio).toInt()
        val cropped = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(cropped)

        val imageWidth = src.width.toFloat()
        val imageHeight = src.height.toFloat()
        val imageRatio = imageWidth / imageHeight

        val containerWidth = containerSize.width.toFloat()
        val containerHeight = containerSize.height.toFloat()

        val (fitWidth, fitHeight) = if (imageRatio > targetRatio) {
            val h = containerHeight
            val w = containerHeight * imageRatio
            w to h
        } else {
            val w = containerWidth
            val h = containerWidth / imageRatio
            w to h
        }

        val initialX = (containerWidth - fitWidth) / 2f
        val initialY = (containerHeight - fitHeight) / 2f

        val targetScale = 1080f / containerWidth
        val matrix = android.graphics.Matrix()

        val drawX = initialX + pan.x
        val drawY = initialY + pan.y
        val pivotX = drawX + fitWidth / 2f
        val pivotY = drawY + fitHeight / 2f

        matrix.postScale(fitWidth / imageWidth, fitHeight / imageHeight)
        matrix.postTranslate(initialX, initialY)
        matrix.postTranslate(pan.x, pan.y)
        matrix.postScale(zoom, zoom, pivotX, pivotY)
        matrix.postScale(targetScale, targetScale)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        canvas.drawBitmap(src, matrix, paint)
        cropped
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
