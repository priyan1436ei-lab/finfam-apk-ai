package com.example.domain.model

data class UserProfile(
    val id: Int = 1,
    val name: String = "Priyanshu Sharma",
    val email: String = "priyan1436ei@gmail.com",
    val phone: String = "+91 98765 43210",
    val initials: String = "PS",
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
    val isNotificationsEnabled: Boolean = true,
    val unreadNotificationsCount: Int = 2
)

data class FinancialHealth(
    val overallScore: Int,
    val statusLabel: String, // Excellent, Very Good, Good, Fair, Poor
    val statusColorHex: String,
    val scoreChange: Int = 6,
    val savingsRateScore: Int, // 0-100
    val spendingConsistencyScore: Int,
    val emergencyFundScore: Int,
    val billAdherenceScore: Int,
    val budgetAdherenceScore: Int,
    val debtBehaviorScore: Int,
    val goalProgressScore: Int,
    val aiSummary: String,
    val recommendations: List<String>
)

data class TransactionItem(
    val id: Long = 0,
    val title: String,
    val category: String,
    val amount: Double,
    val type: String = "EXPENSE", // "EXPENSE", "INCOME", "TRANSFER", "PAYMENT"
    val isCredit: Boolean,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "UPI",
    val notes: String = "",
    val receiptUrl: String? = null,
    val isFamilyShared: Boolean = false,
    val memberName: String = "Priyanshu",
    val iconName: String = "wallet",
    val riskStatus: String = "VERIFIED"
)

data class BudgetItem(
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val spent: Double,
    val month: String = "August 2026",
    val iconName: String = "category",
    val alertThreshold80: Boolean = true,
    val alertThreshold90: Boolean = true,
    val alertThreshold100: Boolean = true
) {
    val utilizationPercentage: Float
        get() = if (monthlyLimit > 0) (spent / monthlyLimit).toFloat().coerceIn(0f, 2f) else 0f

    val remainingAmount: Double
        get() = (monthlyLimit - spent).coerceAtLeast(0.0)

    val isOverspent: Boolean
        get() = spent > monthlyLimit
}

data class GoalItem(
    val id: Long = 0,
    val name: String,
    val emoji: String = "🎯",
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val category: String = "Savings",
    val isFamilyGoal: Boolean = false
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}

data class BillItem(
    val id: Long = 0,
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

data class FamilyMemberItem(
    val id: Long = 0,
    val name: String,
    val role: String = "Member", // Admin, Parent, Member
    val email: String,
    val avatarColor: String = "0xFF3B82F6",
    val monthlyContribution: Double = 0.0,
    val spentThisMonth: Double = 0.0
)

data class ReceiptScanResult(
    val merchantName: String,
    val amount: Double,
    val date: String,
    val category: String,
    val detectedItems: List<String>,
    val taxGst: Double,
    val paymentMode: String,
    val rawText: String,
    val confidence: Int = 96
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isTyping: Boolean = false
)

data class DepositPurpose(
    val id: String,
    val title: String,
    val tag: String,
    val description: String,
    val iconName: String
)

data class PaymentReceipt(
    val transactionId: String,
    val npciRefId: String,
    val purposeTitle: String,
    val amount: Double,
    val platformFee: Double,
    val gst: Double,
    val totalPaid: Double,
    val paymentMethod: String,
    val recipient: String,
    val timestamp: Long,
    val status: String = "SUCCESS",
    val securityVerificationStatus: String = "SHIELD 256-BIT NPCI VERIFIED"
)

enum class RiskLevel(val label: String) {
    LOW("LOW RISK"),
    MODERATE("MODERATE RISK"),
    HIGH("HIGH RISK"),
    CRITICAL("CRITICAL FRAUD")
}

enum class ScanType {
    UPI, PATTERNS, QR_CODE, RECEIPT, AD_LINK, SCREENSHOT
}

data class ScamAuditResult(
    val rawInput: String,
    val scanType: ScanType,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val matchedIndicators: List<String>,
    val recommendedAction: String,
    val detectedMerchant: String = "Unknown / Unverified",
    val pspProvider: String = "Unknown",
    val isTampered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class SimulationScenario(
    val id: String,
    val title: String,
    val icon: String,
    val salary: Double,
    val savings: Double,
    val loanAmount: Double,
    val interestRate: Double,
    val inflationRate: Double,
    val timelineYears: Int
)

data class SimulationMonthPoint(
    val month: Int,
    val netWorth: Double,
    val totalInvested: Double,
    val loanBalance: Double,
    val cashflow: Double
)

data class SimulationResult(
    val monthlyEmi: Double,
    val monthlyCashflow: Double,
    val projectedNetWorthAtEnd: Double,
    val breakEvenMonth: Int,
    val totalInterestPaid: Double,
    val points: List<SimulationMonthPoint>
)
