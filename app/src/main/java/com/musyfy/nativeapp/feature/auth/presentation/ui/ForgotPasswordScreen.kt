package com.musyfy.nativeapp.feature.auth.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.AuthButton
import com.musyfy.nativeapp.core.ui.AuthTextField
import com.musyfy.nativeapp.core.ui.GlassmorphicCard
import androidx.compose.ui.draw.shadow

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.feature.auth.presentation.AuthUiState

@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    // Pre-fill email on startup if remembered
    LaunchedEffect(authState.userEmail) {
        if (authState.userEmail != null && email.isEmpty()) {
            email = authState.userEmail!!
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background Image matching Login and Register
        Image(
            painter = painterResource(id = R.drawable.bg_login_cassette),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Balanced Dark Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000), // Slightly darker top
                            Color(0x40000000), // More transparent center (25% opacity)
                            Color(0xB3000000)  // Slightly darker bottom
                        )
                    )
                )
        )

        // Scrollable content wrapper
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 48.dp, horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lightweight floating layout matching Login and Register exactly
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 340.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Musyfy Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .shadow(elevation = 16.dp, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Title
                Text(
                    text = "Musyfy",
                    color = Color(0xFFF9423A), // Musyfy red accent
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Reset Password",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "🎧 Your Music. Your Vibe. 🍁",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Back to Sign In Link
                Row(
                    modifier = Modifier
                        .clickable { onNavigateBackToLogin() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "←",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Back to Sign In",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Email Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "EMAIL ADDRESS",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthTextField(
                        value = email,
                        onValueChange = { 
                            email = it 
                            viewModel.clearError()
                        },
                        placeholder = "you@example.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Text(
                                text = "✉️",
                                fontSize = 16.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // New Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "NEW PASSWORD",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthTextField(
                        value = password,
                        onValueChange = { 
                            password = it 
                            viewModel.clearError()
                        },
                        placeholder = "At least 8 characters",
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Text(
                                text = "🔑",
                                fontSize = 16.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                                contentDescription = "Toggle password visibility",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clickable { passwordVisible = !passwordVisible }
                                    .padding(8.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Password Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CONFIRM PASSWORD",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it 
                            viewModel.clearError()
                        },
                        placeholder = "••••••••",
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Text(
                                text = "🔑",
                                fontSize = 16.sp
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(if (confirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                                contentDescription = "Toggle password visibility",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clickable { confirmPasswordVisible = !confirmPasswordVisible }
                                    .padding(8.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                val errorMsg = when {
                    uiState is AuthUiState.Error -> (uiState as AuthUiState.Error).message
                    password != confirmPassword && confirmPassword.isNotEmpty() -> "Passwords do not match"
                    else -> null
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFE53935),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Action Button (Simulated Reset)
                AuthButton(
                    text = if (uiState is AuthUiState.Loading) "Resetting..." else "Reset Password →",
                    onClick = {
                        if (password == confirmPassword) {
                            viewModel.resetPassword(email, password, onNavigateBackToLogin)
                        }
                    },
                    enabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword && uiState !is AuthUiState.Loading
                )
            }
        }
    }
}
