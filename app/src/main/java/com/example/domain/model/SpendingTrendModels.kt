package com.example.domain.model

enum class TimeHorizon(val label: String, val monthsCount: Int) {
    LAST_3_MONTHS("Last 3M", 3),
    LAST_6_MONTHS("Last 6M", 6),
    LAST_12_MONTHS("1 Year", 12),
    ALL_TIME("All Time", 24)
}

data class CategoryTrendSeries(
    val category: String,
    val colorHex: String,
    val dataPoints: List<Double>,
    val totalSpent: Double,
    val averageMonthly: Double,
    val momPercentageChange: Double,
    val peakMonth: String,
    val peakAmount: Double,
    val lowestMonth: String,
    val lowestAmount: Double,
    val budgetLimit: Double? = null
)

data class TrendMetrics(
    val averageMonthlySpend: Double,
    val highestSpendMonth: String,
    val highestSpendAmount: Double,
    val lowestSpendMonth: String,
    val lowestSpendAmount: Double,
    val latestMonthSpend: Double,
    val previousMonthSpend: Double,
    val momPercentageChange: Double,
    val topCategory: String,
    val topCategoryPercentage: Double,
    val totalSpendInWindow: Double
)

data class CategoryBreakdownItem(
    val category: String,
    val iconName: String,
    val colorHex: String,
    val totalAmount: Double,
    val percentageShare: Double,
    val monthlyAverage: Double,
    val momPercentageChange: Double,
    val isBudgetExceeded: Boolean = false
)

data class MonthlyDataPoint(
    val monthFull: String,     // e.g. "August 2026"
    val monthShort: String,    // e.g. "Aug"
    val totalExpense: Double,
    val totalIncome: Double,
    val categoryAmounts: Map<String, Double>
)

data class MonthlySpendingTrendsState(
    val monthsFull: List<String> = emptyList(),
    val monthsShort: List<String> = emptyList(),
    val monthlyDataPoints: List<MonthlyDataPoint> = emptyList(),
    val categorySeries: List<CategoryTrendSeries> = emptyList(),
    val totalExpenseSeries: List<Double> = emptyList(),
    val totalIncomeSeries: List<Double> = emptyList(),
    val selectedTimeHorizon: TimeHorizon = TimeHorizon.LAST_6_MONTHS,
    val selectedCategory: String = "ALL", // "ALL" or specific category
    val selectedMultiCategories: Set<String> = emptySet(),
    val isMultiLineMode: Boolean = false,
    val metrics: TrendMetrics = TrendMetrics(
        averageMonthlySpend = 0.0,
        highestSpendMonth = "-",
        highestSpendAmount = 0.0,
        lowestSpendMonth = "-",
        lowestSpendAmount = 0.0,
        latestMonthSpend = 0.0,
        previousMonthSpend = 0.0,
        momPercentageChange = 0.0,
        topCategory = "-",
        topCategoryPercentage = 0.0,
        totalSpendInWindow = 0.0
    ),
    val categoryBreakdowns: List<CategoryBreakdownItem> = emptyList(),
    val selectedMonthIndex: Int = -1 // -1 means latest month or no specific month pinned
)
