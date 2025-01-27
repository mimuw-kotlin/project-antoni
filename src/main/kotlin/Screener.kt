class Screener(private val stocks: Map<String, Stock>){
    fun findStocksMatchingRequirements(requirements: MutableList<Requirement>): MutableList<String> {
        val matchingStocks = mutableListOf<String>()
        for ((stockName, stock) in stocks) {
            val matchesAllRequirements = requirements.all { it.execute(stock) }
            if (matchesAllRequirements) {
                matchingStocks.add(stockName)
            }
        }
        return matchingStocks
    }
}

val industries = listOf(
    "Technology", "E-commerce", "Automotive", "Entertainment", "Semiconductors",
    "Social Media", "Software", "Travel", "Communication"
)

val criteria = listOf(
    "Current Value", "Dividend Amount", "Weighted Moving Average <= Current Price",
    "One Year Annual Growth", "Two Years Annual Growth", "Five Years Annual Growth"
)

val myScreener = Screener(stocks)
var requirements: MutableList<Requirement> = mutableListOf()
var companyScreener: MutableList<String> = mutableListOf()