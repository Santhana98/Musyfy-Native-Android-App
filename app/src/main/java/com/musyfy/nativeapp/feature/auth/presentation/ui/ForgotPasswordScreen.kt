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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Glow background brush for top-left (red) and bottom-right (indigo)
    val glowBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0808), // Subtle top red glow
            Color(0xFF070708), // Base dark background
            Color(0xFF080812)  // Subtle bottom indigo glow
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glowBrush)
    ) {
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
                    // Brand / Logo Top Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateBackToLogin() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Musy-Fi Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Musy-Fi",
                            color = Color(0xFFD62828), // Legacy brand color
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Back Navigation and Title
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { onNavigateBackToLogin() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "←",
                                color = Color(0xFFA1A1AA),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Back to Sign In",
                                color = Color(0xFFA1A1AA),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Reset Password",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Enter your details to securely update your password.",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EMAIL ADDRESS",
                            color = Color(0xFFA1A1AA),
                            fontSize = 10.sp,
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

                    // New Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NEW PASSWORD",
                            color = Color(0xFFA1A1AA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AuthTextField(
                            value = password,
                            onValueChange = { 
                                password = it 
                                viewModel.clearError()
                            },
                            placeholder = "At least 8 characters",
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirm Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "CONFIRM PASSWORD",
                            color = Color(0xFFA1A1AA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AuthTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it 
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
}
