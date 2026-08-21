package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceGlow
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassAmbientShadow
import com.example.ui.theme.GlassBorderTopSheen
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceFrostedSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueGlow
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SecondaryVioletGlow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Domain item representing a financial health axis for the Radar Chart.
 */
data class FinancialHealthAxis(
    val categoryName: String,
    val score: Float, // 0f to 100f
    val benchmarkScore: Float = 75f,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector,
    val metricFormatted: String,
    val aiRecommendation: String
)

/**
 * Default dummy data placeholder set representing a user's multi-dimensional financial health.
 */
object FinancialHealthDummyData {
    val defaultCategories = listOf(
        FinancialHealthAxis(
            categoryName = "Savings",
            score = 86f,
            benchmarkScore = 70f,
            status = "Optimal",
            statusColor = SuccessGreen,
            icon = Icons.Default.Savings,
            metricFormatted = "32% of Income",
            aiRecommendation = "Excellent savings habit! Your monthly vault deposits consistently exceed the 30% golden rule."
        ),
        FinancialHealthAxis(
            categoryName = "Debt",
            score = 78f,
            benchmarkScore = 80f,
            status = "Low Risk",
            statusColor = CyanNeon,
            icon = Icons.Default.CreditCard,
            metricFormatted = "18% DTI Ratio",
            aiRecommendation = "Debt-to-income is well controlled below the 20% danger threshold. Keep credit card balance cleared."
        ),
        FinancialHealthAxis(
            categoryName = "Spending",
            score = 72f,
            benchmarkScore = 75f,
            status = "Moderate",
            statusColor = WarningAmber,
            icon = Icons.Default.AccountBalanceWallet,
            metricFormatted = "₹34,200 Discretionary",
            aiRecommendation = "Dining & weekend entertainment accounted for 42% of discretionary spend. Try trimming dining out by 10%."
        ),
        FinancialHealthAxis(
            categoryName = "Investments",
            score = 64f,
            benchmarkScore = 65f,
            status = "Growing",
            statusColor = SecondaryViolet,
            icon = Icons.Default.TrendingUp,
            metricFormatted = "₹1.45L Active SIPs",
            aiRecommendation = "Index fund SIPs are steady. Consider boosting allocation toward high-yield sovereign gold or ELSS funds."
        ),
        FinancialHealthAxis(
            categoryName = "Budget",
            score = 92f,
            benchmarkScore = 80f,
            status = "Strong",
            statusColor = SuccessGreen,
            icon = Icons.Default.PieChart,
            metricFormatted = "94% Adherence",
            aiRecommendation = "Superb budget discipline! 5 out of 6 category budgets stayed under their set limit for 3 consecutive months."
        ),
        FinancialHealthAxis(
            categoryName = "Emergency",
            score = 80f,
            benchmarkScore = 85f,
            status = "Solid",
            statusColor = PrimaryBlue,
            icon = Icons.Default.Shield,
            metricFormatted = "5.2 Months Buffer",
            aiRecommendation = "Your emergency cushion covers 5.2 months of living expenses. Target 6 months (₹2.4L) for total peace of mind."
        )
    )

    val profilePresets = listOf(
        "Current Profile" to defaultCategories,
        "Saver Focus" to listOf(
            defaultCategories[0].copy(score = 95f),
            defaultCategories[1].copy(score = 90f),
            defaultCategories[2].copy(score = 85f),
            defaultCategories[3].copy(score = 55f),
            defaultCategories[4].copy(score = 96f),
            defaultCategories[5].copy(score = 92f)
        ),
        "Growth Investor" to listOf(
            defaultCategories[0].copy(score = 70f),
            defaultCategories[1].copy(score = 75f),
            defaultCategories[2].copy(score = 68f),
            defaultCategories[3].copy(score = 92f),
            defaultCategories[4].copy(score = 80f),
            defaultCategories[5].copy(score = 75f)
        )
    )
}

/**
 * Modern Cyberpunk/Glassmorphic Financial Health Radar Chart Dashboard Component.
 * Visualizes user's balance across Savings, Debt, Spending, Investments, Budgeting, and Emergency Cushion.
 */
@Composable
fun FinancialHealthRadarCard(
    modifier: Modifier = Modifier,
    categories: List<FinancialHealthAxis> = FinancialHealthDummyData.defaultCategories,
    isSimulating: Boolean = false,
    onToggleSimulation: () -> Unit = {},
    onSimulateStep: () -> Unit = {},
    onExploreAnalyticsClick: () -> Unit = {}
) {
    var selectedPresetIndex by remember { mutableIntStateOf(-1) }
    val activeCategories = remember(selectedPresetIndex, categories) {
        if (selectedPresetIndex >= 0 && selectedPresetIndex < FinancialHealthDummyData.profilePresets.size) {
            FinancialHealthDummyData.profilePresets[selectedPresetIndex].second
        } else {
            categories
        }
    }

    var selectedAxisIndex by remember { mutableIntStateOf(0) }
    var showBenchmarkComparison by remember { mutableStateOf(true) }

    // Overall Average Score Calculation with smooth animated integer ticker
    val targetAverageScore = remember(activeCategories) {
        if (activeCategories.isNotEmpty()) {
            activeCategories.map { it.score }.average().toInt()
        } else 0
    }
    val animatedAverageScore by animateIntAsState(
        targetValue = targetAverageScore,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "avg_score_ticker"
    )

    // Initial load growth animation trigger
    var isChartVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isChartVisible = true
    }

    // Growing bloom animation for polygon expansion upon initial load & dataset updates
    val growthMultiplier by animateFloatAsState(
        targetValue = if (isChartVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "radar_growth_multiplier"
    )

    // Infinite breathing glow for radar vertex indicators
    val infiniteTransition = rememberInfiniteTransition(label = "radar_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = CyanNeon.copy(alpha = 0.18f),
                spotColor = GlassAmbientShadow
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CyanNeon.copy(alpha = 0.12f),
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
                        CyanNeon.copy(alpha = 0.3f),
                        BorderGlass
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("financial_health_radar_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Title & Overall Score Pill + Live Simulation Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FINANCIAL HEALTH RADAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanNeon,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Holistic 360° Health Matrix",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Live Simulation Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSimulating) SuccessGreen.copy(alpha = 0.18f) else DarkSurfaceGlow.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (isSimulating) SuccessGreen.copy(alpha = 0.5f) else BorderGlass,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedPresetIndex = -1
                                onToggleSimulation()
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.Sensors,
                                contentDescription = "Simulate periodic updates",
                                tint = if (isSimulating) SuccessGreen else CyanNeon,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSimulating) "LIVE SIM" else "AUTO UPDATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulating) SuccessGreen else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Health Index Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        PrimaryBlue.copy(alpha = 0.25f),
                                        CyanNeon.copy(alpha = 0.15f)
                                    )
                                )
                            )
                            .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$animatedAverageScore",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Benchmark, Presets & Single Step Update Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preset chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Live Feed chip
                    item {
                        val isLiveSelected = selectedPresetIndex == -1
                        val chipBg by animateColorAsState(
                            targetValue = if (isLiveSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceGlow.copy(alpha = 0.5f),
                            label = "live_chip_bg"
                        )
                        val borderCol by animateColorAsState(
                            targetValue = if (isLiveSelected) CyanNeon else BorderGlass,
                            label = "live_chip_border"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(chipBg)
                                .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedPresetIndex = -1
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSimulating) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Live Stream",
                                    fontSize = 11.sp,
                                    fontWeight = if (isLiveSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLiveSelected) CyanNeon else TextSecondary
                                )
                            }
                        }
                    }

                    itemsIndexed(FinancialHealthDummyData.profilePresets) { idx, preset ->
                        if (idx > 0) { // Skip duplicate default preset, as Live Stream covers it
                            val isSelected = selectedPresetIndex == idx
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceGlow.copy(alpha = 0.5f),
                                label = "chip_bg"
                            )
                            val borderCol by animateColorAsState(
                                targetValue = if (isSelected) CyanNeon else BorderGlass,
                                label = "chip_border"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(chipBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedPresetIndex = idx
                                        selectedAxisIndex = 0
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = preset.first,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyanNeon else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Single Step Simulation Trigger (Instant test)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceGlow.copy(alpha = 0.5f))
                        .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                        .clickable {
                            selectedPresetIndex = -1
                            onSimulateStep()
                        }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Trigger simulated step update",
                            tint = WarningAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Step",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Benchmark toggle button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (showBenchmarkComparison) SecondaryViolet.copy(alpha = 0.2f) else DarkSurfaceGlow.copy(alpha = 0.4f))
                        .border(1.dp, if (showBenchmarkComparison) SecondaryViolet else BorderGlass, RoundedCornerShape(14.dp))
                        .clickable { showBenchmarkComparison = !showBenchmarkComparison }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (showBenchmarkComparison) SecondaryViolet else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Benchmark",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showBenchmarkComparison) SecondaryVioletGlow else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Canvas Radar Spider Web with AnimatedVisibility growing & blooming entrance
            AnimatedVisibility(
                visible = isChartVisible,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                    initialScale = 0.75f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + expandVertically(animationSpec = tween(500)),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RadarChartCanvas(
                        categories = activeCategories,
                        growthFactor = growthMultiplier,
                        showBenchmark = showBenchmarkComparison,
                        selectedAxisIndex = selectedAxisIndex,
                        pulseScale = pulseScale,
                        onAxisTapped = { tappedIndex ->
                            selectedAxisIndex = tappedIndex
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Quick Selector Pills (Horizontal Scroll)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(activeCategories) { index, axis ->
                    val isSelected = selectedAxisIndex == index
                    val itemBg by animateColorAsState(
                        targetValue = if (isSelected) axis.statusColor.copy(alpha = 0.18f) else DarkSurfaceVariant,
                        label = "pill_bg"
                    )
                    val itemBorder by animateColorAsState(
                        targetValue = if (isSelected) axis.statusColor else BorderGlass,
                        label = "pill_border"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(itemBg)
                            .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedAxisIndex = index }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("radar_category_pill_$index")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = axis.icon,
                                contentDescription = null,
                                tint = if (isSelected) axis.statusColor else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = axis.categoryName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = "${axis.score.toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) axis.statusColor else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Category Deep Dive Card (Interactive Insight)
            val currentSelected = activeCategories.getOrNull(selectedAxisIndex) ?: activeCategories.first()
            AnimatedContent(
                targetState = currentSelected,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "category_detail"
            ) { axis ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceFrostedSubtle)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(axis.statusColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = axis.icon,
                                        contentDescription = null,
                                        tint = axis.statusColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = axis.categoryName.uppercase() + " HEALTH",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = axis.metricFormatted,
                                        fontSize = 11.sp,
                                        color = CyanNeon,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Status Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(axis.statusColor.copy(alpha = 0.15f))
                                    .border(1.dp, axis.statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = axis.status.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = axis.statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Score Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current Score: ${axis.score.toInt()}/100",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Target Benchmark: ${axis.benchmarkScore.toInt()}/100",
                                    fontSize = 11.sp,
                                    color = SecondaryVioletGlow,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(axis.score / 100f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    PrimaryBlue,
                                                    axis.statusColor
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Recommendation / Summary
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = axis.aiRecommendation,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: Deep Dive Analytics
            Button(
                onClick = onExploreAnalyticsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("radar_view_analytics_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceGlow
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPLORE CATEGORY BREAKDOWN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Custom Compose Canvas Drawing the Multi-Axis Radar / Spider Web Chart
 */
@Composable
private fun RadarChartCanvas(
    categories: List<FinancialHealthAxis>,
    growthFactor: Float,
    showBenchmark: Boolean,
    selectedAxisIndex: Int,
    pulseScale: Float,
    onAxisTapped: (Int) -> Unit
) {
    // Smooth transition animations for each category score when data updates
    val animatedScores = categories.map { cat ->
        animateFloatAsState(
            targetValue = cat.score,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            label = "axis_score_${cat.categoryName}"
        ).value
    }

    // Smooth transition animation for benchmark overlay
    val animatedBenchmarks = categories.map { cat ->
        animateFloatAsState(
            targetValue = cat.benchmarkScore,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            label = "axis_benchmark_${cat.categoryName}"
        ).value
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allows tapping anywhere near spokes
            }
    ) {
        val count = categories.size
        if (count < 3) return@Canvas

        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (minOf(size.width, size.height) / 2f) - 34.dp.toPx()
        val angleStep = (2 * PI / count).toFloat()

        // 1. Draw Concentric Polygon Grid Web (25%, 50%, 75%, 100%)
        val gridLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        gridLevels.forEach { level ->
            val levelRadius = radius * level
            val gridPath = Path()
            for (i in 0 until count) {
                val angle = (i * angleStep) - (PI / 2).toFloat()
                val x = center.x + levelRadius * cos(angle)
                val y = center.y + levelRadius * sin(angle)
                if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
            }
            gridPath.close()

            drawPath(
                path = gridPath,
                color = if (level == 1.0f) BorderGlassLight else BorderGlass,
                style = Stroke(
                    width = if (level == 1.0f) 1.5.dp.toPx() else 1.dp.toPx(),
                    pathEffect = if (level < 1.0f) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                )
            )

            // Draw level label at top
            val labelY = center.y - levelRadius
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 22f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText("${(level * 100).toInt()}%", center.x - 8f, labelY + 8f, paint)
            }
        }

        // 2. Draw Radial Spokes / Axes from center to each vertex
        for (i in 0 until count) {
            val angle = (i * angleStep) - (PI / 2).toFloat()
            val endX = center.x + radius * cos(angle)
            val endY = center.y + radius * sin(angle)

            val isSelected = (i == selectedAxisIndex)
            drawLine(
                color = if (isSelected) CyanNeon.copy(alpha = 0.7f) else DarkSurfaceGlow,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw category labels around outer edge
            val labelRadius = radius + 22.dp.toPx()
            val labelX = center.x + labelRadius * cos(angle)
            val labelY = center.y + labelRadius * sin(angle)

            val cat = categories[i]
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = if (isSelected) android.graphics.Color.parseColor("#38BDF8") else android.graphics.Color.parseColor("#94A3B8")
                    textSize = if (isSelected) 30f else 26f
                    isFakeBoldText = isSelected
                    isAntiAlias = true
                    textAlign = when {
                        cos(angle) > 0.2 -> android.graphics.Paint.Align.LEFT
                        cos(angle) < -0.2 -> android.graphics.Paint.Align.RIGHT
                        else -> android.graphics.Paint.Align.CENTER
                    }
                }
                drawText(cat.categoryName, labelX, labelY + 8f, paint)
            }
        }

        // 3. Draw Benchmark Polygon Overlay (Dashed Secondary Violet) if enabled
        if (showBenchmark) {
            val benchmarkPath = Path()
            for (i in 0 until count) {
                val angle = (i * angleStep) - (PI / 2).toFloat()
                val scoreFrac = ((animatedBenchmarks.getOrElse(i) { 75f } / 100f) * growthFactor).coerceIn(0.05f, 1.0f)
                val x = center.x + (radius * scoreFrac) * cos(angle)
                val y = center.y + (radius * scoreFrac) * sin(angle)
                if (i == 0) benchmarkPath.moveTo(x, y) else benchmarkPath.lineTo(x, y)
            }
            benchmarkPath.close()

            drawPath(
                path = benchmarkPath,
                color = SecondaryViolet.copy(alpha = 0.08f),
                style = Fill
            )
            drawPath(
                path = benchmarkPath,
                color = SecondaryVioletGlow.copy(alpha = 0.75f),
                style = Stroke(
                    width = 1.8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                    join = StrokeJoin.Round
                )
            )
        }

        // 4. Draw User's Current Financial Health Polygon (Animated Gradient Fill & Stroke)
        val userPath = Path()
        val userVertices = mutableListOf<Offset>()

        for (i in 0 until count) {
            val angle = (i * angleStep) - (PI / 2).toFloat()
            val animatedScore = animatedScores.getOrElse(i) { categories[i].score }
            val scoreFrac = ((animatedScore / 100f) * growthFactor).coerceIn(0.02f, 1.0f)
            val x = center.x + (radius * scoreFrac) * cos(angle)
            val y = center.y + (radius * scoreFrac) * sin(angle)
            val vertex = Offset(x, y)
            userVertices.add(vertex)

            if (i == 0) userPath.moveTo(x, y) else userPath.lineTo(x, y)
        }
        userPath.close()

        // Filled translucent polygon with neon gradient
        drawPath(
            path = userPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    CyanNeon.copy(alpha = 0.40f),
                    PrimaryBlue.copy(alpha = 0.25f),
                    SecondaryViolet.copy(alpha = 0.10f)
                ),
                center = center,
                radius = radius
            ),
            style = Fill
        )

        // Outer contour line with vibrant glow
        drawPath(
            path = userPath,
            brush = Brush.linearGradient(
                listOf(
                    CyanNeon,
                    PrimaryBlueGlow,
                    SuccessGreen
                )
            ),
            style = Stroke(
                width = 2.5.dp.toPx(),
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )

        // 5. Draw Glowing Vertices Points with Pulse Effect
        userVertices.forEachIndexed { idx, point ->
            val isSelected = (idx == selectedAxisIndex)
            val catColor = categories[idx].statusColor

            // Outer pulsing aura
            drawCircle(
                color = catColor.copy(alpha = if (isSelected) 0.45f else 0.25f),
                radius = if (isSelected) (9.dp.toPx() * pulseScale) else 6.dp.toPx(),
                center = point
            )

            // Inner solid dot
            drawCircle(
                color = if (isSelected) Color.White else catColor,
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = point
            )

            // Ring for selected vertex
            if (isSelected) {
                drawCircle(
                    color = CyanNeon,
                    radius = 8.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
