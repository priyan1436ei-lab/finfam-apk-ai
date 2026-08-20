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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.BudgetItem
import com.example.domain.model.GoalItem
import com.example.ui.MainViewModel
import com.example.ui.components.AddBillDialog
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.BillRowItem
import com.example.ui.components.BudgetCard
import com.example.ui.components.GoalCard
import com.example.ui.components.TopUpGoalDialog
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceGlow
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

/**
 * 1. Analytics Screen (7-Pillar Health Score Breakdown & Trends)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val health by viewModel.financialHealth.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Health & Trends", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Score Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryBlue.copy(alpha = 0.25f), SecondaryViolet.copy(alpha = 0.25f))
                            )
                        )
                        .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("OVERALL SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("${health.overallScore}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    Text("/100", fontSize = 16.sp, color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SuccessGreen.copy(alpha = 0.2f))
                                    .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(health.statusLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(health.aiSummary, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                    }
                }
            }

            // 7 Pillars Section
            item {
                Text(
                    text = "7-PILLAR HEALTH BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                PillarCard(title = "Savings Rate", score = health.savingsRateScore, desc = "Target 30%+ of monthly income saved.")
            }
            item {
                PillarCard(title = "Spending Consistency", score = health.spendingConsistencyScore, desc = "Stable weekly outflow without erratic spikes.")
            }
            item {
                PillarCard(title = "Emergency Fund Buffer", score = health.emergencyFundScore, desc = "6 months of living expenses reserve.")
            }
            item {
                PillarCard(title = "Bill & Utility Adherence", score = health.billAdherenceScore, desc = "On-time recurring bill payments.")
            }
            item {
                PillarCard(title = "Budget Discipline", score = health.budgetAdherenceScore, desc = "Staying under category allocation thresholds.")
            }
            item {
                PillarCard(title = "Debt & EMI Behavior", score = health.debtBehaviorScore, desc = "Low debt-to-income ratio and on-time credit settlement.")
            }
            item {
                PillarCard(title = "Goal Milestone Progress", score = health.goalProgressScore, desc = "Systematic progression towards short/long-term goals.")
            }

            // AI Actionable Recommendations
            item {
                Text(
                    text = "AI ACTIONABLE RECOMMENDATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(health.recommendations) { rec ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(rec, fontSize = 12.sp, color = TextPrimary, lineHeight = 18.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PillarCard(
    title: String,
    score: Int,
    desc: String,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        score >= 80 -> SuccessGreen
        score >= 60 -> CyanNeon
        score >= 40 -> WarningAmber
        else -> DangerRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text("$score/100", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = barColor)
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (score / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = DarkSurfaceGlow
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(desc, fontSize = 11.sp, color = TextMuted)
        }
    }
}

/**
 * 2. Goals & Budgets Tabbed Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsAndBudgetsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var selectedGoalForTopUp by remember { mutableStateOf<GoalItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals & Budgets", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Tab Switcher
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Savings Goals (${goals.size})", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) PrimaryBlue else TextMuted) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Monthly Budgets (${budgets.size})", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) PrimaryBlue else TextMuted) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedTab == 0) {
                    // Goals Tab
                    item {
                        Button(
                            onClick = { showAddGoalDialog = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Savings Goal", fontWeight = FontWeight.Bold)
                        }
                    }

                    items(goals) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { selectedGoalForTopUp = goal },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Budgets Tab
                    item {
                        Button(
                            onClick = { showAddBudgetDialog = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Category Budget", fontWeight = FontWeight.Bold)
                        }
                    }

                    items(budgets) { budget ->
                        BudgetCard(
                            budget = budget,
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { name, emoji, targetAmount, targetDate, category, isFamily ->
                viewModel.addGoal(name, emoji, targetAmount, targetDate, category, isFamily)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onConfirm = { category, limit ->
                viewModel.addBudget(category, limit)
                showAddBudgetDialog = false
            }
        )
    }

    selectedGoalForTopUp?.let { goal ->
        TopUpGoalDialog(
            goal = goal,
            onDismiss = { selectedGoalForTopUp = null },
            onConfirm = { amount ->
                viewModel.depositGoal(goal.id, amount)
                selectedGoalForTopUp = null
            }
        )
    }
}

/**
 * 3. Family & Bills Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyAndBillsScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showAddBillDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family & Bills", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                    text = { Text("Family Vault (${familyMembers.size})", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) PrimaryBlue else TextMuted) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Bills & Auto-Pay (${bills.size})", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) PrimaryBlue else TextMuted) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedTab == 0) {
                    // Family Members List
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(userProfile.familyName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("Shared Vault", fontSize = 11.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("All family members have synchronized transparent tracking.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }

                    items(familyMembers) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(member.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text("${member.role} • ${member.email}", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Spent ${FinancialEngine.formatINR(member.spentThisMonth)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Contrib: ${FinancialEngine.formatINR(member.monthlyContribution)}", fontSize = 10.sp, color = SuccessGreen)
                            }
                        }
                    }
                } else {
                    // Bills Manager Tab
                    item {
                        Button(
                            onClick = { showAddBillDialog = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Recurring / Utility Bill", fontWeight = FontWeight.Bold, color = DarkBackground)
                        }
                    }

                    items(bills) { bill ->
                        BillRowItem(
                            bill = bill,
                            onPayClick = { viewModel.payBill(bill.id, bill.name, bill.amount, "UPI") }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { name, amount, dueDate, category, isRecurring, autoPay ->
                viewModel.addBill(name, amount, dueDate, category, isRecurring, autoPay)
                showAddBillDialog = false
            }
        )
    }
}

/**
 * 4. User Profile & Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var biometricState by remember { mutableStateOf(userProfile.isBiometricEnabled) }
    var notificationsState by remember { mutableStateOf(userProfile.isNotificationsEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PrimaryBlue, SecondaryViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(userProfile.initials, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(userProfile.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(userProfile.email, fontSize = 12.sp, color = TextMuted)
                            Text(userProfile.phone, fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            // Subscription & Membership Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, if (userProfile.isPremium) WarningAmber.copy(alpha = 0.5f) else BorderGlassLight, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (userProfile.isPremium) "FinFam ${userProfile.premiumTier}" else "FinFam Free Tier",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (userProfile.isPremium) WarningAmber else TextPrimary
                            )
                            Text("Access AI Financial Coach & Unlimited Vaults", fontSize = 11.sp, color = TextMuted)
                        }

                        Button(
                            onClick = { onNavigate("payment") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (userProfile.isPremium) "Manage" else "Upgrade", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Security Preferences
            item {
                Text("SECURITY & NOTIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Biometric App Lock", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Switch(
                            checked = biometricState,
                            onCheckedChange = { biometricState = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderGlassLight, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Budget 80%/90% Push Alerts", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Switch(
                            checked = notificationsState,
                            onCheckedChange = { notificationsState = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryBlue)
                        )
                    }
                }
            }

            // Export Reports
            item {
                Text("DATA & REPORTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            }

            item {
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Exporting FinFam FY 2026-27 Tax Statement PDF...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Tax & Expense Report (PDF / CSV)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
