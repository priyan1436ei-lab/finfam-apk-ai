package com.example.domain.engine

import com.example.domain.model.AmortizationRow
import com.example.domain.model.EmiCalculationResult
import com.example.domain.model.LoanPreset
import com.example.domain.model.PrepaymentAnalysis
import kotlin.math.pow
import kotlin.math.roundToInt

object EmiCalculatorEngine {

    val LOAN_PRESETS = listOf(
        LoanPreset(
            id = "bike",
            title = "Bike / Two-Wheeler",
            category = "Vehicle",
            defaultAmount = 120000.0,
            defaultAnnualRate = 9.5,
            defaultTenureMonths = 24,
            iconName = "two_wheeler",
            defaultLender = "HDFC Bank",
            description = "Commuter or sports bike auto loan"
        ),
        LoanPreset(
            id = "car",
            title = "Car / Four-Wheeler",
            category = "Vehicle",
            defaultAmount = 750000.0,
            defaultAnnualRate = 8.75,
            defaultTenureMonths = 48,
            iconName = "car",
            defaultLender = "SBI Car Loan",
            description = "New passenger vehicle loan"
        ),
        LoanPreset(
            id = "laptop",
            title = "Laptop & Workstation",
            category = "Electronics",
            defaultAmount = 85000.0,
            defaultAnnualRate = 0.0,
            defaultTenureMonths = 12,
            iconName = "laptop",
            defaultLender = "Bajaj Finserv",
            description = "Zero-cost or consumer electronics EMI"
        ),
        LoanPreset(
            id = "mobile",
            title = "Flagship Smartphone",
            category = "Mobile",
            defaultAmount = 65000.0,
            defaultAnnualRate = 0.0,
            defaultTenureMonths = 6,
            iconName = "smartphone",
            defaultLender = "ICICI Card EMI",
            description = "No-cost smartphone installment"
        ),
        LoanPreset(
            id = "home",
            title = "Home & Property Loan",
            category = "Home",
            defaultAmount = 4500000.0,
            defaultAnnualRate = 8.5,
            defaultTenureMonths = 240,
            iconName = "home",
            defaultLender = "Axis Bank Home Loan",
            description = "Housing & construction loan"
        ),
        LoanPreset(
            id = "education",
            title = "Higher Education Loan",
            category = "Education",
            defaultAmount = 600000.0,
            defaultAnnualRate = 10.5,
            defaultTenureMonths = 60,
            iconName = "school",
            defaultLender = "Canara Bank Education",
            description = "University tuition & study fee loan"
        ),
        LoanPreset(
            id = "personal",
            title = "Personal Loan",
            category = "Personal",
            defaultAmount = 200000.0,
            defaultAnnualRate = 13.0,
            defaultTenureMonths = 36,
            iconName = "account_balance",
            defaultLender = "Kotak Mahindra Bank",
            description = "Instant personal & emergency loan"
        )
    )

    fun calculateEmi(
        principal: Double,
        annualRate: Double,
        tenureMonths: Int,
        extraMonthlyPrepayment: Double = 0.0
    ): EmiCalculationResult {
        val safePrincipal = principal.coerceAtLeast(1000.0)
        val safeTenure = tenureMonths.coerceAtLeast(1)
        val safeRate = annualRate.coerceAtLeast(0.0)

        val monthlyRate = (safeRate / 12.0) / 100.0

        val monthlyEmi: Double = if (safeRate <= 0.001 || monthlyRate <= 0.0) {
            safePrincipal / safeTenure
        } else {
            val factor = (1 + monthlyRate).pow(safeTenure.toDouble())
            if (factor.isInfinite() || factor == 1.0) {
                safePrincipal / safeTenure
            } else {
                (safePrincipal * monthlyRate * factor) / (factor - 1)
            }
        }

        val totalPayable = monthlyEmi * safeTenure
        val totalInterest = (totalPayable - safePrincipal).coerceAtLeast(0.0)

        val totalForRatio = (safePrincipal + totalInterest).coerceAtLeast(1.0)
        val principalPct = ((safePrincipal / totalForRatio) * 100f).toFloat().coerceIn(0f, 100f)
        val interestPct = ((totalInterest / totalForRatio) * 100f).toFloat().coerceIn(0f, 100f)

        // Generate Monthly Amortization Table
        val monthlyList = mutableListOf<AmortizationRow>()
        var currentBalance = safePrincipal
        var cumulativeInterest = 0.0

        for (m in 1..safeTenure) {
            val opening = currentBalance
            val interestForMonth = if (monthlyRate > 0) opening * monthlyRate else 0.0
            val principalForMonth = (monthlyEmi - interestForMonth).coerceAtMost(opening).coerceAtLeast(0.0)
            val closing = (opening - principalForMonth).coerceAtLeast(0.0)
            cumulativeInterest += interestForMonth

            monthlyList.add(
                AmortizationRow(
                    periodIndex = m,
                    periodLabel = "Month $m",
                    openingBalance = opening,
                    emiPaid = principalForMonth + interestForMonth,
                    principalPaid = principalForMonth,
                    interestPaid = interestForMonth,
                    closingBalance = closing,
                    cumulativeInterest = cumulativeInterest
                )
            )
            currentBalance = closing
            if (currentBalance <= 0.01) break
        }

        // Generate Yearly Amortization Table
        val yearlyList = mutableListOf<AmortizationRow>()
        val totalYears = (safeTenure + 11) / 12
        for (y in 1..totalYears) {
            val startMonth = (y - 1) * 12 + 1
            val endMonth = (y * 12).coerceAtMost(safeTenure)
            val monthsInYear = monthlyList.filter { it.periodIndex in startMonth..endMonth }
            if (monthsInYear.isNotEmpty()) {
                val opening = monthsInYear.first().openingBalance
                val principalYear = monthsInYear.sumOf { it.principalPaid }
                val interestYear = monthsInYear.sumOf { it.interestPaid }
                val emiYear = monthsInYear.sumOf { it.emiPaid }
                val closing = monthsInYear.last().closingBalance
                val cumInt = monthsInYear.last().cumulativeInterest

                yearlyList.add(
                    AmortizationRow(
                        periodIndex = y,
                        periodLabel = "Year $y",
                        openingBalance = opening,
                        emiPaid = emiYear,
                        principalPaid = principalYear,
                        interestPaid = interestYear,
                        closingBalance = closing,
                        cumulativeInterest = cumInt
                    )
                )
            }
        }

        // Compute Prepayment Scenario if requested
        val prepayment = if (extraMonthlyPrepayment > 0) {
            computePrepayment(
                principal = safePrincipal,
                monthlyRate = monthlyRate,
                standardEmi = monthlyEmi,
                originalTenure = safeTenure,
                originalTotalInterest = totalInterest,
                extraMonthly = extraMonthlyPrepayment
            )
        } else null

        return EmiCalculationResult(
            principal = safePrincipal,
            annualInterestRate = safeRate,
            tenureMonths = safeTenure,
            monthlyEmi = monthlyEmi,
            totalInterest = totalInterest,
            totalPayable = totalPayable,
            interestPercentageOfTotal = interestPct,
            principalPercentageOfTotal = principalPct,
            yearlyAmortization = yearlyList,
            monthlyAmortization = monthlyList,
            prepaymentScenario = prepayment
        )
    }

    private fun computePrepayment(
        principal: Double,
        monthlyRate: Double,
        standardEmi: Double,
        originalTenure: Int,
        originalTotalInterest: Double,
        extraMonthly: Double
    ): PrepaymentAnalysis {
        var balance = principal
        val effectiveMonthly = standardEmi + extraMonthly
        var newTenureMonths = 0
        var newTotalInterest = 0.0

        while (balance > 0.01 && newTenureMonths < originalTenure * 2) {
            newTenureMonths++
            val interestMonth = if (monthlyRate > 0) balance * monthlyRate else 0.0
            newTotalInterest += interestMonth
            val principalMonth = (effectiveMonthly - interestMonth).coerceAtMost(balance)
            balance = (balance - principalMonth).coerceAtLeast(0.0)
        }

        val monthsSaved = (originalTenure - newTenureMonths).coerceAtLeast(0)
        val interestSaved = (originalTotalInterest - newTotalInterest).coerceAtLeast(0.0)
        val savingsPct = if (originalTotalInterest > 0) {
            ((interestSaved / originalTotalInterest) * 100f).toFloat().coerceIn(0f, 100f)
        } else 0f

        return PrepaymentAnalysis(
            extraMonthlyPayment = extraMonthly,
            originalTenureMonths = originalTenure,
            newTenureMonths = newTenureMonths,
            monthsSaved = monthsSaved,
            originalTotalInterest = originalTotalInterest,
            newTotalInterest = newTotalInterest,
            totalInterestSaved = interestSaved,
            interestSavingsPercent = savingsPct
        )
    }
}
