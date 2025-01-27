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
 
class WeightedMovingAverage : Requirement {
    override fun execute(stock: Stock): Boolean {
        return stock.weightedMovingAverage <= stock.current
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
        println(annualGrowth)
        // Check for special cases with Infinity or -Infinity
        val isAboveMinimum = if (minimum == Double.NEGATIVE_INFINITY) true else annualGrowth >= minimum
        val isBelowMaximum = if (maximum == Double.POSITIVE_INFINITY) true else annualGrowth <= maximum

        return isAboveMinimum && isBelowMaximum
    }
}

fun prepareRequirements(criteriaParameters: MutableMap<String, Pair<Double, Double>>, requirements: MutableList<Requirement>, selectedIndustry: String){
    for ((key, value) in criteriaParameters) {
        val (first, second) = value
        if (key == "Current Value"){
            requirements.add(CurrentValue(first, second))
        }
        else if(key == "Dividend Amount"){
            requirements.add(Dividend(first))
        }
        else if(key == "One Year Annual Growth"){
            requirements.add(AnnualGrowth(1, first, second))
        }
        else if(key == "Two Years Annual Growth"){
            requirements.add(AnnualGrowth(2, first, second))
        }
        else if(key == "Five Years Annual Growth"){
            requirements.add(AnnualGrowth(5, first, second))
        }
        else{
            requirements.add(WeightedMovingAverage())
        }
    }
    if (selectedIndustry != "Select Industry"){
        requirements.add(Industry(selectedIndustry))
    }
}