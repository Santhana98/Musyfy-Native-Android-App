package com.musyfy.nativeapp.feature.appearance.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.core.ui.theme.LocalAccentColor
import com.musyfy.nativeapp.feature.appearance.domain.model.AccentMode
import com.musyfy.nativeapp.feature.appearance.domain.model.AppearanceState
import com.musyfy.nativeapp.feature.appearance.domain.model.ParallaxLevel
import com.musyfy.nativeapp.feature.appearance.domain.model.ThemeMode
import com.musyfy.nativeapp.feature.appearance.domain.model.WallpaperScale
import com.musyfy.nativeapp.feature.appearance.presentation.AppearanceViewModel

enum class CustomThemeFlowState {
    IDLE,
    CROP,
    PREVIEW,
    PREVIEW_ONLY
}

@Composable
fun AppearanceScreen(
    viewModel: AppearanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val rawAccentColor = LocalAccentColor.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var flowState by remember { mutableStateOf(CustomThemeFlowState.IDLE) }
    var currentFlowUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var sourceBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var croppedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            currentFlowUri = uri
            val decoded = com.musyfy.nativeapp.feature.appearance.domain.WallpaperProcessor.decodeUriToBitmap(context, uri)
            if (decoded != null) {
                sourceBitmap = decoded
                flowState = CustomThemeFlowState.CROP
            } else {
                flowState = CustomThemeFlowState.IDLE
            }
        }
    }
    
    // Resolve dynamic accent color based on user selection in UI Mode
    val accentColor = if (state.accentMode == AccentMode.AUTO) {
        // Fallback to theme primary/default accent color in auto mode
        rawAccentColor
    } else {
        // Musyfy Orange / Red brand color
        Color(0xFFF9423A)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070708))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF111112))
                        .border(1.dp, Color(0xFF1A1A1C), RoundedCornerShape(10.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Appearance",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Customize how Musyfy looks and feels.",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Live Preview Mockup Container
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LivePreviewMockup(
                        state = state,
                        accentColor = accentColor,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                // Official Themes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Official Themes",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeCard(
                            title = "X Theme",
                            description = "Premium Dark Experience",
                            badgeText = "Default",
                            isSelected = state.themeMode == ThemeMode.X,
                            onClick = { viewModel.updateState(state.copy(themeMode = ThemeMode.X)) },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                        ThemeCard(
                            title = "Y Theme",
                            description = "Classic Experience",
                            badgeText = "Classic",
                            isSelected = state.themeMode == ThemeMode.Y,
                            onClick = { viewModel.updateState(state.copy(themeMode = ThemeMode.Y)) },
                            accentColor = accentColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Custom Theme
                val customWallpaperFile = state.customWallpaperPath?.let { java.io.File(it) }
                val hasCustomWallpaper = customWallpaperFile != null && customWallpaperFile.exists()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Custom Theme",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF111112))
                            .border(1.dp, Color(0xFF1A1A1C), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasCustomWallpaper) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(context)
                                        .data(customWallpaperFile)
                                        .memoryCacheKey("custom_wallpaper_${state.wallpaperVersion}")
                                        .diskCacheKey("custom_wallpaper_${state.wallpaperVersion}")
                                        .build(),

                                    contentDescription = "Custom Wallpaper Thumbnail",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF2A2A2E), RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E1E20))
                                        .border(1.dp, Color(0xFF2A2A2E), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🖼️", fontSize = 28.sp)
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Theme",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (hasCustomWallpaper) "Custom wallpaper active • Last updated: Today" else "Choose your own wallpaper from your gallery.",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }

                            // Active custom status indicator
                            if (hasCustomWallpaper) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom theme action buttons based on presence of wallpaper
                        if (hasCustomWallpaper) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemeActionButton(
                                    text = "Replace",
                                    onClick = {
                                        photoLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeActionButton(
                                    text = "Preview",
                                    onClick = {
                                        state.customWallpaperPath?.let { path ->
                                            val file = java.io.File(path)
                                            if (file.exists()) {
                                                val bitmap = android.graphics.BitmapFactory.decodeFile(path)
                                                if (bitmap != null) {
                                                    croppedBitmap = bitmap
                                                    flowState = CustomThemeFlowState.PREVIEW_ONLY
                                                }
                                            }
                                        }
                                    },
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeActionButton(
                                    text = "Remove",
                                    onClick = {
                                        viewModel.removeCustomWallpaper()
                                    },
                                    accentColor = accentColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ThemeActionButton(
                                    text = "Choose Image",
                                    onClick = {
                                        photoLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    accentColor = accentColor,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }


                // Accent Colors
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Accent Color",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    SegmentedSelector(
                        options = listOf("Auto (Palette)", "Musyfy Orange"),
                        selectedOption = if (state.accentMode == AccentMode.AUTO) "Auto (Palette)" else "Musyfy Orange",
                        onOptionSelected = { option ->
                            val mode = if (option == "Auto (Palette)") AccentMode.AUTO else AccentMode.MUSYFY_ORANGE
                            viewModel.updateState(state.copy(accentMode = mode))
                        },
                        accentColor = accentColor
                    )
                }

                // Wallpaper Controls
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Wallpaper Controls",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF111112))
                            .border(1.dp, Color(0xFF1A1A1C), RoundedCornerShape(24.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Visibility
                        PremiumSlider(
                            label = "Wallpaper Visibility",
                            value = state.visibility,
                            onValueChange = { viewModel.updateState(state.copy(visibility = it)) },
                            valueRange = 0f..1f,
                            valueText = "${(state.visibility * 100).toInt()}%",
                            accentColor = accentColor
                        )

                        // Blur
                        PremiumSlider(
                            label = "Blur Amount",
                            value = state.blurAmount,
                            onValueChange = { viewModel.updateState(state.copy(blurAmount = it)) },
                            valueRange = 0f..20f,
                            valueText = "${state.blurAmount.toInt()}dp",
                            accentColor = accentColor
                        )

                        // Brightness
                        PremiumSlider(
                            label = "Brightness",
                            value = state.brightness,
                            onValueChange = { viewModel.updateState(state.copy(brightness = it)) },
                            valueRange = -0.5f..0.5f,
                            valueText = "${if (state.brightness >= 0) "+" else ""}${(state.brightness * 100).toInt()}%",
                            accentColor = accentColor
                        )

                        // Dark Overlay
                        PremiumSlider(
                            label = "Dark Overlay",
                            value = state.darkOverlay,
                            onValueChange = { viewModel.updateState(state.copy(darkOverlay = it)) },
                            valueRange = 0f..0.8f,
                            valueText = "${(state.darkOverlay * 100).toInt()}%",
                            accentColor = accentColor
                        )

                        // Saturation
                        PremiumSlider(
                            label = "Saturation",
                            value = state.saturation,
                            onValueChange = { viewModel.updateState(state.copy(saturation = it)) },
                            valueRange = 0f..2f,
                            valueText = "${(state.saturation * 100).toInt()}%",
                            accentColor = accentColor
                        )

                        // Wallpaper Scale Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Wallpaper Scale",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            SegmentedSelector(
                                options = listOf("Fit", "Fill", "Zoom"),
                                selectedOption = when (state.scale) {
                                    WallpaperScale.FIT -> "Fit"
                                    WallpaperScale.FILL -> "Fill"
                                    WallpaperScale.ZOOM -> "Zoom"
                                },
                                onOptionSelected = { option ->
                                    val scale = when (option) {
                                        "Fit" -> WallpaperScale.FIT
                                        "Fill" -> WallpaperScale.FILL
                                        else -> WallpaperScale.ZOOM
                                    }
                                    viewModel.updateState(state.copy(scale = scale))
                                },
                                accentColor = accentColor
                            )
                        }

                        // Parallax Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Parallax Motion",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            SegmentedSelector(
                                options = listOf("Off", "Low", "Med", "High"),
                                selectedOption = when (state.parallax) {
                                    ParallaxLevel.OFF -> "Off"
                                    ParallaxLevel.LOW -> "Low"
                                    ParallaxLevel.MEDIUM -> "Med"
                                    ParallaxLevel.HIGH -> "High"
                                },
                                onOptionSelected = { option ->
                                    val lvl = when (option) {
                                        "Off" -> ParallaxLevel.OFF
                                        "Low" -> ParallaxLevel.LOW
                                        "Med" -> ParallaxLevel.MEDIUM
                                        else -> ParallaxLevel.HIGH
                                    }
                                    viewModel.updateState(state.copy(parallax = lvl))
                                },
                                accentColor = accentColor
                            )
                        }

                        // Noise Texture Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Noise Texture",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Adds premium grain overlay",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = state.noiseTexture,
                                onCheckedChange = { viewModel.updateState(state.copy(noiseTexture = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                    uncheckedTrackColor = Color(0xFF2A2A2E)
                                )
                            )
                        }

                        // Corner Fade Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Corner Fade",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Soft border fading to black",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = state.cornerFade,
                                onCheckedChange = { viewModel.updateState(state.copy(cornerFade = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = accentColor,
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                    uncheckedTrackColor = Color(0xFF2A2A2E)
                                )
                            )
                        }
                    }
                }

                // Info Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "💡 Information",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "• Only one custom wallpaper will ever be stored.\n" +
                               "• Choosing a new wallpaper automatically replaces the previous one.\n" +
                               "• Designed for premium performance and minimal storage usage.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(60.dp)) // Padding for bottom navbar overlay space
            }
        }

        // Overlay screens for Custom Wallpaper flow
        if (flowState == CustomThemeFlowState.CROP && sourceBitmap != null) {
            CropScreen(
                bitmap = sourceBitmap!!,
                onCropDone = { cropped ->
                    croppedBitmap = cropped
                    flowState = CustomThemeFlowState.PREVIEW
                },
                onCancel = {
                    flowState = CustomThemeFlowState.IDLE
                },
                accentColor = accentColor
            )
        }

        if (flowState == CustomThemeFlowState.PREVIEW && croppedBitmap != null) {
            FullscreenPreviewScreen(
                bitmap = croppedBitmap!!,
                onApply = {
                    viewModel.saveCustomWallpaper(croppedBitmap!!)
                    flowState = CustomThemeFlowState.IDLE
                },
                onCancel = {
                    flowState = CustomThemeFlowState.IDLE
                },
                accentColor = accentColor
            )
        }

        if (flowState == CustomThemeFlowState.PREVIEW_ONLY && croppedBitmap != null) {
            FullscreenPreviewScreen(
                bitmap = croppedBitmap!!,
                onApply = {
                    flowState = CustomThemeFlowState.IDLE
                },
                onCancel = {
                    flowState = CustomThemeFlowState.IDLE
                },
                accentColor = accentColor
            )
        }
    }
}


@Composable
fun ThemeCard(
    title: String,
    description: String,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val targetOutlineColor = if (isSelected) accentColor else Color(0xFF1A1A1C)
    val outlineColor by animateColorAsState(targetOutlineColor)
    val outlineWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF111112))
            .border(outlineWidth, outlineColor, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun MockActionButton(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E20))
            .border(1.dp, Color(0xFF2A2A2E), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Inactive in Milestone 2 */ }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.4f), // Muted to represent upcoming state
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SegmentedSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111112))
            .border(1.dp, Color(0xFF1A1A1C), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            val targetBg = if (isSelected) accentColor else Color.Transparent
            val bg by animateColorAsState(targetBg)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PremiumSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF1E1E20)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ThemeActionButton(
    text: String,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E20))
            .border(1.dp, Color(0xFF2A2A2E), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


