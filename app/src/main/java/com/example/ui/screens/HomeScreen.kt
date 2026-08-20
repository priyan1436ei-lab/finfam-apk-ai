package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.GoalItem
import com.example.ui.MainViewModel
import com.example.ui.components.AddBillDialog
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BudgetUtilizationSection
import com.example.ui.components.FinFamTopAppBar
import com.example.ui.components.GoalsSection
import com.example.ui.components.HealthScoreCard
import com.example.ui.components.QuickActionsGrid
import com.example.ui.components.TopUpGoalDialog
import com.example.ui.components.TransactionRow
import com.example.ui.components.UpcomingBillsSection
import com.example.ui.components.VaultCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

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

            // 3. Quick Actions Grid
            item {
                QuickActionsGrid(
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onAddIncomeClick = { showAddIncomeDialog = true },
                    onScanReceiptClick = { onNavigate("advisor") },
                    onPayBillClick = { onNavigate("family") },
                    onAddGoalClick = { showAddGoalDialog = true },
                    onFamilyWalletClick = { onNavigate("family") }
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
                        text = "${transactions.size} records",
                        fontSize = 11.sp,
                        color = TextMuted
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
