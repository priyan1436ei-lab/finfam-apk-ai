package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.EmiCalculatorEngine
import com.example.domain.model.AmortizationRow
import com.example.domain.model.EmiCalculationResult
import com.example.domain.model.LoanPreset
import com.example.domain.model.PrepaymentAnalysis
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkNavyCanvas
import com.example.ui.theme.DarkNavyElevated
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceGlow
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GrayBorder
import com.example.ui.theme.GrayMuted
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueGlow
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SecondaryVioletGlow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Active Slider & Parameter States
    var selectedPresetId by remember { mutableStateOf("bike") }
    var loanTitle by remember { mutableStateOf("Bike / Two-Wheeler EMI") }
    var loanCategory by remember { mutableStateOf("Vehicle") }
    var loanIconName by remember { mutableStateOf("two_wheeler") }
    var lenderBank by remember { mutableStateOf("HDFC Bank") }

    var principalAmount by remember { mutableDoubleStateOf(120000.0) }
    var annualInterestRate by remember { mutableDoubleStateOf(9.5) }
    var tenureMonths by remember { mutableIntStateOf(24) }
    var isTenureInYears by remember { mutableStateOf(false) }

    // Prepayment Simulator State
    var isPrepaymentEnabled by remember { mutableStateOf(false) }
    var extraMonthlyPrepayment by remember { mutableDoubleStateOf(2000.0) }

    // Amortization Schedule View Tab: 0 = Yearly, 1 = Monthly
    var selectedScheduleTab by remember { mutableIntStateOf(0) }
    var isScheduleExpanded by remember { mutableStateOf(true) }

    // Add to EMI Manager Bottom Sheet
    var showAddToManagerSheet by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var lastAddedEmiTitle by remember { mutableStateOf("") }

    // Real-Time Calculation Engine Computation
    val calculationResult by remember(principalAmount, annualInterestRate, tenureMonths, isPrepaymentEnabled, extraMonthlyPrepayment) {
        derivedStateOf {
            EmiCalculatorEngine.calculateEmi(
                principal = principalAmount,
                annualRate = annualInterestRate,
                tenureMonths = tenureMonths,
                extraMonthlyPrepayment = if (isPrepaymentEnabled) extraMonthlyPrepayment else 0.0
            )
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyCanvas),
        containerColor = DarkNavyCanvas,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Smart EMI Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Real-time Amortization & Loan Engine",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanNeon,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate("emi") },
                        modifier = Modifier.testTag("emi_calc_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to EMI Manager",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { onNavigate("emi") },
                        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Manager →",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkNavySurface
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Success Banner if just added to EMI Manager
            if (showSuccessBanner) {
                item {
                    EmiAddedSuccessBanner(
                        title = lastAddedEmiTitle,
                        monthlyEmi = currencyFormatter.format(calculationResult.monthlyEmi),
                        onViewInManager = { onNavigate("emi") },
                        onDismiss = { showSuccessBanner = false }
                    )
                }
            }

            // 2. Loan Type / Category Preset Chips Carousel
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECT LOAN TEMPLATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmiCalculatorEngine.LOAN_PRESETS.forEach { preset ->
                            val isSelected = selectedPresetId == preset.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPresetId = preset.id
                                    loanTitle = preset.title
                                    loanCategory = preset.category
                                    loanIconName = preset.iconName
                                    lenderBank = preset.defaultLender
                                    principalAmount = preset.defaultAmount
                                    annualInterestRate = preset.defaultAnnualRate
                                    tenureMonths = preset.defaultTenureMonths
                                    isTenureInYears = preset.defaultTenureMonths >= 36
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = getPresetIcon(preset.iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.Black else CyanNeon,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = preset.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanNeon,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkNavySurface,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CyanNeon else GrayBorder
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // 3. Real-Time EMI Output Hero Display Card with Donut Chart
            item {
                EmiHeroDisplayCard(
                    result = calculationResult,
                    currencyFormatter = currencyFormatter,
                    onOpenAddSheet = { showAddToManagerSheet = true }
                )
            }

            // 4. Interactive Sliders & Numerical Inputs Section
            item {
                EmiInteractiveSlidersCard(
                    principal = principalAmount,
                    onPrincipalChange = { principalAmount = it },
                    annualRate = annualInterestRate,
                    onRateChange = { annualInterestRate = it },
                    tenureMonths = tenureMonths,
                    onTenureMonthsChange = { tenureMonths = it },
                    isTenureInYears = isTenureInYears,
                    onToggleTenureMode = { isTenureInYears = it },
                    currencyFormatter = currencyFormatter
                )
            }

            // 5. Prepayment & Pre-closure Savings Simulator Card
            item {
                PrepaymentSimulatorCard(
                    isPrepaymentEnabled = isPrepaymentEnabled,
                    onTogglePrepayment = { isPrepaymentEnabled = it },
                    extraMonthly = extraMonthlyPrepayment,
                    onExtraMonthlyChange = { extraMonthlyPrepayment = it },
                    prepaymentResult = calculationResult.prepaymentScenario,
                    currencyFormatter = currencyFormatter
                )
            }

            // 6. Interactive Amortization Schedule Table (Yearly / Monthly)
            item {
                AmortizationScheduleSection(
                    result = calculationResult,
                    selectedTab = selectedScheduleTab,
                    onSelectTab = { selectedScheduleTab = it },
                    isExpanded = isScheduleExpanded,
                    onToggleExpanded = { isScheduleExpanded = !isScheduleExpanded },
                    currencyFormatter = currencyFormatter
                )
            }

            // 7. Quick Preclosure / Tax Benefit Insight Note
            item {
                EmiFinancialWisdomCard(
                    annualRate = annualInterestRate,
                    loanCategory = loanCategory
                )
            }
        }
    }

    // Add to EMI Manager Bottom Sheet
    if (showAddToManagerSheet) {
        AddToEmiManagerBottomSheet(
            defaultTitle = loanTitle,
            defaultCategory = loanCategory,
            defaultBank = lenderBank,
            defaultIcon = loanIconName,
            principal = principalAmount,
            monthlyEmi = calculationResult.monthlyEmi,
            interestRate = annualInterestRate,
            tenureMonths = tenureMonths,
            currencyFormatter = currencyFormatter,
            onDismiss = { showAddToManagerSheet = false },
            onConfirmAdd = { title, category, bank, dueDay, isAutoDebit, icon ->
                viewModel.addEmi(
                    title = title,
                    category = category,
                    totalAmount = calculationResult.totalPayable,
                    paidAmount = 0.0,
                    monthlyEmi = calculationResult.monthlyEmi,
                    interestRate = annualInterestRate,
                    totalTenureMonths = tenureMonths,
                    paidTenureMonths = 0,
                    dueDate = "${dueDay.toString().padStart(2, '0')}th of every month",
                    dueDayOfMonth = dueDay,
                    lenderBank = bank,
                    isAutoDebit = isAutoDebit,
                    iconName = icon
                )
                lastAddedEmiTitle = title
                showAddToManagerSheet = false
                showSuccessBanner = true
                Toast.makeText(context, "Added \"$title\" to EMI Manager!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// HERO DISPLAY CARD WITH LIVE DONUT CHART
// -----------------------------------------------------------------------------------------

@Composable
private fun EmiHeroDisplayCard(
    result: EmiCalculationResult,
    currencyFormatter: NumberFormat,
    onOpenAddSheet: () -> Unit
) {
    val animatedPrincipalPct by animateFloatAsState(
        targetValue = result.principalPercentageOfTotal,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "principalPct"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CyanNeon.copy(alpha = 0.6f), SecondaryViolet.copy(alpha = 0.4f))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MONTHLY INSTALLMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Calculated reducing balance EMI",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (result.annualInterestRate <= 0.01) SuccessGreen.copy(alpha = 0.2f)
                            else SecondaryViolet.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (result.annualInterestRate <= 0.01) "0% No-Cost" else "${String.format(Locale.US, "%.1f", result.annualInterestRate)}% p.a.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.annualInterestRate <= 0.01) SuccessGreen else SecondaryVioletGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big EMI Amount
            Text(
                text = "${currencyFormatter.format(result.monthlyEmi)} / mo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Chart & Breakdown Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavyElevated)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Custom Canvas Donut Chart
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2f
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val arcSize = Size(diameter, diameter)

                        // 1. Total Circle Background (Dark track)
                        drawArc(
                            color = DarkSurfaceGlow,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // 2. Interest Arc (Secondary Violet/Amber)
                        val interestSweep = (360f * (1f - (animatedPrincipalPct / 100f))).coerceIn(0f, 360f)
                        drawArc(
                            color = WarningAmber,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 3. Principal Arc (Cyan Neon)
                        val principalSweep = (360f * (animatedPrincipalPct / 100f)).coerceIn(0f, 360f)
                        drawArc(
                            color = CyanNeon,
                            startAngle = -90f,
                            sweepAngle = principalSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.principalPercentageOfTotal.toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Text(
                            text = "Principal",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Breakdown Metrics Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BreakdownRowItem(
                        label = "Principal Loan",
                        amount = currencyFormatter.format(result.principal),
                        percent = "${result.principalPercentageOfTotal.toInt()}%",
                        dotColor = CyanNeon
                    )
                    BreakdownRowItem(
                        label = "Total Interest",
                        amount = currencyFormatter.format(result.totalInterest),
                        percent = "${result.interestPercentageOfTotal.toInt()}%",
                        dotColor = WarningAmber
                    )
                    BreakdownRowItem(
                        label = "Total Payable",
                        amount = currencyFormatter.format(result.totalPayable),
                        percent = "100%",
                        dotColor = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Add to EMI Manager
            Button(
                onClick = onOpenAddSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_calculated_emi_to_manager_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to EMI Manager",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownRowItem(
    label: String,
    amount: String,
    percent: String,
    dotColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($percent)",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// INTERACTIVE SLIDERS & CONTROLS CARD
// -----------------------------------------------------------------------------------------

@Composable
private fun EmiInteractiveSlidersCard(
    principal: Double,
    onPrincipalChange: (Double) -> Unit,
    annualRate: Double,
    onRateChange: (Double) -> Unit,
    tenureMonths: Int,
    onTenureMonthsChange: (Int) -> Unit,
    isTenureInYears: Boolean,
    onToggleTenureMode: (Boolean) -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, GrayBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "LOAN PARAMETERS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon,
                letterSpacing = 1.sp
            )

            // -----------------------------------------------------------------
            // 1. PRINCIPAL LOAN AMOUNT
            // -----------------------------------------------------------------
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Loan Amount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = currencyFormatter.format(principal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanNeon
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = principal.toFloat(),
                    onValueChange = {
                        val snapped = when {
                            it < 100000 -> ((it / 5000).toInt() * 5000.0).coerceAtLeast(10000.0)
                            it < 1000000 -> ((it / 10000).toInt() * 10000.0)
                            else -> ((it / 50000).toInt() * 50000.0)
                        }
                        onPrincipalChange(snapped)
                    },
                    valueRange = 10000f..5000000f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanNeon,
                        activeTrackColor = CyanNeon,
                        inactiveTrackColor = DarkSurfaceGlow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Amount Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(50000.0, 100000.0, 250000.0, 500000.0, 1000000.0, 2500000.0, 4500000.0).forEach { amt ->
                        val isCurrent = (principal - amt).let { it >= -100 && it <= 100 }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) CyanNeon.copy(alpha = 0.25f) else DarkNavyElevated)
                                .border(1.dp, if (isCurrent) CyanNeon else GrayBorder, RoundedCornerShape(8.dp))
                                .clickable { onPrincipalChange(amt) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currencyFormatter.format(amt),
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) CyanNeon else TextSecondary
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 2. ANNUAL INTEREST RATE
            // -----------------------------------------------------------------
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Percent,
                            contentDescription = null,
                            tint = SecondaryVioletGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interest Rate (p.a.)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onRateChange((annualRate - 0.25).coerceAtLeast(0.0)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Decrease rate",
                                tint = SecondaryVioletGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = if (annualRate <= 0.01) "0% (No Cost)" else "${String.format(Locale.US, "%.2f", annualRate)}%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (annualRate <= 0.01) SuccessGreen else SecondaryVioletGlow,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { onRateChange((annualRate + 0.25).coerceAtMost(25.0)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Increase rate",
                                tint = SecondaryVioletGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = annualRate.toFloat(),
                    onValueChange = { onRateChange(((it * 4).toInt() / 4.0).coerceIn(0.0, 25.0)) },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(
                        thumbColor = SecondaryViolet,
                        activeTrackColor = SecondaryViolet,
                        inactiveTrackColor = DarkSurfaceGlow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Rate Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0.0 to "0% No-Cost",
                        8.5 to "8.5% Home",
                        8.9 to "8.9% Auto",
                        10.5 to "10.5% Edu",
                        12.5 to "12.5% Personal",
                        15.0 to "15% Gadget"
                    ).forEach { (rate, label) ->
                        val isCurrent = (annualRate - rate).let { it >= -0.05 && it <= 0.05 }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) SecondaryViolet.copy(alpha = 0.25f) else DarkNavyElevated)
                                .border(1.dp, if (isCurrent) SecondaryViolet else GrayBorder, RoundedCornerShape(8.dp))
                                .clickable { onRateChange(rate) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) SecondaryVioletGlow else TextSecondary
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 3. LOAN TENURE (MONTHS / YEARS SWITCHER)
            // -----------------------------------------------------------------
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Loan Tenure",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    // Months vs Years Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavyElevated)
                            .border(1.dp, GrayBorder, RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isTenureInYears) SuccessGreen else Color.Transparent)
                                .clickable { onToggleTenureMode(false) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Months",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isTenureInYears) Color.Black else TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTenureInYears) SuccessGreen else Color.Transparent)
                                .clickable { onToggleTenureMode(true) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Years",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTenureInYears) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTenureInYears) "${tenureMonths / 12} Years (${tenureMonths} Months)" else "$tenureMonths Months",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Slider(
                    value = tenureMonths.toFloat(),
                    onValueChange = {
                        val snapped = if (isTenureInYears) {
                            ((it / 12).toInt() * 12).coerceIn(12, 360)
                        } else {
                            it.toInt().coerceIn(3, 360)
                        }
                        onTenureMonthsChange(snapped)
                    },
                    valueRange = 3f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = SuccessGreen,
                        activeTrackColor = SuccessGreen,
                        inactiveTrackColor = DarkSurfaceGlow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Tenure Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        3 to "3 Mos",
                        6 to "6 Mos",
                        12 to "1 Year",
                        24 to "2 Years",
                        36 to "3 Years",
                        60 to "5 Years",
                        120 to "10 Years",
                        240 to "20 Years",
                        360 to "30 Years"
                    ).forEach { (months, label) ->
                        val isCurrent = tenureMonths == months
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) SuccessGreen.copy(alpha = 0.25f) else DarkNavyElevated)
                                .border(1.dp, if (isCurrent) SuccessGreen else GrayBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onTenureMonthsChange(months)
                                    onToggleTenureMode(months >= 36)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) SuccessGreen else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// PREPAYMENT & PRE-CLOSURE SAVINGS SIMULATOR
// -----------------------------------------------------------------------------------------

@Composable
private fun PrepaymentSimulatorCard(
    isPrepaymentEnabled: Boolean,
    onTogglePrepayment: (Boolean) -> Unit,
    extraMonthly: Double,
    onExtraMonthlyChange: (Double) -> Unit,
    prepaymentResult: PrepaymentAnalysis?,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(
            1.dp,
            if (isPrepaymentEnabled) SuccessGreen.copy(alpha = 0.5f) else GrayBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Prepayment & Pre-closure Simulator",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "See how extra monthly payment saves interest & years",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Switch(
                    checked = isPrepaymentEnabled,
                    onCheckedChange = onTogglePrepayment,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SuccessGreen,
                        checkedTrackColor = SuccessGreen.copy(alpha = 0.3f)
                    )
                )
            }

            AnimatedVisibility(visible = isPrepaymentEnabled) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Extra Monthly Contribution",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "+ ${currencyFormatter.format(extraMonthly)} / mo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }

                    Slider(
                        value = extraMonthly.toFloat(),
                        onValueChange = { onExtraMonthlyChange(((it / 500).toInt() * 500.0).coerceIn(500.0, 25000.0)) },
                        valueRange = 500f..25000f,
                        colors = SliderDefaults.colors(
                            thumbColor = SuccessGreen,
                            activeTrackColor = SuccessGreen,
                            inactiveTrackColor = DarkSurfaceGlow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (prepaymentResult != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkNavyElevated)
                                .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "TENURE REDUCED BY", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${prepaymentResult.monthsSaved} Months",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "(${prepaymentResult.newTenureMonths} mos remaining)",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(GrayBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "INTEREST SAVED", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currencyFormatter.format(prepaymentResult.totalInterestSaved),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WarningAmber
                                )
                                Text(
                                    text = "${prepaymentResult.interestSavingsPercent.toInt()}% less interest",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// AMORTIZATION SCHEDULE SECTION (YEARLY / MONTHLY TABLES)
// -----------------------------------------------------------------------------------------

@Composable
private fun AmortizationScheduleSection(
    result: EmiCalculationResult,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, GrayBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Amortization Paydown Schedule",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onToggleExpanded, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Schedule",
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Tab Selector: Yearly vs Monthly
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkNavyElevated,
                        contentColor = CyanNeon,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = CyanNeon
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { onSelectTab(0) },
                            text = {
                                Text(
                                    text = "Yearly Summary (${result.yearlyAmortization.size} Yrs)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) CyanNeon else TextSecondary
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { onSelectTab(1) },
                            text = {
                                Text(
                                    text = "Monthly Breakdown (${result.monthlyAmortization.size} Mos)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) CyanNeon else TextSecondary
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavyElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Period", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(60.dp))
                        Text(text = "Principal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanNeon, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                        Text(text = "Interest", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                        Text(text = "Balance", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val scheduleList = if (selectedTab == 0) result.yearlyAmortization else result.monthlyAmortization.take(60)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        scheduleList.forEach { row ->
                            AmortizationRowCard(
                                row = row,
                                currencyFormatter = currencyFormatter
                            )
                        }

                        if (selectedTab == 1 && result.monthlyAmortization.size > 60) {
                            Text(
                                text = "Showing first 60 of ${result.monthlyAmortization.size} monthly installments. Switch to Yearly Summary for full horizon view.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmortizationRowCard(
    row: AmortizationRow,
    currencyFormatter: NumberFormat
) {
    val totalPaid = (row.principalPaid + row.interestPaid).coerceAtLeast(1.0)
    val principalRatio = (row.principalPaid / totalPaid).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = row.periodLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.width(60.dp)
            )
            Text(
                text = currencyFormatter.format(row.principalPaid),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyanNeon,
                modifier = Modifier.width(65.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = currencyFormatter.format(row.interestPaid),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )
            Text(
                text = currencyFormatter.format(row.closingBalance),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.width(70.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dual-color progress bar showing Principal vs Interest share for this specific period
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(WarningAmber)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(principalRatio)
                    .height(3.dp)
                    .background(CyanNeon)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// FINANCIAL WISDOM & TAX NOTE CARD
// -----------------------------------------------------------------------------------------

@Composable
private fun EmiFinancialWisdomCard(
    annualRate: Double,
    loanCategory: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "AI Debt Strategy Insight",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(2.dp))
                val wisdomText = when (loanCategory) {
                    "Home" -> "Home Loans qualify for Section 80C principal deduction (up to ₹1.5L) and Section 24(b) interest deduction (up to ₹2L/yr). A 10% annual prepayment can cut a 20-year loan down to ~12 years."
                    "Education" -> "Section 80E provides 100% tax deduction on education loan interest without any upper ceiling for up to 8 continuous assessment years."
                    "Vehicle" -> "Two-wheelers and car loans carry reducing balance interest. Paying an extra ₹1,000 to ₹2,000 monthly clears the loan up to 6 months faster with significant interest savings."
                    else -> if (annualRate <= 0.01) "0% No-Cost EMI keeps your capital liquid in high-yield mutual funds or emergency deposits while paying in zero-interest installments."
                    else "High-interest personal/gadget loans (>12%) should be prioritized for early pre-closure to protect your AI Health Score and debt-to-income ratio."
                }
                Text(
                    text = wisdomText,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// SUCCESS BANNER AFTER ADDING TO EMI MANAGER
// -----------------------------------------------------------------------------------------

@Composable
private fun EmiAddedSuccessBanner(
    title: String,
    monthlyEmi: String,
    onViewInManager: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, SuccessGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Added to Active Loans!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Text(
                        text = "$title • $monthlyEmi/mo",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onViewInManager,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "View", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// ADD TO EMI MANAGER MODAL BOTTOM SHEET
// -----------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToEmiManagerBottomSheet(
    defaultTitle: String,
    defaultCategory: String,
    defaultBank: String,
    defaultIcon: String,
    principal: Double,
    monthlyEmi: Double,
    interestRate: Double,
    tenureMonths: Int,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onConfirmAdd: (
        title: String,
        category: String,
        bank: String,
        dueDay: Int,
        isAutoDebit: Boolean,
        icon: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var loanTitleInput by remember { mutableStateOf(defaultTitle) }
    var categorySelection by remember { mutableStateOf(defaultCategory) }
    var bankInput by remember { mutableStateOf(defaultBank) }
    var dueDayInput by remember { mutableStateOf("05") }
    var isAutoDebit by remember { mutableStateOf(true) }

    val categories = listOf("Vehicle", "Electronics", "Mobile", "Education", "Home", "Personal")
    val popularBanks = listOf("HDFC Bank", "SBI", "ICICI Bank", "Axis Bank", "Kotak Bank", "Bajaj Finserv")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkNavyCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, tint = CyanNeon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Active Loans Tracker",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Card of the calculated values
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Principal", fontSize = 10.sp, color = TextMuted)
                        Text(text = currencyFormatter.format(principal), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Monthly EMI", fontSize = 10.sp, color = TextMuted)
                        Text(text = currencyFormatter.format(monthlyEmi), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Tenure", fontSize = 10.sp, color = TextMuted)
                        Text(text = "$tenureMonths Mos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title Field
            OutlinedTextField(
                value = loanTitleInput,
                onValueChange = { loanTitleInput = it },
                label = { Text("Loan Title / Item Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Selector Chips
            Text(text = "Category", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = categorySelection.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanNeon else DarkNavyElevated)
                            .border(1.dp, if (isSelected) CyanNeon else GrayBorder, RoundedCornerShape(8.dp))
                            .clickable { categorySelection = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bank / Lender Selection
            OutlinedTextField(
                value = bankInput,
                onValueChange = { bankInput = it },
                label = { Text("Financing Partner / Lender Bank") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedColors()
            )

            // Popular Bank Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                popularBanks.forEach { b ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkNavyElevated)
                            .border(1.dp, GrayBorder, RoundedCornerShape(6.dp))
                            .clickable { bankInput = b }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = b, fontSize = 9.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due Date & Auto Debit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dueDayInput,
                    onValueChange = { dueDayInput = it },
                    label = { Text("Due Day of Month (1-28)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(170.dp),
                    colors = outlinedColors()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Auto-Debit", color = TextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isAutoDebit,
                        onCheckedChange = { isAutoDebit = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanNeon,
                            checkedTrackColor = CyanNeon.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Button(
                onClick = {
                    val dueDayInt = dueDayInput.toIntOrNull()?.coerceIn(1, 28) ?: 5
                    val icon = when (categorySelection) {
                        "Vehicle" -> "two_wheeler"
                        "Electronics" -> "laptop"
                        "Mobile" -> "smartphone"
                        "Education" -> "school"
                        "Home" -> "home"
                        else -> "account_balance"
                    }
                    onConfirmAdd(
                        if (loanTitleInput.isBlank()) "$categorySelection Loan" else loanTitleInput,
                        categorySelection,
                        if (bankInput.isBlank()) "Bank" else bankInput,
                        dueDayInt,
                        isAutoDebit,
                        icon
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_add_emi_dialog_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save & Track in EMI Manager",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanNeon,
    unfocusedBorderColor = GrayBorder,
    focusedLabelColor = CyanNeon,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyanNeon
)

private fun getPresetIcon(iconName: String): ImageVector {
    return when (iconName) {
        "two_wheeler" -> Icons.Default.DirectionsBike
        "car" -> Icons.Default.DirectionsCar
        "laptop" -> Icons.Default.LaptopMac
        "smartphone" -> Icons.Default.Smartphone
        "home" -> Icons.Default.Home
        "school" -> Icons.Default.School
        else -> Icons.Default.AccountBalance
    }
}
