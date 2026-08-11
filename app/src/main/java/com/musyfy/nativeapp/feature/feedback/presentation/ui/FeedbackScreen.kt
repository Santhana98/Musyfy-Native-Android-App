package com.musyfy.nativeapp.feature.feedback.presentation.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.core.ui.components.PremiumThemeBackground
import com.musyfy.nativeapp.core.ui.haptics.MusyfyHaptics
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.feature.feedback.presentation.FeedbackViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    feedbackViewModel: FeedbackViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val themeState by authViewModel.theme.collectAsState()

    var feedbackText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val maxChars = 300

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Static Premium Background
        PremiumThemeBackground(themeState = themeState)

        // 2. Main Scrollable Container to fit keyboard naturally
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Navigation Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161618))
                        .border(1.dp, Color(0xFF26262A), CircleShape)
                        .clickable {
                            MusyfyHaptics.performLight(view)
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Hero Section
            Text(
                text = "Share Your Thoughts",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us what you think. Whether something went wrong, something feels confusing, or you simply have an idea — we'd love to hear it.",
                color = Color(0xFFA0A0A0),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Multiline Feedback Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111112), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF222226), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { input ->
                            if (input.length <= maxChars) {
                                feedbackText = input
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Write your feedback here...",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Character Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${feedbackText.length} / $maxChars",
                            color = if (feedbackText.length >= maxChars) Color(0xFFFF5252) else Color(0xFF666666),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Send Feedback CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSubmitting) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primary
                    )
                    .clickable(enabled = !isSubmitting) {
                        val trimmedText = feedbackText.trim()
                        if (trimmedText.isEmpty()) {
                            MusyfyHaptics.performLight(view)
                            Toast.makeText(
                                context,
                                "Please write something before sending.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@clickable
                        }

                        // Prevent rapid double-taps
                        isSubmitting = true
                        MusyfyHaptics.performConfirmation(view)

                        coroutineScope.launch {
                            // Check rolling 24-hour rate limit (max 3 per 24h)
                            if (!feedbackViewModel.canInitiateFeedback()) {
                                isSubmitting = false
                                Toast.makeText(
                                    context,
                                    "You've shared enough for today ❤️\nPlease try again tomorrow.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }

                            // Log analytics initiation ONCE right before launching intent
                            feedbackViewModel.logFeedbackSendInitiated()

                            val launched = launchEmailComposer(context, trimmedText)
                            if (launched) {
                                // Successful intent launch -> Record rate limit quota & reset form
                                feedbackViewModel.recordFeedbackInitiated()
                                feedbackText = ""
                                isSubmitting = false
                                Toast.makeText(
                                    context,
                                    "Your feedback is ready to send ❤️",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Intent launch failed (no email app) -> Do NOT clear text or consume quota
                                isSubmitting = false
                                Toast.makeText(
                                    context,
                                    "No email app is available to send feedback.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Send Feedback",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun launchEmailComposer(context: Context, feedbackText: String): Boolean {
    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }

    val androidVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

    val emailBody = buildString {
        append("Musyfy User Feedback\n\n")
        append(feedbackText)
        append("\n\n──────────────────────────\n")
        append("Technical Context:\n")
        append("App Version: ").append(appVersion).append("\n")
        append("Android Version: ").append(androidVersion).append("\n")
        append("Device: ").append(deviceModel)
    }

    val subject = "Musyfy User Feedback"
    val recipient = "musyfy72@gmail.com"

    // Encode subject and body both inside mailto: URI AND in Intent Extras
    // to guarantee body populates across all Android email apps & Gmail versions.
    val mailtoUri = Uri.parse(
        "mailto:$recipient?subject=${Uri.encode(subject)}&body=${Uri.encode(emailBody)}"
    )

    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = mailtoUri
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    return try {
        val chooserIntent = Intent.createChooser(mailIntent, "Send Feedback via Email")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: Exception) {
        false
    }
}
