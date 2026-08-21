package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.FinancialEngine
import com.example.domain.engine.SpendingTrendsEngine
import com.example.domain.model.CategoryTrendSeries
import com.example.domain.model.MonthlySpendingTrendsState
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
import kotlin.math.roundToInt

/**
 * High-Precision Interactive Line Chart for Category-Wise Monthly Expenses
 */
@Composable
fun MonthlySpendingLineChart(
    state: MonthlySpendingTrendsState,
    onMonthSelected: (Int) -> Unit,
    onCategorySelected: (String) -> Unit,
    onToggleMultiCategory: (String) -> Unit,
    onToggleMultiLineMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthsShort = state.monthsShort
    val monthsFull = state.monthsFull
    val selectedCategory = state.selectedCategory
    val isMultiMode = state.isMultiLineMode
    val multiCategories = state.selectedMultiCategories

    // Animation progress for entering or transitioning
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(state.selectedTimeHorizon, selectedCategory, isMultiMode) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    // Active touch scrubber state
    var isDragging by remember { mutableStateOf(false) }
    var scrubX by remember { mutableFloatStateOf(-1f) }
    var scrubIndex by remember { mutableIntStateOf(state.selectedMonthIndex.coerceAtLeast(0)) }

    // Synchronize scrubIndex with external state
    LaunchedEffect(state.selectedMonthIndex) {
        if (state.selectedMonthIndex in monthsShort.indices) {
            scrubIndex = state.selectedMonthIndex
        }
    }

    // Determine active lines to draw
    val activeSeriesToDraw = remember(state, selectedCategory, isMultiMode, multiCategories) {
        if (isMultiMode) {
            state.categorySeries.filter { multiCategories.contains(it.category) }
        } else if (selectedCategory == "ALL") {
            listOf(
                CategoryTrendSeries(
                    category = "Total Expense",
                    colorHex = "#38BDF8", // Cyan Neon
                    dataPoints = state.totalExpenseSeries,
                    totalSpent = state.metrics.totalSpendInWindow,
                    averageMonthly = state.metrics.averageMonthlySpend,
                    momPercentageChange = state.metrics.momPercentageChange,
                    peakMonth = state.metrics.highestSpendMonth,
                    peakAmount = state.metrics.highestSpendAmount,
                    lowestMonth = state.metrics.lowestSpendMonth,
                    lowestAmount = state.metrics.lowestSpendAmount
                )
            )
        } else {
            state.categorySeries.filter { it.category == selectedCategory }
        }
    }

    // Calculate chart Y-axis scale bounds
    val maxYVal = remember(activeSeriesToDraw, state.totalExpenseSeries) {
        val maxFromActive = activeSeriesToDraw.flatMap { it.dataPoints }.maxOrNull() ?: 50000.0
        val maxFromTotal = if (selectedCategory == "ALL") state.totalExpenseSeries.maxOrNull() ?: 50000.0 else maxFromActive
        val ceiling = kotlin.math.max(maxFromActive, maxFromTotal) * 1.15
        val rounded = ((ceiling / 5000.0).toInt() + 1) * 5000.0
        rounded.coerceAtLeast(20000.0)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // 1. Chart Header & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedCategory == "ALL") "MONTHLY SPENDING CURVE" else "$selectedCategory TREND",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyanNeon,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Active Inspection Value
                    val activeIndex = scrubIndex.coerceIn(0, (monthsShort.size - 1).coerceAtLeast(0))
                    val activeMonthName = monthsFull.getOrElse(activeIndex) { "August 2026" }
                    val activeAmount = if (selectedCategory == "ALL") {
                        state.totalExpenseSeries.getOrElse(activeIndex) { 0.0 }
                    } else {
                        activeSeriesToDraw.firstOrNull()?.dataPoints?.getOrElse(activeIndex) { 0.0 } ?: 0.0
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = FinancialEngine.formatINR(activeAmount),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "in $activeMonthName",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                // Mode Toggle: Single Series vs Multi-Line Overlay
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isMultiMode) PrimaryBlue.copy(alpha = 0.25f) else DarkSurfaceGlow)
                        .border(1.dp, if (isMultiMode) PrimaryBlue else BorderGlass, RoundedCornerShape(10.dp))
                        .clickable { onToggleMultiLineMode(!isMultiMode) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Multi-Line Overlay",
                            tint = if (isMultiMode) CyanNeon else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMultiMode) "Multi-Line ON" else "Multi-Line",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMultiMode) CyanNeon else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Interactive Line Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .pointerInput(monthsShort) {
                        detectTapGestures(
                            onPress = { offset ->
                                val width = size.width
                                val leftPadding = 45.dp.toPx()
                                val rightPadding = 16.dp.toPx()
                                val chartWidth = width - leftPadding - rightPadding
                                val count = monthsShort.size
                                if (count > 1 && chartWidth > 0) {
                                    val step = chartWidth / (count - 1)
                                    val localX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                                    val idx = (localX / step).roundToInt().coerceIn(0, count - 1)
                                    scrubIndex = idx
                                    scrubX = leftPadding + idx * step
                                    onMonthSelected(idx)
                                }
                            }
                        )
                    }
                    .pointerInput(monthsShort) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val width = size.width
                                val leftPadding = 45.dp.toPx()
                                val rightPadding = 16.dp.toPx()
                                val chartWidth = width - leftPadding - rightPadding
                                val count = monthsShort.size
                                if (count > 1 && chartWidth > 0) {
                                    val step = chartWidth / (count - 1)
                                    val localX = (offset.x - leftPadding).coerceIn(0f, chartWidth)
                                    val idx = (localX / step).roundToInt().coerceIn(0, count - 1)
                                    scrubIndex = idx
                                    scrubX = leftPadding + idx * step
                                    onMonthSelected(idx)
                                }
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                val width = size.width
                                val leftPadding = 45.dp.toPx()
                                val rightPadding = 16.dp.toPx()
                                val chartWidth = width - leftPadding - rightPadding
                                val count = monthsShort.size
                                if (count > 1 && chartWidth > 0) {
                                    val step = chartWidth / (count - 1)
                                    val localX = (change.position.x - leftPadding).coerceIn(0f, chartWidth)
                                    val idx = (localX / step).roundToInt().coerceIn(0, count - 1)
                                    scrubIndex = idx
                                    scrubX = leftPadding + idx * step
                                    onMonthSelected(idx)
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val leftPadding = 45.dp.toPx()
                    val rightPadding = 16.dp.toPx()
                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 28.dp.toPx()

                    val chartWidth = width - leftPadding - rightPadding
                    val chartHeight = height - topPadding - bottomPadding
                    val count = monthsShort.size
                    val progress = animProgress.value

                    // Draw Horizontal Grid & Y-Axis Labels
                    val ySteps = 4
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 10.sp.toPx()
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }

                    for (i in 0..ySteps) {
                        val yRatio = i.toFloat() / ySteps
                        val yPos = topPadding + chartHeight * (1f - yRatio)
                        val valueAtLine = maxYVal * yRatio

                        // Dotted grid line
                        drawLine(
                            color = Color(0x1AFFFFFF),
                            start = Offset(leftPadding, yPos),
                            end = Offset(width - rightPadding, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Y-axis INR label
                        val labelText = if (valueAtLine >= 1000) "₹${(valueAtLine / 1000).toInt()}k" else "₹0"
                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            leftPadding - 8.dp.toPx(),
                            yPos + 4.dp.toPx(),
                            textPaint
                        )
                    }

                    // Draw X-Axis Month Labels
                    val xTextPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 11.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val xPositions = FloatArray(count)
                    if (count > 0) {
                        val step = if (count > 1) chartWidth / (count - 1) else 0f
                        for (idx in 0 until count) {
                            val xPos = leftPadding + idx * step
                            xPositions[idx] = xPos

                            val isSelected = idx == scrubIndex
                            if (isSelected) {
                                xTextPaint.color = android.graphics.Color.parseColor("#38BDF8")
                                xTextPaint.isFakeBoldText = true
                            } else {
                                xTextPaint.color = android.graphics.Color.parseColor("#64748B")
                                xTextPaint.isFakeBoldText = false
                            }

                            drawContext.canvas.nativeCanvas.drawText(
                                monthsShort[idx],
                                xPos,
                                height - 6.dp.toPx(),
                                xTextPaint
                            )
                        }
                    }

                    // Draw Active Scrubber Line & Touch Highlight
                    if (scrubIndex in 0 until count && count > 1) {
                        val activeX = xPositions[scrubIndex]
                        drawLine(
                            color = CyanNeon.copy(alpha = 0.5f),
                            start = Offset(activeX, topPadding),
                            end = Offset(activeX, topPadding + chartHeight),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }

                    // Draw Series Lines (Cubic Bezier curves)
                    activeSeriesToDraw.forEach { series ->
                        val lineColor = try {
                            Color(android.graphics.Color.parseColor(series.colorHex))
                        } catch (e: Exception) {
                            CyanNeon
                        }

                        val dataPoints = series.dataPoints
                        if (dataPoints.size == count && count > 1) {
                            val points = mutableListOf<Offset>()
                            for (idx in 0 until count) {
                                val x = xPositions[idx]
                                val rawY = (dataPoints[idx] / maxYVal).coerceIn(0.0, 1.0).toFloat()
                                val y = topPadding + chartHeight * (1f - rawY * progress)
                                points.add(Offset(x, y))
                            }

                            // Smooth Path construction
                            val linePath = Path()
                            val fillPath = Path()

                            if (points.isNotEmpty()) {
                                linePath.moveTo(points[0].x, points[0].y)
                                fillPath.moveTo(points[0].x, topPadding + chartHeight)
                                fillPath.lineTo(points[0].x, points[0].y)

                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                                    val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                                    linePath.cubicTo(
                                        controlPoint1.x, controlPoint1.y,
                                        controlPoint2.x, controlPoint2.y,
                                        p1.x, p1.y
                                    )
                                    fillPath.cubicTo(
                                        controlPoint1.x, controlPoint1.y,
                                        controlPoint2.x, controlPoint2.y,
                                        p1.x, p1.y
                                    )
                                }

                                fillPath.lineTo(points.last().x, topPadding + chartHeight)
                                fillPath.close()

                                // Draw gradient fill below line (only in single-series mode or total mode)
                                if (!isMultiMode || activeSeriesToDraw.size == 1) {
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                lineColor.copy(alpha = 0.35f * progress),
                                                lineColor.copy(alpha = 0.05f * progress),
                                                Color.Transparent
                                            ),
                                            startY = topPadding,
                                            endY = topPadding + chartHeight
                                        )
                                    )
                                }

                                // Draw the sleek line curve
                                drawPath(
                                    path = linePath,
                                    color = lineColor,
                                    style = Stroke(
                                        width = if (isMultiMode) 2.5.dp.toPx() else 3.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                // Draw data node circles
                                points.forEachIndexed { idx, point ->
                                    val isNodeActive = idx == scrubIndex
                                    if (isNodeActive) {
                                        // Outer glow ring
                                        drawCircle(
                                            color = lineColor.copy(alpha = 0.3f),
                                            radius = 11.dp.toPx(),
                                            center = point
                                        )
                                        // Inner solid circle
                                        drawCircle(
                                            color = Color.White,
                                            radius = 5.dp.toPx(),
                                            center = point
                                        )
                                        drawCircle(
                                            color = lineColor,
                                            radius = 3.dp.toPx(),
                                            center = point
                                        )
                                    } else {
                                        drawCircle(
                                            color = DarkBackground,
                                            radius = 4.dp.toPx(),
                                            center = point
                                        )
                                        drawCircle(
                                            color = lineColor,
                                            radius = 2.5.dp.toPx(),
                                            center = point
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Category Filter Chips Carousel
            Text(
                text = "FILTER CATEGORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "ALL" chip
                CategoryFilterPill(
                    label = "All Categories",
                    color = CyanNeon,
                    amount = state.metrics.totalSpendInWindow,
                    isSelected = selectedCategory == "ALL" && !isMultiMode,
                    onClick = {
                        onToggleMultiLineMode(false)
                        onCategorySelected("ALL")
                    }
                )

                // Individual Category Chips
                state.categorySeries.forEach { series ->
                    val isSelected = if (isMultiMode) {
                        multiCategories.contains(series.category)
                    } else {
                        selectedCategory == series.category
                    }

                    val pillColor = try {
                        Color(android.graphics.Color.parseColor(series.colorHex))
                    } catch (e: Exception) {
                        CyanNeon
                    }

                    CategoryFilterPill(
                        label = series.category,
                        color = pillColor,
                        amount = series.totalSpent,
                        isSelected = isSelected,
                        onClick = {
                            if (isMultiMode) {
                                onToggleMultiCategory(series.category)
                            } else {
                                onCategorySelected(series.category)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterPill(
    label: String,
    color: Color,
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) color.copy(alpha = 0.2f) else DarkSurface
    val borderCol = if (isSelected) color else BorderGlass

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TextPrimary else TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = FinancialEngine.formatINR(amount, compact = true),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) color else TextMuted
            )
        }
    }
}
