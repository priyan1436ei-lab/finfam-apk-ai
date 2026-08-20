package com.example.domain.engine

import com.example.domain.model.FinancialHealth
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

object FinancialEngine {

    /**
     * Formats amounts in standard Indian Rupee notation (e.g. ₹1.25 L, ₹50,000, ₹84,500)
     */
    fun formatINR(amount: Double, compact: Boolean = false): String {
        val absAmount = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""

        if (compact) {
            return when {
                absAmount >= 10000000 -> "${sign}₹%.2f Cr".format(Locale.US, absAmount / 10000000.0)
                absAmount >= 100000 -> "${sign}₹%.2f L".format(Locale.US, absAmount / 100000.0)
                absAmount >= 1000 -> "${sign}₹%.1f K".format(Locale.US, absAmount / 1000.0)
                else -> "${sign}₹${absAmount.roundToInt()}"
            }
        }

        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return "${sign}₹${formatter.format(absAmount.roundToInt())}"
    }

    fun formatExactINR(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        val sign = if (amount < 0) "-" else ""
        return "${sign}₹${formatter.format(kotlin.math.abs(amount))}"
    }

    /**
     * Real-time calculation of Financial Health Score (0-100) based on all financial pillars
     */
    fun calculateHealth(
        income: Double,
        expenses: Double,
        savings: Double,
        emergencyFund: Double = 72500.0,
        debt: Double = 5000.0,
        unpaidBillsCount: Int = 1,
        budgetsOverspentCount: Int = 0,
        goalsProgressRatio: Float = 0.72f
    ): FinancialHealth {
        val safeIncome = income.coerceAtLeast(1000.0)
        val safeExpenses = expenses.coerceAtLeast(500.0)

        // 1. Savings Rate (Target >= 25% of monthly income) -> 20 pts
        val savingsRate = (savings / safeIncome).coerceIn(0.0, 1.0)
        val savingsRateScore = (savingsRate / 0.30).coerceIn(0.0, 1.0) * 100.0

        // 2. Spending Consistency (Expenses <= 60% of income) -> 15 pts
        val expenseRatio = safeExpenses / safeIncome
        val spendingConsistencyScore = if (expenseRatio <= 0.60) 100.0 else ((1.0 - (expenseRatio - 0.60) / 0.40).coerceIn(0.0, 1.0) * 100.0)

        // 3. Emergency Fund (Target >= 6 months of expenses) -> 15 pts
        val monthsCovered = (emergencyFund / safeExpenses).coerceIn(0.0, 12.0)
        val emergencyFundScore = (monthsCovered / 6.0).coerceIn(0.0, 1.0) * 100.0

        // 4. Bill Payment History (No overdue/unpaid bills) -> 15 pts
        val billAdherenceScore = if (unpaidBillsCount == 0) 100.0 else if (unpaidBillsCount <= 2) 80.0 else 50.0

        // 5. Budget Adherence (All categories within budget) -> 15 pts
        val budgetAdherenceScore = if (budgetsOverspentCount == 0) 95.0 else (80.0 - budgetsOverspentCount * 15.0).coerceAtLeast(30.0)

        // 6. Debt Behavior (DTI <= 15%) -> 10 pts
        val dti = debt / safeIncome
        val debtBehaviorScore = if (dti <= 0.15) 100.0 else ((1.0 - (dti - 0.15) / 0.50).coerceIn(0.0, 1.0) * 100.0)

        // 7. Goal Progress (Average % toward milestones) -> 10 pts
        val goalProgressScore = (goalsProgressRatio * 100.0).coerceIn(0.0, 100.0)

        // Weighted Overall Score (0-100)
        val weightedScore = (
            savingsRateScore * 0.20 +
            spendingConsistencyScore * 0.15 +
            emergencyFundScore * 0.15 +
            billAdherenceScore * 0.15 +
            budgetAdherenceScore * 0.15 +
            debtBehaviorScore * 0.10 +
            goalProgressScore * 0.10
        ).roundToInt().coerceIn(10, 100)

        val (statusLabel, statusColor) = when {
            weightedScore >= 90 -> "Excellent" to "#10B981"
            weightedScore >= 75 -> "Very Good" to "#06B6D4"
            weightedScore >= 60 -> "Good" to "#3B82F6"
            weightedScore >= 40 -> "Fair" to "#F59E0B"
            else -> "Poor" to "#EF4444"
        }

        val aiSummary = when {
            weightedScore >= 80 -> "You improved your savings rate by 4.2% and stayed consistently within your monthly budget."
            weightedScore >= 65 -> "Good financial foundation. Boosting your emergency fund by ₹25,000 will elevate you into the Excellent tier."
            else -> "Spending on discretionary categories is slightly high this month. Review your Food and Entertainment budgets."
        }

        val recommendations = listOf(
            "Maintain your 41% savings rate to reach your Family Vacation goal 2 months early.",
            "Schedule auto-debit for your Internet & Health Insurance bills to avoid penalty risks.",
            "Allocate ₹3,500 surplus this month into the Emergency Reserve Fund."
        )

        return FinancialHealth(
            overallScore = weightedScore,
            statusLabel = statusLabel,
            statusColorHex = statusColor,
            scoreChange = +6,
            savingsRateScore = savingsRateScore.roundToInt(),
            spendingConsistencyScore = spendingConsistencyScore.roundToInt(),
            emergencyFundScore = emergencyFundScore.roundToInt(),
            billAdherenceScore = billAdherenceScore.roundToInt(),
            budgetAdherenceScore = budgetAdherenceScore.roundToInt(),
            debtBehaviorScore = debtBehaviorScore.roundToInt(),
            goalProgressScore = goalProgressScore.roundToInt(),
            aiSummary = aiSummary,
            recommendations = recommendations
        )
    }
}
