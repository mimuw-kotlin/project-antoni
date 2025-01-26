
sealed interface Requirement {
  fun execute(stock : Stock): Boolean
}

class CurrentValue(private val minimum: Double, private val maximum: Double) : Requirement {
    override fun execute(stock: Stock): Boolean {
        return stock.current in minimum..maximum
    }
}

class Industry(private val industryName: String) : Requirement {
    override fun execute(stock: Stock): Boolean {
        return stock.industry == industryName
    }
}

class Dividend(private val minimum: Double) : Requirement {
    override fun execute(stock: Stock): Boolean {
        return stock.dividend >= minimum
    }
}
 
class WeightedMovingAverage() : Requirement {
    override fun execute(stock: Stock): Boolean {
        return stock.weightedMovingAverage >= stock.current
    }
}

class AnnualGrowth(
    private val option: Int,
    private val minimum: Double,
    private val maximum: Double
) : Requirement {
    override fun execute(stock: Stock): Boolean {
        val annualGrowth = when (option) {
            1 -> stock.annual(1)
            2 -> stock.annual(2)
            else -> stock.annual(5)
        }
        return annualGrowth in minimum..maximum
    }
}