package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Priyanshu Sharma",
    val email: String = "priyan1436ei@gmail.com",
    val phone: String = "+91 98765 43210",
    val currencySymbol: String = "₹",
    val totalBalance: Double = 84500.0,
    val monthlyIncome: Double = 65000.0,
    val monthlyExpenses: Double = 38250.0,
    val monthlySavings: Double = 26750.0,
    val emergencyFund: Double = 72500.0,
    val healthScore: Int = 82,
    val previousHealthScore: Int = 76,
    val isPremium: Boolean = false,
    val premiumTier: String = "FREE",
    val premiumValidUntil: String = "N/A",
    val familyId: String = "fam_sharma_001",
    val familyName: String = "Sharma Family Vault",
    val isBiometricEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Food, Travel, Shopping, Bills, Entertainment, Education, Healthcare, Rent, Subscriptions, Salary, Freelance, Others
    val amount: Double,
    val type: String = "EXPENSE", // "EXPENSE", "INCOME", "TRANSFER", "PAYMENT"
    val isCredit: Boolean = false,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "UPI", // "UPI", "Credit Card", "Debit Card", "Net Banking", "Cash"
    val notes: String = "",
    val receiptUrl: String? = null,
    val isFamilyShared: Boolean = false,
    val memberName: String = "Priyanshu",
    val iconName: String = "wallet",
    val riskStatus: String = "VERIFIED"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val spent: Double,
    val month: String = "August 2026",
    val iconName: String = "category",
    val alertThreshold80: Boolean = true,
    val alertThreshold90: Boolean = true,
    val alertThreshold100: Boolean = true
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🎯",
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val category: String = "Savings",
    val isFamilyGoal: Boolean = false
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDate: String,
    val dueTimestamp: Long,
    val category: String = "Utilities",
    val isRecurring: Boolean = true,
    val isPaid: Boolean = false,
    val reminderDays: Int = 3,
    val autoPayEnabled: Boolean = false
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "Member", // Father, Mother, Son, Daughter, Admin, Parent, Member
    val email: String,
    val avatarColor: String = "0xFF3B82F6",
    val monthlyContribution: Double = 0.0,
    val spentThisMonth: Double = 0.0,
    // Incomes
    val salaryIncome: Double = 0.0,
    val freelanceIncome: Double = 0.0,
    val businessIncome: Double = 0.0,
    val rentalIncome: Double = 0.0,
    val otherIncome: Double = 0.0,
    // Expenses
    val foodExpense: Double = 0.0,
    val transportExpense: Double = 0.0,
    val shoppingExpense: Double = 0.0,
    val educationExpense: Double = 0.0,
    val healthExpense: Double = 0.0,
    val entertainmentExpense: Double = 0.0,
    // Savings
    val bankSavings: Double = 0.0,
    val emergencyFund: Double = 0.0,
    val fixedDeposit: Double = 0.0,
    val mutualFund: Double = 0.0,
    // Debt & EMI
    val monthlyEmi: Double = 0.0,
    // Investments
    val equityInvestments: Double = 0.0,
    val goldInvestments: Double = 0.0,
    val ppfInvestments: Double = 0.0,
    // Interest Income
    val fdInterest: Double = 0.0,
    val rdInterest: Double = 0.0,
    val savingsInterest: Double = 0.0,
    val investmentReturns: Double = 0.0
)

@Entity(tableName = "payment_orders")
data class PaymentOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val paymentId: String?,
    val signature: String?,
    val userId: String = "user_priyanshu_sharma",
    val planId: String,
    val planTitle: String,
    val amount: Double,
    val currency: String = "INR",
    val status: String, // CREATED, PENDING, SUCCESS, FAILED, CANCELLED, REFUND_INITIATED, REFUNDED
    val paymentMethod: String = "UPI",
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val refundStatus: String? = null,
    val refundId: String? = null,
    val failureReason: String? = null
)

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val amount: Double,
    val date: String,
    val category: String,
    val rawText: String,
    val confidenceScore: Int = 95,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emis")
data class EmiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Vehicle", // Vehicle, Electronics, Mobile, Education, Personal, Home
    val totalAmount: Double,
    val paidAmount: Double,
    val monthlyEmi: Double,
    val interestRate: Double = 0.0,
    val totalTenureMonths: Int,
    val paidTenureMonths: Int,
    val dueDate: String = "05th of every month",
    val dueDayOfMonth: Int = 5,
    val lenderBank: String = "HDFC Bank",
    val isAutoDebit: Boolean = true,
    val isPaidThisMonth: Boolean = false,
    val lastPaymentDate: String? = null,
    val iconName: String = "two_wheeler"
)

