import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JEditorPane
import javax.swing.JLabel
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.ChartUtils
import java.awt.Color
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.math.pow
import org.json.JSONObject
//import kotlinx.serialization.json.*

class Stock(val symbol: String, val name: String, val industry: String) {
    var averages: List<Pair<String, Double>> = emptyList()
    var dividend: Double = 0.0
    var fetched: Boolean = false
    private val incomeStatementPath = "./income_statement"
    private val balanceSheetPath = "./balance_sheet"
    private var incomeStatementData: JSONObject? = null
    private var balanceSheetData: JSONObject? = null
    private var incomeStatements: List<AnnualReport> = emptyList()
    val financial: FinancialCalculator

    init {
        runBlocking {
            // I fetch data from the API at most once every 24 hours.
            // Then, the program saves the response to the appropriate file.
            // If the last API fetch occurred less than 24 hours ago,
            // the program loads the data from the file instead.

            val currentDir = System.getProperty("user.dir")
            val imagesDir = "$currentDir/images"
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")
            val currentTimestamp = now.format(formatter)
            
            val regex = Regex("${symbol}_(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2})\\.json")
            
            var latestFilePath: String? = null
            File(imagesDir).walkTopDown().forEach { file ->
                if (file.isFile && file.name.matches(regex)) {
                    val matchResult = regex.find(file.name)
                    if (matchResult != null) {
                        val (datePart, timePart) = matchResult.destructured
                        val fileDateTime = LocalDateTime.parse("$datePart $timePart", DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm"))
                        
                        // Checking whether file is older than 24 hours
                        val duration = Duration.between(fileDateTime, now)
                        if (duration.toHours() > 24) {
                            println("Removing file : ${file.name} (older than 24 hours)")
                            file.delete()
                        } else {
                            // Choosing latest file
                            if (latestFilePath == null || fileDateTime.isAfter(LocalDateTime.parse(latestFilePath!!.substringAfter("${symbol}_").substringBefore(".json"), DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")))) {
                                latestFilePath = file.path
                            }
                        }
                    }
                }
            }
            
            val jsonResponse = latestFilePath?.let { path ->
                println("Downloading data from file: ${File(path).name}")
                File(path).readText()
            } ?: run {
                println("Downloading new data for symbol: $symbol")
                val response = fetchStockData(symbol, apiKey)
                val newFileName = "$imagesDir/${symbol}_${currentTimestamp}.json"
                File(newFileName).apply {
                    writeText(response)
                }
                response
            }

            val json = Json { ignoreUnknownKeys = true }
            val parsedResponse = json.decodeFromString<MonthlyAdjustedTimeSeriesResponse>(jsonResponse)

            dividend = parsedResponse.monthlyAdjustedData.entries
                .maxByOrNull { it.key }
                ?.value?.dividendAmount ?: 0.0

            averages = parsedResponse.monthlyAdjustedData.entries
                .sortedBy { it.key } 
                .map { it.key to it.value.adjustedClose.toDouble() }
        }
        setIncomeStatement()
        val currentReport = incomeStatements[0]
        val previousReport = incomeStatements[1]
        financial = FinancialCalculator(currentReport, previousReport)
    }

    fun getBalanceAndIncome(): Pair<JSONObject?, JSONObject?> {
        if (!fetched) {
            incomeStatementData = loadJsonFromFile("$incomeStatementPath/${symbol}_income_statement.json")
            balanceSheetData = loadJsonFromFile("$balanceSheetPath/${symbol}_balance_sheet.json")
            fetched = true
        }
        return Pair(balanceSheetData, incomeStatementData)
    }

    private fun loadJsonFromFile(filePath: String): JSONObject? {
        val file = File(filePath)
        return if (file.exists()) {
            val jsonData = file.readText()
            JSONObject(jsonData)
        } else {
            println("File not found: $filePath")
            null
        }
    }

    fun getBalanceSheetData(): JSONObject? {
        if (!fetched) getBalanceAndIncome()
        return balanceSheetData
    }

    fun getIncomeStatementData(): JSONObject? {
        if (!fetched) getBalanceAndIncome()
        return incomeStatementData
    }

    fun setIncomeStatement() {
        if (!fetched) getBalanceAndIncome()
        incomeStatements = parseIncomeStatement(incomeStatementData!!)
    }

    private fun parseIncomeStatement(json: JSONObject): List<AnnualReport> {
        val reports = mutableListOf<AnnualReport>()

        val annualReportsArray = json.optJSONArray("annualReports")
        if (annualReportsArray != null) {
            for (i in 0 until annualReportsArray.length()) {
                val reportJson = annualReportsArray.getJSONObject(i)
                val report = AnnualReport(
                    fiscalDateEnding = reportJson.optString("fiscalDateEnding"),
                    grossProfit = reportJson.optDouble("grossProfit"),
                    totalRevenue = reportJson.optDouble("totalRevenue"),
                    costOfRevenue = reportJson.optDouble("costOfRevenue"),
                    operatingIncome = reportJson.optDouble("operatingIncome"),
                    netIncome = reportJson.optDouble("netIncome"),
                    operatingExpenses = reportJson.optDouble("operatingExpenses"),
                    researchAndDevelopment = reportJson.optDouble("researchAndDevelopment"),
                    incomeBeforeTax = reportJson.optDouble("incomeBeforeTax"),
                    incomeTaxExpense = reportJson.optDouble("incomeTaxExpense"),
                    interestExpense = reportJson.optDouble("interestExpense").takeIf { !it.isNaN() },
                    ebit = reportJson.optDouble("ebit"),
                    ebitda = reportJson.optDouble("ebitda")
                )
                reports.add(report)
            }
        }

        return reports
    }

    fun getIncomeStatements(): List<AnnualReport> {
        return incomeStatements
    }

    val current: Double
        get() = averages.lastOrNull()?.second ?: 0.0
    
    val weightedMovingAverage: Double
    get() = averages.takeLast(12)
        .mapIndexed { index, (_, adjustedClose) -> 
            adjustedClose * (index + 1) 
        }
        .sum() / averages.takeLast(12).indices.sumOf { it + 1 }
    
    fun annual(years: Int): Double {
        val availableMonths = this.averages.size
        val maxYears = availableMonths / 12

        val effectiveYears = minOf(years, maxYears)

        if (effectiveYears == 0) {
            throw IllegalArgumentException("Not enough data to calculate annual growth.")
        }

        val startingPrice = this.averages[this.averages.size - effectiveYears * 12].second
        val endingPrice = this.averages.last().second
        return (endingPrice / startingPrice).pow(1.0 / effectiveYears) - 1
    }


    // return path to plot
    fun get(option : TimePeriod): String {
        val timePeriod = when (option) {
            TimePeriod.LAST_24_MONTHS -> TimePeriod.LAST_24_MONTHS.limit
            TimePeriod.LAST_60_MONTHS -> TimePeriod.LAST_60_MONTHS.limit
            TimePeriod.LAST_120_MONTHS -> TimePeriod.LAST_120_MONTHS.limit
            TimePeriod.ALL_DATA -> averages.size
        }
        val limitedAverages = averages.takeLast(timePeriod)

        val dates = limitedAverages.map { it.first }
        val prices = limitedAverages.map { it.second }


        val dataset = DefaultCategoryDataset()
        for (i in dates.indices) {
            dataset.addValue(prices[i], "Price", dates[i])
        }

        val chart: JFreeChart = ChartFactory.createLineChart(
            "${name} Stock Price Over Time",
            "Date",
            "Price",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        chart.plot.backgroundPaint = Color.WHITE
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/images"
        val filename = "${timePeriod}_${symbol}.png"

        val file = File(folderPath, filename)
        ImageIO.write(chart.createBufferedImage(800, 600), "PNG", file)

        return file.absolutePath
    }

}