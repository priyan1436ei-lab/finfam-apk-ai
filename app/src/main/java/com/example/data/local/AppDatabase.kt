package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BillDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.EmiDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.PaymentOrderDao
import com.example.data.local.dao.ScanHistoryDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.model.BillEntity
import com.example.data.local.model.BudgetEntity
import com.example.data.local.model.EmiEntity
import com.example.data.local.model.FamilyMemberEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.PaymentOrderEntity
import com.example.data.local.model.ScanHistoryEntity
import com.example.data.local.model.TransactionEntity
import com.example.data.local.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        BillEntity::class,
        FamilyMemberEntity::class,
        PaymentOrderEntity::class,
        ScanHistoryEntity::class,
        EmiEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun billDao(): BillDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun paymentOrderDao(): PaymentOrderDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun emiDao(): EmiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finfam_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // User Profile
            db.userProfileDao().insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Priyanshu Sharma",
                    email = "priyan1436ei@gmail.com",
                    phone = "+91 98765 43210",
                    currencySymbol = "₹",
                    totalBalance = 84500.0,
                    monthlyIncome = 65000.0,
                    monthlyExpenses = 38250.0,
                    monthlySavings = 26750.0,
                    emergencyFund = 72500.0,
                    healthScore = 82,
                    previousHealthScore = 76,
                    isPremium = false,
                    premiumTier = "FREE",
                    premiumValidUntil = "N/A",
                    familyName = "Sharma Family Vault",
                    isBiometricEnabled = true,
                    isNotificationsEnabled = true
                )
            )

            // Initial Transactions
            val initialTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Salary (TechCorp)",
                    category = "Salary",
                    amount = 65000.0,
                    type = "INCOME",
                    isCredit = true,
                    date = "Today, 09:30 AM",
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    paymentMethod = "Direct Bank Transfer",
                    notes = "Monthly payroll credited",
                    iconName = "briefcase",
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "House Rent & Maintenance",
                    category = "Rent",
                    amount = 18000.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "Yesterday, 02:15 PM",
                    timestamp = System.currentTimeMillis() - 86400000,
                    paymentMethod = "UPI",
                    notes = "Landlord rental transfer",
                    iconName = "home",
                    isFamilyShared = true,
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Whole Foods & Groceries",
                    category = "Food",
                    amount = 5400.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "Yesterday, 06:45 PM",
                    timestamp = System.currentTimeMillis() - 86400000 - 3600000 * 4,
                    paymentMethod = "Credit Card",
                    notes = "Weekly family supermarket run",
                    iconName = "shopping_cart",
                    isFamilyShared = true,
                    memberName = "Ananya"
                ),
                TransactionEntity(
                    title = "Freelance UI Consultation",
                    category = "Freelance",
                    amount = 12500.0,
                    type = "INCOME",
                    isCredit = true,
                    date = "16 Aug 2026",
                    timestamp = System.currentTimeMillis() - 86400000 * 3,
                    paymentMethod = "UPI",
                    notes = "Fintech dashboard design milestone",
                    iconName = "palette",
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Electricity & Water Bill",
                    category = "Bills",
                    amount = 3200.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "15 Aug 2026",
                    timestamp = System.currentTimeMillis() - 86400000 * 4,
                    paymentMethod = "UPI",
                    notes = "State Electricity Board",
                    iconName = "bolt",
                    isFamilyShared = true,
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Family Dinner at Royal Orchid",
                    category = "Food",
                    amount = 2850.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "14 Aug 2026",
                    timestamp = System.currentTimeMillis() - 86400000 * 5,
                    paymentMethod = "Debit Card",
                    notes = "Weekend celebration",
                    iconName = "restaurant",
                    isFamilyShared = true,
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Uber & Fuel Refill",
                    category = "Travel",
                    amount = 2100.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "12 Aug 2026",
                    timestamp = System.currentTimeMillis() - 86400000 * 7,
                    paymentMethod = "UPI",
                    notes = "Commute expenses",
                    iconName = "car",
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Zerodha Nifty 50 Index SIP",
                    category = "Investment",
                    amount = 5000.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "10 Aug 2026",
                    timestamp = System.currentTimeMillis() - 86400000 * 9,
                    paymentMethod = "Auto-Debit",
                    notes = "Monthly wealth compounding",
                    iconName = "trending_up",
                    memberName = "Priyanshu"
                ),
                // July 2026 Historical Expenses
                TransactionEntity(
                    title = "House Rent & Maintenance (July)",
                    category = "Rent",
                    amount = 18000.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "01 Jul 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 45,
                    paymentMethod = "UPI",
                    notes = "July rent",
                    iconName = "home",
                    isFamilyShared = true,
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Supermarket & Organic Mart",
                    category = "Food",
                    amount = 8400.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "18 Jul 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 33,
                    paymentMethod = "Credit Card",
                    notes = "Monthly groceries",
                    iconName = "shopping_cart",
                    isFamilyShared = true,
                    memberName = "Ananya"
                ),
                TransactionEntity(
                    title = "Electricity & Water Bill (July)",
                    category = "Bills",
                    amount = 3600.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "15 Jul 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 35,
                    paymentMethod = "UPI",
                    notes = "Utilities",
                    iconName = "bolt",
                    isFamilyShared = true,
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Amazon Prime Day Electronics Sale",
                    category = "Shopping",
                    amount = 4900.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "12 Jul 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 38,
                    paymentMethod = "Credit Card",
                    notes = "Wireless noise canceling headphones",
                    iconName = "shopping_bag",
                    memberName = "Priyanshu"
                ),
                // June 2026 Historical Expenses
                TransactionEntity(
                    title = "House Rent & Maintenance (June)",
                    category = "Rent",
                    amount = 18000.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "01 Jun 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 75,
                    paymentMethod = "UPI",
                    notes = "June rent",
                    iconName = "home",
                    isFamilyShared = true,
                    memberName = "Priyanshu"
                ),
                TransactionEntity(
                    title = "Groceries & Farm Fresh",
                    category = "Food",
                    amount = 7600.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "14 Jun 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 65,
                    paymentMethod = "Credit Card",
                    notes = "Monthly ration",
                    iconName = "shopping_cart",
                    isFamilyShared = true,
                    memberName = "Ananya"
                ),
                TransactionEntity(
                    title = "Weekend Road Trip Fuel & Tolls",
                    category = "Travel",
                    amount = 2600.0,
                    type = "EXPENSE",
                    isCredit = false,
                    date = "22 Jun 2026",
                    timestamp = System.currentTimeMillis() - 86400000L * 58,
                    paymentMethod = "UPI",
                    notes = "Family getaway",
                    iconName = "car",
                    memberName = "Priyanshu"
                )
            )
            initialTransactions.forEach { db.transactionDao().insertTransaction(it) }

            // Budgets
            val initialBudgets = listOf(
                BudgetEntity(category = "Food", monthlyLimit = 8000.0, spent = 5400.0, iconName = "restaurant"),
                BudgetEntity(category = "Rent & Housing", monthlyLimit = 20000.0, spent = 18000.0, iconName = "home"),
                BudgetEntity(category = "Bills & Utilities", monthlyLimit = 6000.0, spent = 3200.0, iconName = "bolt"),
                BudgetEntity(category = "Travel & Fuel", monthlyLimit = 4000.0, spent = 2100.0, iconName = "car"),
                BudgetEntity(category = "Shopping & Clothing", monthlyLimit = 5000.0, spent = 2450.0, iconName = "shopping_bag"),
                BudgetEntity(category = "Entertainment & Subs", monthlyLimit = 3000.0, spent = 1600.0, iconName = "tv"),
                BudgetEntity(category = "Healthcare", monthlyLimit = 4000.0, spent = 800.0, iconName = "favorite")
            )
            initialBudgets.forEach { db.budgetDao().insertBudget(it) }

            // Goals
            val initialGoals = listOf(
                GoalEntity(
                    name = "Emergency Reserve Fund",
                    emoji = "🛡️",
                    targetAmount = 100000.0,
                    currentAmount = 72500.0,
                    targetDate = "Dec 2026",
                    category = "Safety",
                    isFamilyGoal = true
                ),
                GoalEntity(
                    name = "Family Vacation to Japan",
                    emoji = "✈️",
                    targetAmount = 250000.0,
                    currentAmount = 120000.0,
                    targetDate = "May 2027",
                    category = "Travel",
                    isFamilyGoal = true
                ),
                GoalEntity(
                    name = "EV Car Down Payment",
                    emoji = "🚗",
                    targetAmount = 300000.0,
                    currentAmount = 85000.0,
                    targetDate = "Mar 2028",
                    category = "Vehicle",
                    isFamilyGoal = false
                ),
                GoalEntity(
                    name = "Kids Higher Education Fund",
                    emoji = "🎓",
                    targetAmount = 500000.0,
                    currentAmount = 180000.0,
                    targetDate = "2032",
                    category = "Education",
                    isFamilyGoal = true
                )
            )
            initialGoals.forEach { db.goalDao().insertGoal(it) }

            // Bills Center (Electricity, Water, Gas, Mobile Recharge, DTH, Broadband, Credit Card Bills)
            val initialBills = listOf(
                BillEntity(
                    name = "Tata Power Electricity Bill",
                    amount = 2850.0,
                    dueDate = "24 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 3,
                    category = "Electricity",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = true
                ),
                BillEntity(
                    name = "Municipal Water Supply Board",
                    amount = 650.0,
                    dueDate = "28 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 7,
                    category = "Water",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 2,
                    autoPayEnabled = false
                ),
                BillEntity(
                    name = "Indane Piped Natural Gas (PNG)",
                    amount = 920.0,
                    dueDate = "29 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 8,
                    category = "Gas",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = true
                ),
                BillEntity(
                    name = "Jio 5G Family Mobile Recharge",
                    amount = 999.0,
                    dueDate = "26 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 5,
                    category = "Mobile Recharge",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 2,
                    autoPayEnabled = true
                ),
                BillEntity(
                    name = "Tata Play Ultra HD DTH",
                    amount = 450.0,
                    dueDate = "30 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 9,
                    category = "DTH",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = false
                ),
                BillEntity(
                    name = "Airtel Xstream Fiber Broadband (300 Mbps)",
                    amount = 1179.0,
                    dueDate = "23 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 2,
                    category = "Broadband",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = true
                ),
                BillEntity(
                    name = "HDFC Regalia Credit Card Statement",
                    amount = 12450.0,
                    dueDate = "25 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 4,
                    category = "Credit Card Bills",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = false
                )
            )
            initialBills.forEach { db.billDao().insertBill(it) }

            // Family Members (Father 40%, Mother 30%, Son 20%, Daughter 10%)
            val initialMembers = listOf(
                FamilyMemberEntity(
                    name = "Rajesh Sharma",
                    role = "Father",
                    email = "rajesh.sharma@example.com",
                    avatarColor = "0xFF10B981", // Emerald Green
                    monthlyContribution = 80000.0,
                    spentThisMonth = 32000.0,
                    salaryIncome = 70000.0,
                    rentalIncome = 10000.0,
                    foodExpense = 12000.0,
                    transportExpense = 6000.0,
                    shoppingExpense = 6000.0,
                    healthExpense = 8000.0,
                    bankSavings = 40000.0,
                    emergencyFund = 150000.0,
                    fixedDeposit = 200000.0,
                    mutualFund = 180000.0,
                    monthlyEmi = 18500.0,
                    equityInvestments = 250000.0,
                    goldInvestments = 120000.0,
                    ppfInvestments = 150000.0,
                    fdInterest = 14500.0,
                    savingsInterest = 3200.0,
                    investmentReturns = 28000.0
                ),
                FamilyMemberEntity(
                    name = "Sunita Sharma",
                    role = "Mother",
                    email = "sunita.sharma@example.com",
                    avatarColor = "0xFF06B6D4", // Electric Cyan
                    monthlyContribution = 60000.0,
                    spentThisMonth = 24000.0,
                    businessIncome = 45000.0,
                    freelanceIncome = 15000.0,
                    foodExpense = 8000.0,
                    shoppingExpense = 9000.0,
                    educationExpense = 7000.0,
                    bankSavings = 30000.0,
                    emergencyFund = 80000.0,
                    fixedDeposit = 120000.0,
                    mutualFund = 90000.0,
                    monthlyEmi = 0.0,
                    equityInvestments = 80000.0,
                    goldInvestments = 150000.0,
                    ppfInvestments = 50000.0,
                    fdInterest = 8400.0,
                    rdInterest = 4200.0,
                    savingsInterest = 2100.0,
                    investmentReturns = 11500.0
                ),
                FamilyMemberEntity(
                    name = "Priyanshu Sharma",
                    role = "Son",
                    email = "priyan1436ei@gmail.com",
                    avatarColor = "0xFF3B82F6", // Electric Blue
                    monthlyContribution = 40000.0,
                    spentThisMonth = 16000.0,
                    salaryIncome = 52000.0,
                    freelanceIncome = 13000.0,
                    foodExpense = 5000.0,
                    transportExpense = 3000.0,
                    shoppingExpense = 4500.0,
                    entertainmentExpense = 3500.0,
                    bankSavings = 25000.0,
                    emergencyFund = 72500.0,
                    fixedDeposit = 50000.0,
                    mutualFund = 85000.0,
                    monthlyEmi = 2999.0,
                    equityInvestments = 110000.0,
                    goldInvestments = 25000.0,
                    ppfInvestments = 30000.0,
                    fdInterest = 3500.0,
                    savingsInterest = 1800.0,
                    investmentReturns = 14200.0
                ),
                FamilyMemberEntity(
                    name = "Ananya Sharma",
                    role = "Daughter",
                    email = "ananya.sharma@example.com",
                    avatarColor = "0xFFA855F7", // Purple Neon
                    monthlyContribution = 20000.0,
                    spentThisMonth = 8000.0,
                    freelanceIncome = 18000.0,
                    otherIncome = 2000.0,
                    foodExpense = 2500.0,
                    transportExpense = 1500.0,
                    shoppingExpense = 2500.0,
                    entertainmentExpense = 1500.0,
                    bankSavings = 12000.0,
                    emergencyFund = 25000.0,
                    fixedDeposit = 25000.0,
                    mutualFund = 30000.0,
                    monthlyEmi = 0.0,
                    equityInvestments = 35000.0,
                    goldInvestments = 10000.0,
                    ppfInvestments = 10000.0,
                    fdInterest = 1750.0,
                    savingsInterest = 950.0,
                    investmentReturns = 3800.0
                )
            )
            initialMembers.forEach { db.familyMemberDao().insertMember(it) }

            // Payment Orders
            db.paymentOrderDao().insertOrder(
                PaymentOrderEntity(
                    orderId = "order_init_demo_001",
                    paymentId = "pay_live_001_demo",
                    signature = "sig_hmac_verified",
                    planId = "finfam_monthly_pro",
                    planTitle = "FinFam Pro",
                    amount = 199.0,
                    currency = "INR",
                    status = "SUCCESS",
                    paymentMethod = "Razorpay UPI",
                    date = "01 Aug 2026",
                    timestamp = System.currentTimeMillis() - 86400000 * 19
                )
            )

            // Initial EMIs
            val initialEmis = listOf(
                EmiEntity(
                    title = "Royal Enfield Hunter 350",
                    category = "Vehicle",
                    totalAmount = 185000.0,
                    paidAmount = 110000.0,
                    monthlyEmi = 6250.0,
                    interestRate = 8.5,
                    totalTenureMonths = 36,
                    paidTenureMonths = 18,
                    dueDate = "05th of every month",
                    dueDayOfMonth = 5,
                    lenderBank = "HDFC Bank",
                    isAutoDebit = true,
                    isPaidThisMonth = false,
                    lastPaymentDate = "05 Jul 2026",
                    iconName = "two_wheeler"
                ),
                EmiEntity(
                    title = "Apple MacBook Pro M3 (16GB)",
                    category = "Electronics",
                    totalAmount = 149900.0,
                    paidAmount = 99900.0,
                    monthlyEmi = 8325.0,
                    interestRate = 0.0,
                    totalTenureMonths = 18,
                    paidTenureMonths = 12,
                    dueDate = "10th of every month",
                    dueDayOfMonth = 10,
                    lenderBank = "Bajaj Finserv",
                    isAutoDebit = true,
                    isPaidThisMonth = true,
                    lastPaymentDate = "10 Aug 2026",
                    iconName = "laptop"
                ),
                EmiEntity(
                    title = "iPhone 15 Pro Titanium",
                    category = "Mobile",
                    totalAmount = 134900.0,
                    paidAmount = 67450.0,
                    monthlyEmi = 5620.0,
                    interestRate = 0.0,
                    totalTenureMonths = 24,
                    paidTenureMonths = 12,
                    dueDate = "15th of every month",
                    dueDayOfMonth = 15,
                    lenderBank = "ICICI Bank",
                    isAutoDebit = true,
                    isPaidThisMonth = false,
                    lastPaymentDate = "15 Jul 2026",
                    iconName = "smartphone"
                ),
                EmiEntity(
                    title = "Executive AI & Cloud Cert",
                    category = "Education",
                    totalAmount = 350000.0,
                    paidAmount = 140000.0,
                    monthlyEmi = 10500.0,
                    interestRate = 7.8,
                    totalTenureMonths = 48,
                    paidTenureMonths = 16,
                    dueDate = "20th of every month",
                    dueDayOfMonth = 20,
                    lenderBank = "SBI Education",
                    isAutoDebit = false,
                    isPaidThisMonth = false,
                    lastPaymentDate = "20 Jul 2026",
                    iconName = "school"
                ),
                EmiEntity(
                    title = "Home Living Room & Audio",
                    category = "Home",
                    totalAmount = 240000.0,
                    paidAmount = 180000.0,
                    monthlyEmi = 10000.0,
                    interestRate = 9.0,
                    totalTenureMonths = 24,
                    paidTenureMonths = 18,
                    dueDate = "25th of every month",
                    dueDayOfMonth = 25,
                    lenderBank = "Axis Bank",
                    isAutoDebit = true,
                    isPaidThisMonth = true,
                    lastPaymentDate = "25 Jul 2026",
                    iconName = "home"
                )
            )
            initialEmis.forEach { db.emiDao().insertEmi(it) }
        }
    }
}
