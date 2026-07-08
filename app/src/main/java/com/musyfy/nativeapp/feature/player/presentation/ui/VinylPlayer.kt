package com.musyfy.nativeapp.feature.player.presentation.ui

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import kotlinx.coroutines.delay

data class FloatingNote(
    val id: Long,
    val xOffsetPct: Float,
    val symbol: String,
    val color: Color,
    val scale: Float
)

@Composable
fun VinylPlayer(
    imageUrl: Any?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableStateOf(0f) }

    // Smooth frame-independent 60fps rotation that freezes instantly on pause
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTime = System.nanoTime()
            while (true) {
                val now = System.nanoTime()
                val elapsedSec = (now - lastTime) / 1_000_000_000f
                // Rotate 120 degrees per second (3 seconds per full turn)
                val deltaAngle = elapsedSec * 120f
                rotationAngle = (rotationAngle + deltaAngle) % 360f
                lastTime = now
                withFrameMillis { }
            }
        }
    }

    // Dynamic color palette extracted from loaded bitmap, falling back to legacy colors
    val defaultColors = listOf(
        Color(0xFFF4A261),
        Color(0xFFE76F51),
        Color(0xFF2A9D8F),
        Color(0xFFE9C46A),
        Color(0xFFEF476F)
    )
    var extractedColors by remember { mutableStateOf(defaultColors) }
    val context = LocalContext.current

    // Extract dominant colors from Coil artwork to color the music notes
    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            val loader = coil.ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Required to convert to bitmap for Palette extraction
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
                            ).filter { colorCode -> colorCode != 0 }
                                .map { colorCode -> Color(colorCode) }

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

    Box(
        modifier = modifier
            .size(width = 300.dp, height = 340.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 3D Bevel Shadow Plate Casing underneath
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 300.dp)
                .background(Color(0xFFE76F51).copy(alpha = 0.4f), RoundedCornerShape(40.dp))
                .offset(x = 10.dp, y = 10.dp)
        )

        // Retro Turntable Box Casing
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 300.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFD166), Color(0xFFF4A261))),
                    RoundedCornerShape(40.dp)
                )
                .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(40.dp))
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Casing Inner Bevel line
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color(0x1EFF476F), RoundedCornerShape(32.dp))
            )

            // Accent knob on bottom-left
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 12.dp, y = (-12).dp)
                    .background(Color(0xFF27272A), CircleShape)
                    .border(2.dp, Color(0xFF52525B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFA1A1AA), CircleShape))
            }

            // Small accent lights next to knob
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 52.dp, y = (-16).dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF2A9D8F), CircleShape))
                Box(modifier = Modifier.size(10.dp).background(Color(0xFFEF476F), CircleShape))
            }

            // Vent grills on bottom-right
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = (-16).dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 3.dp)
                            .background(Color(0xFFE76F51).copy(alpha = 0.6f), RoundedCornerShape(1.dp))
                    )
                }
            }

            // Turntable Platter (concentric sound grooves)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(y = 4.dp)
                    .background(Color(0xFF111111), CircleShape)
                    .border(4.dp, Color(0xFF27272A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl Groove concentric rings
                Box(modifier = Modifier.size(208.dp).border(1.dp, Color(0x2671717A), CircleShape))
                Box(modifier = Modifier.size(188.dp).border(1.dp, Color(0x1F71717A), CircleShape))
                Box(modifier = Modifier.size(168.dp).border(1.dp, Color(0x1771717A), CircleShape))
                Box(modifier = Modifier.size(148.dp).border(1.dp, Color(0x0F71717A), CircleShape))
                Box(modifier = Modifier.size(128.dp).border(1.dp, Color(0x0A71717A), CircleShape))

                // Rotating Vinyl Center Label (Album Artwork)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            rotationZ = rotationAngle
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF18181B))
                        .border(2.dp, Color(0xFF09090B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Center label image fills circle completely with no gaps
                    AsyncImage(
                        model = imageUrl ?: R.drawable.logo,
                        contentDescription = "Vinyl Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )

                    // Spindle Hole Cover
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8))),
                                CircleShape
                            )
                            .border(1.dp, Color(0xFF64748B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF111111), CircleShape)
                        )
                    }
                }
            }

            // Tonearm component (Metallic shaft overlapping Platter)
            ToneArm(
                isPlaying = isPlaying,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 12.dp)
            )
        }

        // Floating Music Notes Particles Container (rendered on top of casing so they are visible)
        MusicNotes(
            isPlaying = isPlaying,
            colorsPool = extractedColors,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
fun ToneArm(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 20f else -15f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "TonearmAngle"
    )

    // Parent container corresponding to w-10 h-36 (40dp x 144dp)
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 144.dp)
            .graphicsLayer {
                rotationZ = tonearmAngle
                transformOrigin = TransformOrigin(0.5f, 0.1388f) // Pivot at center of 40dp base (20dp/144dp)
            }
    ) {
        // Metallic shaft arm (top=32dp, right=17dp, width=6dp, height=96dp)
        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-17).dp)
                .size(width = 6.dp, height = 96.dp)
                .graphicsLayer {
                    rotationZ = -6f
                    transformOrigin = TransformOrigin(0.5f, 0.0f) // origin-top
                }
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFD4D4D8), Color(0xFF71717A))),
                    RoundedCornerShape(3.dp)
                )
        )

        // Headshell Cartridge (top=110dp, right=28dp, width=20dp, height=40dp)
        Box(
            modifier = Modifier
                .offset(x = (-28).dp, y = 110.dp)
                .align(Alignment.TopEnd)
                .size(width = 20.dp, height = 40.dp)
                .graphicsLayer {
                    rotationZ = -25f
                }
                .background(Color(0xFF27272A), RoundedCornerShape(2.dp))
                .border(1.dp, Color(0xFF52525B), RoundedCornerShape(2.dp))
        ) {
            // Pin on headshell (w=4dp, h=12dp, bottom=4dp, right=(-4)dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(width = 4.dp, height = 12.dp)
                    .background(Color(0xFFA1A1AA), CircleShape)
            )
        }

        // Base Pivot knob (w=40dp, h=40dp, top=0, right=0)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFE4E4E7), Color(0xFFA1A1AA))),
                    CircleShape
                )
                .border(1.dp, Color(0xFF71717A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner ring
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFF27272A), CircleShape)
                    .border(2.dp, Color(0xFF52525B), CircleShape)
            )
        }
    }
}

@Composable
fun MusicNotes(
    isPlaying: Boolean,
    colorsPool: List<Color>,
    modifier: Modifier = Modifier
) {
    val notesList = remember { mutableStateListOf<FloatingNote>() }

    LaunchedEffect(isPlaying, colorsPool) {
        if (isPlaying) {
            val symbols = listOf("♪", "♫", "♬", "♩")
            while (true) {
                val symbol = symbols.random()
                val color = colorsPool.random()
                // Random horizontal position relative to casing width (15% to 85% range)
                val xOffset = 0.15f + (Math.random() * 0.7f).toFloat()
                val scale = 0.8f + (Math.random() * 0.4f).toFloat()

                notesList.add(
                    FloatingNote(
                        id = System.currentTimeMillis() + (Math.random() * 1000).toLong(),
                        xOffsetPct = xOffset,
                        symbol = symbol,
                        color = color,
                        scale = scale
                    )
                )
                delay(600)
            }
        } else {
            notesList.clear()
        }
    }

    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.BottomCenter
    ) {
        notesList.forEach { note ->
            key(note.id) {
                AnimatedNote(note = note, onAnimationEnd = { notesList.remove(note) })
            }
        }
    }
}

@Composable
fun AnimatedNote(
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
    // Float upwards by 200dp
    val yTranslation = -progress * 200.dp.value * density.density
    // Wobble left and right (sinuous wave)
    val xTranslation = Math.sin(progress.toDouble() * Math.PI * 2.0).toFloat() * 16.dp.value * density.density
    val alpha = if (progress < 0.2f) progress / 0.2f else (1f - progress) / 0.8f

    Text(
        text = note.symbol,
        color = note.color,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = ((note.xOffsetPct - 0.5f) * 260.dp.value * density.density + xTranslation).toInt(),
                    y = yTranslation.toInt()
                )
            }
            .alpha(alpha)
            .scale(note.scale + progress * 0.2f)
    )
}
