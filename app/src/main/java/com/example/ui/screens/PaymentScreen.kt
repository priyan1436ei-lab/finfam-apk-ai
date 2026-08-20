package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.PaymentRepository
import com.example.data.repository.RealPaymentRepository
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.PaymentMethodType
import com.example.domain.model.RazorpayPaymentUiState
import com.example.domain.model.RazorpayTransactionRecord
import com.example.domain.model.SubscriptionPlanTier
import com.example.ui.PaymentViewModel
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
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

/**
 * Main PaymentScreen Composable integrating Razorpay Checkout SDK
 * with the server-backed PaymentRepository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    paymentViewModel: PaymentViewModel = viewModel(),
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val paymentState by paymentViewModel.paymentState.collectAsStateWithLifecycle()
    val isSubscriptionActive by paymentViewModel.isSubscriptionActive.collectAsStateWithLifecycle()
    val activePlanTier by paymentViewModel.activePlanTier.collectAsStateWithLifecycle()
    val selectedPlan by paymentViewModel.selectedPlan.collectAsStateWithLifecycle()
    val selectedMethod by paymentViewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val paymentHistory by paymentViewModel.paymentHistory.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Checkout, 1: History
    var showQrDialog by remember { mutableStateOf(false) }
    var selectedRefundRecord by remember { mutableStateOf<RazorpayTransactionRecord?>(null) }

    Scaffold(
        modifier = modifier.testTag("payment_screen_container"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FinFam Premium",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Razorpay Official Checkout • 256-Bit SSL",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate("home") },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        when (val state = paymentState) {
            is RazorpayPaymentUiState.Success -> {
                PaymentSuccessCard(
                    state = state,
                    onContinue = {
                        paymentViewModel.resetState()
                        onNavigate("home")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.Failed -> {
                PaymentFailedCard(
                    state = state,
                    onRetry = {
                        activity?.let { act ->
                            paymentViewModel.initiateRealPayment(act)
                        }
                    },
                    onBackToPlans = {
                        paymentViewModel.resetState()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.Pending -> {
                PaymentPendingCard(
                    state = state,
                    onCheckStatus = {
                        paymentViewModel.checkPendingPaymentStatus(state.orderId)
                    },
                    onBack = {
                        paymentViewModel.resetState()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.UserCancelled -> {
                PaymentCancelledCard(
                    message = state.message,
                    onTryAgain = {
                        paymentViewModel.resetState()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(DarkBackground)
                ) {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = DarkBackground,
                        contentColor = PrimaryBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = PrimaryBlue
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            modifier = Modifier.testTag("tab_checkout"),
                            text = { Text("Plans & Checkout", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            modifier = Modifier.testTag("tab_history"),
                            text = {
                                Text("Payment History (${paymentHistory.size})", fontWeight = FontWeight.Bold)
                            }
                        )
                    }

                    if (activeTab == 0) {
                        CheckoutPlansContent(
                            selectedPlan = selectedPlan,
                            selectedMethod = selectedMethod,
                            isSubscriptionActive = isSubscriptionActive,
                            activePlanTier = activePlanTier,
                            isProcessing = state is RazorpayPaymentUiState.CreatingOrder ||
                                    state is RazorpayPaymentUiState.CheckoutLaunched ||
                                    state is RazorpayPaymentUiState.VerifyingSignature,
                            onPlanSelect = { plan -> paymentViewModel.selectPlan(plan) },
                            onMethodSelect = { method ->
                                paymentViewModel.selectPaymentMethod(method)
                                if (method == PaymentMethodType.UPI_QR) {
                                    showQrDialog = true
                                }
                            },
                            onPayClick = {
                                activity?.let { act ->
                                    if (selectedMethod == PaymentMethodType.UPI_INTENT) {
                                        paymentViewModel.launchUpiIntentDirect(act)
                                    } else {
                                        paymentViewModel.initiateRealPayment(act, selectedPlan, selectedMethod)
                                    }
                                }
                            }
                        )
                    } else {
                        PaymentHistoryList(
                            records = paymentHistory,
                            onRefundClick = { record -> selectedRefundRecord = record }
                        )
                    }
                }
            }
        }
    }

    // Real UPI QR Code Modal
    if (showQrDialog) {
        UpiQrModal(
            plan = selectedPlan,
            onDismiss = { showQrDialog = false },
            onDirectUpi = {
                showQrDialog = false
                activity?.let { act -> paymentViewModel.launchUpiIntentDirect(act) }
            }
        )
    }

    // Refund Confirmation Dialog
    selectedRefundRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRefundRecord = null },
            title = { Text("Request Razorpay Refund", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    "Confirm full refund request of ${FinancialEngine.formatINR(record.amountInr)} for Order ${record.orderId}? The amount will be credited back via Razorpay to your original account in 3-5 business days.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toRefund = selectedRefundRecord
                        selectedRefundRecord = null
                        if (toRefund != null) {
                            paymentViewModel.requestRefund(toRefund)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.testTag("confirm_refund_button")
                ) {
                    Text("Confirm Refund", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedRefundRecord = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

/**
 * Direct Composable instantiation using explicit PaymentRepository
 */
@Composable
fun PaymentScreen(
    paymentRepository: PaymentRepository,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vm = remember {
        PaymentViewModel(
            context.applicationContext as android.app.Application,
            paymentRepository
        )
    }
    PaymentScreen(paymentViewModel = vm, onNavigate = onNavigate, modifier = modifier)
}

@Composable
private fun CheckoutPlansContent(
    selectedPlan: SubscriptionPlanTier,
    selectedMethod: PaymentMethodType,
    isSubscriptionActive: Boolean,
    activePlanTier: SubscriptionPlanTier?,
    isProcessing: Boolean,
    onPlanSelect: (SubscriptionPlanTier) -> Unit,
    onMethodSelect: (PaymentMethodType) -> Unit,
    onPayClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("checkout_plans_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                PrimaryBlue.copy(alpha = 0.25f),
                                SecondaryViolet.copy(alpha = 0.25f),
                                DarkSurface
                            )
                        )
                    )
                    .border(1.dp, BorderGlassLight, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "RAZORPAY VERIFIED SECURE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                letterSpacing = 1.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = CyanNeon,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Upgrade to FinFam Pro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Real-time AI fraud detection, family group wallets, automated bill splitting & bank-grade analytics.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    if (isSubscriptionActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Plan: ${activePlanTier?.title ?: "Premium Pro"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Select Plan
        item {
            Text(
                text = "CHOOSE YOUR PLAN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        // Plan Cards
        items(SubscriptionPlanTier.entries.toTypedArray()) { plan ->
            val isSelected = selectedPlan == plan

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plan_card_${plan.planId}")
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) DarkSurfaceGlowCustom else DarkSurface)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PrimaryBlue else BorderGlass,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onPlanSelect(plan) }
                    .padding(16.dp)
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
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                    .border(2.dp, if (isSelected) PrimaryBlue else TextMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = plan.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text(text = plan.savingsText ?: plan.billingCycle, fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FinancialEngine.formatINR(plan.priceInr),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (isSelected) CyanNeon else TextPrimary
                            )
                            Text(text = plan.billingCycle, fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    if (plan.discountBadge != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SecondaryViolet.copy(alpha = 0.25f))
                                .border(1.dp, SecondaryViolet.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = plan.discountBadge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderGlass)
                    Spacer(modifier = Modifier.height(10.dp))

                    plan.features.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = feature, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Section Title: Payment Method
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SELECT PAYMENT METHOD (UPI FIRST)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        // Payment Method Options
        items(PaymentMethodType.entries.toTypedArray()) { method ->
            val isSelected = selectedMethod == method
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("method_${method.name.lowercase()}")
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) DarkSurfaceGlowCustom else DarkSurface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CyanNeon else BorderGlass,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onMethodSelect(method) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (method) {
                                PaymentMethodType.UPI -> Icons.Default.Security
                                PaymentMethodType.UPI_INTENT -> Icons.Default.AccountBalanceWallet
                                PaymentMethodType.UPI_QR -> Icons.Default.QrCode
                                PaymentMethodType.CARD -> Icons.Default.CreditCard
                                PaymentMethodType.NET_BANKING -> Icons.Default.AccountBalance
                                PaymentMethodType.WALLET -> Icons.Default.AccountBalanceWallet
                            }
                            Icon(icon, contentDescription = method.title, tint = if (isSelected) CyanNeon else TextSecondary, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = method.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                if (method == PaymentMethodType.UPI || method == PaymentMethodType.UPI_INTENT) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SuccessGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text("INSTANT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    }
                                }
                            }
                            Text(text = method.subtitle, fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CyanNeon else Color.Transparent)
                            .border(2.dp, if (isSelected) CyanNeon else TextMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        // Primary Pay Now Button & Security Guarantees
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPayClick,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("pay_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Securing Gateway...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAY ${FinancialEngine.formatINR(selectedPlan.priceInr)} SECURELY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trust & Encryption Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "256-Bit SSL • Razorpay Official • RBI Compliant",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentSuccessCard(
    state: RazorpayPaymentUiState.Success,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("payment_success_view")
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Payment Verified!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Premium membership is now activated", fontSize = 13.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(label = "Transaction ID", value = state.paymentId)
            DetailRow(label = "Order ID", value = state.orderId)
            DetailRow(label = "Amount Paid", value = FinancialEngine.formatINR(state.amountInr))
            DetailRow(label = "Plan", value = state.plan.title)
            DetailRow(label = "Valid Until", value = state.validUntil)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_to_app_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Continue to FinFam", fontWeight = FontWeight.Bold, color = DarkBackground)
            }
        }
    }
}

@Composable
private fun PaymentFailedCard(
    state: RazorpayPaymentUiState.Failed,
    onRetry: () -> Unit,
    onBackToPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("payment_failed_view")
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = DangerRed, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Payment Incomplete", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state.errorMessage,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("retry_payment_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Payment", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBackToPlans,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Change Plan / Method", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PaymentPendingCard(
    state: RazorpayPaymentUiState.Pending,
    onCheckStatus: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = WarningAmber, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Awaiting Bank Confirmation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Your payment for order ${state.orderId} is being verified by the banking switch.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCheckStatus,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
            ) {
                Text("Check Status Now", fontWeight = FontWeight.Bold, color = DarkBackground)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Back to Plans", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PaymentCancelledCard(
    message: String,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Payment Dismissed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(message, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onTryAgain,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Try Again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentHistoryList(
    records: List<RazorpayTransactionRecord>,
    onRefundClick: (RazorpayTransactionRecord) -> Unit
) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Transactions Yet", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your Razorpay orders & invoices will appear here.", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(records) { record ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(record.planTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(record.date, fontSize = 11.sp, color = TextMuted)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    FinancialEngine.formatINR(record.amountInr),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                StatusPill(status = record.status)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderGlass)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Order: ${record.orderId.take(16)}...", fontSize = 10.sp, color = TextMuted)

                            if (record.status == "SUCCESS" && record.refundStatus == null) {
                                Text(
                                    text = "Request Refund",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed,
                                    modifier = Modifier.clickable { onRefundClick(record) }
                                )
                            } else if (record.refundStatus != null) {
                                Text("Refunded", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (bgColor, textColor) = when (status) {
        "SUCCESS" -> SuccessGreen.copy(alpha = 0.2f) to SuccessGreen
        "PENDING", "CREATED" -> WarningAmber.copy(alpha = 0.2f) to WarningAmber
        "REFUNDED" -> SecondaryViolet.copy(alpha = 0.2f) to Color(0xFFC084FC)
        else -> DangerRed.copy(alpha = 0.2f) to DangerRed
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun UpiQrModal(
    plan: SubscriptionPlanTier,
    onDismiss: () -> Unit,
    onDirectUpi: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(300) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Scan UPI QR Code", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.Black, modifier = Modifier.size(130.dp))
                        Text("finfam.pay@icici", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Amount: ${FinancialEngine.formatINR(plan.priceInr)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Expires in: ${countdown / 60}:${(countdown % 60).toString().padStart(2, '0')}",
                    fontSize = 11.sp,
                    color = WarningAmber
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDirectUpi,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Open UPI App", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceVariant
    )
}

private val DarkSurfaceGlowCustom = Color(0xFF1B2644)
