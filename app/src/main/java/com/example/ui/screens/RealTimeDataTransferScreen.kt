package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.LivePeerNode
import com.example.domain.model.RealTimeTransferRecord
import com.example.domain.model.RealTimeTransferStatus
import com.example.domain.model.RealTimeTransferType
import com.example.domain.security.BiometricAuthManager
import com.example.domain.security.BiometricAuthResult
import com.example.ui.MainViewModel
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkNavyElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassAmbientShadow
import com.example.ui.theme.GlassBorderTopSheen
import com.example.ui.theme.GlassSurfaceDark
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassSurfaceFrostedSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SecondaryVioletGlow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeDataTransferScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transferHistory by viewModel.realTimeTransferHistory.collectAsStateWithLifecycle()
    val peerNodes by viewModel.liveP2pNodes.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isLiveTransferStreaming.collectAsStateWithLifecycle()
    val streamProgress by viewModel.transferStreamingProgress.collectAsStateWithLifecycle()
    val currentStep by viewModel.transferCurrentStep.collectAsStateWithLifecycle()
    val isBackgroundSyncActive by viewModel.isLiveBackgroundSyncActive.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Funds Transfer, 1: Data Beam Sync, 2: Real-Time QR, 3: Live Audit Stream
    var selectedReceiptRecord by remember { mutableStateOf<RealTimeTransferRecord?>(null) }

    fun authorizeAndExecute(
        title: String,
        amountFormatted: String,
        onAuthorized: () -> Unit
    ) {
        val fragActivity = context as? FragmentActivity
            ?: (context as? android.content.ContextWrapper)?.baseContext as? FragmentActivity

        if (fragActivity != null && userProfile.isBiometricEnabled) {
            BiometricAuthManager.promptBiometric(
                activity = fragActivity,
                title = "Authorize $title",
                subtitle = "Authenticate to stream $amountFormatted in real-time",
                description = "256-bit AES hardware keystore verification protects this transfer",
                onResult = { result ->
                    when (result) {
                        is BiometricAuthResult.Success -> onAuthorized()
                        is BiometricAuthResult.Cancelled -> {
                            Toast.makeText(context, "Transfer cancelled", Toast.LENGTH_SHORT).show()
                        }
                        is BiometricAuthResult.Error -> {
                            Toast.makeText(context, "Biometric error: ${result.errString}", Toast.LENGTH_SHORT).show()
                        }
                        is BiometricAuthResult.Failed -> {
                            Toast.makeText(context, "Authentication failed. Transfer blocked.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        } else {
            onAuthorized()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("real_time_transfer_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "REAL-TIME TRANSFER",
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
                                    text = "P2P LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanNeon
                                )
                            }
                        }
                        Text(
                            text = "256-Bit Encrypted Data & Fund Streaming",
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
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleLiveBackgroundSync()
                            Toast.makeText(
                                context,
                                if (!isBackgroundSyncActive) "Real-Time Auto-Sync Activated" else "Auto-Sync Paused",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            imageVector = if (isBackgroundSyncActive) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = "Toggle Sync",
                            tint = if (isBackgroundSyncActive) CyanNeon else TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            // Telemetry Banner
            RealTimeConnectionTelemetryHeader(
                isSyncActive = isBackgroundSyncActive,
                peerCount = peerNodes.size,
                vaultBalance = userProfile.totalBalance
            )

            // Mode Tabs
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
                    modifier = Modifier.testTag("tab_instant_money"),
                    text = { Text("MONEY TRANSFER", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier.testTag("tab_data_beam"),
                    text = { Text("DATA BEAM & SYNC", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    modifier = Modifier.testTag("tab_live_qr"),
                    text = { Text("REAL-TIME QR", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    modifier = Modifier.testTag("tab_live_stream"),
                    text = { Text("LIVE AUDIT STREAM (${transferHistory.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (activeTab) {
                    0 -> RealTimeFundsTransferContent(
                        peerNodes = peerNodes,
                        vaultBalance = userProfile.totalBalance,
                        isStreaming = isStreaming,
                        streamProgress = streamProgress,
                        currentStep = currentStep,
                        onInitiateTransfer = { receiverName, receiverVpa, amount, type, note ->
                            authorizeAndExecute(
                                title = "Real-Time Fund Transfer",
                                amountFormatted = "₹${NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())}"
                            ) {
                                viewModel.executeRealTimeFundsTransfer(
                                    receiverName = receiverName,
                                    receiverVpa = receiverVpa,
                                    amount = amount,
                                    transferType = type,
                                    note = note,
                                    onSuccess = { record ->
                                        selectedReceiptRecord = record
                                    }
                                )
                            }
                        }
                    )
                    1 -> RealTimeDataBeamSyncContent(
                        peerNodes = peerNodes,
                        isStreaming = isStreaming,
                        streamProgress = streamProgress,
                        currentStep = currentStep,
                        onBeamData = { packageName, peerNode, sizeKb ->
                            authorizeAndExecute(
                                title = "Real-Time Data Beam",
                                amountFormatted = "$packageName (${sizeKb.toInt()} KB)"
                            ) {
                                viewModel.executeRealTimeDataBeam(
                                    dataPackageName = packageName,
                                    peerNode = peerNode,
                                    payloadSizeKb = sizeKb,
                                    onSuccess = { record ->
                                        selectedReceiptRecord = record
                                    }
                                )
                            }
                        }
                    )
                    2 -> RealTimeDynamicQrContent(
                        vpa = "priyan1436ei@okhdfcbank",
                        name = userProfile.name,
                        balance = userProfile.totalBalance
                    )
                    3 -> RealTimeAuditStreamContent(
                        history = transferHistory,
                        onSelectRecord = { selectedReceiptRecord = it }
                    )
                }

                // Streaming Packet Overlay
                if (isStreaming) {
                    RealTimeStreamingProgressOverlay(
                        progress = streamProgress,
                        step = currentStep
                    )
                }
            }
        }
    }

    // Digital Proof Dialog
    selectedReceiptRecord?.let { record ->
        RealTimeTransferReceiptDialog(
            record = record,
            onDismiss = { selectedReceiptRecord = null },
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "FinFam Real-Time Transfer Proof")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "⚡ FinFam Real-Time Transfer Proof\n" +
                                "Txn ID: ${record.id}\n" +
                                "UTR: ${record.utrNumber}\n" +
                                "Sender: ${record.senderName}\n" +
                                "Receiver: ${record.receiverName} (${record.receiverVpaOrAcc})\n" +
                                (if (record.amount != null) "Amount: ₹${record.amount}\n" else "Data Payload: ${record.payloadSizeKb} KB\n") +
                                "Protocol: ${record.protocol}\n" +
                                "Latency: ${record.latencyMs} ms\n" +
                                "Hash: ${record.sha256Hash}\n" +
                                "Status: VERIFIED & SETTLED IN REAL-TIME"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Real-Time Transfer Receipt"))
            }
        )
    }
}

/**
 * Live Connection & Telemetry Header
 */
@Composable
fun RealTimeConnectionTelemetryHeader(
    isSyncActive: Boolean,
    peerCount: Int,
    vaultBalance: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = GlassAmbientShadow)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        GlassSurfaceElevated,
                        GlassSurfaceDark
                    )
                )
            )
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
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
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isSyncActive) CyanNeon.copy(alpha = pulseAlpha) else WarningAmber)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isSyncActive) "SOCKET STREAM: CONNECTED" else "STREAM: MANUAL SYNC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSyncActive) CyanNeon else WarningAmber,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$peerCount Active Family Nodes • Latency ~12ms",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "VAULT AVAILABLE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Text(
                    text = "₹${NumberFormat.getNumberInstance(Locale.US).format(vaultBalance.toLong())}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = SuccessGreen
                )
            }
        }
    }
}

/**
 * Tab 0: Real-Time Funds / Money Transfer View
 */
@Composable
fun RealTimeFundsTransferContent(
    peerNodes: List<LivePeerNode>,
    vaultBalance: Double,
    isStreaming: Boolean,
    streamProgress: Float,
    currentStep: RealTimeTransferStatus,
    onInitiateTransfer: (String, String, Double, RealTimeTransferType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNode by remember { mutableStateOf<LivePeerNode?>(peerNodes.firstOrNull()) }
    var customVpa by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var isCustomRecipient by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("1500") }
    var transferNote by remember { mutableStateOf("Family Vault Real-Time Pool") }
    var selectedTransferType by remember { mutableStateOf(RealTimeTransferType.FUNDS_TRANSFER) }

    val presetAmounts = listOf(500, 1000, 2500, 5000, 10000)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recipient Selection Carousel
        item {
            Text(
                text = "1. SELECT PEER / RECIPIENT NODE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(peerNodes) { node ->
                    val isSelected = !isCustomRecipient && selectedNode?.id == node.id
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(16.dp), spotColor = CyanNeon)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) DarkNavyElevated else GlassSurfaceDark)
                            .border(
                                1.5.dp,
                                if (isSelected) CyanNeon else BorderGlass,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                isCustomRecipient = false
                                selectedNode = node
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(node.avatarColorHex).copy(alpha = 0.25f))
                                        .border(1.dp, Color(node.avatarColorHex), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = node.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(node.avatarColorHex)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (node.isOnline) SuccessGreen else DangerRed)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = node.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = node.relationship,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${node.pingMs}ms • ${node.lastSyncText}",
                                fontSize = 9.sp,
                                color = CyanNeon
                            )
                        }
                    }
                }

                // Custom VPA option card
                item {
                    val isSelected = isCustomRecipient
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(16.dp), spotColor = SecondaryViolet)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) DarkNavyElevated else GlassSurfaceDark)
                            .border(
                                1.5.dp,
                                if (isSelected) SecondaryVioletGlow else BorderGlass,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                isCustomRecipient = true
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SecondaryViolet.copy(alpha = 0.25f))
                                    .border(1.dp, SecondaryViolet, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = SecondaryViolet, modifier = Modifier.size(16.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Custom UPI / A/C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Any VPA / Bank",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instant IMPS",
                                fontSize = 9.sp,
                                color = SecondaryVioletGlow
                            )
                        }
                    }
                }
            }
        }

        // Custom VPA Input (if selected)
        if (isCustomRecipient) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceDark)
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Custom Recipient Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Recipient Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = BorderGlassLight
                            )
                        )
                        OutlinedTextField(
                            value = customVpa,
                            onValueChange = { customVpa = it },
                            label = { Text("UPI ID / VPA (e.g. name@okhdfcbank)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = BorderGlassLight
                            )
                        )
                    }
                }
            }
        }

        // 2. Amount Input & Preset Chips
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = GlassAmbientShadow)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GlassSurfaceElevated,
                                GlassSurfaceDark
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "2. TRANSFER AMOUNT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "₹",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetAmounts.forEach { preset ->
                            val isSelected = amountText == preset.toString()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CyanNeon.copy(alpha = 0.2f) else GlassSurfaceFrostedSubtle)
                                    .border(1.dp, if (isSelected) CyanNeon else BorderGlassLight, RoundedCornerShape(10.dp))
                                    .clickable { amountText = preset.toString() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹$preset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyanNeon else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Note & Transfer Purpose
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "3. TRANSFER TYPE & NOTE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            RealTimeTransferType.FUNDS_TRANSFER to "Instant P2P",
                            RealTimeTransferType.FAMILY_ALLOWANCE to "Family Pool",
                            RealTimeTransferType.INSTANT_UPI to "Direct UPI"
                        ).forEach { (type, label) ->
                            val isSelected = selectedTransferType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SecondaryViolet.copy(alpha = 0.25f) else GlassSurfaceFrostedSubtle)
                                    .border(1.dp, if (isSelected) SecondaryVioletGlow else BorderGlassLight, RoundedCornerShape(10.dp))
                                    .clickable { selectedTransferType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SecondaryVioletGlow else TextSecondary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = transferNote,
                        onValueChange = { transferNote = it },
                        label = { Text("Transfer Purpose / Ledger Memo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = BorderGlassLight
                        )
                    )
                }
            }
        }

        // Action Button
        item {
            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            val isValid = amountVal > 0.0 && (!isCustomRecipient || (customName.isNotBlank() && customVpa.isNotBlank()))

            Button(
                onClick = {
                    val receiverName = if (isCustomRecipient) customName else selectedNode?.name ?: "Family Peer"
                    val receiverVpa = if (isCustomRecipient) customVpa else selectedNode?.vpa ?: "family@okhdfc"
                    onInitiateTransfer(receiverName, receiverVpa, amountVal, selectedTransferType, transferNote)
                },
                enabled = isValid && !isStreaming,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = CyanNeon),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkSurfaceVariant,
                    disabledContentColor = TextMuted
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STREAM ₹${NumberFormat.getNumberInstance(Locale.US).format(amountVal.toLong())} IN REAL-TIME",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Tab 1: Real-Time Peer Data Beam & Family Sync
 */
@Composable
fun RealTimeDataBeamSyncContent(
    peerNodes: List<LivePeerNode>,
    isStreaming: Boolean,
    streamProgress: Float,
    currentStep: RealTimeTransferStatus,
    onBeamData: (String, LivePeerNode, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTargetNode by remember { mutableStateOf(peerNodes.firstOrNull() ?: peerNodes[0]) }

    val dataPackages = listOf(
        Triple("Family Budget Ledger (4 Active Envelopes)", 48.0, Icons.Default.AccountBalance),
        Triple("Shared Household Bills & Split Ledgers", 24.5, Icons.Default.Bolt),
        Triple("Savings Goals & Milestones Audit", 36.2, Icons.Default.CheckCircle),
        Triple("Complete Encrypted Room DB Backup Archive", 248.0, Icons.Default.Security)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PrimaryBlue)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                PrimaryBlue.copy(alpha = 0.25f),
                                GlassSurfaceElevated,
                                GlassSurfaceDark
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PEER-TO-PEER DATA BEAM",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("AES-256 GCM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Stream encrypted financial ledgers, household budgets, and Room database checkpoints directly between family nodes over real-time WebSockets without third-party exposure.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Select Destination Node
        item {
            Text(
                text = "TARGET FAMILY NODE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(peerNodes) { node ->
                    val isSelected = selectedTargetNode.id == node.id
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) DarkNavyElevated else GlassSurfaceDark)
                            .border(1.5.dp, if (isSelected) CyanNeon else BorderGlass, RoundedCornerShape(14.dp))
                            .clickable { selectedTargetNode = node }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(node.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text(node.relationship, fontSize = 10.sp, color = TextSecondary)
                            Text("${node.pingMs}ms latency", fontSize = 9.sp, color = CyanNeon)
                        }
                    }
                }
            }
        }

        // Data Packages to Beam
        item {
            Text(
                text = "SELECT DATA BEAM PACKAGE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        items(dataPackages) { (pkgName, sizeKb, icon) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = GlassAmbientShadow)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(14.dp)
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanNeon.copy(alpha = 0.15f))
                                .border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(pkgName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("Payload: ${sizeKb.toInt()} KB • Real-time stream", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = { onBeamData(pkgName, selectedTargetNode, sizeKb) },
                        enabled = !isStreaming,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Beam", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Dynamic Real-Time QR Transfer
 */
@Composable
fun RealTimeDynamicQrContent(
    vpa: String,
    name: String,
    balance: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var secondsLeft by remember { mutableIntStateOf(300) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = CyanNeon.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GlassSurfaceElevated,
                            GlassSurfaceDark
                        )
                    )
                )
                .border(1.5.dp, BorderGlass, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(vpa, fontSize = 11.sp, color = CyanNeon)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarningAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Token: ${secondsLeft / 60}:${"%02d".format(secondsLeft % 60)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Simulated Matrix QR Canvas
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = size.width / 15f
                        for (r in 0 until 15) {
                            for (c in 0 until 15) {
                                val isCorner = (r < 4 && c < 4) || (r < 4 && c > 10) || (r > 10 && c < 4)
                                val isPattern = (r * c + r + c) % 3 == 0 || isCorner
                                if (isPattern) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(c * cellSize, r * cellSize),
                                        size = androidx.compose.ui.geometry.Size(cellSize * 0.9f, cellSize * 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Scan with any UPI App or FinFam peer node to stream funds or receive encrypted ledger beam instantly.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("FinFam UPI", "upi://pay?pa=$vpa&pn=${name.replace(" ", "%20")}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "UPI Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassSurfaceDark)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy UPI", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "FinFam Real-Time QR Payload")
                                putExtra(Intent.EXTRA_TEXT, "Pay $name instantly: upi://pay?pa=$vpa&pn=${name.replace(" ", "%20")}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBackground)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Live Transfer Audit Stream & History
 */
@Composable
fun RealTimeAuditStreamContent(
    history: List<RealTimeTransferRecord>,
    onSelectRecord: (RealTimeTransferRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME SETTLED AUDIT TRAIL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "● LIVE SOCKET FEED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            }
        }

        items(history) { record ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = GlassAmbientShadow)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurfaceDark)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .clickable { onSelectRecord(record) }
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
                                    .clip(CircleShape)
                                    .background(
                                        if (record.amount != null) SuccessGreen.copy(alpha = 0.2f)
                                        else PrimaryBlue.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (record.amount != null) Icons.Default.ArrowUpward else Icons.Default.SyncAlt,
                                    contentDescription = null,
                                    tint = if (record.amount != null) SuccessGreen else CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(record.receiverName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(record.receiverVpaOrAcc, fontSize = 10.sp, color = TextSecondary)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (record.amount != null) {
                                Text(
                                    text = "-₹${NumberFormat.getNumberInstance(Locale.US).format(record.amount.toLong())}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = DangerRed
                                )
                            } else {
                                Text(
                                    text = "${record.payloadSizeKb?.toInt()} KB",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CyanNeon
                                )
                            }
                            Text(
                                text = record.timestampFormatted,
                                fontSize = 9.sp,
                                color = TextMuted
                            )
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
                            text = "Ref: ${record.utrNumber}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${record.latencyMs}ms", fontSize = 10.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Streaming Progress Overlay during real-time transfer
 */
@Composable
fun RealTimeStreamingProgressOverlay(
    progress: Float,
    step: RealTimeTransferStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stream_packet")
    val packetOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packet_pos"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = CyanNeon)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassSurfaceDark)
                .border(1.5.dp, CyanNeon, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "STREAMING REAL-TIME TRANSFER",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = CyanNeon,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Visual Packet Stream Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val startX = 40f
                    val endX = size.width - 40f
                    val midY = size.height / 2

                    // Base wire
                    drawLine(
                        color = Color(0x3300E5FF),
                        start = Offset(startX, midY),
                        end = Offset(endX, midY),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    // Moving Packet
                    val curX = startX + (endX - startX) * packetOffset
                    drawCircle(
                        color = CyanNeon,
                        radius = 10f,
                        center = Offset(curX, midY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(curX, midY)
                    )

                    // Endpoints
                    drawCircle(color = PrimaryBlue, radius = 14f, center = Offset(startX, midY))
                    drawCircle(color = SuccessGreen, radius = 14f, center = Offset(endX, midY))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = CyanNeon,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = step.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Protocol: WSS // AES-GCM-256 • Low-latency channel",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Real-Time Digital Transfer Receipt Proof Dialog
 */
@Composable
fun RealTimeTransferReceiptDialog(
    record: RealTimeTransferRecord,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        containerColor = GlassSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transfer Settled & Verified",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessGreen.copy(alpha = 0.12f))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (record.amount != null) {
                            Text(
                                text = "₹${NumberFormat.getNumberInstance(Locale.US).format(record.amount.toLong())}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessGreen
                            )
                        } else {
                            Text(
                                text = "${record.payloadSizeKb} KB DATA BEAM",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = CyanNeon
                            )
                        }
                        Text("Transferred in ${record.latencyMs} ms", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReceiptMetaRow("Transaction ID", record.id)
                    ReceiptMetaRow("UTR Number", record.utrNumber)
                    ReceiptMetaRow("Sender", record.senderName)
                    ReceiptMetaRow("Recipient", "${record.receiverName} (${record.receiverVpaOrAcc})")
                    ReceiptMetaRow("Protocol", record.protocol)
                    ReceiptMetaRow("SHA-256 Hash", record.sha256Hash)
                    ReceiptMetaRow("Status", "SETTLED IN REAL-TIME")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBackground)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Proof", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ReceiptMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text(
            value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
