import org.jfree.chart.ChartFactory
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.ChartUtils
import java.io.FileWriter
import java.io.FileReader
import java.io.IOException
import java.io.BufferedWriter
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path

data class StockInformation(
    val symbol: String, 
    val price: Double,  
    val volume: Double  
)

class VirtualPortfolio(private val name: String){
    private val stockDataList = mutableListOf<StockInformation>()
    private val myStocks = mutableMapOf<String, Double>()
    var invested : Double = 0.0
    var currentValue : Double = 0.0

    // Creating new folder to represent portfolio
    fun create(){
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"

        val directory = File(folderPath)
        directory.mkdirs()
        val dataFile = File(directory, "stocks.csv")
        try {
            dataFile.createNewFile()
            val writer = BufferedWriter(FileWriter(dataFile))
            writer.write("stock,price_start,owned\n")
            writer.close()
        } catch (e: IOException) {
            println("An error occurred while creating the file: ${e.message}")
        }
    }

    // loading information about all the previous sells and buys
    fun getData() {
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"
        val dataFile = File(folderPath, "stocks.csv")
        try {
            val reader = BufferedReader(FileReader(dataFile))
            @Suppress("UNUSED_VARIABLE")
            val header = reader.readLine()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split(",")
                if (parts.size == 3) {
                    val stock = parts[0]
                    val priceStart = parts[1].toDouble()
                    val owned = parts[2].toDouble()
                    stockDataList.add(StockInformation(symbol = stock, price = priceStart, volume = owned))
                }
            }
            reader.close()
        } catch (e: IOException) {
            println("An error occurred while reading the file: ${e.message}")
        }
    }

    fun calculatePortfolioValue(stocks: Map<String, Stock>) {
        stockDataList.asReversed().forEach { (symbol, price, volume) ->
            val cur = stocks[symbol]?.current ?: 0.0
            val value = volume * cur / price
            if (symbol !in myStocks) {
                invested += volume
                currentValue += value
                myStocks[symbol] = value
            }
        }
    }

    // new Buy/Sell operation
    fun newPos(stocks: Map<String, Stock>, stockName: String, value: Double) {
        val priceStart = stocks[stockName]?.current ?: throw IllegalArgumentException("Stock not found")
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"
        val dataFile = File(folderPath, "stocks.csv")

        invested += value
        if (stockName !in myStocks) {
            myStocks[stockName] = 0.0
        }
        myStocks[stockName] = myStocks[stockName]?.plus(value) ?: value
        currentValue += value
        try {
            val writer = BufferedWriter(FileWriter(dataFile, true))
            writer.write("$stockName,$priceStart,${myStocks[stockName]}\n")
            writer.close()
        } catch (e: IOException) {
            println("An error occurred while writing to the file: ${e.message}")
        }

    }

    fun getStockValue(stockName: String): Double {
        if (stockName !in myStocks) {
            return 0.0
        }
        return myStocks[stockName]!!
    }

    // Chart of all stocks in the portfolio
    fun createPieChart(): Path {
        val currentDir = System.getProperty("user.dir")
        val directoryPath = "$currentDir/virtual/$name"
        val directory = File(directoryPath)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val existingFile = File("$directoryPath/chart.png")
        if (existingFile.exists()) {
            existingFile.delete()
        }

        val dataset: DefaultPieDataset<String> = DefaultPieDataset()
        for ((key, value) in myStocks) {
            dataset.setValue(key, value)
        }

        // Create the pie chart
        val pieChart = ChartFactory.createPieChart(
            "$name Portfolio Distribution", // Chart title
            dataset,                  // Dataset
            true,                     // Include legend
            true,                     // Include tooltips
            false                     // Do not generate URLs
        )

        val chartFile = File("$directoryPath/chart.png")
        ChartUtils.saveChartAsPNG(chartFile, pieChart, 800, 600)
        return chartFile.toPath()
    }
}