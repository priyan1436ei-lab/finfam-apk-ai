package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.FinGuardRepository
import com.example.domain.engine.FinancialEngine
import com.example.domain.engine.GeminiAiEngine
import com.example.domain.model.BillItem
import com.example.domain.model.BudgetItem
import com.example.domain.model.ChatMessage
import com.example.domain.model.FamilyMemberItem
import com.example.domain.model.FinancialHealth
import com.example.domain.model.GoalItem
import com.example.domain.model.ReceiptScanResult
import com.example.domain.model.TransactionItem
import com.example.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

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

    // --- Search & Filter State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("ALL") // ALL, EXPENSE, INCOME, FAMILY
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    // --- Receipt OCR Scanner State ---
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

    fun addFamilyMember(name: String, role: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFamilyMember(name, role, email)
        }
    }

    fun updateProfile(name: String, email: String, phone: String, currencySymbol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(name, email, phone, currencySymbol)
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
}
