package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.BillItem
import com.example.domain.model.DailySpendDataPoint
import com.example.domain.model.ExpensePrediction
import com.example.domain.model.FamilyContributionShare
import com.example.domain.model.FamilyMemberItem
import com.example.domain.model.NotificationAlertItem
import com.example.domain.model.NotificationType
import com.example.domain.model.WeeklySpendBar
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkNavyCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceGlow
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueGlow
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

// ==========================================
// 1. DAILY SPENDING - LINE CHART
// ==========================================
@Composable
fun DailySpendingLineChartCard(
    dataPoints: List<DailySpendDataPoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(20) } // Default to day 21 (index 20)
    val activePoint = dataPoints.getOrNull(selectedIndex) ?: dataPoints.lastOrNull()
    val maxSpend = (dataPoints.maxOfOrNull { it.amount } ?: 3000.0).coerceAtLeast(100.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Daily Spend",
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Spending",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "30-Day Velocity & Projections",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                activePoint?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = FinancialEngine.formatINR(it.amount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (it.isProjected) SecondaryViolet else CyanNeon
                        )
                        Text(
                            text = if (it.isProjected) "Projected (${it.dateLabel})" else "Actual (${it.dateLabel})",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavyCanvas)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                if (dataPoints.isNotEmpty()) {
                                    val step = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                                    val index = ((offset.x + step / 2) / step).toInt().coerceIn(0, dataPoints.size - 1)
                                    selectedIndex = index
                                }
                            }
                        }
                ) {
                    if (dataPoints.size < 2) return@Canvas
                    val width = size.width
                    val height = size.height
                    val stepX = width / (dataPoints.size - 1)

                    val linePath = Path()
                    val areaPath = Path()

                    val points = dataPoints.mapIndexed { index, dp ->
                        val x = index * stepX
                        val normalizedY = (dp.amount / maxSpend).toFloat().coerceIn(0f, 1f)
                        val y = height - (normalizedY * (height * 0.8f)) - 10f
                        Offset(x, y)
                    }

                    // Build smooth cubic bezier
                    linePath.moveTo(points.first().x, points.first().y)
                    areaPath.moveTo(points.first().x, height)
                    areaPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2
                        linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        areaPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }

                    areaPath.lineTo(points.last().x, height)
                    areaPath.close()

                    // Fill gradient
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyanNeon.copy(alpha = 0.35f),
                                PrimaryBlue.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw Line
                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(CyanNeon, PrimaryBlue, SecondaryViolet)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Selected Point Marker
                    if (selectedIndex in points.indices) {
                        val selPt = points[selectedIndex]
                        // Vertical guideline
                        drawLine(
                            color = CyanNeon.copy(alpha = 0.5f),
                            start = Offset(selPt.x, 0f),
                            end = Offset(selPt.x, height),
                            strokeWidth = 1.dp.toPx()
                        )
                        // Outer glow circle
                        drawCircle(
                            color = CyanNeon.copy(alpha = 0.3f),
                            radius = 9.dp.toPx(),
                            center = selPt
                        )
                        // Inner solid circle
                        drawCircle(
                            color = CyanNeon,
                            radius = 4.5.dp.toPx(),
                            center = selPt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Day 01", fontSize = 10.sp, color = TextMuted)
                Text("Day 15 (Mid)", fontSize = 10.sp, color = TextMuted)
                Text("Day 21 (Today)", fontSize = 10.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                Text("Day 31 (Forecast)", fontSize = 10.sp, color = SecondaryViolet, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 2. WEEKLY SPENDING - BAR CHART
// ==========================================
@Composable
fun WeeklySpendingBarChartCard(
    weeklyBars: List<WeeklySpendBar>,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<WeeklySpendBar?>(weeklyBars.firstOrNull { it.isPeakDay }) }
    val maxVal = (weeklyBars.maxOfOrNull { it.amount } ?: 10000.0).coerceAtLeast(100.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Weekly Spend",
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Weekly Spending",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Monday to Sunday Analysis",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                selectedDay?.let { bar ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = FinancialEngine.formatINR(bar.amount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (bar.isPeakDay) WarningAmber else SuccessGreen
                        )
                        Text(
                            text = if (bar.isPeakDay) "${bar.dayName} (Peak Day 🔥)" else bar.dayName,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavyCanvas)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyBars.forEach { bar ->
                    val isSelected = selectedDay?.dayName == bar.dayName
                    val heightRatio = (bar.amount / maxVal).toFloat().coerceIn(0.12f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedDay = bar }
                    ) {
                        // Amount Label on Top when selected
                        Text(
                            text = if (isSelected) "₹${(bar.amount / 1000).toInt()}k" else "",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bar.isPeakDay) WarningAmber else CyanNeon,
                            modifier = Modifier.height(14.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar Capsule
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 22.dp else 16.dp)
                                .fillMaxSize(heightRatio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = when {
                                            bar.isPeakDay -> listOf(WarningAmber, DangerRed)
                                            isSelected -> listOf(CyanNeon, PrimaryBlue)
                                            else -> listOf(SuccessGreen.copy(alpha = 0.8f), SuccessGreen.copy(alpha = 0.3f))
                                        }
                                    )
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (bar.isPeakDay) WarningAmber else CyanNeon,
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day Label
                        Text(
                            text = bar.dayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || bar.isPeakDay) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                bar.isPeakDay -> WarningAmber
                                isSelected -> TextPrimary
                                else -> TextMuted
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MONTHLY TREND - AREA CHART
// ==========================================
@Composable
fun MonthlyTrendAreaChartCard(
    modifier: Modifier = Modifier
) {
    val months = listOf("May", "Jun", "Jul", "Aug", "Sep (Est)")
    val spendValues = listOf(34200.0, 36800.0, 37200.0, 40550.0, 35000.0)
    val maxVal = 45000.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryViolet.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Monthly Trend",
                            tint = SecondaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Monthly Trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Multi-Month Velocity Comparison",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+8.2% vs Jul",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Area Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavyCanvas)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (spendValues.size - 1)

                    val linePath = Path()
                    val areaPath = Path()

                    val points = spendValues.mapIndexed { idx, amt ->
                        val x = idx * stepX
                        val norm = (amt / maxVal).toFloat().coerceIn(0f, 1f)
                        val y = height - (norm * (height * 0.75f)) - 15f
                        Offset(x, y)
                    }

                    linePath.moveTo(points.first().x, points.first().y)
                    areaPath.moveTo(points.first().x, height)
                    areaPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2
                        linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        areaPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }

                    areaPath.lineTo(points.last().x, height)
                    areaPath.close()

                    // Gradient Fill Area
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SecondaryViolet.copy(alpha = 0.4f),
                                PrimaryBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )

                    // Stroke
                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryBlue, SecondaryViolet, CyanNeon)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Data Dots and Labels
                    points.forEachIndexed { idx, pt ->
                        drawCircle(
                            color = if (idx == 3) WarningAmber else SecondaryViolet,
                            radius = if (idx == 3) 6.dp.toPx() else 4.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEachIndexed { idx, m ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = m,
                            fontSize = 11.sp,
                            fontWeight = if (idx == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (idx == 3) CyanNeon else TextMuted
                        )
                        Text(
                            text = "₹${(spendValues[idx] / 1000).toInt()}k",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. CATEGORY DISTRIBUTION - PIE / DONUT CHART
// ==========================================
@Composable
fun CategoryDistributionPieChartCard(
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Triple("Food & Dining", 12500.0, Color(0xFF10B981)), // Emerald
        Triple("Housing & Rent", 10000.0, Color(0xFF06B6D4)), // Cyan
        Triple("Shopping", 6500.0, Color(0xFFA855F7)), // Purple
        Triple("Utilities & Bills", 4500.0, Color(0xFFF59E0B)), // Amber
        Triple("Transport", 4050.0, Color(0xFF3B82F6)), // Blue
        Triple("Healthcare", 3000.0, Color(0xFFEF4444))  // Red
    )

    val totalSpend = categories.sumOf { it.second }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val activeSlice = categories.getOrNull(selectedCategoryIndex) ?: categories.first()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WarningAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Category Distribution",
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Category Distribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pie & Donut Allocation",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = "Total: ${FinancialEngine.formatINR(totalSpend)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart Canvas + Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavyCanvas)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier
                        .size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        var startAngle = -90f
                        val strokeWidth = 26.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        categories.forEachIndexed { index, item ->
                            val sweepAngle = ((item.second / totalSpend) * 360f).toFloat()
                            val isSelected = index == selectedCategoryIndex
                            val actualStroke = if (isSelected) strokeWidth + 6f else strokeWidth

                            drawArc(
                                color = item.third,
                                startAngle = startAngle + 2f,
                                sweepAngle = sweepAngle - 4f,
                                useCenter = false,
                                style = Stroke(width = actualStroke, cap = StrokeCap.Round),
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(diameter, diameter)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center text inside donut
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${((activeSlice.second / totalSpend) * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeSlice.third
                        )
                        Text(
                            text = activeSlice.first.split(" ").first(),
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend List
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEachIndexed { index, item ->
                        val isSelected = index == selectedCategoryIndex
                        val percent = ((item.second / totalSpend) * 100).toInt()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) item.third.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { selectedCategoryIndex = index }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(item.third)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.first,
                                    fontSize = 11.sp,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$percent%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = item.third
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. FAMILY CONTRIBUTION - MEMBER BREAKDOWN
// ==========================================
@Composable
fun FamilyContributionCard(
    contributions: List<FamilyContributionShare>,
    onMemberClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Family Contribution",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Family Contribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Household Pool & Share Ratio",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Text(
                    text = "${contributions.size} Members",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-segment Stacked Progress Bar (e.g. 40%, 30%, 20%, 10%)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(DarkNavyCanvas)
            ) {
                contributions.forEach { share ->
                    val color = Color(android.graphics.Color.parseColor(share.avatarColorHex.replace("0x", "#")))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(share.percentageShare.coerceAtLeast(1f))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Members Detailed Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                contributions.forEach { share ->
                    val color = Color(android.graphics.Color.parseColor(share.avatarColorHex.replace("0x", "#")))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkNavyCanvas)
                            .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp))
                            .clickable { onMemberClick(share.role) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.2f))
                                    .border(1.dp, color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = share.role.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = share.memberName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Spent: ${FinancialEngine.formatINR(share.spentAmount)}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${share.percentageShare.toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = color
                            )
                            Text(
                                text = FinancialEngine.formatINR(share.contributionAmount),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. EXPENSE PREDICTION - AI PREDICTS
// ==========================================
@Composable
fun ExpensePredictionCard(
    prediction: ExpensePrediction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecondaryViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Prediction",
                            tint = SecondaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Expense Prediction",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "FinFam AI Predictive Engine",
                            fontSize = 11.sp,
                            color = SecondaryViolet
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AI Alert",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prediction Metrics (End-of-month spending & Future savings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Projected End of Month
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkNavyCanvas)
                        .border(1.dp, BorderGlassLight, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("End-of-Month Spend", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FinancialEngine.formatINR(prediction.projectedEndOfMonthSpend),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarningAmber
                        )
                        Text("Forecasted Total", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                // Future Savings
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkNavyCanvas)
                        .border(1.dp, BorderGlassLight, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("Future Savings", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FinancialEngine.formatINR(prediction.projectedFutureSavings),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                        Text("Est. Month-End Pool", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Prediction Alert Callout Box (Exact user example: At your current spending, you will exceed your food budget by ₹2,300)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                DangerRed.copy(alpha = 0.15f),
                                WarningAmber.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Budget Overflow",
                        tint = DangerRed,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Budget Overflow Warning",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "At your current spending, you will exceed your ${prediction.overflowCategory.lowercase()} budget by ₹${FinancialEngine.formatINR(prediction.overflowAmount).replace("₹", "")}.",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recommendation: ${prediction.recommendation}",
                            fontSize = 11.sp,
                            color = CyanNeon
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. BILLS CENTER - COMPREHENSIVE UTILITIES
// ==========================================
@Composable
fun BillsCenterCard(
    bills: List<BillItem>,
    onPayBill: (BillItem) -> Unit,
    onAddBill: () -> Unit,
    onDeleteBill: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "Electricity", "Water", "Gas", "Mobile Recharge", "DTH", "Broadband", "Credit Card Bills")

    val filteredBills = if (selectedCategoryFilter == "ALL") {
        bills
    } else {
        bills.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WarningAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bills Center",
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Bills Center",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Electricity, Water, Gas, Mobile, DTH, CC",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Button(
                    onClick = onAddBill,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+ Add Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips for Utility Types
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(4).forEach { cat ->
                    val isSel = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) CyanNeon.copy(alpha = 0.2f) else DarkNavyCanvas)
                            .border(1.dp, if (isSel) CyanNeon else BorderGlassLight, RoundedCornerShape(10.dp))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) CyanNeon else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bills List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredBills.forEach { bill ->
                    val icon = getBillCategoryIcon(bill.category)
                    val iconColor = getBillCategoryColor(bill.category)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkNavyCanvas)
                            .border(1.dp, BorderGlassLight, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iconColor.copy(alpha = 0.15f))
                                    .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = bill.category,
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = bill.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Due: ${bill.dueDate}",
                                        fontSize = 11.sp,
                                        color = if (bill.isPaid) SuccessGreen else WarningAmber
                                    )
                                    if (bill.autoPayEnabled) {
                                        Text(
                                            text = " • Auto-Pay",
                                            fontSize = 10.sp,
                                            color = CyanNeon,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FinancialEngine.formatINR(bill.amount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (bill.isPaid) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("PAID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            } else {
                                Button(
                                    onClick = { onPayBill(bill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getBillCategoryIcon(category: String): ImageVector {
    return when {
        category.contains("Electric", ignoreCase = true) -> Icons.Default.Bolt
        category.contains("Water", ignoreCase = true) -> Icons.Default.WaterDrop
        category.contains("Gas", ignoreCase = true) -> Icons.Default.Whatshot
        category.contains("Mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        category.contains("DTH", ignoreCase = true) -> Icons.Default.Tv
        category.contains("Broadband", ignoreCase = true) || category.contains("Internet", ignoreCase = true) -> Icons.Default.Router
        category.contains("Credit", ignoreCase = true) || category.contains("Card", ignoreCase = true) -> Icons.Default.CreditCard
        else -> Icons.Default.Payments
    }
}

fun getBillCategoryColor(category: String): Color {
    return when {
        category.contains("Electric", ignoreCase = true) -> Color(0xFFF59E0B)
        category.contains("Water", ignoreCase = true) -> Color(0xFF06B6D4)
        category.contains("Gas", ignoreCase = true) -> Color(0xFFEF4444)
        category.contains("Mobile", ignoreCase = true) -> Color(0xFF10B981)
        category.contains("DTH", ignoreCase = true) -> Color(0xFFA855F7)
        category.contains("Broadband", ignoreCase = true) -> Color(0xFF3B82F6)
        category.contains("Credit", ignoreCase = true) -> Color(0xFFEC4899)
        else -> Color(0xFF10B981)
    }
}

// ==========================================
// 8. NOTIFICATIONS ALERTS CENTER
// ==========================================
@Composable
fun NotificationsFeedDialog(
    notifications: List<NotificationAlertItem>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onMarkAllRead) {
                Text("Mark All Read", color = CyanNeon, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = CyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant Alerts", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                }
                val unreadCount = notifications.count { it.isUnread }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DangerRed)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("$unreadCount New", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (notifications.isEmpty()) {
                    Text("No alerts at this moment.", color = TextMuted, fontSize = 12.sp)
                } else {
                    notifications.forEach { item ->
                        val alertColor = when (item.type) {
                            NotificationType.PAYMENT_SUCCESS -> SuccessGreen
                            NotificationType.BILL_DUE_TOMORROW -> WarningAmber
                            NotificationType.SAVINGS_GOAL_REACHED -> CyanNeon
                            NotificationType.SCORE_INCREASED -> Color(0xFF10B981)
                            NotificationType.BUDGET_CROSSED -> DangerRed
                            NotificationType.SECURITY_ALERT -> DangerRed
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (item.isUnread) alertColor.copy(alpha = 0.12f) else DarkNavyCanvas)
                                .border(1.dp, if (item.isUnread) alertColor.copy(alpha = 0.4f) else BorderGlassLight, RoundedCornerShape(14.dp))
                                .clickable {
                                    item.actionRoute?.let { onNavigate(it); onDismiss() }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(alertColor)
                                    .padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = item.timeAgo,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.message,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

// ==========================================
// 9. FAMILY MEMBER PROFILE DETAIL DIALOG
// ==========================================
@Composable
fun FamilyMemberProfileDetailDialog(
    member: FamilyMemberItem,
    onDismiss: () -> Unit,
    onSaveMember: (FamilyMemberItem) -> Unit,
    onDeleteMember: (Long) -> Unit
) {
    var salary by remember { mutableStateOf(if (member.salaryIncome > 0) member.salaryIncome.toInt().toString() else "") }
    var business by remember { mutableStateOf(if (member.businessIncome > 0) member.businessIncome.toInt().toString() else "") }
    var rental by remember { mutableStateOf(if (member.rentalIncome > 0) member.rentalIncome.toInt().toString() else "") }
    var freelance by remember { mutableStateOf(if (member.freelanceIncome > 0) member.freelanceIncome.toInt().toString() else "") }
    var foodExp by remember { mutableStateOf(if (member.foodExpense > 0) member.foodExpense.toInt().toString() else "") }
    var transportExp by remember { mutableStateOf(if (member.transportExpense > 0) member.transportExpense.toInt().toString() else "") }
    var shoppingExp by remember { mutableStateOf(if (member.shoppingExpense > 0) member.shoppingExpense.toInt().toString() else "") }
    var bankSav by remember { mutableStateOf(if (member.bankSavings > 0) member.bankSavings.toInt().toString() else "") }
    var fdSav by remember { mutableStateOf(if (member.fixedDeposit > 0) member.fixedDeposit.toInt().toString() else "") }
    var mutualFundSav by remember { mutableStateOf(if (member.mutualFund > 0) member.mutualFund.toInt().toString() else "") }
    var emiVal by remember { mutableStateOf(if (member.monthlyEmi > 0) member.monthlyEmi.toInt().toString() else "") }
    var fdInt by remember { mutableStateOf(if (member.fdInterest > 0) member.fdInterest.toInt().toString() else "") }
    var investReturns by remember { mutableStateOf(if (member.investmentReturns > 0) member.investmentReturns.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${member.name} (${member.role})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Detailed Financial Profile",
                        fontSize = 11.sp,
                        color = CyanNeon
                    )
                }
                IconButton(onClick = { onDeleteMember(member.id); onDismiss() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Incomes Group
                Text("INCOME BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        label = { Text("Salary (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuccessGreen)
                    )
                    OutlinedTextField(
                        value = business,
                        onValueChange = { business = it },
                        label = { Text("Business (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuccessGreen)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rental,
                        onValueChange = { rental = it },
                        label = { Text("Rental (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuccessGreen)
                    )
                    OutlinedTextField(
                        value = freelance,
                        onValueChange = { freelance = it },
                        label = { Text("Freelance (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SuccessGreen)
                    )
                }

                // Expenses Group
                Text("EXPENSE BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = foodExp,
                        onValueChange = { foodExp = it },
                        label = { Text("Food (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarningAmber)
                    )
                    OutlinedTextField(
                        value = transportExp,
                        onValueChange = { transportExp = it },
                        label = { Text("Transport (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarningAmber)
                    )
                }

                // Savings & Interest
                Text("SAVINGS & INTEREST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bankSav,
                        onValueChange = { bankSav = it },
                        label = { Text("Bank Savings (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanNeon)
                    )
                    OutlinedTextField(
                        value = fdInt,
                        onValueChange = { fdInt = it },
                        label = { Text("FD Interest (₹)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanNeon)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = member.copy(
                        salaryIncome = salary.toDoubleOrNull() ?: 0.0,
                        businessIncome = business.toDoubleOrNull() ?: 0.0,
                        rentalIncome = rental.toDoubleOrNull() ?: 0.0,
                        freelanceIncome = freelance.toDoubleOrNull() ?: 0.0,
                        foodExpense = foodExp.toDoubleOrNull() ?: 0.0,
                        transportExpense = transportExp.toDoubleOrNull() ?: 0.0,
                        shoppingExpense = shoppingExp.toDoubleOrNull() ?: 0.0,
                        bankSavings = bankSav.toDoubleOrNull() ?: 0.0,
                        fdInterest = fdInt.toDoubleOrNull() ?: 0.0,
                        monthlyContribution = (salary.toDoubleOrNull() ?: 0.0) + (business.toDoubleOrNull() ?: 0.0) + (rental.toDoubleOrNull() ?: 0.0) + (freelance.toDoubleOrNull() ?: 0.0),
                        spentThisMonth = (foodExp.toDoubleOrNull() ?: 0.0) + (transportExp.toDoubleOrNull() ?: 0.0) + (shoppingExp.toDoubleOrNull() ?: 0.0)
                    )
                    onSaveMember(updated)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
