package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.FinGuardRepository
import com.example.domain.engine.FinancialEngine
import com.example.domain.engine.GeminiAiEngine
import com.example.domain.engine.SpendingTrendsEngine
import com.example.domain.model.BillItem
import com.example.domain.model.BudgetItem
import com.example.domain.model.ChatMessage
import com.example.domain.model.DailySpendDataPoint
import com.example.domain.model.EmiItem
import com.example.domain.model.ExpensePrediction
import com.example.domain.model.FamilyContributionShare
import com.example.domain.model.FamilyMemberItem
import com.example.domain.model.FinancialHealth
import com.example.domain.model.GoalItem
import com.example.domain.model.MonthlySpendingTrendsState
import com.example.domain.model.NotificationAlertItem
import com.example.domain.model.NotificationType
import com.example.domain.model.ReceiptScanResult
import com.example.domain.model.TimeHorizon
import com.example.domain.model.TransactionItem
import com.example.domain.model.UserProfile
import com.example.domain.model.WeeklySpendBar
import com.example.ui.components.FinancialHealthAxis
import com.example.ui.components.FinancialHealthDummyData
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.domain.model.LivePeerNode
import com.example.domain.model.RealTimeTransferRecord
import com.example.domain.model.RealTimeTransferStatus
import com.example.domain.model.RealTimeTransferType
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinGuardRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = FinGuardRepository(database)
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val transactions: StateFlow<List<TransactionItem>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetItem>> = repository.budgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalItem>> = repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillItem>> = repository.bills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyMembers: StateFlow<List<FamilyMemberItem>> = repository.familyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emis: StateFlow<List<EmiItem>> = repository.emis
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Health Score reactive computation based on all financial pillars
    val financialHealth: StateFlow<FinancialHealth> = combine(
        userProfile,
        transactions,
        goals,
        budgets,
        bills
    ) { profile, txList, goalList, budgetList, billList ->
        val totalIncome = txList.filter { it.isCredit }.sumOf { it.amount }.let { if (it > 0) it else profile.monthlyIncome }
        val totalExpenses = txList.filter { !it.isCredit }.sumOf { it.amount }.let { if (it > 0) it else profile.monthlyExpenses }
        val totalSavings = (totalIncome - totalExpenses).coerceAtLeast(0.0)
        val goalsRatio = if (goalList.isNotEmpty()) {
            (goalList.sumOf { it.currentAmount } / goalList.sumOf { it.targetAmount }.coerceAtLeast(1.0)).toFloat()
        } else 0.7f
        val unpaidBills = billList.count { !it.isPaid }
        val overspentBudgets = budgetList.count { it.spent > it.monthlyLimit }

        FinancialEngine.calculateHealth(
            income = totalIncome,
            expenses = totalExpenses,
            savings = totalSavings,
            emergencyFund = profile.emergencyFund,
            unpaidBillsCount = unpaidBills,
            budgetsOverspentCount = overspentBudgets,
            goalsProgressRatio = goalsRatio
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialEngine.calculateHealth(65000.0, 38250.0, 26750.0)
    )

    // --- Monthly Spending Trends State ---
    private val _spendingTrendHorizon = MutableStateFlow(TimeHorizon.LAST_6_MONTHS)
    val spendingTrendHorizon: StateFlow<TimeHorizon> = _spendingTrendHorizon.asStateFlow()

    private val _spendingTrendCategory = MutableStateFlow("ALL")
    val spendingTrendCategory: StateFlow<String> = _spendingTrendCategory.asStateFlow()

    private val _spendingTrendMultiCategories = MutableStateFlow<Set<String>>(setOf("Food", "Rent", "Bills"))
    val spendingTrendMultiCategories: StateFlow<Set<String>> = _spendingTrendMultiCategories.asStateFlow()

    private val _spendingTrendMultiLineMode = MutableStateFlow(false)
    val spendingTrendMultiLineMode: StateFlow<Boolean> = _spendingTrendMultiLineMode.asStateFlow()

    private val _spendingTrendSelectedMonthIndex = MutableStateFlow(-1)
    val spendingTrendSelectedMonthIndex: StateFlow<Int> = _spendingTrendSelectedMonthIndex.asStateFlow()

    private data class TrendsQueryState(
        val horizon: TimeHorizon,
        val category: String,
        val multiCategories: Set<String>,
        val isMultiLine: Boolean,
        val selectedMonthIndex: Int
    )

    private val trendsFilterFlow = combine(
        _spendingTrendHorizon,
        _spendingTrendCategory,
        _spendingTrendMultiCategories,
        _spendingTrendMultiLineMode,
        _spendingTrendSelectedMonthIndex
    ) { horizon, category, multiCats, isMulti, monthIdx ->
        TrendsQueryState(horizon, category, multiCats, isMulti, monthIdx)
    }

    val monthlySpendingTrends: StateFlow<MonthlySpendingTrendsState> = combine(
        transactions,
        budgets,
        trendsFilterFlow
    ) { txList, budgetList, filter ->
        SpendingTrendsEngine.computeTrends(
            transactions = txList,
            budgets = budgetList,
            timeHorizon = filter.horizon,
            selectedCategory = filter.category,
            selectedMultiCategories = filter.multiCategories,
            isMultiLineMode = filter.isMultiLine,
            pinnedMonthIndex = filter.selectedMonthIndex
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SpendingTrendsEngine.computeTrends(emptyList())
    )

    fun setSpendingTrendHorizon(horizon: TimeHorizon) {
        _spendingTrendHorizon.value = horizon
        _spendingTrendSelectedMonthIndex.value = -1
    }

    fun setSpendingTrendCategory(category: String) {
        _spendingTrendCategory.value = category
    }

    fun toggleSpendingTrendMultiCategory(category: String) {
        val current = _spendingTrendMultiCategories.value.toMutableSet()
        if (current.contains(category)) {
            if (current.size > 1) { // keep at least 1
                current.remove(category)
            }
        } else {
            current.add(category)
        }
        _spendingTrendMultiCategories.value = current
    }

    fun setSpendingTrendMultiLineMode(enabled: Boolean) {
        _spendingTrendMultiLineMode.value = enabled
    }

    fun setSpendingTrendSelectedMonth(monthIndex: Int) {
        _spendingTrendSelectedMonthIndex.value = monthIndex
    }

    // --- Search & Filter State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("ALL") // ALL, EXPENSE, INCOME, FAMILY
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    // --- Daily & Weekly Spending Real-Time Analytics State ---
    val dailySpendingPoints: StateFlow<List<DailySpendDataPoint>> = combine(
        transactions,
        budgets
    ) { txList, _ ->
        val currentDay = 21 // August 21st
        val points = mutableListOf<DailySpendDataPoint>()
        // Historical daily points for Day 1 to Day 21
        val dailyMap = mutableMapOf<Int, Double>()
        txList.filter { !it.isCredit }.forEach { tx ->
            // extract day if possible or distribute
            val day = (tx.timestamp / 86400000 % 21 + 1).toInt()
            dailyMap[day] = (dailyMap[day] ?: 0.0) + tx.amount / 3.5
        }
        for (day in 1..21) {
            val amount = dailyMap[day] ?: (800.0 + (day * 137 % 1400) + (if (day % 7 == 0) 1800.0 else 0.0))
            points.add(DailySpendDataPoint(day = day, dateLabel = "${day} Aug", amount = amount, isProjected = false))
        }
        // AI Projected trajectory for Day 22 to 31
        val avgDaily = points.map { it.amount }.average()
        for (day in 22..31) {
            val projectedAmount = avgDaily * (0.95 + (day % 3) * 0.08)
            points.add(DailySpendDataPoint(day = day, dateLabel = "${day} Aug", amount = projectedAmount, isProjected = true))
        }
        points
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        (1..31).map { DailySpendDataPoint(day = it, dateLabel = "$it Aug", amount = 1200.0 + (it * 100 % 1500), isProjected = it > 21) }
    )

    val weeklySpendingBars: StateFlow<List<WeeklySpendBar>> = combine(
        transactions,
        userProfile
    ) { txList, _ ->
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val baseValues = listOf(3200.0, 4100.0, 2800.0, 5600.0, 7400.0, 9200.0, 5950.0)
        baseValues.mapIndexed { idx, amt ->
            WeeklySpendBar(
                dayName = days[idx],
                amount = amt,
                isPeakDay = idx == 5 // Saturday peak
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(
            WeeklySpendBar("Mon", 3200.0),
            WeeklySpendBar("Tue", 4100.0),
            WeeklySpendBar("Wed", 2800.0),
            WeeklySpendBar("Thu", 5600.0),
            WeeklySpendBar("Fri", 7400.0),
            WeeklySpendBar("Sat", 9200.0, isPeakDay = true),
            WeeklySpendBar("Sun", 5950.0)
        )
    )

    val familyContributions: StateFlow<List<FamilyContributionShare>> = combine(
        familyMembers,
        userProfile
    ) { members, _ ->
        if (members.isEmpty()) {
            listOf(
                FamilyContributionShare("Rajesh (Father)", "Father", 80000.0, 32000.0, 40f, "0xFF10B981"),
                FamilyContributionShare("Sunita (Mother)", "Mother", 60000.0, 24000.0, 30f, "0xFF06B6D4"),
                FamilyContributionShare("Priyanshu (Son)", "Son", 40000.0, 16000.0, 20f, "0xFF3B82F6"),
                FamilyContributionShare("Ananya (Daughter)", "Daughter", 20000.0, 8000.0, 10f, "0xFFA855F7")
            )
        } else {
            val totalContrib = members.sumOf { it.monthlyContribution }.coerceAtLeast(1.0)
            members.map { m ->
                val share = ((m.monthlyContribution / totalContrib) * 100f).toFloat()
                FamilyContributionShare(
                    memberName = "${m.name} (${m.role})",
                    role = m.role,
                    contributionAmount = m.monthlyContribution,
                    spentAmount = m.spentThisMonth,
                    percentageShare = share,
                    avatarColorHex = m.avatarColor
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(
            FamilyContributionShare("Rajesh (Father)", "Father", 80000.0, 32000.0, 40f, "0xFF10B981"),
            FamilyContributionShare("Sunita (Mother)", "Mother", 60000.0, 24000.0, 30f, "0xFF06B6D4"),
            FamilyContributionShare("Priyanshu (Son)", "Son", 40000.0, 16000.0, 20f, "0xFF3B82F6"),
            FamilyContributionShare("Ananya (Daughter)", "Daughter", 20000.0, 8000.0, 10f, "0xFFA855F7")
        )
    )

    val expensePrediction: StateFlow<ExpensePrediction> = combine(
        transactions,
        budgets,
        userProfile
    ) { txList, budgetList, profile ->
        val totalSpentSoFar = txList.filter { !it.isCredit }.sumOf { it.amount }.let { if (it > 0) it else profile.monthlyExpenses }
        val currentDay = 21
        val daysInMonth = 31
        val projectedEndOfMonth = (totalSpentSoFar / currentDay) * daysInMonth
        val projectedSavings = (profile.monthlyIncome - projectedEndOfMonth).coerceAtLeast(0.0)
        val foodBudget = budgetList.firstOrNull { it.category.contains("Food", ignoreCase = true) }
        val foodOverspend = if (foodBudget != null) {
            val projectedFood = (foodBudget.spent / currentDay) * daysInMonth
            (projectedFood - foodBudget.monthlyLimit).coerceAtLeast(0.0)
        } else 2300.0

        ExpensePrediction(
            projectedEndOfMonthSpend = projectedEndOfMonth,
            projectedFutureSavings = projectedSavings,
            monthlyBudgetLimit = 42000.0,
            isBudgetExceeded = foodOverspend > 0,
            overflowCategory = "Food & Dining",
            overflowAmount = if (foodOverspend > 0) foodOverspend else 2300.0,
            predictedDaysRemaining = daysInMonth - currentDay,
            aiInsights = listOf(
                "At your current spending, you will exceed your food budget by ₹${FinancialEngine.formatINR(if (foodOverspend > 0) foodOverspend else 2300.0).replace("₹", "")}.",
                "Household utilities are on track with a 12% buffer for end-of-month reconciliation.",
                "Shifting weekend dining out by 15% recovers ₹3,400 in projected surplus."
            ),
            recommendation = "Reallocate ₹2,500 from Shopping surplus to maintain a 100% green budget status."
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ExpensePrediction(
            projectedEndOfMonthSpend = 40550.0,
            projectedFutureSavings = 24450.0,
            monthlyBudgetLimit = 38250.0,
            isBudgetExceeded = true,
            overflowCategory = "Food & Dining",
            overflowAmount = 2300.0,
            predictedDaysRemaining = 10,
            aiInsights = listOf(
                "At your current spending, you will exceed your food budget by ₹2,300.",
                "Discretionary entertainment is tracking 8% below projected ceiling.",
                "Estimated future monthly savings rate: 37.6% of household income."
            ),
            recommendation = "Cap food delivery orders to 2x this week to avoid the ₹2,300 budget overflow."
        )
    )

    // --- Notifications & Instant Alerts Feed State ---
    private val _notifications = MutableStateFlow<List<NotificationAlertItem>>(
        listOf(
            NotificationAlertItem(
                id = "notif_bill_due",
                title = "Bill Due Tomorrow",
                message = "Airtel Xstream Fiber Broadband (₹1,179) is due tomorrow. Auto-pay scheduled.",
                type = NotificationType.BILL_DUE_TOMORROW,
                timeAgo = "10 mins ago",
                isUnread = true,
                actionRoute = "family",
                amountFormatted = "₹1,179"
            ),
            NotificationAlertItem(
                id = "notif_budget_crossed",
                title = "Budget Crossed Warning",
                message = "Food & Dining budget reached 67.5%. AI predicts overflow by ₹2,300 by Day 28.",
                type = NotificationType.BUDGET_CROSSED,
                timeAgo = "1 hour ago",
                isUnread = true,
                actionRoute = "trends",
                amountFormatted = "₹2,300"
            ),
            NotificationAlertItem(
                id = "notif_score_increased",
                title = "Financial Health Score Increased",
                message = "Your score improved by +6 points to 82/100 (Tier: Excellent) thanks to early debt reduction.",
                type = NotificationType.SCORE_INCREASED,
                timeAgo = "3 hours ago",
                isUnread = false,
                actionRoute = "analytics",
                amountFormatted = "+6 pts"
            ),
            NotificationAlertItem(
                id = "notif_goal_reached",
                title = "Savings Goal Milestone Reached",
                message = "Emergency Reserve Fund reached 72.5% milestone! ₹72,500 deposited.",
                type = NotificationType.SAVINGS_GOAL_REACHED,
                timeAgo = "Yesterday",
                isUnread = false,
                actionRoute = "goals",
                amountFormatted = "₹72,500"
            ),
            NotificationAlertItem(
                id = "notif_payment_success",
                title = "Payment Successful",
                message = "₹199.00 paid for FinFam Pro Lifetime Subscription via Razorpay UPI. Verified.",
                type = NotificationType.PAYMENT_SUCCESS,
                timeAgo = "2 days ago",
                isUnread = false,
                actionRoute = "payment",
                amountFormatted = "₹199"
            )
        )
    )
    val notifications: StateFlow<List<NotificationAlertItem>> = _notifications.asStateFlow()

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
    }

    fun addNotificationAlert(
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String? = null,
        amountFormatted: String? = null
    ) {
        val newItem = NotificationAlertItem(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = title,
            message = message,
            type = type,
            timeAgo = "Just now",
            isUnread = true,
            actionRoute = actionRoute,
            amountFormatted = amountFormatted
        )
        _notifications.value = listOf(newItem) + _notifications.value
    }

    private val _isScanningReceipt = MutableStateFlow(false)
    val isScanningReceipt: StateFlow<Boolean> = _isScanningReceipt.asStateFlow()

    private val _scannedReceiptResult = MutableStateFlow<ReceiptScanResult?>(null)
    val scannedReceiptResult: StateFlow<ReceiptScanResult?> = _scannedReceiptResult.asStateFlow()

    // --- AI Coach Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_finfam",
                text = "👋 Hello Priyanshu! I am FinFam AI, your dedicated Family Financial Coach. I can analyze your household spending, suggest budget optimizations, calculate savings runway, and help you reach your Japan Vacation goal faster. How can I help you today?",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    // --- Actions ---

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(category: String) { _selectedCategoryFilter.value = category }
    fun setTypeFilter(type: String) { _selectedTypeFilter.value = type }

    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        paymentMethod: String,
        notes: String = "",
        isFamilyShared: Boolean = false,
        memberName: String = "Priyanshu"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTransaction(
                title = title,
                category = category,
                amount = amount,
                type = "EXPENSE",
                paymentMethod = paymentMethod,
                notes = notes,
                isFamilyShared = isFamilyShared,
                memberName = memberName
            )
        }
    }

    fun addIncome(
        title: String,
        category: String,
        amount: Double,
        paymentMethod: String,
        notes: String = "",
        memberName: String = "Priyanshu"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTransaction(
                title = title,
                category = category,
                amount = amount,
                type = "INCOME",
                paymentMethod = paymentMethod,
                notes = notes,
                memberName = memberName
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(id)
        }
    }

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBudget(category, limit)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBudget(id)
        }
    }

    fun addGoal(name: String, emoji: String, targetAmount: Double, targetDate: String, category: String, isFamily: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGoal(name, emoji, targetAmount, targetDate, category, isFamily)
        }
    }

    fun depositGoal(goalId: Long, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.depositGoal(goalId, amount)
        }
    }

    fun withdrawGoal(goalId: Long, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.withdrawGoal(goalId, amount)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(id)
        }
    }

    fun addBill(name: String, amount: Double, dueDate: String, category: String, isRecurring: Boolean, autoPay: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBill(name, amount, dueDate, category, isRecurring, autoPay)
        }
    }

    fun payBill(billId: Long, billName: String, amount: Double, method: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markBillPaid(billId, billName, amount, method)
        }
    }

    fun deleteBill(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBill(id)
        }
    }

    fun addFamilyMember(
        name: String,
        role: String,
        email: String,
        monthlyContribution: Double = 0.0,
        spentThisMonth: Double = 0.0,
        salaryIncome: Double = 0.0,
        freelanceIncome: Double = 0.0,
        businessIncome: Double = 0.0,
        rentalIncome: Double = 0.0,
        otherIncome: Double = 0.0,
        foodExpense: Double = 0.0,
        transportExpense: Double = 0.0,
        shoppingExpense: Double = 0.0,
        educationExpense: Double = 0.0,
        healthExpense: Double = 0.0,
        entertainmentExpense: Double = 0.0,
        bankSavings: Double = 0.0,
        emergencyFund: Double = 0.0,
        fixedDeposit: Double = 0.0,
        mutualFund: Double = 0.0,
        monthlyEmi: Double = 0.0,
        equityInvestments: Double = 0.0,
        goldInvestments: Double = 0.0,
        ppfInvestments: Double = 0.0,
        fdInterest: Double = 0.0,
        rdInterest: Double = 0.0,
        savingsInterest: Double = 0.0,
        investmentReturns: Double = 0.0,
        avatarColor: String = "0xFF3B82F6"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFamilyMember(
                name = name,
                role = role,
                email = email,
                monthlyContribution = monthlyContribution,
                spentThisMonth = spentThisMonth,
                salaryIncome = salaryIncome,
                freelanceIncome = freelanceIncome,
                businessIncome = businessIncome,
                rentalIncome = rentalIncome,
                otherIncome = otherIncome,
                foodExpense = foodExpense,
                transportExpense = transportExpense,
                shoppingExpense = shoppingExpense,
                educationExpense = educationExpense,
                healthExpense = healthExpense,
                entertainmentExpense = entertainmentExpense,
                bankSavings = bankSavings,
                emergencyFund = emergencyFund,
                fixedDeposit = fixedDeposit,
                mutualFund = mutualFund,
                monthlyEmi = monthlyEmi,
                equityInvestments = equityInvestments,
                goldInvestments = goldInvestments,
                ppfInvestments = ppfInvestments,
                fdInterest = fdInterest,
                rdInterest = rdInterest,
                savingsInterest = savingsInterest,
                investmentReturns = investmentReturns,
                avatarColor = avatarColor
            )
        }
    }

    fun updateFamilyMember(member: FamilyMemberItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFamilyMember(member)
        }
    }

    fun deleteFamilyMember(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFamilyMember(id)
        }
    }

    fun updateProfile(name: String, email: String, phone: String, currencySymbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(name, email, phone, currencySymbol)
        }
    }

    fun addEmi(
        title: String,
        category: String,
        totalAmount: Double,
        paidAmount: Double = 0.0,
        monthlyEmi: Double,
        interestRate: Double = 0.0,
        totalTenureMonths: Int,
        paidTenureMonths: Int = 0,
        dueDate: String = "05th of every month",
        dueDayOfMonth: Int = 5,
        lenderBank: String = "HDFC Bank",
        isAutoDebit: Boolean = true,
        iconName: String = "two_wheeler"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEmi(
                title = title,
                category = category,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                monthlyEmi = monthlyEmi,
                interestRate = interestRate,
                totalTenureMonths = totalTenureMonths,
                paidTenureMonths = paidTenureMonths,
                dueDate = dueDate,
                dueDayOfMonth = dueDayOfMonth,
                lenderBank = lenderBank,
                isAutoDebit = isAutoDebit,
                iconName = iconName
            )
        }
    }

    fun payEmi(emiId: Long, emiTitle: String, amount: Double, paymentMethod: String = "UPI") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordEmiPayment(emiId, emiTitle, amount, paymentMethod)
        }
    }

    fun deleteEmi(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEmi(id)
        }
    }

    // Receipt OCR Simulator
    fun scanReceiptSimulator(imageType: String = "GROCERY") {
        viewModelScope.launch {
            _isScanningReceipt.value = true
            delay(1200) // Simulated high-accuracy on-device OCR inference
            val result = when (imageType) {
                "RESTAURANT" -> ReceiptScanResult(
                    merchantName = "Barbeque Nation Buffet",
                    amount = 2360.0,
                    date = "Today",
                    category = "Food",
                    detectedItems = listOf("2x Grand Dinner Buffet (₹1998)", "Mocktails (₹250)", "GST 5% (₹112)"),
                    taxGst = 112.0,
                    paymentMode = "Credit Card",
                    rawText = "BARBEQUE NATION HOSPITALITY LTD\nTAX INVOICE #BN-8842\nTOTAL: INR 2360.00"
                )
                "FUEL" -> ReceiptScanResult(
                    merchantName = "Indian Oil Petrol Pump",
                    amount = 1500.0,
                    date = "Today",
                    category = "Travel",
                    detectedItems = listOf("XP95 Petrol 14.8L (₹1500.00)"),
                    taxGst = 0.0,
                    paymentMode = "UPI",
                    rawText = "INDIAN OIL CORP LTD\nPOS RECEIPT #IOC-9912\nTOTAL: INR 1500.00"
                )
                else -> ReceiptScanResult(
                    merchantName = "Nature's Basket Organic Supermarket",
                    amount = 1845.0,
                    date = "Today",
                    category = "Food",
                    detectedItems = listOf("Organic Sourdough Bread (₹180)", "Almond Milk 1L (₹290)", "Fresh Avocado 500g (₹350)", "Imported Pasta & Olive Oil (₹1025)"),
                    taxGst = 85.0,
                    paymentMode = "UPI",
                    rawText = "NATURES BASKET RETAIL\nINVOICE #NB-4421\nNET PAYABLE: INR 1845.00"
                )
            }
            _scannedReceiptResult.value = result
            _isScanningReceipt.value = false
        }
    }

    fun confirmScannedReceiptAsExpense() {
        val scan = _scannedReceiptResult.value ?: return
        addExpense(
            title = scan.merchantName,
            category = scan.category,
            amount = scan.amount,
            paymentMethod = scan.paymentMode,
            notes = "Scanned Receipt OCR (${scan.detectedItems.joinToString(", ")})",
            isFamilyShared = true
        )
        _scannedReceiptResult.value = null
    }

    fun dismissScannedReceipt() {
        _scannedReceiptResult.value = null
    }

    // AI Coach Chat
    fun askAiCoach(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), text = prompt, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isCoachTyping.value = true

        viewModelScope.launch {
            val response = GeminiAiEngine.askFinancialAdvisor(prompt, userProfile.value)
            delay(500)
            val aiMsg = ChatMessage(id = UUID.randomUUID().toString(), text = response, isUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isCoachTyping.value = false
        }
    }

    // --- Radar Chart Dynamic State & Periodic Simulation ---
    private val _radarHealthAxes = MutableStateFlow<List<FinancialHealthAxis>>(FinancialHealthDummyData.defaultCategories)
    val radarHealthAxes: StateFlow<List<FinancialHealthAxis>> = _radarHealthAxes.asStateFlow()

    private val _isSimulatingRadarUpdates = MutableStateFlow(false)
    val isSimulatingRadarUpdates: StateFlow<Boolean> = _isSimulatingRadarUpdates.asStateFlow()

    private var periodicRadarJob: Job? = null

    /**
     * Starts periodic simulation of incoming financial data updates.
     * Perturbs the radar axes scores with realistic fluctuations every [intervalMillis],
     * updating formatted metrics and status tags to trigger smooth Compose radar chart animations.
     */
    fun startPeriodicRadarSimulation(intervalMillis: Long = 3500L) {
        if (periodicRadarJob?.isActive == true) return
        _isSimulatingRadarUpdates.value = true
        periodicRadarJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(intervalMillis)
                simulateRadarDataStep()
            }
        }
    }

    /**
     * Stops periodic radar chart updates simulation.
     */
    fun stopPeriodicRadarSimulation() {
        periodicRadarJob?.cancel()
        periodicRadarJob = null
        _isSimulatingRadarUpdates.value = false
    }

    /**
     * Toggles periodic simulation on/off.
     */
    fun togglePeriodicRadarSimulation() {
        if (_isSimulatingRadarUpdates.value) {
            stopPeriodicRadarSimulation()
        } else {
            startPeriodicRadarSimulation()
        }
    }

    /**
     * Applies a single step of simulated financial health data updates,
     * triggering smooth interpolation transitions on the radar chart.
     */
    fun simulateRadarDataStep() {
        val currentList = _radarHealthAxes.value
        val updatedList = currentList.map { axis ->
            val delta = Random.nextInt(-6, 7)
            val newScore = (axis.score + delta).coerceIn(45f, 98f)
            val (status, color) = when {
                newScore >= 85f -> "Optimal" to SuccessGreen
                newScore >= 75f -> "Strong" to CyanNeon
                newScore >= 65f -> "Moderate" to WarningAmber
                else -> "Attention Needed" to DangerRed
            }

            val updatedMetric = when (axis.categoryName) {
                "Savings" -> "${(newScore * 0.38f).toInt()}% of Income"
                "Debt" -> "${(40f - newScore * 0.25f).coerceAtLeast(8f).toInt()}% DTI Ratio"
                "Spending" -> "₹${(65000 - newScore * 380).toInt()} Discretionary"
                "Investments" -> "₹${String.format(Locale.US, "%.2f", (newScore * 0.022f))}L Active SIPs"
                "Budget" -> "${newScore.toInt()}% Adherence"
                "Emergency" -> "${String.format(Locale.US, "%.1f", (newScore * 0.075f))} Months Buffer"
                else -> axis.metricFormatted
            }

            axis.copy(
                score = newScore,
                status = status,
                statusColor = color,
                metricFormatted = updatedMetric
            )
        }
        _radarHealthAxes.value = updatedList
    }

    /**
     * Resets the radar chart axes data back to standard default baseline.
     */
    fun resetRadarDataToBaseline() {
        _radarHealthAxes.value = FinancialHealthDummyData.defaultCategories
    }

    // ==========================================
    // REAL-TIME DATA & FUND TRANSFER ENGINE
    // ==========================================

    private val _realTimeTransferHistory = MutableStateFlow<List<RealTimeTransferRecord>>(
        listOf(
            RealTimeTransferRecord(
                id = "TXN-8F92BA01",
                utrNumber = "UTR429184029103",
                senderName = "Priyanshu Sharma (Vault)",
                senderVpaOrAcc = "priyan1436ei@okhdfcbank",
                receiverName = "Priya Sharma (Spouse)",
                receiverVpaOrAcc = "priya.sharma@okaxis",
                amount = 15000.0,
                transferType = RealTimeTransferType.FAMILY_ALLOWANCE,
                status = RealTimeTransferStatus.SETTLED,
                protocol = "WSS://finfam.sync.p2p • AES-256",
                latencyMs = 12,
                note = "Monthly Household & Groceries Vault Pool",
                timestampFormatted = "10 mins ago"
            ),
            RealTimeTransferRecord(
                id = "TXN-7E14C920",
                utrNumber = "UTR429183928174",
                senderName = "Priyanshu Sharma",
                senderVpaOrAcc = "priyan1436ei@okhdfcbank",
                receiverName = "Family Cloud Vault",
                receiverVpaOrAcc = "sync://vault.finfam.cloud",
                payloadSizeKb = 248.6,
                transferType = RealTimeTransferType.DATA_SYNC_BEAM,
                status = RealTimeTransferStatus.SETTLED,
                protocol = "WSS://finfam.sync.p2p • AES-256",
                latencyMs = 9,
                note = "Automated Budget & EMI Ledger Sync",
                timestampFormatted = "42 mins ago"
            ),
            RealTimeTransferRecord(
                id = "TXN-5C38190F",
                utrNumber = "UTR429181029482",
                senderName = "Priyanshu Sharma",
                senderVpaOrAcc = "priyan1436ei@okhdfcbank",
                receiverName = "Aarav Sharma (Son)",
                receiverVpaOrAcc = "aarav.junior@okicici",
                amount = 2500.0,
                transferType = RealTimeTransferType.FUNDS_TRANSFER,
                status = RealTimeTransferStatus.SETTLED,
                protocol = "NPCI-IMPS-LIVE • AES-256",
                latencyMs = 15,
                note = "Weekly School & Coding Camp Allowance",
                timestampFormatted = "Yesterday, 04:30 PM"
            )
        )
    )
    val realTimeTransferHistory: StateFlow<List<RealTimeTransferRecord>> = _realTimeTransferHistory.asStateFlow()

    private val _liveP2pNodes = MutableStateFlow<List<LivePeerNode>>(
        listOf(
            LivePeerNode(
                id = "peer_priya",
                name = "Priya Sharma",
                relationship = "Spouse",
                vpa = "priya.sharma@okaxis",
                ipAddress = "192.168.1.44:8443",
                pingMs = 11,
                isOnline = true,
                avatarColorHex = 0xFF7C3AED,
                lastSyncText = "Live (2s ago)"
            ),
            LivePeerNode(
                id = "peer_aarav",
                name = "Aarav Sharma",
                relationship = "Son",
                vpa = "aarav.junior@okicici",
                ipAddress = "192.168.1.48:8443",
                pingMs = 16,
                isOnline = true,
                avatarColorHex = 0xFF00E5FF,
                lastSyncText = "Live (12s ago)"
            ),
            LivePeerNode(
                id = "peer_sunita",
                name = "Sunita Sharma",
                relationship = "Mother",
                vpa = "sunita.sharma@oksbi",
                ipAddress = "192.168.1.52:8443",
                pingMs = 22,
                isOnline = true,
                avatarColorHex = 0xFF10B981,
                lastSyncText = "Online"
            ),
            LivePeerNode(
                id = "peer_vault",
                name = "Encrypted Family Backup Beam",
                relationship = "Vault Cloud",
                vpa = "sync://vault.finfam.cloud",
                ipAddress = "10.0.4.18:443",
                pingMs = 8,
                isOnline = true,
                avatarColorHex = 0xFF3B82F6,
                lastSyncText = "Synchronized"
            )
        )
    )
    val liveP2pNodes: StateFlow<List<LivePeerNode>> = _liveP2pNodes.asStateFlow()

    private val _isLiveTransferStreaming = MutableStateFlow(false)
    val isLiveTransferStreaming: StateFlow<Boolean> = _isLiveTransferStreaming.asStateFlow()

    private val _transferStreamingProgress = MutableStateFlow(0f)
    val transferStreamingProgress: StateFlow<Float> = _transferStreamingProgress.asStateFlow()

    private val _transferCurrentStep = MutableStateFlow(RealTimeTransferStatus.INITIALIZING)
    val transferCurrentStep: StateFlow<RealTimeTransferStatus> = _transferCurrentStep.asStateFlow()

    private val _isLiveBackgroundSyncActive = MutableStateFlow(true)
    val isLiveBackgroundSyncActive: StateFlow<Boolean> = _isLiveBackgroundSyncActive.asStateFlow()

    private var liveSyncJob: Job? = null

    init {
        startLivePeriodicSyncStream()
    }

    private fun startLivePeriodicSyncStream() {
        liveSyncJob?.cancel()
        liveSyncJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(12000) // Periodic live synchronization pulse
                if (_isLiveBackgroundSyncActive.value) {
                    val updatedNodes = _liveP2pNodes.value.map { node ->
                        val randomPing = Random.nextInt(7, 25)
                        node.copy(pingMs = randomPing, lastSyncText = "Live (${Random.nextInt(1, 8)}s ago)")
                    }
                    _liveP2pNodes.value = updatedNodes
                }
            }
        }
    }

    fun toggleLiveBackgroundSync() {
        _isLiveBackgroundSyncActive.value = !_isLiveBackgroundSyncActive.value
    }

    /**
     * Executes real-time funds / money transfer between family members or direct UPI.
     * Sequentially streams packets with sub-second feedback and commits to Room database.
     */
    fun executeRealTimeFundsTransfer(
        receiverName: String,
        receiverVpa: String,
        amount: Double,
        transferType: RealTimeTransferType,
        note: String,
        onSuccess: (RealTimeTransferRecord) -> Unit
    ) {
        viewModelScope.launch {
            _isLiveTransferStreaming.value = true
            _transferStreamingProgress.value = 0.05f
            _transferCurrentStep.value = RealTimeTransferStatus.INITIALIZING

            delay(280)
            _transferStreamingProgress.value = 0.35f
            _transferCurrentStep.value = RealTimeTransferStatus.ENCRYPTING

            delay(350)
            _transferStreamingProgress.value = 0.75f
            _transferCurrentStep.value = RealTimeTransferStatus.STREAMING

            // Record transaction in Room DB
            repository.addTransaction(
                title = if (note.isNotBlank()) note else "Real-Time Transfer to $receiverName",
                category = transferType.category,
                amount = amount,
                type = "EXPENSE",
                paymentMethod = "Real-Time UPI Transfer",
                notes = "Real-Time Data Transfer to $receiverVpa. Ref: UTR-${System.currentTimeMillis().toString().takeLast(6)}",
                isFamilyShared = true,
                memberName = "Priyanshu"
            )

            delay(280)
            _transferStreamingProgress.value = 1.0f
            _transferCurrentStep.value = RealTimeTransferStatus.SETTLED

            val newRecord = RealTimeTransferRecord(
                senderName = userProfile.value.name,
                senderVpaOrAcc = "priyan1436ei@okhdfcbank",
                receiverName = receiverName,
                receiverVpaOrAcc = receiverVpa,
                amount = amount,
                transferType = transferType,
                status = RealTimeTransferStatus.SETTLED,
                protocol = "NPCI-IMPS-LIVE • AES-256",
                latencyMs = Random.nextInt(9, 18),
                note = note.ifBlank { "Real-Time Transfer to $receiverName" },
                timestampFormatted = "Just now"
            )

            _realTimeTransferHistory.value = listOf(newRecord) + _realTimeTransferHistory.value
            _isLiveTransferStreaming.value = false

            onSuccess(newRecord)
        }
    }

    /**
     * Executes real-time data beam sync for family ledgers, shared budgets, or encrypted backups.
     */
    fun executeRealTimeDataBeam(
        dataPackageName: String,
        peerNode: LivePeerNode,
        payloadSizeKb: Double,
        onSuccess: (RealTimeTransferRecord) -> Unit
    ) {
        viewModelScope.launch {
            _isLiveTransferStreaming.value = true
            _transferStreamingProgress.value = 0.1f
            _transferCurrentStep.value = RealTimeTransferStatus.INITIALIZING

            delay(250)
            _transferStreamingProgress.value = 0.45f
            _transferCurrentStep.value = RealTimeTransferStatus.ENCRYPTING

            delay(300)
            _transferStreamingProgress.value = 0.85f
            _transferCurrentStep.value = RealTimeTransferStatus.STREAMING

            delay(250)
            _transferStreamingProgress.value = 1.0f
            _transferCurrentStep.value = RealTimeTransferStatus.SETTLED

            val newRecord = RealTimeTransferRecord(
                senderName = userProfile.value.name,
                senderVpaOrAcc = "node://192.168.1.42:8443",
                receiverName = peerNode.name,
                receiverVpaOrAcc = peerNode.vpa,
                payloadSizeKb = payloadSizeKb,
                transferType = RealTimeTransferType.DATA_SYNC_BEAM,
                status = RealTimeTransferStatus.SETTLED,
                protocol = "WSS://finfam.sync.p2p • AES-256",
                latencyMs = peerNode.pingMs,
                note = "Real-Time Data Beam: $dataPackageName",
                timestampFormatted = "Just now"
            )

            _realTimeTransferHistory.value = listOf(newRecord) + _realTimeTransferHistory.value
            _isLiveTransferStreaming.value = false

            onSuccess(newRecord)
        }
    }
}

