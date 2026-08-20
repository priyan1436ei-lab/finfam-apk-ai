package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.ChatMessage
import com.example.domain.model.ReceiptScanResult
import com.example.ui.MainViewModel
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAdvisorTyping by viewModel.isCoachTyping.collectAsStateWithLifecycle()
    val isScanningReceipt by viewModel.isScanningReceipt.collectAsStateWithLifecycle()
    val scannedReceipt by viewModel.scannedReceiptResult.collectAsStateWithLifecycle()

    var inputPrompt by remember { mutableStateOf("") }
    var showReceiptDialog by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "💡 How to save ₹10,000 extra this month?",
        "🍽️ Analyze my grocery & dining spend",
        "✈️ Japan trip savings milestone check",
        "📈 Best SIP investment strategy for family"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryBlue, SecondaryViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FinFam AI Assistant", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("AI Financial Coach", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) PrimaryBlue else TextMuted) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Receipt OCR Scanner", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) PrimaryBlue else TextMuted) }
                )
            }

            if (selectedTab == 0) {
                // Tab 1: AI Coach Chat
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Quick Prompt Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickQuestions) { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, BorderGlassLight, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.askAiCoach(q) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(q, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat messages list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatMessages) { msg ->
                            ChatMessageBubble(msg = msg)
                        }

                        if (isAdvisorTyping) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanNeon, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("FinFam AI is analyzing household finances...", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat input bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = { Text("Ask anything about budget, goals, taxes...", color = TextMuted, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputPrompt.isNotBlank()) {
                                        viewModel.askAiCoach(inputPrompt)
                                        inputPrompt = ""
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (inputPrompt.isNotBlank()) {
                                    viewModel.askAiCoach(inputPrompt)
                                    inputPrompt = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryBlue)
                        }
                    }
                }
            } else {
                // Tab 2: Smart Receipt OCR Scanner
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceVariant)
                            .border(2.dp, Brush.linearGradient(listOf(PrimaryBlue, CyanNeon)), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Smart Receipt OCR Scanner", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Extracts merchant, total bill, tax breakdown & items", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.scanReceiptSimulator("GROCERY")
                            showReceiptDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (isScanningReceipt) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Receipt with AI OCR...")
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Supermarket Grocery Bill (₹1,845)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.scanReceiptSimulator("FUEL")
                            showReceiptDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Indian Oil Fuel Receipt (₹1,500)", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    scannedReceipt?.let { scan ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(scan.merchantName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(FinancialEngine.formatINR(scan.amount), fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                                }
                                Text("Extracted: ${scan.detectedItems.size} items • GST: ₹${scan.taxGst}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReceiptDialog && scannedReceipt != null) {
        ReceiptPreviewDialog(
            scan = scannedReceipt!!,
            onDismiss = { showReceiptDialog = false },
            onConfirm = {
                viewModel.confirmScannedReceiptAsExpense()
                showReceiptDialog = false
            }
        )
    }
}

@Composable
fun ChatMessageBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) PrimaryBlue else DarkSurfaceVariant)
                .border(1.dp, if (isUser) PrimaryBlue else BorderGlassLight, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = msg.text,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
