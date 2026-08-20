package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BillDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.PaymentOrderDao
import com.example.data.local.dao.ScanHistoryDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.model.BillEntity
import com.example.data.local.model.BudgetEntity
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
        ScanHistoryEntity::class
    ],
    version = 2,
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

            // Bills
            val initialBills = listOf(
                BillEntity(
                    name = "High-Speed Fiber Internet",
                    amount = 1199.0,
                    dueDate = "23 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 3,
                    category = "Internet",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = true
                ),
                BillEntity(
                    name = "HDFC Credit Card Statement",
                    amount = 8450.0,
                    dueDate = "25 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 5,
                    category = "Credit Card",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 3,
                    autoPayEnabled = false
                ),
                BillEntity(
                    name = "Family Health Insurance Premium",
                    amount = 3500.0,
                    dueDate = "28 Aug 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 8,
                    category = "Insurance",
                    isRecurring = true,
                    isPaid = false,
                    reminderDays = 5,
                    autoPayEnabled = true
                ),
                BillEntity(
                    name = "Netflix 4K Ultra Subscription",
                    amount = 649.0,
                    dueDate = "01 Sep 2026",
                    dueTimestamp = System.currentTimeMillis() + 86400000 * 12,
                    category = "Entertainment",
                    isRecurring = true,
                    isPaid = true,
                    reminderDays = 1,
                    autoPayEnabled = true
                )
            )
            initialBills.forEach { db.billDao().insertBill(it) }

            // Family Members
            val initialMembers = listOf(
                FamilyMemberEntity(
                    name = "Priyanshu Sharma",
                    role = "Admin",
                    email = "priyan1436ei@gmail.com",
                    avatarColor = "0xFF2563EB",
                    monthlyContribution = 45000.0,
                    spentThisMonth = 24350.0
                ),
                FamilyMemberEntity(
                    name = "Ananya Sharma",
                    role = "Parent",
                    email = "ananya.sharma@example.com",
                    avatarColor = "0xFF7C3AED",
                    monthlyContribution = 20000.0,
                    spentThisMonth = 11400.0
                ),
                FamilyMemberEntity(
                    name = "Aarav Sharma",
                    role = "Member",
                    email = "aarav.sharma@example.com",
                    avatarColor = "0xFF06B6D4",
                    monthlyContribution = 0.0,
                    spentThisMonth = 2500.0
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
        }
    }
}
