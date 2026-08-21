package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.EmiItem
import com.example.domain.model.GoalItem
import com.example.ui.MainViewModel
import com.example.ui.components.AddBillDialog
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BudgetUtilizationSection
import com.example.ui.components.FinFamTopAppBar
import com.example.ui.components.FinancialHealthRadarCard
import com.example.ui.components.GoalsSection
import com.example.ui.components.HealthScoreCard
import com.example.ui.components.QuickActionsGrid
import com.example.ui.components.TopUpGoalDialog
import com.example.ui.components.TransactionRow
import com.example.ui.components.UpcomingBillsSection
import com.example.ui.components.VaultCard
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkNavyCanvas
import com.example.ui.theme.DarkNavyElevated
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GrayBorder
import com.example.ui.theme.GrayMuted
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SecondaryVioletGlow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val health by viewModel.financialHealth.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val emis by viewModel.emis.collectAsStateWithLifecycle()
    val radarAxes by viewModel.radarHealthAxes.collectAsStateWithLifecycle()
    val isSimulatingRadar by viewModel.isSimulatingRadarUpdates.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var selectedGoalForTopUp by remember { mutableStateOf<GoalItem?>(null) }

    Scaffold(
        topBar = {
            FinFamTopAppBar(
                initials = userProfile.initials,
                familyName = userProfile.familyName,
                unreadCount = userProfile.unreadNotificationsCount,
                isPremium = userProfile.isPremium,
                onUpgradeClick = { onNavigate("payment") },
                onNotificationsClick = { onNavigate("family") },
                onProfileClick = { onNavigate("profile") }
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Family Vault Balance Card
            item {
                VaultCard(
                    userProfile = userProfile,
                    onDepositClick = { showAddIncomeDialog = true },
                    onAddExpenseClick = { showAddExpenseDialog = true }
                )
            }

            // 2. Financial Health Score (CRED style circular gauge)
            item {
                HealthScoreCard(
                    health = health,
                    onViewAnalysisClick = { onNavigate("analytics") }
                )
            }

            // 2b. Financial Health Radar Matrix (Multi-Axis Spider Web Chart)
            item {
                FinancialHealthRadarCard(
                    categories = radarAxes,
                    isSimulating = isSimulatingRadar,
                    onToggleSimulation = { viewModel.togglePeriodicRadarSimulation() },
                    onSimulateStep = { viewModel.simulateRadarDataStep() },
                    onExploreAnalyticsClick = { onNavigate("analytics") }
                )
            }

            // 3. Quick Actions Grid
            item {
                QuickActionsGrid(
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onAddIncomeClick = { showAddIncomeDialog = true },
                    onScanReceiptClick = { onNavigate("advisor") },
                    onPayBillClick = { onNavigate("family") },
                    onAddGoalClick = { showAddGoalDialog = true },
                    onFamilyWalletClick = { onNavigate("family") },
                    onTransferClick = { onNavigate("transfer") },
                    onFinFamPayClick = { onNavigate("payment_gateway") }
                )
            }

            // 4. Budget Utilization Carousel
            item {
                BudgetUtilizationSection(
                    budgets = budgets,
                    onAddBudgetClick = { showAddBudgetDialog = true },
                    onBudgetClick = { onNavigate("goals") }
                )
            }

            // 5. Upcoming Bills Tracker
            item {
                UpcomingBillsSection(
                    bills = bills,
                    onPayBillClick = { bill ->
                        viewModel.payBill(bill.id, bill.name, bill.amount, "UPI")
                    },
                    onAddBillClick = { showAddBillDialog = true }
                )
            }

            // 5b. Active EMIs & Loans Tracker
            item {
                ActiveEmiHomeSection(
                    emis = emis,
                    onViewAllEmis = { onNavigate("emi") },
                    onOpenCalculator = { onNavigate("emi_calculator") }
                )
            }

            // 6. Savings Goals Section
            item {
                GoalsSection(
                    goals = goals,
                    onAddGoalClick = { showAddGoalDialog = true },
                    onGoalClick = { goal -> selectedGoalForTopUp = goal }
                )
            }

            // 7. Recent Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT TRANSACTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Monthly Trends →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        modifier = Modifier.clickable { onNavigate("trends") }
                    )
                }
            }

            // Transaction items
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions found.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(transactions.take(8)) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialogs
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

    if (showAddIncomeDialog) {
        AddTransactionDialog(
            isIncomeMode = true,
            onDismiss = { showAddIncomeDialog = false },
            onConfirm = { title, category, amount, method, notes, _ ->
                viewModel.addIncome(
                    title = title,
                    category = category,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes
                )
                showAddIncomeDialog = false
            }
        )
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

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { name, amount, dueDate, category, isRecurring, autoPay ->
                viewModel.addBill(name, amount, dueDate, category, isRecurring, autoPay)
                showAddBillDialog = false
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

@Composable
fun ActiveEmiHomeSection(
    emis: List<EmiItem>,
    onViewAllEmis: () -> Unit,
    onOpenCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEmis = emis.filter { !it.isCompleted }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACTIVE EMIS & LOANS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                if (activeEmis.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanNeon.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${activeEmis.size} active",
                            fontSize = 10.sp,
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Calculator",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryVioletGlow,
                    modifier = Modifier.clickable(onClick = onOpenCalculator)
                )
                Text(text = "•", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = "Manage →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    modifier = Modifier.clickable(onClick = onViewAllEmis)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeEmis.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .clickable(onClick = onViewAllEmis)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No active EMIs. Tap to add your Bike, Gadget, or Loan installments.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeEmis.take(5)) { emi ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable(onClick = onViewAllEmis),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(CyanNeon.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (emi.category.lowercase()) {
                                                "vehicle" -> Icons.Default.DirectionsBike
                                                "electronics" -> Icons.Default.LaptopMac
                                                "mobile" -> Icons.Default.Smartphone
                                                "education" -> Icons.Default.School
                                                "home" -> Icons.Default.Home
                                                else -> Icons.Default.AccountBalance
                                            },
                                            contentDescription = null,
                                            tint = CyanNeon,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = emi.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "₹${emi.monthlyEmi.toInt()}/mo",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanNeon,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${(emi.progressPercentage * 100).toInt()}% Paid",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SuccessGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { emi.progressPercentage.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanNeon,
                                trackColor = DarkNavyElevated
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${emi.remainingTenureMonths} mos left",
                                    color = GrayMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "₹${emi.remainingAmount.toInt()} rem",
                                    color = WarningAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
