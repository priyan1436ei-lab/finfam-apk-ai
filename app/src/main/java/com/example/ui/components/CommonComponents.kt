package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueGlow
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

/**
 * Premium dark glassmorphism card container
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurface,
    borderColor: Color = BorderGlassLight,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .shadow(12.dp, shape = shape, spotColor = PrimaryBlue.copy(alpha = 0.12f)),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

/**
 * Top App Bar for FinFam
 */
@Composable
fun FinFamTopAppBar(
    initials: String = "PS",
    familyName: String = "Sharma Family Vault",
    unreadCount: Int = 2,
    isPremium: Boolean = false,
    onUpgradeClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Family tag
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FinFam",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                // FINTECH pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FAMILY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Text(
                text = familyName,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
        }

        // Actions: Upgrade Pro, Notifications Bell, Profile Circle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Upgrade / Pro pill button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isPremium) listOf(WarningAmber.copy(alpha = 0.25f), SecondaryViolet.copy(alpha = 0.25f))
                            else listOf(PrimaryBlue.copy(alpha = 0.2f), SecondaryViolet.copy(alpha = 0.2f))
                        )
                    )
                    .border(
                        1.dp,
                        if (isPremium) WarningAmber.copy(alpha = 0.6f) else PrimaryBlue.copy(alpha = 0.6f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(onClick = onUpgradeClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPremium) "👑 PRO" else "⚡ Upgrade",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPremium) WarningAmber else TextPrimary
                    )
                }
            }

            // Notification Bell with badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(38.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = DangerRed,
                                contentColor = TextPrimary
                            ) {
                                Text(unreadCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Profile initials avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryBlue, SecondaryViolet)))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar for FinFam
 */
@Composable
fun FinFamBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nav_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground.copy(alpha = 0.95f))
    ) {
        // Bar background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.BottomCenter)
                .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = DarkSurface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                BottomNavItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )

                // 2. Analytics
                BottomNavItem(
                    title = "Trends",
                    icon = Icons.Default.PieChart,
                    isSelected = currentRoute == "analytics",
                    onClick = { onNavigate("analytics") }
                )

                // Center placeholder space for elevated FAB
                Spacer(modifier = Modifier.width(56.dp))

                // 4. Family & Bills
                BottomNavItem(
                    title = "Family",
                    icon = Icons.Default.Group,
                    isSelected = currentRoute == "family",
                    onClick = { onNavigate("family") }
                )

                // 5. AI Coach
                BottomNavItem(
                    title = "AI Coach",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = currentRoute == "advisor",
                    onClick = { onNavigate("advisor") }
                )
            }
        }

        // Center Floating FAB for Goals & Budgets
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .size(60.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = PrimaryBlue.copy(alpha = pulseGlow)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryBlueGlow, PrimaryBlue, SecondaryViolet),
                        radius = 80f
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clickable { onNavigate("goals") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = "Goals & Budgets",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) PrimaryBlue else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PrimaryBlue else TextMuted
        )
    }
}
