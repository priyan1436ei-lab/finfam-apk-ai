package com.example.domain.model

data class AmortizationRow(
    val periodIndex: Int,
    val periodLabel: String, // e.g. "Year 1" or "Month 1"
    val openingBalance: Double,
    val emiPaid: Double,
    val principalPaid: Double,
    val interestPaid: Double,
    val closingBalance: Double,
    val cumulativeInterest: Double
)

data class EmiCalculationResult(
    val principal: Double,
    val annualInterestRate: Double,
    val tenureMonths: Int,
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalPayable: Double,
    val interestPercentageOfTotal: Float, // e.g. 24.5%
    val principalPercentageOfTotal: Float, // e.g. 75.5%
    val yearlyAmortization: List<AmortizationRow>,
    val monthlyAmortization: List<AmortizationRow>,
    val prepaymentScenario: PrepaymentAnalysis? = null
)

data class PrepaymentAnalysis(
    val extraMonthlyPayment: Double,
    val originalTenureMonths: Int,
    val newTenureMonths: Int,
    val monthsSaved: Int,
    val originalTotalInterest: Double,
    val newTotalInterest: Double,
    val totalInterestSaved: Double,
    val interestSavingsPercent: Float
)

data class LoanPreset(
    val id: String,
    val title: String,
    val category: String,
    val defaultAmount: Double,
    val defaultAnnualRate: Double,
    val defaultTenureMonths: Int,
    val iconName: String,
    val defaultLender: String,
    val description: String
)
