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

class Stock(val symbol: String, val name: String) {
    var averages: List<Pair<String, Double>> = emptyList()
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
                val response = fetchStockData(symbol, "33ZQ4YW7O1VKD01X")
                val newFileName = "$imagesDir/${symbol}_${currentTimestamp}.json"
                File(newFileName).apply {
                    writeText(response)
                }
                response
            }

            val json = Json { ignoreUnknownKeys = true }
            val parsedResponse = json.decodeFromString<MonthlyAdjustedTimeSeriesResponse>(jsonResponse)

            averages = parsedResponse.monthlyAdjustedData.entries
                .sortedBy { it.key } 
                .map { it.key to it.value.adjustedClose.toDouble() }
        }
    }

    // returns current stock value 
    fun getCurrent(): Double {
        return averages.lastOrNull()?.second ?: 0.0
    }

    // return path to plot
    fun get(option : Int): String {
        // time period
        val limit = when (option) {
            1 -> 24
            2 -> 60
            3 -> 120
            else -> averages.size
        }
        val limitedAverages = averages.takeLast(limit)

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

        val filename = "${limit}_${symbol}.png"

        val file = File(folderPath, filename)
        ImageIO.write(chart.createBufferedImage(800, 600), "PNG", file)

        return file.absolutePath
    }

}