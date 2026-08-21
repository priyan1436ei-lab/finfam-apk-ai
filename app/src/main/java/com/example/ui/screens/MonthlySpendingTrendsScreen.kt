package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.engine.FinancialEngine
import androidx.compose.material.icons.filled.Notifications
import com.example.ui.components.CategoryDistributionPieChartCard
import com.example.ui.components.DailySpendingLineChartCard
import com.example.ui.components.ExpensePredictionCard
import com.example.ui.components.FamilyContributionCard
import com.example.ui.components.FamilyMemberProfileDetailDialog
import com.example.ui.components.MonthlyTrendAreaChartCard
import com.example.ui.components.NotificationsFeedDialog
import com.example.ui.components.WeeklySpendingBarChartCard
import com.example.domain.model.FamilyMemberItem
import com.example.domain.model.CategoryBreakdownItem
import com.example.domain.model.TimeHorizon
import com.example.domain.model.TransactionItem
import com.example.ui.MainViewModel
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.MonthlySpendingLineChart
import com.example.ui.components.TransactionRow
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

/**
 * Full 'Monthly Spending Trends' Screen visualizing category-wise monthly expenses
 * backed by Room Database transactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySpendingTrendsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendsState by viewModel.monthlySpendingTrends.collectAsStateWithLifecycle()
    val allTransactions by viewModel.transactions.collectAsStateWithLifecycle()
    val dailySpendPoints by viewModel.dailySpendingPoints.collectAsStateWithLifecycle()
    val weeklySpendBars by viewModel.weeklySpendingBars.collectAsStateWithLifecycle()
    val familyContributions by viewModel.familyContributions.collectAsStateWithLifecycle()
    val expensePrediction by viewModel.expensePrediction.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var selectedMemberForDetail by remember { mutableStateOf<FamilyMemberItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Real-Time Analytics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "AI Predictor • Line, Bar, Area & Pie",
                            fontSize = 11.sp,
                            color = CyanNeon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Instant Alerts Feed Icon
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = CyanNeon,
                                modifier = Modifier.size(24.dp)
                            )
                            val unreadCount = notifications.count { it.isUnread }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(DangerRed)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { showAddExpenseDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.2f))
                                .border(1.dp, PrimaryBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Expense",
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. AI Expense Prediction Card (Highlighted prominently at top)
            item {
                ExpensePredictionCard(prediction = expensePrediction)
            }

            // 2. Daily Spending - Line Chart
            item {
                DailySpendingLineChartCard(dataPoints = dailySpendPoints)
            }

            // 3. Weekly Spending - Bar Chart (Mon-Sun)
            item {
                WeeklySpendingBarChartCard(weeklyBars = weeklySpendBars)
            }

            // 4. Monthly Trend - Area Chart
            item {
                MonthlyTrendAreaChartCard()
            }

            // 5. Category Distribution - Donut / Pie Chart
            item {
                CategoryDistributionPieChartCard()
            }

            // 6. Family Contribution (Father 40%, Mother 30%, Son 20%, Daughter 10%)
            item {
                FamilyContributionCard(
                    contributions = familyContributions,
                    onMemberClick = { role ->
                        val member = familyMembers.firstOrNull { it.role.equals(role, ignoreCase = true) }
                            ?: familyMembers.firstOrNull()
                        selectedMemberForDetail = member
                    }
                )
            }

            // 7. Time Horizon Switcher (Last 3M, Last 6M, 1 Year, All Time)
            item {
                TimeHorizonSelector(
                    selectedHorizon = trendsState.selectedTimeHorizon,
                    onHorizonSelected = { horizon ->
                        viewModel.setSpendingTrendHorizon(horizon)
                    }
                )
            }

            // 8. Multi-Category Velocity Matrix
            item {
                MonthlySpendingLineChart(
                    state = trendsState,
                    onMonthSelected = { monthIdx ->
                        viewModel.setSpendingTrendSelectedMonth(monthIdx)
                    },
                    onCategorySelected = { category ->
                        viewModel.setSpendingTrendCategory(category)
                    },
                    onToggleMultiCategory = { category ->
                        viewModel.toggleSpendingTrendMultiCategory(category)
                    },
                    onToggleMultiLineMode = { enabled ->
                        viewModel.setSpendingTrendMultiLineMode(enabled)
                    }
                )
            }

            // 9. Key Velocity & Trend KPI Cards (4-Grid)
            item {
                TrendMetricsGrid(metrics = trendsState.metrics)
            }

            // 10. Monthly Active Slice Inspector (Details for selected month)
            item {
                val activeIdx = trendsState.selectedMonthIndex.coerceIn(0, (trendsState.monthsFull.size - 1).coerceAtLeast(0))
                val activeMonthPoint = trendsState.monthlyDataPoints.getOrNull(activeIdx)

                activeMonthPoint?.let { monthPoint ->
                    MonthOverviewCard(
                        monthPoint = monthPoint,
                        totalIncome = monthPoint.totalIncome,
                        totalExpense = monthPoint.totalExpense,
                        selectedCategory = trendsState.selectedCategory
                    )
                }
            }

            // 11. Category Spending Breakdown Matrix
            item {
                Text(
                    text = "ROOM DATABASE CATEGORY BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(trendsState.categoryBreakdowns) { item ->
                CategoryBreakdownRow(
                    item = item,
                    isSelected = trendsState.selectedCategory == item.category,
                    onClick = {
                        viewModel.setSpendingTrendMultiLineMode(false)
                        viewModel.setSpendingTrendCategory(
                            if (trendsState.selectedCategory == item.category) "ALL" else item.category
                        )
                    }
                )
            }

            // 12. Active Month Transaction Ledger Drilldown
            item {
                val activeIdx = trendsState.selectedMonthIndex.coerceIn(0, (trendsState.monthsFull.size - 1).coerceAtLeast(0))
                val monthName = trendsState.monthsFull.getOrElse(activeIdx) { "Current Month" }
                val activeCategory = trendsState.selectedCategory

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeCategory == "ALL") "TRANSACTIONS IN $monthName" else "$activeCategory IN $monthName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "+ Add Record",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable { showAddExpenseDialog = true }
                    )
                }
            }

            // Filter Room transactions matching active category if selected
            val filteredTxs = allTransactions.filter { tx ->
                !tx.isCredit && (trendsState.selectedCategory == "ALL" || tx.category.equals(trendsState.selectedCategory, ignoreCase = true))
            }

            if (filteredTxs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceVariant)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No direct transaction entries found for this category filter. Tap '+ Add Record' to log new expenses into Room.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredTxs.take(6)) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onDelete = { viewModel.deleteTransaction(tx.id) }
                    )
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        NotificationsFeedDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDismissNotification = { viewModel.dismissNotification(it) },
            onNavigate = onNavigate
        )
    }

    // Member Profile Detail Modal
    selectedMemberForDetail?.let { member ->
        FamilyMemberProfileDetailDialog(
            member = member,
            onDismiss = { selectedMemberForDetail = null },
            onSaveMember = { updated ->
                viewModel.updateFamilyMember(updated)
                selectedMemberForDetail = null
            },
            onDeleteMember = { id ->
                viewModel.deleteFamilyMember(id)
                selectedMemberForDetail = null
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddTransactionDialog(
            isIncomeMode = false,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, category, amount, method, notes, isFamily ->
                viewModel.addExpense(
                    title = title,
                    category = category,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes,
                    isFamilyShared = isFamily
                )
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun TimeHorizonSelector(
    selectedHorizon: TimeHorizon,
    onHorizonSelected: (TimeHorizon) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeHorizon.values().forEach { horizon ->
            val isSelected = horizon == selectedHorizon
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                    .clickable { onHorizonSelected(horizon) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = horizon.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
fun TrendMetricsGrid(
    metrics: com.example.domain.model.TrendMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Average Monthly Spend
            MetricCard(
                title = "Avg Monthly Spend",
                value = FinancialEngine.formatINR(metrics.averageMonthlySpend),
                subtitle = "Across selected window",
                icon = Icons.Default.Payments,
                iconColor = CyanNeon,
                modifier = Modifier.weight(1f)
            )

            // MoM Velocity Growth
            val isExpenseDown = metrics.momPercentageChange <= 0
            MetricCard(
                title = "30-Day Velocity",
                value = "${if (metrics.momPercentageChange > 0) "+" else ""}${String.format("%.1f", metrics.momPercentageChange)}%",
                subtitle = if (isExpenseDown) "Spending decreased" else "Spending increased",
                icon = if (isExpenseDown) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                iconColor = if (isExpenseDown) SuccessGreen else DangerRed,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Peak Month
            MetricCard(
                title = "Peak Spend Month",
                value = "${metrics.highestSpendMonth} (${FinancialEngine.formatINR(metrics.highestSpendAmount, compact = true)})",
                subtitle = "Highest recorded expenses",
                icon = Icons.Default.ArrowUpward,
                iconColor = WarningAmber,
                modifier = Modifier.weight(1f)
            )

            // Top Category Share
            MetricCard(
                title = "Top Category",
                value = "${metrics.topCategory} (${metrics.topCategoryPercentage.toInt()}%)",
                subtitle = "Largest budget share",
                icon = Icons.Default.Star,
                iconColor = SecondaryViolet,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MonthOverviewCard(
    monthPoint: com.example.domain.model.MonthlyDataPoint,
    totalIncome: Double,
    totalExpense: Double,
    selectedCategory: String,
    modifier: Modifier = Modifier
) {
    val netSavings = (totalIncome - totalExpense).coerceAtLeast(0.0)
    val savingsRate = if (totalIncome > 0) (netSavings / totalIncome) * 100.0 else 0.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(DarkSurfaceVariant, DarkSurfaceGlow)
                )
            )
            .border(1.dp, BorderGlassLight, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthPoint.monthFull,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${savingsRate.toInt()}% Saved",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Outflow", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = FinancialEngine.formatINR(totalExpense),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                }

                Column {
                    Text("Total Inflow", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = FinancialEngine.formatINR(totalIncome),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Column {
                    Text("Net Retained", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = FinancialEngine.formatINR(netSavings),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownRow(
    item: CategoryBreakdownItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        CyanNeon
    }

    val iconVector = when (item.category.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "rent" -> Icons.Default.Home
        "bills" -> Icons.Default.Bolt
        "travel" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "entertainment" -> Icons.Default.Tv
        "healthcare" -> Icons.Default.Favorite
        "education" -> Icons.Default.School
        else -> Icons.Default.Receipt
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) DarkSurfaceGlow else DarkSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) categoryColor else BorderGlass,
                RoundedCornerShape(14.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = iconVector, contentDescription = item.category, tint = categoryColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.category,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (item.isBudgetExceeded) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DangerRed.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("OVER BUDGET", fontSize = 8.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "Avg ${FinancialEngine.formatINR(item.monthlyAverage)}/mo • ${item.percentageShare.toInt()}% of wallet",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FinancialEngine.formatINR(item.totalAmount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    val isUp = item.momPercentageChange > 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isUp) DangerRed else SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (isUp) "+" else ""}${String.format("%.1f", item.momPercentageChange)}% MoM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUp) DangerRed else SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Distribution Progress Bar
            LinearProgressIndicator(
                progress = { (item.percentageShare / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = categoryColor,
                trackColor = DarkSurface
            )
        }
    }
}
