package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.domain.security.BiometricAuthManager
import com.example.domain.security.BiometricAuthResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPortalScreen(
    paymentViewModel: PaymentViewModel,
    onNavigate: (String) -> Unit,
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
    val dynamicUpiQrString by paymentViewModel.dynamicUpiQrString.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Checkout, 1: History
    var showQrDialog by remember { mutableStateOf(false) }
    var selectedRefundRecord by remember { mutableStateOf<RazorpayTransactionRecord?>(null) }

    Scaffold(
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
                            text = "Real Payment Gateway • 256-Bit SSL",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        // Handle Dedicated Full-Screen State Overlays
        when (val state = paymentState) {
            is RazorpayPaymentUiState.Success -> {
                PaymentSuccessView(
                    state = state,
                    onContinue = {
                        paymentViewModel.resetState()
                        onNavigate("home")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.Failed -> {
                PaymentFailureView(
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
                PortalPaymentPendingView(
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
                PaymentCancelledView(
                    message = state.message,
                    onTryAgain = {
                        paymentViewModel.resetState()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                // Main Checkout & History UI
                Column(
                    modifier = modifier
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
                            text = { Text("Checkout", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Text("Payment History (${paymentHistory.size})", fontWeight = FontWeight.Bold)
                            }
                        )
                    }

                    if (activeTab == 0) {
                        MainCheckoutView(
                            selectedPlan = selectedPlan,
                            selectedMethod = selectedMethod,
                            isSubscriptionActive = isSubscriptionActive,
                            activePlanTier = activePlanTier,
                            isCreatingOrder = state is RazorpayPaymentUiState.CreatingOrder || state is RazorpayPaymentUiState.CheckoutLaunched || state is RazorpayPaymentUiState.VerifyingSignature,
                            onPlanSelect = { plan -> paymentViewModel.selectPlan(plan) },
                            onMethodSelect = { method ->
                                paymentViewModel.selectPaymentMethod(method)
                                if (method == PaymentMethodType.UPI_QR) {
                                    showQrDialog = true
                                }
                            },
                            onPayClick = {
                                val fragAct = activity as? FragmentActivity
                                if (fragAct != null) {
                                    BiometricAuthManager.promptBiometric(
                                        activity = fragAct,
                                        title = "Authorize Subscription Checkout",
                                        subtitle = "${selectedPlan.title} (₹${selectedPlan.priceInr.toInt()})",
                                        description = "Verify biometric or screen lock to complete transaction",
                                        onResult = { result ->
                                            when (result) {
                                                is BiometricAuthResult.Success -> {
                                                    if (selectedMethod == PaymentMethodType.UPI) {
                                                        paymentViewModel.launchUpiIntentDirect(fragAct)
                                                    } else {
                                                        paymentViewModel.initiateRealPayment(fragAct, selectedPlan, selectedMethod)
                                                    }
                                                }
                                                is BiometricAuthResult.Cancelled -> {
                                                    Toast.makeText(context, "Payment cancelled", Toast.LENGTH_SHORT).show()
                                                }
                                                is BiometricAuthResult.Error -> {
                                                    Toast.makeText(context, "Biometric error: ${result.errString}", Toast.LENGTH_SHORT).show()
                                                }
                                                is BiometricAuthResult.Failed -> {
                                                    Toast.makeText(context, "Authentication failed. Transaction blocked.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    activity?.let { act ->
                                        if (selectedMethod == PaymentMethodType.UPI) {
                                            paymentViewModel.launchUpiIntentDirect(act)
                                        } else {
                                            paymentViewModel.initiateRealPayment(act, selectedPlan, selectedMethod)
                                        }
                                    }
                                }
                            },
                            onOpenQrModal = { showQrDialog = true }
                        )
                    } else {
                        PaymentHistoryView(
                            records = paymentHistory,
                            onRefundClick = { record -> selectedRefundRecord = record }
                        )
                    }
                }
            }
        }
    }

    // Dynamic Real UPI QR Dialog
    if (showQrDialog) {
        UpiQrDialog(
            plan = selectedPlan,
            onDismiss = { showQrDialog = false },
            onLaunchUpiDirect = {
                showQrDialog = false
                activity?.let { act -> paymentViewModel.launchUpiIntentDirect(act) }
            },
            onLaunchGateway = {
                showQrDialog = false
                activity?.let { act -> paymentViewModel.initiateRealPayment(act, selectedPlan, PaymentMethodType.UPI) }
            }
        )
    }

    // Refund Confirmation Dialog
    selectedRefundRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRefundRecord = null },
            title = { Text("Confirm Refund Request", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to request a full refund of ${FinancialEngine.formatINR(record.amountInr)} for Order ${record.orderId}? The amount will be credited back to your original payment source via Razorpay within 3-5 business days.",
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
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
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

@Composable
private fun MainCheckoutView(
    selectedPlan: SubscriptionPlanTier,
    selectedMethod: PaymentMethodType,
    isSubscriptionActive: Boolean,
    activePlanTier: SubscriptionPlanTier?,
    isCreatingOrder: Boolean,
    onPlanSelect: (SubscriptionPlanTier) -> Unit,
    onMethodSelect: (PaymentMethodType) -> Unit,
    onPayClick: () -> Unit,
    onOpenQrModal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryBlue.copy(alpha = 0.35f), SecondaryViolet.copy(alpha = 0.35f))
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "FINFAM PREMIUM",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                        if (isSubscriptionActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.2f))
                                    .border(1.dp, SuccessGreen, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Enterprise-grade family financial management with real-time AI advisor, receipt OCR, automated budgeting & scam detection.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section 1: Choose Subscription Plan
        item {
            Text(
                "SELECT PREMIUM PLAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        items(SubscriptionPlanTier.entries) { plan ->
            val isSelected = selectedPlan == plan
            val isCurrentActive = isSubscriptionActive && activePlanTier == plan

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                    .border(
                        1.5.dp,
                        if (isSelected) PrimaryBlue else BorderGlassLight,
                        RoundedCornerShape(18.dp)
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
                                    .border(2.dp, if (isSelected) PrimaryBlue else TextMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = plan.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                plan.discountBadge?.let { badge ->
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SecondaryViolet.copy(alpha = 0.25f))
                                            .border(0.5.dp, SecondaryViolet, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanNeon
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${FinancialEngine.formatINR(plan.priceInr)} ${plan.billingCycle}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (isSelected) CyanNeon else TextPrimary
                            )
                            if (isCurrentActive) {
                                Text("Current Plan", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    plan.savingsText?.let { savings ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(savings, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderGlassLight.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    plan.features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feature, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Section 2: Choose Indian Payment Method
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CHOOSE PAYMENT METHOD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    "India Real Payments",
                    fontSize = 10.sp,
                    color = CyanNeon,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        items(PaymentMethodType.entries) { method ->
            val isSelected = selectedMethod == method

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                    .border(
                        1.dp,
                        if (isSelected) PrimaryBlue else BorderGlassLight,
                        RoundedCornerShape(14.dp)
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
                        // Radio indicator
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, if (isSelected) PrimaryBlue else TextMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue)
                                    )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Method Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else DarkSurface)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (method) {
                                PaymentMethodType.UPI -> Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CyanNeon)
                                PaymentMethodType.RUPAY_CARD, PaymentMethodType.CARD -> Icon(Icons.Default.CreditCard, contentDescription = null, tint = PrimaryBlue)
                                PaymentMethodType.NET_BANKING -> Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SecondaryViolet)
                                PaymentMethodType.WALLET -> Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = WarningAmber)
                                PaymentMethodType.UPI_QR -> Icon(Icons.Default.QrCode, contentDescription = null, tint = SuccessGreen)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = method.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                if (method == PaymentMethodType.UPI) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryBlue.copy(alpha = 0.3f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text("POPULAR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                                    }
                                }
                            }
                            Text(
                                text = method.subtitle,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (method == PaymentMethodType.UPI_QR) {
                        Text(
                            text = "Show QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { onOpenQrModal() }
                        )
                    }
                }
            }
        }

        // Security Assurance Indicator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .border(0.5.dp, BorderGlassLight, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "🔒 256-Bit SSL • PCI-DSS Level 1 • Powered by Razorpay",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }

        // Primary Pay Button
        item {
            Button(
                onClick = onPayClick,
                enabled = !isCreatingOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isCreatingOrder) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Connecting Gateway...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAY ${FinancialEngine.formatINR(selectedPlan.priceInr)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PaymentSuccessView(
    state: RazorpayPaymentUiState.Success,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Checkmark Icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.15f))
                .border(2.dp, SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "✓ Payment Successful",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SuccessGreen
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Premium Activated",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, BorderGlassLight, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReceiptRow("Amount:", FinancialEngine.formatINR(state.amountInr), isBold = true, isHighlighted = true)
                ReceiptRow("Payment Method:", state.paymentMethod)
                ReceiptRow("Transaction ID:", state.paymentId)
                ReceiptRow("Order ID:", state.orderId)
                ReceiptRow("Plan:", state.plan.title)
                ReceiptRow("Valid Until:", state.validUntil)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                "CONTINUE TO FINFAM",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun PaymentFailureView(
    state: RazorpayPaymentUiState.Failed,
    onRetry: () -> Unit,
    onBackToPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(DangerRed.copy(alpha = 0.15f))
                .border(2.dp, DangerRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Failed",
                tint = DangerRed,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Payment Failed",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DangerRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your payment could not be completed.",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = state.errorMessage,
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("RETRY PAYMENT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToPlans,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("BACK TO PLANS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PortalPaymentPendingView(
    state: RazorpayPaymentUiState.Pending,
    onCheckStatus: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(WarningAmber.copy(alpha = 0.15f))
                .border(2.dp, WarningAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.HourglassTop,
                contentDescription = "Pending",
                tint = WarningAmber,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Payment Processing",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WarningAmber
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your payment is being processed.",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Please do not make another payment while we verify transaction settlement with your bank.",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCheckStatus,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("CHECK PAYMENT STATUS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("BACK", color = TextSecondary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentCancelledView(
    message: String,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Shield, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Cancelled", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onTryAgain,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("RETURN TO CHECKOUT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentHistoryView(
    records: List<RazorpayTransactionRecord>,
    onRefundClick: (RazorpayTransactionRecord) -> Unit
) {
    if (records.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No payment orders found", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Your verified transaction history will appear here.", fontSize = 12.sp, color = TextMuted)
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
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderGlassLight, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(record.date, fontSize = 11.sp, color = TextMuted)
                                Text(record.planTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            }
                            Text(
                                FinancialEngine.formatINR(record.amountInr),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (record.status == "SUCCESS") SuccessGreen else TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderGlassLight.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Method: ${record.paymentMethod}", fontSize = 11.sp, color = TextSecondary)
                                Text("Order: ${record.orderId}", fontSize = 10.sp, color = TextMuted)
                                record.paymentId?.let {
                                    Text("Pay ID: $it", fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (record.status) {
                                                "SUCCESS" -> SuccessGreen.copy(alpha = 0.15f)
                                                "REFUNDED", "REFUND_INITIATED" -> SecondaryViolet.copy(alpha = 0.2f)
                                                "FAILED" -> DangerRed.copy(alpha = 0.15f)
                                                else -> WarningAmber.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = record.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when (record.status) {
                                            "SUCCESS" -> SuccessGreen
                                            "REFUNDED", "REFUND_INITIATED" -> CyanNeon
                                            "FAILED" -> DangerRed
                                            else -> WarningAmber
                                        }
                                    )
                                }

                                if (record.status == "SUCCESS" && record.refundStatus == null && record.paymentId != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Request Refund",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        modifier = Modifier.clickable { onRefundClick(record) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(
            value,
            fontSize = if (isHighlighted) 16.sp else 13.sp,
            fontWeight = if (isBold || isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) CyanNeon else TextPrimary
        )
    }
}

@Composable
private fun UpiQrDialog(
    plan: SubscriptionPlanTier,
    onDismiss: () -> Unit,
    onLaunchUpiDirect: () -> Unit,
    onLaunchGateway: () -> Unit
) {
    var timerSeconds by remember { mutableIntStateOf(300) } // 5 minutes validity

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
    }

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Scan UPI QR Code", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WarningAmber.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(formattedTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Scan with Google Pay, PhonePe, Paytm, BHIM, or any UPI App",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // High-Fidelity Rendered QR Canvas Pattern
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = size.width / 15f
                        val darkColor = Color(0xFF0F172A)
                        
                        // Corner finder patterns
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(cellSize * 5, cellSize * 5),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(cellSize, cellSize),
                            size = Size(cellSize * 3, cellSize * 3)
                        )
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(cellSize * 2, cellSize * 2),
                            size = Size(cellSize, cellSize)
                        )

                        // Top right finder
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(size.width - cellSize * 5, 0f),
                            size = Size(cellSize * 5, cellSize * 5),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(size.width - cellSize * 4, cellSize),
                            size = Size(cellSize * 3, cellSize * 3)
                        )
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(size.width - cellSize * 3, cellSize * 2),
                            size = Size(cellSize, cellSize)
                        )

                        // Bottom left finder
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(0f, size.height - cellSize * 5),
                            size = Size(cellSize * 5, cellSize * 5),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(cellSize, size.height - cellSize * 4),
                            size = Size(cellSize * 3, cellSize * 3)
                        )
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(cellSize * 2, size.height - cellSize * 3),
                            size = Size(cellSize, cellSize)
                        )

                        // Central data dots simulation
                        val dotCoords = listOf(
                            6 to 2, 8 to 2, 7 to 4, 6 to 6, 8 to 6, 10 to 6,
                            6 to 8, 7 to 9, 9 to 9, 11 to 8, 7 to 11, 8 to 12,
                            11 to 11, 13 to 12, 12 to 14, 14 to 14
                        )
                        for ((x, y) in dotCoords) {
                            drawCircle(
                                color = darkColor,
                                radius = cellSize * 0.4f,
                                center = Offset(x * cellSize + cellSize / 2, y * cellSize + cellSize / 2)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VPA: finfam.pay@icici",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Text(
                    text = "Amount: ${FinancialEngine.formatINR(plan.priceInr)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onLaunchUpiDirect,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Open Installed UPI App", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onLaunchGateway) {
                Text("Pay via Gateway Sheet", fontSize = 12.sp, color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceVariant
    )
}
