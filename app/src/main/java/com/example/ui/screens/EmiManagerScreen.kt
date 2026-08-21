package com.example.ui.screens

import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.domain.security.BiometricAuthManager
import com.example.domain.security.BiometricAuthResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.EmiItem
import com.example.ui.MainViewModel
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkNavyCanvas
import com.example.ui.theme.DarkNavyElevated
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.GrayBorder
import com.example.ui.theme.GrayMuted
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiManagerScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emis by viewModel.emis.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedFilterCategory by remember { mutableStateOf("All") }
    var showAddEmiSheet by remember { mutableStateOf(false) }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    var payingEmiItem by remember { mutableStateOf<EmiItem?>(null) }
    var emiToDelete by remember { mutableStateOf<EmiItem?>(null) }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    // Computed overview statistics
    val totalLoanAmount = emis.sumOf { it.totalAmount }
    val totalPaidAmount = emis.sumOf { it.paidAmount }
    val totalOutstanding = (totalLoanAmount - totalPaidAmount).coerceAtLeast(0.0)
    val totalMonthlyEmi = emis.filter { !it.isCompleted }.sumOf { it.monthlyEmi }
    val overallProgress = if (totalLoanAmount > 0) {
        (totalPaidAmount / totalLoanAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val activeEmis = emis.filter { !it.isCompleted }
    val completedEmis = emis.filter { it.isCompleted }

    val filteredList = when (selectedFilterCategory) {
        "All" -> emis
        "Vehicles" -> emis.filter { it.category.equals("Vehicle", ignoreCase = true) }
        "Gadgets" -> emis.filter { it.category.equals("Electronics", ignoreCase = true) || it.category.equals("Mobile", ignoreCase = true) }
        "Education" -> emis.filter { it.category.equals("Education", ignoreCase = true) }
        "Home/Personal" -> emis.filter { it.category.equals("Home", ignoreCase = true) || it.category.equals("Personal", ignoreCase = true) }
        "Completed" -> completedEmis
        else -> emis
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
                            text = "EMI Manager & Loans",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${activeEmis.size} Active Loans • ${currencyFormatter.format(totalMonthlyEmi)}/mo",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanNeon,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate("home") },
                        modifier = Modifier.testTag("emi_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("emi_calculator") },
                        modifier = Modifier.testTag("emi_calculator_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "EMI Calculator",
                            tint = CyanNeon
                        )
                    }
                    IconButton(
                        onClick = { showAddEmiSheet = true },
                        modifier = Modifier.testTag("add_emi_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New EMI",
                            tint = SuccessGreen
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
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Overview Card with Outstanding & Progress Bar
            item {
                EmiHeroOverviewCard(
                    totalOutstanding = totalOutstanding,
                    totalLoanAmount = totalLoanAmount,
                    totalPaidAmount = totalPaidAmount,
                    totalMonthlyEmi = totalMonthlyEmi,
                    overallProgress = overallProgress,
                    activeCount = activeEmis.size,
                    currencyFormatter = currencyFormatter,
                    onOpenCalculator = { onNavigate("emi_calculator") },
                    onAddNew = { showAddEmiSheet = true }
                )
            }

            // 2. Upcoming Payment Alert Banner
            if (activeEmis.isNotEmpty()) {
                val nextUpcoming = activeEmis.firstOrNull { !it.isPaidThisMonth } ?: activeEmis.first()
                item {
                    EmiUpcomingDueBanner(
                        emi = nextUpcoming,
                        currencyFormatter = currencyFormatter,
                        onPayNow = { payingEmiItem = nextUpcoming }
                    )
                }
            }

            // 3. Category Filter Chips
            item {
                EmiFilterChipsRow(
                    selectedCategory = selectedFilterCategory,
                    onSelectCategory = { selectedFilterCategory = it },
                    totalCount = emis.size,
                    vehicleCount = emis.count { it.category.equals("Vehicle", true) },
                    gadgetCount = emis.count { it.category.equals("Electronics", true) || it.category.equals("Mobile", true) },
                    educationCount = emis.count { it.category.equals("Education", true) },
                    completedCount = completedEmis.size
                )
            }

            // 4. EMI List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Installments & Loans (${filteredList.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Sort: Due Date",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 5. Individual EMI Cards with Progress Bars
            if (filteredList.isEmpty()) {
                item {
                    EmiEmptyStateCard(
                        category = selectedFilterCategory,
                        onAddNew = { showAddEmiSheet = true }
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { emi ->
                    EmiItemCard(
                        emi = emi,
                        currencyFormatter = currencyFormatter,
                        onPayClick = { payingEmiItem = emi },
                        onDeleteClick = { emiToDelete = emi }
                    )
                }
            }

            // 6. EMI Tips & Pre-closure Advice Card
            item {
                EmiPreclosureTipsCard()
            }
        }
    }

    // Modal Bottom Sheet: Add New EMI
    if (showAddEmiSheet) {
        AddEmiBottomSheet(
            onDismiss = { showAddEmiSheet = false },
            onAddEmi = { title, category, totalAmount, monthlyEmi, interestRate, totalMonths, paidMonths, lender, dueDate, isAutoDebit, icon ->
                viewModel.addEmi(
                    title = title,
                    category = category,
                    totalAmount = totalAmount,
                    paidAmount = monthlyEmi * paidMonths,
                    monthlyEmi = monthlyEmi,
                    interestRate = interestRate,
                    totalTenureMonths = totalMonths,
                    paidTenureMonths = paidMonths,
                    dueDate = dueDate,
                    lenderBank = lender,
                    isAutoDebit = isAutoDebit,
                    iconName = icon
                )
                showAddEmiSheet = false
                Toast.makeText(context, "Added $title to EMI tracker", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Bottom Sheet: EMI & Loan Calculator
    if (showCalculatorSheet) {
        EmiCalculatorBottomSheet(
            currencyFormatter = currencyFormatter,
            onDismiss = { showCalculatorSheet = false },
            onAddCalculatedEmi = { loanName, category, principal, rate, tenureMonths, monthlyInstallment ->
                viewModel.addEmi(
                    title = loanName,
                    category = category,
                    totalAmount = principal,
                    paidAmount = 0.0,
                    monthlyEmi = monthlyInstallment,
                    interestRate = rate,
                    totalTenureMonths = tenureMonths,
                    paidTenureMonths = 0,
                    dueDate = "10th of every month",
                    lenderBank = "Preferred Bank",
                    isAutoDebit = true,
                    iconName = when (category) {
                        "Vehicle" -> "two_wheeler"
                        "Electronics" -> "laptop"
                        "Mobile" -> "smartphone"
                        "Education" -> "school"
                        else -> "account_balance"
                    }
                )
                showCalculatorSheet = false
                Toast.makeText(context, "Saved $loanName to active EMIs", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Pay EMI UPI Sheet with Biometric Protection
    payingEmiItem?.let { emi ->
        PayEmiUpiBottomSheet(
            emi = emi,
            currencyFormatter = currencyFormatter,
            onDismiss = { payingEmiItem = null },
            onConfirmPayment = { method ->
                var act: FragmentActivity? = context as? FragmentActivity
                var c = context
                while (act == null && c is ContextWrapper) {
                    if (c is FragmentActivity) {
                        act = c
                    }
                    c = c.baseContext
                }

                if (act != null) {
                    BiometricAuthManager.promptBiometric(
                        activity = act,
                        title = "Authorize EMI Payment",
                        subtitle = "Debit of ${currencyFormatter.format(emi.monthlyEmi)} for ${emi.title}",
                        description = "Verify biometric or screen lock to complete EMI installment",
                        onResult = { result ->
                            when (result) {
                                is BiometricAuthResult.Success -> {
                                    viewModel.payEmi(
                                        emiId = emi.id,
                                        emiTitle = emi.title,
                                        amount = emi.monthlyEmi,
                                        paymentMethod = method
                                    )
                                    payingEmiItem = null
                                    Toast.makeText(context, "Biometric verified! EMI installment paid via $method", Toast.LENGTH_LONG).show()
                                }
                                is BiometricAuthResult.Cancelled -> {
                                    Toast.makeText(context, "Payment authorization cancelled", Toast.LENGTH_SHORT).show()
                                }
                                is BiometricAuthResult.Error -> {
                                    Toast.makeText(context, "Authentication error: ${result.errString}", Toast.LENGTH_SHORT).show()
                                }
                                is BiometricAuthResult.Failed -> {
                                    Toast.makeText(context, "Biometric failed. Payment rejected.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                } else {
                    viewModel.payEmi(
                        emiId = emi.id,
                        emiTitle = emi.title,
                        amount = emi.monthlyEmi,
                        paymentMethod = method
                    )
                    payingEmiItem = null
                    Toast.makeText(context, "EMI installment paid successfully via $method!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    emiToDelete?.let { emi ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { emiToDelete = null },
            title = { Text(text = "Close Loan / Delete EMI?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to remove '${emi.title}' from your active EMI list? This will not delete recorded past transaction payments.",
                    color = GrayMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEmi(emi.id)
                        emiToDelete = null
                        Toast.makeText(context, "EMI removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.DangerRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { emiToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = DarkNavySurface
        )
    }
}

// -------------------------------------------------------------
// UI SUBCOMPONENTS
// -------------------------------------------------------------

@Composable
fun EmiHeroOverviewCard(
    totalOutstanding: Double,
    totalLoanAmount: Double,
    totalPaidAmount: Double,
    totalMonthlyEmi: Double,
    overallProgress: Float,
    activeCount: Int,
    currencyFormatter: NumberFormat,
    onOpenCalculator: () -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "hero_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanNeon.copy(alpha = 0.12f),
                            SecondaryViolet.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
                .padding(20.dp)
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
                                .clip(CircleShape)
                                .background(CyanNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TOTAL OUTSTANDING LOANS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanNeon,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$activeCount active liabilities",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GrayMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Surface(
                        color = if (overallProgress >= 0.5f) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (overallProgress >= 0.5f) SuccessGreen.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "${(overallProgress * 100).toInt()}% Repaid",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (overallProgress >= 0.5f) SuccessGreen else WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Outstanding Amount
                Text(
                    text = currencyFormatter.format(totalOutstanding),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar Container
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Repaid: ${currencyFormatter.format(totalPaidAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "Total Principal: ${currencyFormatter.format(totalLoanAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dual-color progress bar with glow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(DarkNavyElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(CyanNeon, SuccessGreen)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly commitment & Action strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkNavyElevated.copy(alpha = 0.7f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Outflow",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayMuted,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "${currencyFormatter.format(totalMonthlyEmi)} / month",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenCalculator,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calculator",
                                color = CyanNeon,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = onAddNew,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add EMI",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmiUpcomingDueBanner(
    emi: EmiItem,
    currencyFormatter: NumberFormat,
    onPayNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WarningAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Next Upcoming Due",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${emi.dueDate}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Text(
                        text = "${emi.title} • ${currencyFormatter.format(emi.monthlyEmi)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onPayNow,
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Pay Now",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiFilterChipsRow(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    totalCount: Int,
    vehicleCount: Int,
    gadgetCount: Int,
    educationCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "All" to "All ($totalCount)",
        "Vehicles" to "🏍️ Bike & Car ($vehicleCount)",
        "Gadgets" to "💻 Laptops & Phones ($gadgetCount)",
        "Education" to "🎓 Education ($educationCount)",
        "Home/Personal" to "🏠 Home & Personal",
        "Completed" to "✅ Completed ($completedCount)"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (catKey, label) ->
            val isSelected = selectedCategory == catKey
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(catKey) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = DarkNavySurface,
                    labelColor = GrayMuted,
                    selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                    selectedLabelColor = CyanNeon
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) CyanNeon else GrayBorder,
                    selectedBorderColor = CyanNeon,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun EmiItemCard(
    emi: EmiItem,
    currencyFormatter: NumberFormat,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = emi.progressPercentage,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "emi_card_progress"
    )

    val categoryIcon = getCategoryIcon(emi.category)
    val categoryColor = getCategoryColor(emi.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize()
            .testTag("emi_card_${emi.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(
            1.dp,
            if (emi.isCompleted) SuccessGreen.copy(alpha = 0.4f) else GrayBorder.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Icon, Title, Bank Badge, Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = emi.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = emi.lenderBank,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GrayMuted,
                                    fontSize = 11.sp
                                )
                            )
                            Text(text = "•", color = GrayMuted, fontSize = 10.sp)
                            Surface(
                                color = if (emi.interestRate == 0.0) SuccessGreen.copy(alpha = 0.15f) else SecondaryViolet.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (emi.interestRate == 0.0) "0% No-Cost" else "${emi.interestRate}% p.a.",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (emi.interestRate == 0.0) SuccessGreen else SecondaryViolet,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Monthly EMI Tag
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(emi.monthlyEmi),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "/ month",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrayMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar Section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(emi.progressPercentage * 100).toInt()}% Paid (${emi.paidTenureMonths}/${emi.totalTenureMonths} mos)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (emi.isCompleted) SuccessGreen else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = "${currencyFormatter.format(emi.remainingAmount)} left",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (emi.isCompleted) SuccessGreen else WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Animated Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkNavyElevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (emi.isCompleted) {
                                        listOf(SuccessGreen, SuccessGreen)
                                    } else {
                                        listOf(categoryColor, CyanNeon)
                                    }
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key Metrics Grid: Total Loan vs Paid vs Due Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkNavyElevated.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total Principal", color = GrayMuted, fontSize = 10.sp)
                    Text(
                        text = currencyFormatter.format(emi.totalAmount),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Paid So Far", color = GrayMuted, fontSize = 10.sp)
                    Text(
                        text = currencyFormatter.format(emi.paidAmount),
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(text = "Due Schedule", color = GrayMuted, fontSize = 10.sp)
                    Text(
                        text = emi.dueDate,
                        color = CyanNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Pay Now Button + Expand Details + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!emi.isCompleted) {
                        Button(
                            onClick = onPayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pay ${currencyFormatter.format(emi.monthlyEmi)}",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Loan Fully Repaid",
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand Breakdown",
                            tint = GrayMuted
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete EMI",
                            tint = GrayMuted
                        )
                    }
                }
            }

            // Expanded Breakdown Section
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkNavyElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Amortization & Schedule Breakdown",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BreakdownRow(label = "Category", value = emi.category)
                    BreakdownRow(label = "Lender / Financier", value = emi.lenderBank)
                    BreakdownRow(label = "Tenure Total", value = "${emi.totalTenureMonths} Months (${emi.paidTenureMonths} paid, ${emi.remainingTenureMonths} left)")
                    BreakdownRow(label = "Auto-Debit Status", value = if (emi.isAutoDebit) "Active (NACH / e-Mandate)" else "Manual UPI Payment")
                    BreakdownRow(label = "Last Payment Date", value = emi.lastPaymentDate ?: "N/A")
                    BreakdownRow(
                        label = "Estimated Total Interest",
                        value = if (emi.interestRate == 0.0) "₹0 (No Cost EMI)" else "₹${((emi.totalAmount * emi.interestRate * (emi.totalTenureMonths / 12.0)) / 100).toInt()}"
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = GrayMuted, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmiEmptyStateCard(
    category: String,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No EMIs in $category",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Track your Bike, Laptop, Mobile or Loan installments with smart progress bars and due date alerts.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = GrayMuted,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "+ Add New EMI", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmiPreclosureTipsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Smart Debt Optimization Tip",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CyanNeon,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Making 1 extra EMI payment per year on your highest interest loan (e.g. 9.0% Axis Home Loan) reduces your total repayment tenure by up to 18% and saves substantial interest.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GrayMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// BOTTOM SHEETS & MODALS
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmiBottomSheet(
    onDismiss: () -> Unit,
    onAddEmi: (
        title: String,
        category: String,
        totalAmount: Double,
        monthlyEmi: Double,
        interestRate: Double,
        totalMonths: Int,
        paidMonths: Int,
        lender: String,
        dueDate: String,
        isAutoDebit: Boolean,
        icon: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var loanTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Vehicle") }
    var totalPrincipal by remember { mutableStateOf("") }
    var monthlyEmiInput by remember { mutableStateOf("") }
    var interestRateInput by remember { mutableStateOf("0.0") }
    var tenureMonthsInput by remember { mutableStateOf("12") }
    var paidMonthsInput by remember { mutableStateOf("0") }
    var lenderBankInput by remember { mutableStateOf("HDFC Bank") }
    var dueDayInput by remember { mutableStateOf("05") }
    var isAutoDebit by remember { mutableStateOf(true) }

    val categories = listOf("Vehicle", "Electronics", "Mobile", "Education", "Home", "Personal")

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
                Text(
                    text = "Add New Loan or EMI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GrayMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = category == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkNavySurface,
                            labelColor = GrayMuted,
                            selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                            selectedLabelColor = CyanNeon
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Loan Title
            OutlinedTextField(
                value = loanTitle,
                onValueChange = { loanTitle = it },
                label = { Text("Item / Loan Name (e.g. Royal Enfield Hunter 350)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emi_title_input"),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Total Amount & Monthly EMI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = totalPrincipal,
                    onValueChange = { totalPrincipal = it },
                    label = { Text("Total Principal (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("emi_total_input"),
                    colors = outlinedTextFieldColors()
                )

                OutlinedTextField(
                    value = monthlyEmiInput,
                    onValueChange = { monthlyEmiInput = it },
                    label = { Text("Monthly EMI (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("emi_monthly_input"),
                    colors = outlinedTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tenure & Paid Months
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tenureMonthsInput,
                    onValueChange = { tenureMonthsInput = it },
                    label = { Text("Total Tenure (Mos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = outlinedTextFieldColors()
                )

                OutlinedTextField(
                    value = paidMonthsInput,
                    onValueChange = { paidMonthsInput = it },
                    label = { Text("Already Paid (Mos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = outlinedTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interest % & Lender Bank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = interestRateInput,
                    onValueChange = { interestRateInput = it },
                    label = { Text("Interest Rate %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = outlinedTextFieldColors()
                )

                OutlinedTextField(
                    value = lenderBankInput,
                    onValueChange = { lenderBankInput = it },
                    label = { Text("Bank / Lender") },
                    modifier = Modifier.weight(1.3f),
                    colors = outlinedTextFieldColors()
                )
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
                    label = { Text("Due Day of Month") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(160.dp),
                    colors = outlinedTextFieldColors()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Auto-Debit", color = Color.White, fontSize = 12.sp)
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

            // Submit Button
            Button(
                onClick = {
                    val principal = totalPrincipal.toDoubleOrNull() ?: 50000.0
                    val monthly = monthlyEmiInput.toDoubleOrNull() ?: (principal / (tenureMonthsInput.toIntOrNull() ?: 12))
                    val rate = interestRateInput.toDoubleOrNull() ?: 0.0
                    val totalMos = tenureMonthsInput.toIntOrNull() ?: 12
                    val paidMos = paidMonthsInput.toIntOrNull() ?: 0

                    onAddEmi(
                        if (loanTitle.isBlank()) "$category EMI" else loanTitle,
                        category,
                        principal,
                        monthly,
                        rate,
                        totalMos,
                        paidMos,
                        if (lenderBankInput.isBlank()) "Bank" else lenderBankInput,
                        "${dueDayInput.padStart(2, '0')}th of every month",
                        isAutoDebit,
                        when (category) {
                            "Vehicle" -> "two_wheeler"
                            "Electronics" -> "laptop"
                            "Mobile" -> "smartphone"
                            "Education" -> "school"
                            "Home" -> "home"
                            else -> "account_balance"
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_add_emi_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save EMI Tracker",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorBottomSheet(
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onAddCalculatedEmi: (name: String, category: String, principal: Double, rate: Double, tenureMonths: Int, monthlyEmi: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var loanName by remember { mutableStateOf("New Loan / Gadget") }
    var loanCategory by remember { mutableStateOf("Electronics") }
    var principalAmount by remember { mutableDoubleStateOf(100000.0) }
    var interestRate by remember { mutableDoubleStateOf(8.5) }
    var tenureMonths by remember { mutableIntStateOf(12) }

    // EMI Calculation formula: E = P * r * (1+r)^n / ((1+r)^n - 1)
    val monthlyRate = (interestRate / 12.0) / 100.0
    val calculatedEmi = if (interestRate <= 0.01) {
        principalAmount / tenureMonths.coerceAtLeast(1)
    } else {
        val factor = (1 + monthlyRate).pow(tenureMonths.toDouble())
        (principalAmount * monthlyRate * factor) / (factor - 1)
    }

    val totalPayment = calculatedEmi * tenureMonths
    val totalInterest = (totalPayment - principalAmount).coerceAtLeast(0.0)

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
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = CyanNeon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart EMI & Loan Calculator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GrayMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calculated EMI Result Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ESTIMATED MONTHLY INSTALLMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanNeon,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currencyFormatter.format(calculatedEmi)} / mo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkNavyElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Principal", color = GrayMuted, fontSize = 10.sp)
                            Text(
                                text = currencyFormatter.format(principalAmount),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Interest", color = GrayMuted, fontSize = 10.sp)
                            Text(
                                text = currencyFormatter.format(totalInterest),
                                color = WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total Payable", color = GrayMuted, fontSize = 10.sp)
                            Text(
                                text = currencyFormatter.format(totalPayment),
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sliders for Principal, Interest, Tenure
            // 1. Principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Loan Principal Amount", color = Color.White, fontSize = 13.sp)
                Text(
                    text = currencyFormatter.format(principalAmount),
                    color = CyanNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = principalAmount.toFloat(),
                onValueChange = { principalAmount = (it / 5000).toInt() * 5000.0 },
                valueRange = 10000f..1000000f,
                colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
            )

            // 2. Interest Rate %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Interest Rate (p.a.)", color = Color.White, fontSize = 13.sp)
                Text(
                    text = if (interestRate == 0.0) "0% (No Cost EMI)" else "${String.format(Locale.US, "%.1f", interestRate)}%",
                    color = if (interestRate == 0.0) SuccessGreen else SecondaryViolet,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = interestRate.toFloat(),
                onValueChange = { interestRate = (it * 2).toInt() / 2.0 },
                valueRange = 0f..24f,
                colors = SliderDefaults.colors(thumbColor = SecondaryViolet, activeTrackColor = SecondaryViolet)
            )

            // 3. Tenure in Months
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Loan Tenure", color = Color.White, fontSize = 13.sp)
                Text(
                    text = "$tenureMonths Months (${tenureMonths / 12} yrs ${tenureMonths % 12} mos)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Slider(
                value = tenureMonths.toFloat(),
                onValueChange = { tenureMonths = it.toInt() },
                valueRange = 3f..60f,
                steps = 56,
                colors = SliderDefaults.colors(thumbColor = SuccessGreen, activeTrackColor = SuccessGreen)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onAddCalculatedEmi(loanName, loanCategory, principalAmount, interestRate, tenureMonths, calculatedEmi)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to My Active EMIs", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayEmiUpiBottomSheet(
    emi: EmiItem,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onConfirmPayment: (method: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

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
                Text(
                    text = "Pay Monthly EMI Installment",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GrayMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = emi.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Lender: ${emi.lenderBank} • Due: ${emi.dueDate}", color = GrayMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currencyFormatter.format(emi.monthlyEmi),
                        color = CyanNeon,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Instant UPI App",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = GrayMuted,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // UPI Buttons: GPay, PhonePe, Paytm, BHIM
            val upiApps = listOf(
                Triple("Google Pay", "com.google.android.apps.nbu.paisa.user", Color(0xFF4285F4)),
                Triple("PhonePe", "com.phonepe.app", Color(0xFF5F259F)),
                Triple("Paytm UPI", "net.one97.paytm", Color(0xFF00B9F1)),
                Triple("BHIM UPI", "in.org.npci.upiapp", Color(0xFF00796B))
            )

            upiApps.forEach { (appName, pkgName, appColor) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            val upiUri = Uri.parse(
                                "upi://pay?pa=priyan1436ei@okhdfcbank&pn=FinFam-EMI&am=${emi.monthlyEmi}&cu=INR&tn=EMI-${emi.title}"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                                setPackage(pkgName)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback to generic intent
                                val genericIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                try {
                                    context.startActivity(genericIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Opening UPI payment simulation...", Toast.LENGTH_SHORT).show()
                                }
                            }
                            onConfirmPayment(appName)
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(appColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Pay with $appName", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Mark as Paid button
            OutlinedButton(
                onClick = { onConfirmPayment("Auto-Debit Bank Transfer") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Mark as Paid via Auto-Debit", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER FUNCTIONS & STYLING
// -------------------------------------------------------------

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "vehicle" -> Icons.Default.DirectionsBike
        "electronics" -> Icons.Default.LaptopMac
        "mobile" -> Icons.Default.Smartphone
        "education" -> Icons.Default.School
        "home" -> Icons.Default.Home
        else -> Icons.Default.AccountBalance
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "vehicle" -> CyanNeon
        "electronics" -> SecondaryViolet
        "mobile" -> Color(0xFF38BDF8)
        "education" -> Color(0xFFFBBF24)
        "home" -> Color(0xFF34D399)
        else -> CyanNeon
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanNeon,
    unfocusedBorderColor = GrayBorder,
    focusedLabelColor = CyanNeon,
    unfocusedLabelColor = GrayMuted,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = CyanNeon
)
