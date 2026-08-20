package com.example.domain.engine

import com.example.domain.model.SimulationMonthPoint
import com.example.domain.model.SimulationResult
import com.example.domain.model.SimulationScenario
import kotlin.math.pow

object SimulationEngine {

    val PRESET_SCENARIOS = listOf(
        SimulationScenario(
            id = "preset_car",
            title = "Buy Car (₹15L)",
            icon = "directions_car",
            salary = 85000.0,
            savings = 25000.0,
            loanAmount = 1200000.0,
            interestRate = 9.2,
            inflationRate = 6.0,
            timelineYears = 5
        ),
        SimulationScenario(
            id = "preset_marriage",
            title = "Marriage Plan (₹25L)",
            icon = "favorite",
            salary = 95000.0,
            savings = 35000.0,
            loanAmount = 800000.0,
            interestRate = 11.5,
            inflationRate = 6.5,
            timelineYears = 5
        ),
        SimulationScenario(
            id = "preset_raise",
            title = "Salary Raise (+30%)",
            icon = "trending_up",
            salary = 110000.0,
            savings = 45000.0,
            loanAmount = 0.0,
            interestRate = 8.5,
            inflationRate = 5.5,
            timelineYears = 5
        ),
        SimulationScenario(
            id = "preset_edu",
            title = "Higher Education (₹40L)",
            icon = "school",
            salary = 75000.0,
            savings = 15000.0,
            loanAmount = 3000000.0,
            interestRate = 8.75,
            inflationRate = 6.0,
            timelineYears = 5
        )
    )

    /**
     * Calculates monthly EMI using the standard formula:
     * EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     */
    fun calculateEmi(principal: Double, annualInterestRate: Double, tenureMonths: Int): Double {
        if (principal <= 0 || tenureMonths <= 0) return 0.0
        if (annualInterestRate <= 0) return principal / tenureMonths

        val monthlyRate = (annualInterestRate / 12.0) / 100.0
        val numerator = principal * monthlyRate * (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        val denominator = (1.0 + monthlyRate).pow(tenureMonths.toDouble()) - 1.0

        return if (denominator != 0.0) numerator / denominator else 0.0
    }

    /**
     * Generates a 60-month sandbox forecast dataset with net worth compounding, loan amortization, cashflow, and break-even month
     */
    fun runSimulation(
        salary: Double,
        monthlySavings: Double,
        loanAmount: Double,
        interestRate: Double,
        inflationRate: Double,
        timelineYears: Int = 5
    ): SimulationResult {
        val totalMonths = (timelineYears * 12).coerceIn(12, 120)
        val emi = calculateEmi(loanAmount, interestRate, totalMonths)
        val monthlyInflationRate = (inflationRate / 12.0) / 100.0
        val investmentGrowthRate = (12.0 / 12.0) / 100.0 // 12% p.a. expected mutual fund equity CAGR

        val points = mutableListOf<SimulationMonthPoint>()
        var currentNetWorth = 250000.0 // Baseline starting net worth
        var remainingLoan = loanAmount
        var totalInvested = 0.0
        var totalInterestPaid = 0.0
        var breakEvenMonth = -1

        val monthlyRate = (interestRate / 12.0) / 100.0

        for (m in 1..totalMonths) {
            // Net cashflow after EMI
            val effectiveSavings = (monthlySavings - emi).coerceAtLeast(0.0)
            totalInvested += effectiveSavings

            // Interest component of EMI this month
            val interestComponent = remainingLoan * monthlyRate
            val principalComponent = (emi - interestComponent).coerceAtLeast(0.0)
            remainingLoan = (remainingLoan - principalComponent).coerceAtLeast(0.0)
            totalInterestPaid += interestComponent

            // Compounding investment growth
            currentNetWorth = (currentNetWorth + effectiveSavings) * (1.0 + investmentGrowthRate)

            val netWorthWithDebt = currentNetWorth - remainingLoan

            if (breakEvenMonth == -1 && netWorthWithDebt >= loanAmount && loanAmount > 0) {
                breakEvenMonth = m
            }

            points.add(
                SimulationMonthPoint(
                    month = m,
                    netWorth = netWorthWithDebt,
                    totalInvested = totalInvested,
                    loanBalance = remainingLoan,
                    cashflow = salary - (monthlySavings + emi)
                )
            )
        }

        if (breakEvenMonth == -1) {
            breakEvenMonth = if (loanAmount == 0.0) 0 else totalMonths
        }

        return SimulationResult(
            monthlyEmi = emi,
            monthlyCashflow = (salary - monthlySavings - emi).coerceAtLeast(0.0),
            projectedNetWorthAtEnd = points.lastOrNull()?.netWorth ?: currentNetWorth,
            breakEvenMonth = breakEvenMonth,
            totalInterestPaid = totalInterestPaid,
            points = points
        )
    }
}
