package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.dialogTextFieldColors
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 1. Splash Screen with animated logo, radar expansion, and floating waves
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )

    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val center = this.center
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.08f * radarPulse),
                radius = 120.dp.toPx() * radarPulse,
                center = center
            )
            drawCircle(
                color = SecondaryViolet.copy(alpha = 0.15f * radarPulse),
                radius = 80.dp.toPx() * radarPulse,
                center = center,
                style = Stroke(2.dp.toPx())
            )
            drawCircle(
                color = CyanNeon.copy(alpha = 0.25f),
                radius = 50.dp.toPx() * radarPulse,
                center = center,
                style = Stroke(1.5.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(radarPulse * 0.8f + 0.2f)
                    .shadow(24.dp, shape = CircleShape, spotColor = PrimaryBlue)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryBlue, SecondaryViolet)))
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "FinFam",
                    tint = TextPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FinFam",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "FINTECH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Family Financial Management & AI Coach",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 2. Onboarding Screen
 */
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        Triple(
            Icons.Default.Group,
            "Shared Family Vault",
            "Track household expenses, manage category budgets, and coordinate savings goals together seamlessly."
        ),
        Triple(
            Icons.Default.AutoAwesome,
            "AI Financial Coach & OCR",
            "Ask Gemini-powered financial questions, receive customized tips, and scan bills with instant itemized receipt extraction."
        ),
        Triple(
            Icons.Default.Bolt,
            "Bills & Razorpay Subscriptions",
            "Never miss a recurring utility bill with due reminders, 1-tap UPI payments, and flexible Pro subscription plans."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onGetStarted) {
                Text("Skip", color = TextMuted, fontSize = 14.sp)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val (icon, title, desc) = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(20.dp, shape = CircleShape, spotColor = PrimaryBlue)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(DarkSurfaceVariant, DarkSurface)))
                        .border(2.dp, PrimaryBlue.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = CyanNeon,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) PrimaryBlue else BorderGlass)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onGetStarted()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(if (pagerState.currentPage == 2) "Get Started →" else "Continue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

/**
 * 3. Login Screen
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("priyan1436ei@gmail.com") }
    var password by remember { mutableStateOf("••••••••") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PrimaryBlue, SecondaryViolet))),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to FinFam",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        )
        Text(
            text = "Sign in to manage your household finances",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )

        Spacer(modifier = Modifier.height(28.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Sign In to FinFam", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("or authenticate with", fontSize = 12.sp, color = TextMuted)
                }

                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = SuccessGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Unlock with Biometric / PIN", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
