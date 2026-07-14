package com.musyfy.nativeapp.feature.download.presentation.ui

import android.graphics.drawable.BitmapDrawable
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.feature.download.data.YoutubeVideoInfo
import com.musyfy.nativeapp.feature.download.presentation.PreviewUiState
import com.musyfy.nativeapp.feature.download.presentation.UploadViewModel
import com.musyfy.nativeapp.feature.player.presentation.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ImportState {
    Idle,
    Importing,
    Success,
    Error
}

// Custom Premium Vectors to guarantee build stability without external asset issues
val YoutubeIcon = ImageVector.Builder(
    name = "Youtube",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color(0xFFFF0000))) {
        moveTo(23.498f, 6.163f)
        curveTo(23.324f, 5.061f, 22.56f, 4.197f, 21.56f, 4.0f)
        curveTo(19.678f, 3.663f, 12f, 3.663f, 12f, 3.663f)
        reflectiveCurveTo(4.322f, 3.663f, 2.44f, 4.0f)
        curveTo(1.44f, 4.197f, 0.676f, 5.061f, 0.502f, 6.163f)
        curveTo(0.165f, 8.441f, 0.165f, 12f, 0.165f, 12f)
        reflectiveCurveTo(0.165f, 15.559f, 0.502f, 17.837f)
        curveTo(0.676f, 18.939f, 1.44f, 19.803f, 2.44f, 20.0f)
        curveTo(4.322f, 20.337f, 12f, 20.337f, 12f, 20.337f)
        reflectiveCurveTo(19.678f, 20.337f, 21.56f, 20.0f)
        curveTo(22.56f, 19.803f, 23.324f, 18.939f, 23.498f, 17.837f)
        curveTo(23.835f, 15.559f, 23.835f, 12f, 23.835f, 12f)
        reflectiveCurveTo(23.835f, 8.441f, 23.498f, 6.163f)
        close()
    }
    path(fill = SolidColor(Color.White)) {
        moveTo(9.545f, 15.568f)
        lineTo(15.818f, 12.0f)
        lineTo(9.545f, 8.432f)
        close()
    }
}.build()

val ClearIcon = ImageVector.Builder(
    name = "Clear",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round
    ) {
        moveTo(6f, 6f)
        lineTo(18f, 18f)
        moveTo(18f, 6f)
        lineTo(6f, 18f)
    }
}.build()

val SuccessCheckmarkIcon = ImageVector.Builder(
    name = "SuccessCheckmark",
    defaultWidth = 64.dp,
    defaultHeight = 64.dp,
    viewportWidth = 64f,
    viewportHeight = 64f
).apply {
    path(
        stroke = SolidColor(Color(0xFF4CAF50)),
        strokeLineWidth = 4f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(18f, 32f)
        lineTo(28f, 42f)
        lineTo(46f, 22f)
    }
}.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: PlayerViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uploadViewModel: UploadViewModel = hiltViewModel()
    val previewState by uploadViewModel.previewState.collectAsState()

    var url by remember { mutableStateOf("") }
    var importState by remember { mutableStateOf(ImportState.Idle) }
    var error by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Trigger metadata preview fetch when URL changes
    LaunchedEffect(url) {
        if (url.trim().isNotEmpty()) {
            uploadViewModel.fetchPreview(url)
        } else {
            uploadViewModel.clearPreview()
        }
    }

    // Unpack preview states
    val previewInfo = (previewState as? PreviewUiState.Success)?.info
    val isFetchingPreview = previewState is PreviewUiState.Loading
    val previewError = (previewState as? PreviewUiState.Error)?.message

    // Palette Color Extraction
    val primaryColor = MaterialTheme.colorScheme.primary
    var glowColor by remember(primaryColor) { mutableStateOf(primaryColor) } // Dynamic active accent fallback
    LaunchedEffect(previewInfo?.thumbnail, primaryColor) {
        val thumbnailUrl = previewInfo?.thumbnail
        if (thumbnailUrl != null) {
            val loader = coil.ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(thumbnailUrl)
                .allowHardware(false)
                .build()
            try {
                val result = loader.execute(request)
                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    Palette.from(bitmap).generate { palette ->
                        palette?.let {
                            val color = it.getVibrantColor(0)
                                .takeIf { it != 0 }
                                ?: it.getDominantColor(0)
                                .takeIf { it != 0 }
                                ?: it.getMutedColor(0)
                                .takeIf { it != 0 }
                                ?: primaryColor.toArgb()
                            glowColor = Color(color)
                        }
                    }
                }
            } catch (e: Exception) {
                glowColor = primaryColor
            }
        } else {
            glowColor = primaryColor
        }
    }

    val animatedGlowColor by animateColorAsState(
        targetValue = glowColor,
        animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
        label = "GlowColor"
    )

    // Dynamic background glow opacity
    val glowOpacity by animateFloatAsState(
        targetValue = when (importState) {
            ImportState.Importing -> 0.70f
            ImportState.Success -> 0.85f
            else -> 0.35f
        },
        animationSpec = tween(700),
        label = "GlowOpacity"
    )

    // Slow (idle) and fast (importing) vinyl rotation
    var rotationAngle by remember { mutableStateOf(0f) }
    LaunchedEffect(importState) {
        val speed = when (importState) {
            ImportState.Importing -> 360f // 360 deg/sec fast rotation
            else -> 40f  // 40 deg/sec slow idle rotation
        }
        var lastTime = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val elapsedSec = (now - lastTime) / 1_000_000_000f
            rotationAngle = (rotationAngle + elapsedSec * speed) % 360f
            lastTime = now
            withFrameMillis { }
        }
    }

    var showVinyl by remember { mutableStateOf(false) }
    LaunchedEffect(previewInfo) {
        if (previewInfo != null) {
            delay(300)
            showVinyl = true
        } else {
            showVinyl = false
        }
    }
    val vinylOffsetX by animateDpAsState(
        targetValue = if (showVinyl) 48.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "VinylOffsetX"
    )

    // Floating & rotation infinite animation transitions
    val infiniteTransition = rememberInfiniteTransition(label = "HeroInfinite")
    val floatTranslationY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatTranslationY"
    )
    val progressRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "ProgressRotation"
    )

    // Importing Sequenced Status Messages
    val statusMessages = listOf(
        "Preparing artwork...",
        "Loading metadata...",
        "Matching colors...",
        "Building your library...",
        "Saving offline...",
        "Almost done..."
    )
    var currentStatusIndex by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf(statusMessages[0]) }
    LaunchedEffect(importState) {
        if (importState == ImportState.Importing) {
            currentStatusIndex = 0
            while (importState == ImportState.Importing) {
                statusText = statusMessages[currentStatusIndex]
                delay(1200)
                currentStatusIndex = (currentStatusIndex + 1) % statusMessages.size
            }
        }
    }

    // Success Card Fly-Down Animation parameters
    var isFlying by remember { mutableStateOf(false) }
    val flyProgress by animateFloatAsState(
        targetValue = if (isFlying) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "FlyProgress"
    )

    // Scaling/Translation factor for fly animation
    val density = LocalDensity.current.density
    val flyTranslationX = if (isFlying) (-120f) * flyProgress else 0f
    val flyTranslationY = if (isFlying) 500f * flyProgress else 0f
    val flyScale = if (isFlying) 1.15f * (1f - flyProgress) else 1.0f
    val flyAlpha = if (isFlying) 1f - flyProgress else 1f

    // Scale growth factor when metadata loads or during success checkmark
    val scaleFactor by animateFloatAsState(
        targetValue = when (importState) {
            ImportState.Importing -> 1.05f
            ImportState.Success -> 0.85f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "ScaleFactor"
    )

    // No background card colors needed as we render only the rotating glassy vinyl// No background card colors needed as we render only the rotating glassy vinyl

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent) // Make parent completely transparent for wallpaper system
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Dynamic Radial Ambient Glow behind the hero
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(320.dp)
                .blur(72.dp)
                .alpha(glowOpacity)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedGlowColor.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Bar indicator mimic
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ================= SECTION 2: Title and Subtitle =================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Build Your Library",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paste any YouTube link and Musyfy will turn it into a beautiful addition to your personal music library.",
                    color = Color(0xAAFFFFFF),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ================= SECTION 3: Premium URL Input =================
            var isFocused by remember { mutableStateOf(false) }
            val inputScale by animateFloatAsState(
                targetValue = if (isFocused) 1.015f else 1.0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
                label = "InputScale"
            )

            OutlinedTextField(
                value = url,
                onValueChange = {
                    url = it
                    error = ""
                },
                placeholder = {
                    Text(
                        "Paste YouTube Link Here...",
                        color = Color(0x55FFFFFF),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x0AFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = YoutubeIcon,
                        contentDescription = "YouTube",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (url.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    url = ""
                                    uploadViewModel.clearPreview()
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = ClearIcon,
                                contentDescription = "Clear",
                                tint = Color(0x88FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "PASTE",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    val clipText = clipboardManager.getText()?.text.orEmpty()
                                    if (clipText.isNotEmpty()) {
                                        url = clipText
                                        uploadViewModel.fetchPreview(clipText)
                                    }
                                }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(inputScale)
                    .graphicsLayer {
                        // Soft glow outline shadow mapping when focused
                        if (isFocused) {
                            shadowElevation = 8f
                        }
                    }
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )

            // Elegant inline validation check / focus listener callback mimic
            DisposableEffect(Unit) {
                onDispose {
                    isFocused = false
                }
            }

            // Keyboard Focus Monitor Hook
            LaunchedEffect(url) {
                // Focus changes trigger subtle input indicator haptics
                if (url.isNotEmpty() && isFocused) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }

            // ================= SECTION 4: Live Preview & Error States =================
            AnimatedVisibility(
                visible = previewState is PreviewUiState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val errorMsg = previewError ?: "Invalid URL pattern matched"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AEF5350), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x33EF5350), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️  $errorMsg",
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Retry Preview",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    uploadViewModel.fetchPreview(url, force = true)
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        Text(
                            text = "Edit URL",
                            color = Color(0xAAFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0x11FFFFFF), RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Premium Live Preview Card (incorporating Hero animations, metadata, and status text)
            AnimatedVisibility(
                visible = previewInfo != null,
                enter = fadeIn(animationSpec = tween(500)) + expandVertically(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            ) {
                if (previewInfo != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x0FFFFFFF), RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                            .padding(top = 20.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Container: Large album artwork with rotating vinyl sliding out from behind
                        Box(
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth()
                                .graphicsLayer {
                                    this.translationX = flyTranslationX * density
                                    this.translationY = flyTranslationY * density
                                    this.scaleX = flyScale * scaleFactor
                                    this.scaleY = flyScale * scaleFactor
                                    this.alpha = flyAlpha
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Layer 1: Two rotated/blurred background artwork cards
                            // Left blurred card
                            AsyncImage(
                                model = previewInfo.thumbnail,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(110.dp)
                                    .offset(x = (-28).dp, y = 6.dp)
                                    .graphicsLayer { rotationZ = -10f }
                                    .alpha(0.4f)
                                    .blur(6.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Right blurred card
                            AsyncImage(
                                model = previewInfo.thumbnail,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(110.dp)
                                    .offset(x = 28.dp, y = 6.dp)
                                    .graphicsLayer { rotationZ = 10f }
                                    .alpha(0.4f)
                                    .blur(6.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Layer 2: Realistic rotating vinyl record (slides out to the right)
                            Box(
                                modifier = Modifier
                                    .size(118.dp)
                                    .offset(x = vinylOffsetX)
                                    .graphicsLayer {
                                        rotationZ = rotationAngle
                                    }
                                    .shadow(elevation = 6.dp, shape = CircleShape)
                                    .background(Color(0xFF0F0F10), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Concentric sound grooves using nested borders (no Canvas)
                                Box(modifier = Modifier.size(110.dp).border(0.5.dp, Color.White.copy(alpha = 0.08f), CircleShape))
                                Box(modifier = Modifier.size(100.dp).border(0.5.dp, Color.White.copy(alpha = 0.06f), CircleShape))
                                Box(modifier = Modifier.size(90.dp).border(0.5.dp, Color.White.copy(alpha = 0.04f), CircleShape))
                                Box(modifier = Modifier.size(80.dp).border(0.5.dp, Color.White.copy(alpha = 0.03f), CircleShape))
                                Box(modifier = Modifier.size(70.dp).border(0.5.dp, Color.White.copy(alpha = 0.02f), CircleShape))

                                // Glossy anisotropic sweep highlight reflection
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.sweepGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.04f),
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.04f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )

                                // Center label (artwork thumbnail)
                                Box(
                                    modifier = Modifier
                                        .size(118.dp * 0.35f)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E1E20))
                                        .border(1.dp, Color.Black, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = previewInfo.thumbnail,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Spindle Hole
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.Black, CircleShape)
                                    )
                                }
                            }

                            // Layer 3: Main Artwork Card (floats gently in front)
                            Box(
                                modifier = Modifier
                                    .size(122.dp)
                                    .graphicsLayer {
                                        translationY = floatTranslationY * density
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Sweeping circular progress ring around the artwork card during import
                                if (importState == ImportState.Importing) {
                                    Canvas(modifier = Modifier.size(130.dp)) {
                                        drawArc(
                                            color = primaryColor,
                                            startAngle = progressRotation,
                                            sweepAngle = 120f,
                                            useCenter = false,
                                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }

                                // The Artwork Card itself
                                Box(
                                    modifier = Modifier
                                        .size(122.dp)
                                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                                        .background(Color(0xFF121214), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(previewInfo.thumbnail)
                                            .crossfade(true)
                                            .crossfade(600)
                                            .build(),
                                        contentDescription = "Album Artwork",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Glass sheen reflection overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.15f),
                                                        Color.White.copy(alpha = 0.02f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }

                                // Success Checkmark overlay
                                if (importState == ImportState.Success) {
                                    Box(
                                        modifier = Modifier
                                            .size(122.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = SuccessCheckmarkIcon,
                                            contentDescription = "Success",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier
                                                .size(44.dp)
                                                .scale(scaleFactor)
                                        )
                                    }
                                }
                            }
                        }

                        // Song Details block below the Hero
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = previewInfo.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = previewInfo.uploader ?: "Unknown Artist",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Badge("YouTube", Color(0xFFFF0000).copy(alpha = 0.12f), Color(0xFFFF4D4D))
                                Badge("HQ Audio", MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary)
                                val durationText = remember(previewInfo.duration) {
                                    val d = previewInfo.duration ?: 0L
                                    val m = d / 60
                                    val s = d % 60
                                    "$m:${if (s < 10) "0" else ""}$s"
                                }
                                Badge(durationText, Color(0x10FFFFFF), Color(0xFFCCCCCC))
                            }
                        }

                        // Cycling status messages replacing progress indicators during import
                        AnimatedVisibility(
                            visible = importState == ImportState.Importing,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = statusText,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Feature chips (only visible when there is no preview matching / empty state)
            AnimatedVisibility(
                visible = previewInfo == null && !isFetchingPreview && previewState !is PreviewUiState.Error,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeatureChip("✓ High Quality")
                        FeatureChip("✓ Instant Streaming")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeatureChip("✓ Offline Ready")
                        FeatureChip("✓ Private Library")
                    }
                }
            }

            // ================= SECTION 5: Primary CTA Button =================
            val isButtonEnabled = url.trim().isNotEmpty() && previewInfo != null && importState == ImportState.Idle
            val btnInteractionSource = remember { MutableInteractionSource() }
            val isBtnPressed by btnInteractionSource.collectIsPressedAsState()

            val btnScale by animateFloatAsState(
                targetValue = if (isBtnPressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                label = "BtnScale"
            )

            // Button morphs smaller during import phase
            val btnWidthFraction by animateFloatAsState(
                targetValue = if (importState == ImportState.Importing) 0.65f else 1.0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
                label = "BtnWidth"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(btnWidthFraction)
                    .height(52.dp)
                    .scale(btnScale)
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isButtonEnabled || importState != ImportState.Idle) {
                                listOf(Color(0xFF1A1A1C), MaterialTheme.colorScheme.primary, Color(0xFFFF8C42))
                            } else {
                                listOf(Color(0xFF2C2C2E), Color(0xFF1E1E20))
                            }
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .border(
                        1.dp,
                        if (isButtonEnabled || importState != ImportState.Idle) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(26.dp)
                    )
                    .clickable(
                        enabled = isButtonEnabled,
                        interactionSource = btnInteractionSource,
                        indication = null
                    ) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) // Medium tap haptic
                        importState = ImportState.Importing
                        keyboardController?.hide()
                        focusManager.clearFocus()

                        viewModel.importYoutubeSong(
                            url = url.trim(),
                            onSuccess = {
                                coroutineScope.launch {
                                    importState = ImportState.Success
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) // Heavy success haptic
                                    delay(1000) // Keep success checkmark visible
                                    isFlying = true // trigger flying transition
                                    delay(600) // flying transition completes
                                    importState = ImportState.Idle
                                    isFlying = false
                                    onNavigateToHome()
                                }
                            },
                            onError = { errMsg ->
                                importState = ImportState.Error
                                error = errMsg
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) // Soft warning tick
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = importState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "ButtonContent"
                ) { state ->
                    when (state) {
                        ImportState.Idle -> {
                            Text(
                                text = if (isButtonEnabled) "Add to Library →" else "Add to Library",
                                color = if (isButtonEnabled) Color.White else Color(0x66FFFFFF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ImportState.Importing -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Importing...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        ImportState.Success -> {
                            Text(
                                text = "Done ✓",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ImportState.Error -> {
                            Text(
                                text = "Error ✕",
                                color = Color(0xFFEF5350),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sequenced animated status messages visible during import
            AnimatedVisibility(
                visible = importState == ImportState.Importing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Crossfade(
                    targetState = statusText,
                    animationSpec = tween(400),
                    label = "ImportStatusMessage"
                ) { msg ->
                    Text(
                        text = msg,
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // If import business logic throws error, display it gracefully
            if (error.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AEF5350), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x33EF5350), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Clear button to reset import error and type again
                Text(
                    text = "Clear and Try Again",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            error = ""
                            importState = ImportState.Idle
                            uploadViewModel.clearPreview()
                            url = ""
                        }
                        .padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun FeatureChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0x0CFFFFFF), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x05FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFB3B3B3),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun Badge(text: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
