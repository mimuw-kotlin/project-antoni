import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.ChartUtils

import java.awt.Color

import java.io.FileWriter
import java.io.FileReader
import java.io.IOException
import java.io.BufferedWriter
import java.io.BufferedReader
import java.io.File


class VirtualPortfolio(val name: String){
    val stockDataList = mutableListOf<Triple<String, Double, Double>>()
    val myStocks = mutableMapOf<String, Double>()
    var invested : Double
    var currentValue : Double

    init {
        invested = 0.0
        currentValue = 0.0
    }

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

    // loading information about all the previos sells and buys
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
                    stockDataList.add(Triple(stock, priceStart, owned))
                }
            }
            reader.close()
        } catch (e: IOException) {
            println("An error occurred while reading the file: ${e.message}")
        }
    }
    /*
    // old functions that I might need in the future
    fun getData() {
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"
        val dataFile = File(folderPath, "data.csv")
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
                    stockDataList.add(Triple(stock, priceStart, owned))
                }
            }
            reader.close()
        } catch (e: IOException) {
            println("An error occurred while reading the file: ${e.message}")
        }
    }
    */

    // calculating value of portfolio
    fun calculate(stocks: Map<String, Stock>) {
        invested = 0.0
        currentValue = 0.0

        stockDataList.asReversed().forEach { (symbol, priceStart, owned) ->
            val cur = stocks[symbol]?.getCurrent() ?: 0.0
            val value = owned * cur / priceStart
            if (symbol !in myStocks) {
                invested += owned
                currentValue += value
                myStocks[symbol] = value
            }
            //myStocks[symbol] = myStocks[symbol]?.plus(value) ?: value
        }
    }

    // new Buy/Sell operation
    fun newPos(stocks: Map<String, Stock>, stockName: String, value: Double) {
        val priceStart = stocks[stockName]?.getCurrent() ?: throw IllegalArgumentException("Stock not found")
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

    // piechart of all stocks in the portfolio
    fun createPieChart(): String {
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

        val chartPath = "$directoryPath/chart.png"
        ChartUtils.saveChartAsPNG(File(chartPath), pieChart, 800, 600)

        return chartPath
    }

}