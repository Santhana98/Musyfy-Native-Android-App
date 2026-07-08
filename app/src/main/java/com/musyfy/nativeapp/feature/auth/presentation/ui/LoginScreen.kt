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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musyfy.nativeapp.R
import com.musyfy.nativeapp.core.ui.AuthButton
import com.musyfy.nativeapp.core.ui.AuthTextField
import com.musyfy.nativeapp.core.ui.GlassmorphicCard

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.musyfy.nativeapp.feature.auth.presentation.AuthViewModel
import com.musyfy.nativeapp.feature.auth.presentation.AuthUiState

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_login_cassette),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Overlay (0.35 opacity)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        // Scrollable content wrapper
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassmorphicCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Musy-Fi Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .shadow(elevation = 12.dp, shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = "Musy-Fi",
                        color = Color(0xFFE53935),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = "Log in to continue",
                        color = Color(0xFF888888),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Text(
                        text = "🎧 Your Music. Your Vibe.",
                        color = Color(0xFF666666),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EMAIL ADDRESS",
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PASSWORD",
                                color = Color(0xFF888888),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Forgot Password?",
                                color = Color(0xFFE53935),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { 
                                    viewModel.clearError()
                                    onNavigateToForgotPassword() 
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AuthTextField(
                            value = password,
                            onValueChange = { 
                                password = it 
                                viewModel.clearError()
                            },
                            placeholder = "••••••••",
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Text(
                                    text = "🔑",
                                    fontSize = 16.sp
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (uiState is AuthUiState.Error) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = Color(0xFFE53935),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Action Button (Simulated Login)
                    AuthButton(
                        text = if (uiState is AuthUiState.Loading) "Signing In..." else "Sign In →",
                        onClick = {
                            viewModel.login(email, password, onLoginSuccess)
                        },
                        enabled = email.isNotEmpty() && password.isNotEmpty() && uiState !is AuthUiState.Loading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bottom navigation link
                    val signupPrompt = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF555555))) {
                            append("Don't have an account? ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign Up")
                        }
                    }

                    Text(
                        text = signupPrompt,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { onNavigateToRegister() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
