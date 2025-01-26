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
