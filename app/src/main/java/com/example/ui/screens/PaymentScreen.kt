package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.BillCategoryType
import com.example.domain.model.PaymentMethodType
import com.example.domain.model.RazorpayPaymentUiState
import com.example.domain.model.RazorpayTransactionRecord
import com.example.domain.model.ScannedUpiPayload
import com.example.domain.model.SubscriptionPlanTier
import com.example.domain.payment.QrCodeGenerator
import com.example.domain.payment.UpiAppInfo
import com.example.domain.payment.UpiAppType
import com.example.domain.payment.UpiMerchantConfig
import com.example.domain.payment.ZxingQrDecoder
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

/**
 * Production-Ready FinFam Payment Gateway Screen.
 * Implements RuPay Cards, Direct UPI (Priyan: priyan1436ei@okhdfcbank), Razorpay Live Checkout,
 * ZXing Scan & Pay, Bill Payments, and Smart Digital Receipts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    paymentViewModel: PaymentViewModel = viewModel(),
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val paymentState by paymentViewModel.paymentState.collectAsStateWithLifecycle()
    val isSubscriptionActive by paymentViewModel.isSubscriptionActive.collectAsStateWithLifecycle()
    val activePlanTier by paymentViewModel.activePlanTier.collectAsStateWithLifecycle()
    val selectedPlan by paymentViewModel.selectedPlan.collectAsStateWithLifecycle()
    val selectedMethod by paymentViewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val installedUpiApps by paymentViewModel.installedUpiApps.collectAsStateWithLifecycle()
    val paymentHistory by paymentViewModel.paymentHistory.collectAsStateWithLifecycle()
    val scannedPayload by paymentViewModel.scannedPayload.collectAsStateWithLifecycle()
    val userProfile by paymentViewModel.userProfile.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Smart Pay, 1: Premium Plans, 2: Scan & Pay, 3: Pay Bills, 4: Passbook
    var selectedUpiApp by remember { mutableStateOf<UpiAppInfo?>(null) }
    var selectedRefundRecord by remember { mutableStateOf<RazorpayTransactionRecord?>(null) }
    var viewingReceiptRecord by remember { mutableStateOf<RazorpayTransactionRecord?>(null) }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appName = selectedUpiApp?.name ?: "UPI App"
        paymentViewModel.handleUpiActivityResult(
            resultCode = result.resultCode,
            data = result.data,
            plan = selectedPlan,
            appName = appName
        )
    }

    LaunchedEffect(Unit) {
        paymentViewModel.refreshInstalledUpiApps()
    }

    fun authorizePaymentAction(
        title: String,
        amountText: String,
        onApproved: () -> Unit
    ) {
        val fragActivity = context as? FragmentActivity
            ?: (context as? android.content.ContextWrapper)?.baseContext as? FragmentActivity
        if (fragActivity != null) {
            BiometricAuthManager.promptBiometric(
                activity = fragActivity,
                title = "Authorize $title",
                subtitle = "Authenticate to confirm $amountText payment",
                description = "Biometric security verification protects your funds",
                onResult = { result ->
                    when (result) {
                        is BiometricAuthResult.Success -> {
                            onApproved()
                        }
                        is BiometricAuthResult.Cancelled -> {
                            Toast.makeText(context, "Payment authorization cancelled", Toast.LENGTH_SHORT).show()
                        }
                        is BiometricAuthResult.Error -> {
                            Toast.makeText(context, "Biometric error: ${result.errString}", Toast.LENGTH_SHORT).show()
                        }
                        is BiometricAuthResult.Failed -> {
                            Toast.makeText(context, "Authentication failed. Payment blocked.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        } else {
            onApproved()
        }
    }

    Scaffold(
        modifier = modifier.testTag("payment_screen_container"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FINFAM PAYMENTS",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyanNeon.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RUPAY & UPI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanNeon
                                )
                            }
                        }
                        Text(
                            text = "Merchant: ${UpiMerchantConfig.MERCHANT_NAME} • ${UpiMerchantConfig.MERCHANT_UPI_ID}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                actions = {
                    IconButton(
                        onClick = { onNavigate("transfer") },
                        modifier = Modifier.testTag("live_transfer_action_button")
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = "Real-Time Transfer", tint = CyanNeon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        when (val state = paymentState) {
            is RazorpayPaymentUiState.Success -> {
                PaymentSuccessView(
                    state = state,
                    selectedPlan = selectedPlan,
                    onContinue = {
                        paymentViewModel.resetState()
                        activeTab = 4 // View in passbook
                    },
                    onShareReceipt = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "FinFam Payment Receipt")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "FinFam Payment Verified!\nTxn: ${state.paymentId}\nAmount: ₹${"%.2f".format(state.amountInr)}\nMethod: ${state.paymentMethod}\nMerchant: ${UpiMerchantConfig.MERCHANT_NAME} (${UpiMerchantConfig.MERCHANT_UPI_ID})"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Payment Receipt"))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.Failed -> {
                PaymentFailedView(
                    state = state,
                    onRetry = {
                        val act = context as? Activity
                        if (act != null) {
                            paymentViewModel.initiateRealPayment(activity = act, plan = selectedPlan, method = selectedMethod)
                        } else {
                            paymentViewModel.launchDirectUpiPayment(launcher = upiLauncher, plan = selectedPlan, targetApp = selectedUpiApp)
                        }
                    },
                    onBack = { paymentViewModel.resetState() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.Pending -> {
                PaymentPendingView(
                    state = state,
                    onCheckAgain = { paymentViewModel.checkPendingPaymentStatus(state.orderId) },
                    onBack = { paymentViewModel.resetState() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RazorpayPaymentUiState.UserCancelled -> {
                PaymentCancelledView(
                    message = state.message,
                    onTryAgain = {
                        paymentViewModel.resetState()
                    },
                    onBack = { paymentViewModel.resetState() },
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
                    // Scrollable Tab Header
                    ScrollableTabRow(
                        selectedTabIndex = activeTab,
                        containerColor = DarkBackground,
                        contentColor = PrimaryBlue,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = CyanNeon
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            modifier = Modifier.testTag("tab_smart_pay"),
                            text = { Text("SMART PAY", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            modifier = Modifier.testTag("tab_plans"),
                            text = { Text("PREMIUM PLANS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            modifier = Modifier.testTag("tab_scan_pay"),
                            text = { Text("SCAN & PAY", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 3,
                            onClick = { activeTab = 3 },
                            modifier = Modifier.testTag("tab_bills"),
                            text = { Text("PAY BILLS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == 4,
                            onClick = { activeTab = 4 },
                            modifier = Modifier.testTag("tab_history"),
                            text = { Text("PASSBOOK (${paymentHistory.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }

                    when (activeTab) {
                        0 -> SmartPayCustomContent(
                            walletBalance = userProfile?.totalBalance ?: 84500.0,
                            selectedMethod = selectedMethod,
                            installedUpiApps = installedUpiApps,
                            selectedUpiApp = selectedUpiApp,
                            onMethodSelect = { paymentViewModel.selectPaymentMethod(it) },
                            onUpiAppSelect = { selectedUpiApp = it },
                            onPayCustom = { amount, recipientVpa, recipientName, note, method ->
                                authorizePaymentAction(
                                    title = "Custom Payment",
                                    amountText = "₹${amount.toInt()}"
                                ) {
                                    val act = context as? Activity
                                    if (method == PaymentMethodType.UPI) {
                                        paymentViewModel.launchDirectUpiPayment(
                                            launcher = upiLauncher,
                                            targetApp = selectedUpiApp
                                        )
                                    } else if (act != null) {
                                        paymentViewModel.initiateCustomPayment(
                                            activity = act,
                                            amountInr = amount,
                                            title = note.ifBlank { "Smart Payment to $recipientName" },
                                            category = "Shopping",
                                            method = method,
                                            note = note,
                                            recipientVpa = recipientVpa,
                                            recipientName = recipientName
                                        )
                                    } else {
                                        paymentViewModel.completeCustomPaymentSuccess(
                                            amountInr = amount,
                                            title = note.ifBlank { "Smart Payment" },
                                            category = "Shopping",
                                            paymentMethodName = method.title,
                                            note = note
                                        )
                                    }
                                }
                            }
                        )
                        1 -> DirectUpiCheckoutContent(
                            selectedPlan = selectedPlan,
                            selectedMethod = selectedMethod,
                            installedUpiApps = installedUpiApps,
                            selectedUpiApp = selectedUpiApp,
                            isSubscriptionActive = isSubscriptionActive,
                            activePlanTier = activePlanTier,
                            isProcessing = state is RazorpayPaymentUiState.CreatingOrder ||
                                    state is RazorpayPaymentUiState.VerifyingSignature,
                            onPlanSelect = { plan -> paymentViewModel.selectPlan(plan) },
                            onMethodSelect = { method -> paymentViewModel.selectPaymentMethod(method) },
                            onUpiAppSelect = { app -> selectedUpiApp = app },
                            onPayNowClick = {
                                authorizePaymentAction(
                                    title = selectedPlan.title,
                                    amountText = "₹${selectedPlan.priceInr.toInt()}"
                                ) {
                                    val act = context as? Activity
                                    if (selectedMethod == PaymentMethodType.RUPAY_CARD || selectedMethod == PaymentMethodType.CARD || selectedMethod == PaymentMethodType.NET_BANKING || selectedMethod == PaymentMethodType.WALLET) {
                                        if (act != null) {
                                            paymentViewModel.initiateRealPayment(
                                                activity = act,
                                                plan = selectedPlan,
                                                method = selectedMethod
                                            )
                                        } else {
                                            paymentViewModel.launchDirectUpiPayment(
                                                launcher = upiLauncher,
                                                plan = selectedPlan,
                                                targetApp = selectedUpiApp
                                            )
                                        }
                                    } else {
                                        paymentViewModel.launchDirectUpiPayment(
                                            launcher = upiLauncher,
                                            plan = selectedPlan,
                                            targetApp = selectedUpiApp
                                        )
                                    }
                                }
                            }
                        )
                        2 -> ScanAndPayContent(
                            scannedPayload = scannedPayload,
                            onPayloadScanned = { paymentViewModel.setScannedPayload(it) },
                            onPayScanned = { payload, amount ->
                                authorizePaymentAction(
                                    title = "QR Pay (${payload.payeeName})",
                                    amountText = "₹${amount.toInt()}"
                                ) {
                                    val act = context as? Activity
                                    if (act != null) {
                                        paymentViewModel.initiateCustomPayment(
                                            activity = act,
                                            amountInr = amount,
                                            title = "QR Pay to ${payload.payeeName}",
                                            category = "Food & Groceries",
                                            method = PaymentMethodType.UPI,
                                            note = payload.note ?: "QR Payment",
                                            recipientVpa = payload.vpa,
                                            recipientName = payload.payeeName
                                        )
                                    } else {
                                        paymentViewModel.completeCustomPaymentSuccess(
                                            amountInr = amount,
                                            title = "QR Pay to ${payload.payeeName}",
                                            category = "Food & Groceries",
                                            paymentMethodName = "UPI QR (${payload.vpa})",
                                            note = payload.note ?: "QR Payment"
                                        )
                                    }
                                }
                            }
                        )
                        3 -> PayBillsContent(
                            onPayBill = { category, billName, amount, method ->
                                authorizePaymentAction(
                                    title = "Bill Settle: $billName",
                                    amountText = "₹${amount.toInt()}"
                                ) {
                                    val act = context as? Activity
                                    if (act != null) {
                                        paymentViewModel.initiateCustomPayment(
                                            activity = act,
                                            amountInr = amount,
                                            title = "Bill Payment: $billName",
                                            category = "Utilities",
                                            method = method,
                                            note = "Automated Bill Settle",
                                            isBill = true
                                        )
                                    } else {
                                        paymentViewModel.completeCustomPaymentSuccess(
                                            amountInr = amount,
                                            title = "Bill Payment: $billName",
                                            category = "Utilities",
                                            paymentMethodName = method.title,
                                            note = "Automated Bill Settle",
                                            isBill = true
                                        )
                                    }
                                }
                            }
                        )
                        4 -> PaymentHistoryView(
                            records = paymentHistory,
                            onRefundClick = { record -> selectedRefundRecord = record },
                            onViewReceipt = { record -> viewingReceiptRecord = record }
                        )
                    }
                }
            }
        }
    }

    // Refund Confirmation Dialog
    selectedRefundRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRefundRecord = null },
            title = { Text("Request Payment Refund", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    "Confirm full refund request of ${FinancialEngine.formatINR(record.amountInr)} for ${record.planTitle} to ${UpiMerchantConfig.MERCHANT_UPI_ID}? Funds will be credited back to your source payment account.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        paymentViewModel.requestRefund(record)
                        selectedRefundRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Confirm Refund", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedRefundRecord = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Digital Smart Receipt Modal Dialog
    viewingReceiptRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { viewingReceiptRecord = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CyanNeon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Official Tax Receipt", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dynamic Verification QR
                    val qrBitmap = remember(record.orderId) {
                        QrCodeGenerator.generateQrImageBitmap(
                            "FINFAM_VERIFIED|${record.orderId}|${record.paymentId}|INR${record.amountInr}|${record.date}",
                            sizePx = 256
                        )
                    }
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Verification QR",
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(6.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = FinancialEngine.formatINR(record.amountInr),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SuccessGreen
                    )
                    Text(
                        text = record.planTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderGlass)
                    Spacer(modifier = Modifier.height(10.dp))

                    ReceiptRow("Order ID", record.orderId)
                    ReceiptRow("Payment ID", record.paymentId ?: "UPI_DIRECT")
                    ReceiptRow("Date & Time", record.date)
                    ReceiptRow("Payment Method", record.paymentMethod)
                    ReceiptRow("Beneficiary", "${UpiMerchantConfig.MERCHANT_NAME} (${UpiMerchantConfig.MERCHANT_UPI_ID})")
                    ReceiptRow("Status", record.status, isStatus = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "FinFam Payment Receipt - ${record.orderId}")
                            putExtra(Intent.EXTRA_TEXT, paymentViewModel.generateShareableReceipt(record))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Official Receipt"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Export")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewingReceiptRecord = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun ReceiptRow(label: String, value: String, isStatus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isStatus) FontWeight.Bold else FontWeight.Medium,
            color = if (isStatus) SuccessGreen else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 1. Smart Pay Custom Amount & RuPay / UPI Payment Screen
 */
@Composable
fun SmartPayCustomContent(
    walletBalance: Double,
    selectedMethod: PaymentMethodType,
    installedUpiApps: List<UpiAppInfo>,
    selectedUpiApp: UpiAppInfo?,
    onMethodSelect: (PaymentMethodType) -> Unit,
    onUpiAppSelect: (UpiAppInfo) -> Unit,
    onPayCustom: (amount: Double, vpa: String, name: String, note: String, method: PaymentMethodType) -> Unit
) {
    var amountInput by remember { mutableStateOf("500") }
    var recipientVpa by remember { mutableStateOf(UpiMerchantConfig.MERCHANT_UPI_ID) }
    var recipientName by remember { mutableStateOf(UpiMerchantConfig.MERCHANT_NAME) }
    var paymentNote by remember { mutableStateOf("Groceries & Food") }

    val presetAmounts = listOf("99", "249", "500", "799", "1500")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vault Balance Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                DarkSurfaceVariant,
                                DarkSurface
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FINFAM VAULT BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FinancialEngine.formatINR(walletBalance),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("256-BIT NPCI ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
            }
        }

        // Amount Input Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("ENTER AMOUNT (₹)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("₹", fontSize = 32.sp, fontWeight = FontWeight.Black, color = CyanNeon)
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_amount_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = BorderGlass,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    // Preset Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetAmounts) { chipAmount ->
                            val isSelected = amountInput == chipAmount
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) CyanNeon else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) CyanNeon else BorderGlass, RoundedCornerShape(20.dp))
                                    .clickable { amountInput = chipAmount }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "₹$chipAmount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DarkBackground else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recipient / Beneficiary Details
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BENEFICIARY & DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)

                    OutlinedTextField(
                        value = recipientVpa,
                        onValueChange = { recipientVpa = it },
                        label = { Text("Recipient UPI ID / VPA", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        trailingIcon = {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        }
                    )

                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = { paymentNote = it },
                        label = { Text("Payment Note / Purpose", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        }

        // Payment Method Selector (RuPay Card, UPI, NetBanking, Wallets)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SELECT PAYMENT METHOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)

                PaymentMethodType.entries.forEach { method ->
                    val isSelected = selectedMethod == method
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) CyanNeon else BorderGlass,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onMethodSelect(method) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when (method) {
                                    PaymentMethodType.RUPAY_CARD -> Icons.Default.CreditCard
                                    PaymentMethodType.UPI -> Icons.Default.AccountBalanceWallet
                                    PaymentMethodType.CARD -> Icons.Default.CreditCard
                                    PaymentMethodType.NET_BANKING -> Icons.Default.AccountBalance
                                    PaymentMethodType.WALLET -> Icons.Default.AccountBalanceWallet
                                    PaymentMethodType.UPI_QR -> Icons.Default.QrCode
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) CyanNeon else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
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
                                        if (method.isRupay) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(WarningAmber.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text("0% FEE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                            }
                                        }
                                    }
                                    Text(
                                        text = method.subtitle,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pay Now Button
        item {
            val amountNum = amountInput.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountNum > 0) {
                        onPayCustom(amountNum, recipientVpa, recipientName, paymentNote, selectedMethod)
                    }
                },
                enabled = amountNum > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_custom_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAY ${FinancialEngine.formatINR(amountNum)} NOW",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DarkBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 2. Scan & Pay (ZXing) UPI Scanner with Scam Shield
 */
@Composable
fun ScanAndPayContent(
    scannedPayload: ScannedUpiPayload?,
    onPayloadScanned: (ScannedUpiPayload) -> Unit,
    onPayScanned: (ScannedUpiPayload, Double) -> Unit
) {
    var customAmount by remember { mutableStateOf("250") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ZXing UPI QR Scam Shield",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Scans dynamic QR codes and validates merchant reputation before payment",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Preset Scan Targets for Instant Emulation Testing
        item {
            Text("QUICK TEST QR PRESETS (EMULATOR)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }

        items(ZxingQrDecoder.SAMPLE_UPI_QRS) { sample ->
            val isSelected = scannedPayload?.vpa == sample.vpa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else DarkSurface)
                    .border(1.dp, if (isSelected) CyanNeon else BorderGlass, RoundedCornerShape(12.dp))
                    .clickable { onPayloadScanned(sample) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(sample.payeeName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(sample.vpa, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("SHIELD ${sample.safetyScore}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                }
            }
        }

        // If a QR is scanned / selected, show Scam Shield Risk Audit & Payment Card
        scannedPayload?.let { payload ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    DarkSurfaceVariant,
                                    DarkSurface
                                )
                            )
                        )
                        .border(1.dp, if (payload.safetyScore >= 80) SuccessGreen else DangerRed, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SCAM SHIELD AUDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(
                                text = payload.riskLevel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (payload.safetyScore >= 80) SuccessGreen else DangerRed
                            )
                        }

                        Text(payload.payeeName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        Text("VPA: ${payload.vpa}", fontSize = 12.sp, color = TextSecondary)
                        if (payload.note != null) {
                            Text("Note: ${payload.note}", fontSize = 12.sp, color = TextMuted)
                        }

                        val payAmount = payload.amount ?: (customAmount.toDoubleOrNull() ?: 100.0)

                        if (payload.amount == null) {
                            OutlinedTextField(
                                value = customAmount,
                                onValueChange = { customAmount = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Enter Amount (₹)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Button(
                            onClick = { onPayScanned(payload, payAmount) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SECURE PAY ${FinancialEngine.formatINR(payAmount)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Pay Bills Section
 */
@Composable
fun PayBillsContent(
    onPayBill: (category: BillCategoryType, billName: String, amount: Double, method: PaymentMethodType) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(BillCategoryType.ELECTRICITY) }
    var accountIdInput by remember { mutableStateOf("9876543210") }
    var billAmountInput by remember { mutableStateOf("1450") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SELECT UTILITY CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }

        // Bill Category Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val chunks = BillCategoryType.entries.chunked(2)
                for (chunk in chunks) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (cat in chunk) {
                            val isSelected = selectedCategory == cat
                            val icon = when (cat) {
                                BillCategoryType.MOBILE_RECHARGE -> Icons.Default.PhoneAndroid
                                BillCategoryType.ELECTRICITY -> Icons.Default.Bolt
                                BillCategoryType.WATER -> Icons.Default.WaterDrop
                                BillCategoryType.GAS -> Icons.Default.LocalGasStation
                                BillCategoryType.BROADBAND -> Icons.Default.Wifi
                                BillCategoryType.DTH -> Icons.Default.Tv
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else DarkSurface)
                                    .border(1.dp, if (isSelected) CyanNeon else BorderGlass, RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedCategory = cat
                                        billAmountInput = cat.defaultAmount.toInt().toString()
                                    }
                                    .padding(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) CyanNeon else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = cat.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyanNeon else TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bill Input Form
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BILL & CONSUMER DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)

                    OutlinedTextField(
                        value = accountIdInput,
                        onValueChange = { accountIdInput = it },
                        label = { Text("Consumer ID / Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderGlass
                        )
                    )

                    OutlinedTextField(
                        value = billAmountInput,
                        onValueChange = { billAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Bill Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderGlass
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    val amount = billAmountInput.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (amount > 0) {
                                onPayBill(
                                    selectedCategory,
                                    "${selectedCategory.title} ($accountIdInput)",
                                    amount,
                                    PaymentMethodType.RUPAY_CARD
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "PAY BILL ${FinancialEngine.formatINR(amount)} VIA RUPAY / UPI",
                            color = DarkBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. Premium Plans Checkout Content
 */
@Composable
fun DirectUpiCheckoutContent(
    selectedPlan: SubscriptionPlanTier,
    selectedMethod: PaymentMethodType,
    installedUpiApps: List<UpiAppInfo>,
    selectedUpiApp: UpiAppInfo?,
    isSubscriptionActive: Boolean,
    activePlanTier: SubscriptionPlanTier?,
    isProcessing: Boolean,
    onPlanSelect: (SubscriptionPlanTier) -> Unit,
    onMethodSelect: (PaymentMethodType) -> Unit,
    onUpiAppSelect: (UpiAppInfo) -> Unit,
    onPayNowClick: () -> Unit
) {
    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Subscription Status Banner
        if (isSubscriptionActive) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SuccessGreen.copy(alpha = 0.2f),
                                    DarkSurface
                                )
                            )
                        )
                        .border(1.dp, SuccessGreen, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PREMIUM ACTIVE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                            }
                            Text(
                                text = activePlanTier?.title ?: "FinFam Premium Member",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("AUTO RENEW ON", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }
        }

        // Plan Selection Header
        item {
            Text("CHOOSE SUBSCRIPTION PLAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }

        // Plan Cards: Monthly (₹99), Quarterly (₹249), Yearly (₹799)
        items(
            listOf(
                SubscriptionPlanTier.MONTHLY_PRO,
                SubscriptionPlanTier.QUARTERLY_PRO,
                SubscriptionPlanTier.ANNUAL_ELITE
            )
        ) { plan ->
            val isSelected = selectedPlan == plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Brush.linearGradient(
                            listOf(
                                SecondaryViolet.copy(alpha = 0.25f),
                                DarkSurface
                            )
                        ) else Brush.linearGradient(listOf(DarkSurface, DarkSurface))
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CyanNeon else BorderGlass,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onPlanSelect(plan) }
                    .padding(16.dp)
                    .testTag("plan_card_${plan.planId}")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                            if (plan.savingsText != null) {
                                Text(plan.savingsText, fontSize = 11.sp, color = CyanNeon)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${plan.priceInr.toInt()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = TextPrimary
                            )
                            Text(plan.billingCycle, fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    if (plan.discountBadge != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanNeon.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(plan.discountBadge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderGlass)
                    Spacer(modifier = Modifier.height(8.dp))

                    plan.features.take(3).forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(feat, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Payment Method Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SELECT PAYMENT CHANNEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                PaymentMethodType.entries.forEach { method ->
                    val isSelected = selectedMethod == method
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else DarkSurface)
                            .border(1.dp, if (isSelected) CyanNeon else BorderGlass, RoundedCornerShape(12.dp))
                            .clickable { onMethodSelect(method) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (method.isRupay || method == PaymentMethodType.CARD) Icons.Default.CreditCard else Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (isSelected) CyanNeon else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(method.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(method.subtitle, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Show QR Option Button
        item {
            OutlinedButton(
                onClick = { showQrDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SHOW INSTANT UPI QR CODE (₹${selectedPlan.priceInr.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Pay Now Button
        item {
            Button(
                onClick = onPayNowClick,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("pay_now_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("AUTHORIZING TRANSACTION...", color = DarkBackground, fontWeight = FontWeight.Black)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAY ₹${selectedPlan.priceInr.toInt()} NOW (${selectedMethod.title})",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = DarkBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Interactive UPI QR Dialog
    if (showQrDialog) {
        val qrUri = remember(selectedPlan) {
            UpiMerchantConfig.getPlanUpiUri(selectedPlan)
        }
        val qrBitmap = remember(qrUri) {
            QrCodeGenerator.generateQrImageBitmap(qrUri, sizePx = 350)
        }

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Scan to Pay Priyan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("₹${selectedPlan.priceInr.toInt()} • ${selectedPlan.title}", fontSize = 12.sp, color = CyanNeon)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "UPI QR Code",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Merchant VPA: ${UpiMerchantConfig.MERCHANT_UPI_ID}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Supported: GPay, PhonePe, Paytm, BHIM, CRED, RuPay",
                        fontSize = 10.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showQrDialog = false }) {
                    Text("Done")
                }
            },
            containerColor = DarkSurface
        )
    }
}

/**
 * 5. Passbook & History View
 */
@Composable
fun PaymentHistoryView(
    records: List<RazorpayTransactionRecord>,
    onRefundClick: (RazorpayTransactionRecord) -> Unit,
    onViewReceipt: (RazorpayTransactionRecord) -> Unit
) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Transactions Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(
                    "All your RuPay cards, UPI payments, and bill receipts will be recorded securely here.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(records) { record ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                        .clickable { onViewReceipt(record) }
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
                                Text("Order: ${record.orderId}", fontSize = 10.sp, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FinancialEngine.formatINR(record.amountInr),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                val statusColor = when (record.status.uppercase()) {
                                    "SUCCESS" -> SuccessGreen
                                    "FAILED" -> DangerRed
                                    "REFUNDED" -> WarningAmber
                                    else -> CyanNeon
                                }
                                Text(record.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderGlassLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${record.paymentMethod} • ${record.date}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { onViewReceipt(record) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Receipt", fontSize = 10.sp, color = CyanNeon)
                                }

                                if (record.status == "SUCCESS" && record.refundStatus == null) {
                                    OutlinedButton(
                                        onClick = { onRefundClick(record) },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Refund", fontSize = 10.sp, color = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Payment Success View with Green Tick Animation and Digital Receipt
 */
@Composable
fun PaymentSuccessView(
    state: RazorpayPaymentUiState.Success,
    selectedPlan: SubscriptionPlanTier,
    onContinue: () -> Unit,
    onShareReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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
                .size(96.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.2f))
                .border(2.dp, SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = SuccessGreen,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Payment Verified!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )
        Text(
            text = "Funds successfully transferred to ${UpiMerchantConfig.MERCHANT_NAME}",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Receipt Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReceiptRow("Amount Paid", FinancialEngine.formatINR(state.amountInr))
                ReceiptRow("Transaction Ref", state.paymentId)
                ReceiptRow("Order ID", state.orderId)
                ReceiptRow("Method", state.paymentMethod)
                ReceiptRow("Merchant VPA", UpiMerchantConfig.MERCHANT_UPI_ID)
                ReceiptRow("Valid Until", state.validUntil)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onShareReceipt,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", color = TextPrimary)
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Payment Failed View
 */
@Composable
fun PaymentFailedView(
    state: RazorpayPaymentUiState.Failed,
    onRetry: () -> Unit,
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
                .background(DangerRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Error, contentDescription = null, tint = DangerRed, modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Declined", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(state.errorMessage, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
        ) {
            Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Cancel", color = TextSecondary)
        }
    }
}

/**
 * Payment Pending View
 */
@Composable
fun PaymentPendingView(
    state: RazorpayPaymentUiState.Pending,
    onCheckAgain: () -> Unit,
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
        CircularProgressIndicator(color = WarningAmber, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Processing", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(state.message, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCheckAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
        ) {
            Text("Refresh Status", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Return to Dashboard", color = TextSecondary)
        }
    }
}

/**
 * Payment Cancelled View
 */
@Composable
fun PaymentCancelledView(
    message: String,
    onTryAgain: () -> Unit,
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
        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Payment Cancelled", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onTryAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Retry Payment", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Cancel", color = TextSecondary)
        }
    }
}
