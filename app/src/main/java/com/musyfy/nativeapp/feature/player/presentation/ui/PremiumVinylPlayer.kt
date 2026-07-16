package com.musyfy.nativeapp.feature.player.presentation.ui

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.musyfy.nativeapp.R


@Composable
fun PremiumVinylPlayer(
    imageUrl: Any?,
    isPlaying: Boolean,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. Natural acceleration/deceleration simulation
    var rotationAngle by remember { mutableStateOf(0f) }
    var currentSpeed by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastTime = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val elapsedSec = (now - lastTime) / 1_000_000_000f
            lastTime = now

            val targetSpeed = if (isPlaying) 100f else 0f
            val accelRate = if (isPlaying) 120f else 60f // Smooth physical transition

            if (currentSpeed < targetSpeed) {
                currentSpeed = (currentSpeed + accelRate * elapsedSec).coerceAtMost(targetSpeed)
            } else if (currentSpeed > targetSpeed) {
                currentSpeed = (currentSpeed - accelRate * elapsedSec).coerceAtLeast(targetSpeed)
            }

            if (currentSpeed > 0f) {
                rotationAngle = (rotationAngle + currentSpeed * elapsedSec) % 360f
            }
            withFrameMillis { }
        }
    }

    // 2. Extracted palette colors for note animations
    val defaultColors = listOf(
        Color(0xFFE76F51),
        Color(0xFF2A9D8F),
        Color(0xFFE9C46A),
        Color(0xFFEF476F)
    )
    var extractedColors by remember { mutableStateOf(defaultColors) }
    val context = LocalContext.current

    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            val loader = coil.ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            try {
                val result = loader.execute(request)
                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    Palette.from(bitmap).generate { palette ->
                        palette?.let {
                            val colors = listOfNotNull(
                                it.getVibrantColor(0),
                                it.getLightVibrantColor(0),
                                it.getDominantColor(0),
                                it.getMutedColor(0)
                            ).filter { code -> code != 0 }
                                .map { code -> Color(code) }

                            if (colors.isNotEmpty()) {
                                extractedColors = colors
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                extractedColors = defaultColors
            }
        } else {
            extractedColors = defaultColors
        }
    }

    // Colors adjusted for Light/Dark modes
    val containerTextColor = if (isLightMode) Color.Black else Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center
    ) {
        // 3. Offset square album cover behind the vinyl
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp, y = (-10).dp)
                .size(190.dp)
                .shadow(
                    elevation = if (isLightMode) 8.dp else 16.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLightMode) Color(0xFFF0F0F0) else Color(0xFF151515))
        ) {
            Crossfade(targetState = imageUrl, animationSpec = tween(500), label = "BgAlbumCover") { targetImg ->
                AsyncImage(
                    model = targetImg ?: R.drawable.logo,
                    contentDescription = "Album Cover Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
            }
        }

        // 4. Large Floating Vinyl (overlapping the album cover)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-10).dp, y = 10.dp)
                .graphicsLayer {
                    rotationZ = rotationAngle
                }
                .size(236.dp)
                .shadow(
                    elevation = if (isLightMode) 12.dp else 24.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .background(Color(0xFF0C0C0E), CircleShape)
        ) {
            // Draw physical vinyl grooves and reflections using Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val center = Offset(width / 2f, height / 2f)
                val maxRadius = width / 2f

                // Draw grooves concentric circles
                val grooveCount = 45
                for (i in 0 until grooveCount) {
                    val r = maxRadius - 10f - (i * 2.2f)
                    if (r > 46f) { // Stop before reaching the center label area
                        drawCircle(
                            color = Color.White.copy(alpha = 0.035f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 0.7f)
                        )
                    }
                }

                // Draw realistic radial sweep reflection (bowtie sheen)
                val reflectionBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center
                )
                drawCircle(
                    brush = reflectionBrush,
                    radius = maxRadius,
                    center = center
                )
            }

            // Rotating Vinyl Center Label (Album Artwork)
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(0xFF151517))
                    .border(1.5.dp, Color(0xFF050507), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = imageUrl, animationSpec = tween(500), label = "VinylLabel") { targetImg ->
                    AsyncImage(
                        model = targetImg ?: R.drawable.logo,
                        contentDescription = "Vinyl Center Label Artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                }

                // Metal Spindle Center
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFE2E8F0), Color(0xFF64748B))),
                            CircleShape
                        )
                        .border(0.8.dp, Color(0xFF475569), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color(0xFF0F0F11), CircleShape)
                    )
                }
            }
        }

        // 5. Thin Premium Tonearm swing (rests/plays in sync with playback)
        PremiumToneArm(
            isPlaying = isPlaying,
            isLightMode = isLightMode,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-20).dp)
        )

        // 6. Subtle pausable Floating Music Notes
        PremiumMusicNotes(
            isPlaying = isPlaying,
            colorsPool = extractedColors,
            textColor = containerTextColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-30).dp)
        )
    }
}

@Composable
fun PremiumToneArm(
    isPlaying: Boolean,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 16f else -18f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "PremiumTonearmAngle"
    )

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 144.dp)
            .graphicsLayer {
                rotationZ = tonearmAngle
                transformOrigin = TransformOrigin(0.5f, 0.1388f) // Pivot center
            }
    ) {
        // Metallic shaft arm (silver curved metal line)
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-18).dp)
                .size(width = 4.dp, height = 98.dp)
                .graphicsLayer {
                    rotationZ = -6f
                    transformOrigin = TransformOrigin(0.5f, 0.0f)
                }
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFE4E4E7), Color(0xFF71717A))),
                    RoundedCornerShape(2.dp)
                )
        )

        // Sleek Headshell Cartridge (metallic matching color)
        Box(
            modifier = Modifier
                .offset(x = (-27).dp, y = 112.dp)
                .align(Alignment.TopEnd)
                .size(width = 14.dp, height = 32.dp)
                .graphicsLayer {
                    rotationZ = -22f
                }
                .background(Color(0xFF1E1E22), RoundedCornerShape(1.dp))
                .border(0.8.dp, Color(0xFF3F3F46), RoundedCornerShape(1.dp))
        )

        // Glass shadow ring behind pivot base
        val pivotGlassBg = if (isLightMode) Color.Black.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.05f)
        val pivotGlassBorder = if (isLightMode) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.12f)

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(44.dp)
                .background(pivotGlassBg, CircleShape)
                .border(0.8.dp, pivotGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Brushed metallic Pivot Center knob
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFF4F4F5), Color(0xFF71717A))),
                        CircleShape
                    )
                    .border(0.8.dp, Color(0xFF52525B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF1E1E22), CircleShape)
                )
            }
        }
    }
}

@Composable
fun PremiumMusicNotes(
    isPlaying: Boolean,
    colorsPool: List<Color>,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val notesList = remember { mutableStateListOf<FloatingNote>() }

    LaunchedEffect(isPlaying, colorsPool) {
        if (isPlaying) {
            val symbols = listOf("♪", "♫", "♬", "♩")
            while (true) {
                val symbol = symbols.random()
                val color = if (colorsPool.isNotEmpty()) colorsPool.random() else textColor
                
                // Spawn near the left or right edge of the vinyl
                val isLeft = Math.random() < 0.5
                val xOffset = if (isLeft) {
                    0.20f + (Math.random() * 0.08f).toFloat() // Spawn near upper-left
                } else {
                    0.72f + (Math.random() * 0.08f).toFloat() // Spawn near upper-right
                }
                
                val scale = 0.7f + (Math.random() * 0.3f).toFloat()

                notesList.add(
                    FloatingNote(
                        id = System.currentTimeMillis() + (Math.random() * 1000).toLong(),
                        xOffsetPct = xOffset,
                        symbol = symbol,
                        color = color.copy(alpha = 0.55f),
                        scale = scale
                    )
                )
                kotlinx.coroutines.delay(800) // Tasteful, non-distracting interval
            }
        }
    }

    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.BottomCenter
    ) {
        notesList.forEach { note ->
            key(note.id) {
                PremiumAnimatedNote(note = note, onAnimationEnd = { notesList.remove(note) })
            }
        }
    }
}

@Composable
fun PremiumAnimatedNote(
    note: FloatingNote,
    onAnimationEnd: () -> Unit
) {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    val progress = animatable.value
    val density = LocalDensity.current
    
    // Legacy vertical translation (200dp)
    val yTranslation = -progress * 200.dp.value * density.density
    
    // Legacy horizontal drift / wobble (sinuous wave)
    val xTranslation = Math.sin(progress.toDouble() * Math.PI * 2.0).toFloat() * 16.dp.value * density.density
    
    // Legacy alpha curve (fade in then fade out)
    val alpha = if (progress < 0.2f) progress / 0.2f else (1f - progress) / 0.8f

    Text(
        text = note.symbol,
        color = note.color,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = ((note.xOffsetPct - 0.5f) * 236.dp.value * density.density + xTranslation).toInt(),
                    y = yTranslation.toInt()
                )
            }
            .alpha(alpha)
            .scale(note.scale + progress * 0.2f)
    )
}
