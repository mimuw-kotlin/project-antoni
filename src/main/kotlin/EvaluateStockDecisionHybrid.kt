// This program is designed to analyze whether a stock is worth buying, selling, or holding
// Initially, the project was intended to focus on AI-powered stock price prediction
// but the scope was shifted to provide actionable investment recommendations

fun evaluateStockDecisionHybrid(
    financialRatios: Map<String, Any?>,
    currentPrice: Double,
    weightedMovingAverage: Double
): String {
    // Extract financial ratios with default values
    val revenueGrowth = (financialRatios["Revenue Growth Rate (%)"] as? Double) ?: 0.0
    val grossMargin = (financialRatios["Gross Margin (%)"] as? Double) ?: 0.0
    val netProfitMargin = (financialRatios["Net Profit Margin (%)"] as? Double) ?: 0.0
    val operatingMargin = (financialRatios["Operating Margin (%)"] as? Double) ?: 0.0
    val ebitdaMargin = (financialRatios["EBITDA Margin (%)"] as? Double) ?: 0.0
    val interestCoverageRatio = (financialRatios["Interest Coverage Ratio"] as? Double) ?: 0.0
    val effectiveTaxRate = (financialRatios["Effective Tax Rate (%)"] as? Double) ?: 0.0

    // Fundamental analysis
    val conditionsMet = listOf(
        revenueGrowth > 5,
        grossMargin > 40,
        netProfitMargin > 10,
        operatingMargin > 15,
        ebitdaMargin > 20,
        interestCoverageRatio > 3,
        effectiveTaxRate in 10.0..30.0,
        currentPrice < weightedMovingAverage
    ).count { it }

    // Decision thresholds
    return when {
        conditionsMet >= 7 -> "BUY" // Buy signal if most conditions are met
        conditionsMet <= 2 -> "SELL" // Sell signal if few conditions are met
        else -> "STAY"
    }
}