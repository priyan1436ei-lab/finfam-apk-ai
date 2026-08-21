package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.BillItem
import com.example.domain.model.BudgetItem
import com.example.domain.model.FinancialHealth
import com.example.domain.model.GoalItem
import com.example.domain.model.TransactionItem
import com.example.domain.model.UserProfile
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceGlow
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassAmbientShadow
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassBorderTopSheen
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceFrosted
import com.example.ui.theme.GlassSurfaceFrostedSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueGlow
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

/**
 * 1. Family Vault Card
 */
@Composable
fun VaultCard(
    userProfile: UserProfile,
    onDepositClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = PrimaryBlue.copy(alpha = 0.25f),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PrimaryBlue.copy(alpha = 0.28f),
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        GlassBorderTopSheen,
                        CyanNeon.copy(alpha = 0.35f),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Family Name & Visibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userProfile.familyName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (userProfile.isPremium) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(WarningAmber.copy(alpha = 0.2f))
                                .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("👑 PRO", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Balance",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Balance
            Text(
                text = "Total Family Balance",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isBalanceVisible) FinancialEngine.formatExactINR(userProfile.totalBalance) else "₹ ••••••••",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Inflow vs Outflow vs Savings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurfaceDark.copy(alpha = 0.75f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0x33FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Inflow
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", fontSize = 11.sp, color = TextMuted)
                    }
                    Text(
                        text = if (isBalanceVisible) FinancialEngine.formatINR(userProfile.monthlyIncome, true) else "₹•••",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                // Outflow
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = DangerRed, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expenses", fontSize = 11.sp, color = TextMuted)
                    }
                    Text(
                        text = if (isBalanceVisible) FinancialEngine.formatINR(userProfile.monthlyExpenses, true) else "₹•••",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                }

                // Savings
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Savings", fontSize = 11.sp, color = TextMuted)
                    }
                    Text(
                        text = if (isBalanceVisible) FinancialEngine.formatINR(userProfile.monthlySavings, true) else "₹•••",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }
            }
        }
    }
}

/**
 * 2. Financial Health Score Card (CRED-inspired with circular gauge)
 */
@Composable
fun HealthScoreCard(
    health: FinancialHealth,
    onViewAnalysisClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(health.overallScore) {
        animatedProgress.animateTo(
            targetValue = health.overallScore / 100f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = SecondaryViolet.copy(alpha = 0.2f),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SecondaryViolet.copy(alpha = 0.16f),
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        GlassBorderTopSheen,
                        SecondaryViolet.copy(alpha = 0.35f),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FINANCIAL HEALTH SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = health.statusLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = try { Color(android.graphics.Color.parseColor(health.statusColorHex)) } catch (e: Exception) { PrimaryBlue },
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("+${health.scoreChange} this month", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Gauge Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp)
                ) {
                    val statusColor = try { Color(android.graphics.Color.parseColor(health.statusColorHex)) } catch (e: Exception) { PrimaryBlue }
                    Canvas(modifier = Modifier.size(64.dp)) {
                        drawArc(
                            color = DarkSurfaceGlow,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(PrimaryBlue, statusColor, CyanNeon)),
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress.value,
                            useCenter = false,
                            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress.value * 100).toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "/100",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Insight Text in Glass Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassSurfaceFrostedSubtle)
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "✨ " + health.aiSummary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onViewAnalysisClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassSurfaceDark)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View Detailed 7-Pillar Breakdown", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/**
 * 3. Quick Action Buttons Grid
 */
@Composable
fun QuickActionsGrid(
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    onPayBillClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onFamilyWalletClick: () -> Unit,
    onTransferClick: () -> Unit = {},
    onFinFamPayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onFinFamPayClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CyanNeon)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "FinFam Pay →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickActionButton(
                label = "Pay",
                icon = Icons.Default.Payment,
                color = CyanNeon,
                onClick = onFinFamPayClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Transfer",
                icon = Icons.Default.SyncAlt,
                color = PrimaryBlue,
                onClick = onTransferClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "+ Expense",
                icon = Icons.Default.Receipt,
                color = DangerRed,
                onClick = onAddExpenseClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "+ Income",
                icon = Icons.Default.TrendingUp,
                color = SuccessGreen,
                onClick = onAddIncomeClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Pay Bill",
                icon = Icons.Default.Bolt,
                color = WarningAmber,
                onClick = onPayBillClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = color.copy(alpha = 0.22f),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.5f),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.28f),
                            color.copy(alpha = 0.08f)
                        )
                    )
                )
                .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

/**
 * 4. Budget Utilization Carousel
 */
@Composable
fun BudgetUtilizationSection(
    budgets: List<BudgetItem>,
    onAddBudgetClick: () -> Unit,
    onBudgetClick: (BudgetItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MONTHLY BUDGETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "+ Add Budget",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon,
                modifier = Modifier.clickable(onClick = onAddBudgetClick)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = GlassAmbientShadow)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No budgets set for this month. Tap '+ Add Budget' to track categories.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(budgets) { budget ->
                    BudgetCard(budget = budget, onClick = { onBudgetClick(budget) })
                }
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: BudgetItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val util = budget.utilizationPercentage
    val barColor = when {
        util >= 1.0f -> DangerRed
        util >= 0.8f -> WarningAmber
        else -> SuccessGreen
    }

    Box(
        modifier = modifier
            .width(184.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (util >= 0.9f) barColor.copy(alpha = 0.2f) else Color(0x33000000),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                if (util >= 0.9f) {
                    Brush.verticalGradient(listOf(barColor.copy(alpha = 0.7f), barColor.copy(alpha = 0.25f)))
                } else {
                    Brush.linearGradient(listOf(Color(0x38FFFFFF), BorderGlass))
                },
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (util >= 0.9f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DangerRed.copy(alpha = 0.2f))
                            .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(if (util >= 1.0f) "EXCEEDED" else "ALERT 90%", fontSize = 9.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${FinancialEngine.formatINR(budget.spent)} of ${FinancialEngine.formatINR(budget.monthlyLimit)}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { util.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = DarkSurfaceGlow
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${(util * 100).toInt()}% spent (${FinancialEngine.formatINR(budget.remainingAmount)} left)",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

/**
 * 5. Upcoming Bills Section
 */
@Composable
fun UpcomingBillsSection(
    bills: List<BillItem>,
    onPayBillClick: (BillItem) -> Unit,
    onAddBillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UPCOMING BILLS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "+ Add Bill",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon,
                modifier = Modifier.clickable(onClick = onAddBillClick)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        bills.filter { !it.isPaid }.take(3).forEach { bill ->
            BillRowItem(bill = bill, onPayClick = { onPayBillClick(bill) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BillRowItem(
    bill: BillItem,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x28000000),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0x33FFFFFF),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WarningAmber.copy(alpha = 0.15f))
                    .border(1.dp, WarningAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = bill.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Due ${bill.dueDate} • ${bill.category}", fontSize = 11.sp, color = TextMuted)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = FinancialEngine.formatINR(bill.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onPayClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 6. Savings Goals Section
 */
@Composable
fun GoalsSection(
    goals: List<GoalItem>,
    onAddGoalClick: () -> Unit,
    onGoalClick: (GoalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAVINGS GOALS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "+ Add Goal",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon,
                modifier = Modifier.clickable(onClick = onAddGoalClick)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(goals) { goal ->
                GoalCard(goal = goal, onClick = { onGoalClick(goal) })
            }
        }
    }
}

@Composable
fun GoalCard(
    goal: GoalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = goal.progressPercentage

    Box(
        modifier = modifier
            .width(200.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = CyanNeon.copy(alpha = 0.15f),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        CyanNeon.copy(alpha = 0.35f),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = goal.emoji, fontSize = 24.sp)
                if (goal.isFamilyGoal) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("FAMILY", fontSize = 9.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = goal.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Target: ${FinancialEngine.formatINR(goal.targetAmount)} (${goal.targetDate})", fontSize = 11.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyanNeon,
                trackColor = DarkSurfaceGlow
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${(progress * 100).toInt()}% saved", fontSize = 10.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                Text(text = FinancialEngine.formatINR(goal.currentAmount), fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

/**
 * 7. Transaction Row Item
 */
@Composable
fun TransactionRow(
    transaction: TransactionItem,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCredit = transaction.isCredit
    val amountColor = if (isCredit) SuccessGreen else DangerRed
    val amountPrefix = if (isCredit) "+" else "-"

    val categoryIcon = when (transaction.category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "travel" -> Icons.Default.DirectionsCar
        "bills" -> Icons.Default.Bolt
        "rent" -> Icons.Default.Home
        "shopping" -> Icons.Default.ShoppingCart
        "entertainment" -> Icons.Default.Tv
        "healthcare" -> Icons.Default.Favorite
        "education" -> Icons.Default.School
        "salary" -> Icons.Default.TrendingUp
        "freelance" -> Icons.Default.Star
        else -> Icons.Default.Receipt
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x22000000),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0x2EFFFFFF),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isCredit) SuccessGreen.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        if (isCredit) SuccessGreen.copy(alpha = 0.3f) else PrimaryBlue.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category,
                    tint = if (isCredit) SuccessGreen else PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${transaction.date} • ${transaction.paymentMethod}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    if (transaction.isFamilyShared) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecondaryViolet.copy(alpha = 0.2f))
                                .border(1.dp, SecondaryViolet.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("Family", fontSize = 9.sp, color = SecondaryViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$amountPrefix${FinancialEngine.formatINR(transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

