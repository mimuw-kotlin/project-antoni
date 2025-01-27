import kotlin.math.abs

data class AnnualReport(
    val fiscalDateEnding: String,
    val grossProfit: Double,
    val totalRevenue: Double,
    val costOfRevenue: Double,
    val operatingIncome: Double,
    val netIncome: Double,
    val operatingExpenses: Double,
    val researchAndDevelopment: Double,
    val incomeBeforeTax: Double,
    val incomeTaxExpense: Double,
    val interestExpense: Double? = null,
    val ebit: Double,
    val ebitda: Double
)


class FinancialCalculator(
    private val incomeStatements: List<AnnualReport>
) {
    private val currentReport: AnnualReport
    private var previousReport: AnnualReport? = null
    init {
        currentReport = incomeStatements[0]
        if(incomeStatements.size > 1){
            previousReport = incomeStatements[1]
        }
    }
    fun calculateAllRatios(): Map<String, Any?> {
        return mapOf(
            "Gross Margin (%)" to calculateGrossMargin(currentReport),
            "Operating Margin (%)" to calculateOperatingMargin(currentReport),
            "Net Profit Margin (%)" to calculateNetProfitMargin(currentReport),
            "R&D Intensity (%)" to calculateRAndDIntensity(currentReport),
            "Effective Tax Rate (%)" to calculateEffectiveTaxRate(currentReport),
            "Interest Coverage Ratio" to calculateInterestCoverageRatio(currentReport),
            "EBITDA Margin (%)" to calculateEBITDAMargin(currentReport),
            "Revenue Growth Rate (%)" to previousReport?.let {
                calculateRevenueGrowthRate(currentReport, it)
            },
            "Net Income Growth Rate (%)" to previousReport?.let {
                calculateNetIncomeGrowthRate(currentReport, it)
            }
        )
    }

    private fun calculateGrossMargin(report: AnnualReport): Double {
        return (report.grossProfit / report.totalRevenue) * 100
    }

    private fun calculateOperatingMargin(report: AnnualReport): Double {
        return (report.operatingIncome / report.totalRevenue) * 100
    }

    private fun calculateNetProfitMargin(report: AnnualReport): Double {
        return (report.netIncome / report.totalRevenue) * 100
    }

    private fun calculateRAndDIntensity(report: AnnualReport): Double {
        return (report.researchAndDevelopment / report.totalRevenue) * 100
    }

    private fun calculateEffectiveTaxRate(report: AnnualReport): Double {
        return (report.incomeTaxExpense / report.incomeBeforeTax) * 100
    }

    private fun calculateInterestCoverageRatio(report: AnnualReport): Double? {
        return report.interestExpense?.let {
            report.ebit / it
        }
    }

    private fun calculateEBITDAMargin(report: AnnualReport): Double {
        return (report.ebitda / report.totalRevenue) * 100
    }

    private fun calculateRevenueGrowthRate(
        currentReport: AnnualReport,
        previousReport: AnnualReport
    ): Double {
        return ((currentReport.totalRevenue - previousReport.totalRevenue) / previousReport.totalRevenue) * 100
    }

    private fun calculateNetIncomeGrowthRate(
        currentReport: AnnualReport,
        previousReport: AnnualReport
    ): Double {
        val denominator = previousReport.netIncome
        return ((currentReport.netIncome - denominator) / abs(denominator)) * 100
    }
}