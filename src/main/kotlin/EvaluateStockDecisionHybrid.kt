fun evaluateStockDecisionHybrid(
    financialRatios: Map<String, Any?>,
    currentPrice: Double,
    weightedMovingAverage: Double
): String {
    // Evaluate fundamental indicators
    val revenueGrowth = (financialRatios["Revenue Growth Rate (%)"] as? Double) ?: 0.0
    val grossMargin = (financialRatios["Gross Margin (%)"] as? Double) ?: 0.0
    val netIncomeGrowth = (financialRatios["Net Income Growth Rate (%)"] as? Double) ?: 0.0
    //println("Revenue Growth: $revenueGrowth, Gross Margin: $grossMargin, Net Income Growth: $netIncomeGrowth")
    val isFundamentallyStrong = revenueGrowth > 5 && grossMargin > 40
    val isNotFundamentallyStrong = revenueGrowth < -1 && grossMargin < 8
    // Evaluate growth in net income
    val isHighNetIncomeGrowth = netIncomeGrowth > 12.5 // Arbitrary threshold for "high growth"
    val isSuperHighNetIncomeGrowth = netIncomeGrowth > 23
    // Perform technical analysis
    val isUndervalued = currentPrice < weightedMovingAverage

    // Combine results
    return when {
        isFundamentallyStrong && ((isUndervalued && isHighNetIncomeGrowth) || (isSuperHighNetIncomeGrowth)) -> "BUY"
        isNotFundamentallyStrong || netIncomeGrowth < -4 -> "SELL" // Adjusted to consider low growth
        else -> "STAY"
    }
}