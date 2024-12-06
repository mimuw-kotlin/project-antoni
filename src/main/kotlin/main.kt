import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*

import org.jetbrains.letsPlot.*
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.letsPlot

import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JEditorPane
import javax.swing.JLabel

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset

import java.awt.Color



fun loadImageFromFile(path: String): ImageBitmap? {
    return try {
        val file = File(path)
        val bufferedImage = ImageIO.read(file)
        bufferedImage?.let { it.toComposeImageBitmap() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

val client = HttpClient()

suspend fun fetchStockData(symbol: String, apiKey: String): String {
    val url = "https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol=$symbol&apikey=$apiKey"
    return client.get(url).bodyAsText()
}

@Serializable
data class MonthlyAdjustedTimeSeriesResponse(
    @SerialName("Meta Data")
    val metaData: Map<String, String>,
    @SerialName("Monthly Adjusted Time Series")
    val monthlyAdjustedData: Map<String, StockData>
)

@Serializable
data class StockData(
    @SerialName("1. open") val open: String,
    @SerialName("2. high") val high: String,
    @SerialName("3. low") val low: String,
    @SerialName("4. close") val close: String,
    @SerialName("5. adjusted close") val adjustedClose: String,
    @SerialName("6. volume") val volume: String,
    @SerialName("7. dividend amount") val dividendAmount: String
)


class Stock(val symbol: String, val name: String) {
    var averages: List<Pair<String, Double>> = emptyList()
    init {
        runBlocking {
            val currentDir = System.getProperty("user.dir")
            val imagesDir = "$currentDir/images"

            val today = LocalDate.now().toString()
            val regex = Regex("response_${symbol}_(\\d{4}-\\d{2}-\\d{2}).json")

            // Usuwanie starych plików
            File(imagesDir).walkTopDown().forEach { file ->
                if (file.isFile && file.name.matches(regex) && !file.name.contains(today)) {
                    file.delete()
                }
            }

            val fileName = "$imagesDir/response_${symbol}_$today.json"
            val file = File(fileName)

            val jsonResponse = if (file.exists()) {
                file.readText()
            } else {
                val response = fetchStockData(symbol, "33ZQ4YW7O1VKD01X")
                file.writeText(response)
                response
            }

            val json = Json { ignoreUnknownKeys = true }
            val parsedResponse = json.decodeFromString<MonthlyAdjustedTimeSeriesResponse>(jsonResponse)

            averages = parsedResponse.monthlyAdjustedData.entries
                .sortedBy { it.key } // Sortowanie rosnące po dacie
                .map { it.key to it.value.adjustedClose.toDouble() }
        }
    }

    fun get(option : Int): String {

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

    /*
    fun getPlot(): String {
        val dates = averages.map { it.first }
        val prices = averages.map { it.second }

        val data = mapOf("Date" to dates, "Price" to prices)

        val plot = letsPlot(data) {
            x = "Date"
            y = "Price"
        } + geomLine() + themeMinimal()

        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/images" // Path to the folder where you want to save the plot

        // Create the folder if it doesn't exist
        val directory = File(folderPath)
        if (!directory.exists()) {
            directory.mkdirs() // Create the folder if it doesn't exist
        }

        // Define the file name (with the .html extension)
        val filename = "stock_plot_$symbol.svg" // Now the file is a PNG file

        ggsave(plot, filename = filename, path = folderPath)

        // Return the full path where the plot is saved
        return "$folderPath/$filename"
    }
    */
}


@Composable
fun App() {
    val companies = listOf(
        "APPLE", "GOOGLE", "AMAZON", "MICROSOFT",
        "TESLA", "META", "NETFLIX", "NVIDIA",
        "TWITTER", "INTEL", "ADOBE", "SPOTIFY",
        "ORACLE", "IBM", "AIRBNB",
        "LYFT", "SNAP", "ZOOM"
    )

    val stocks = mapOf(
        "APPLE" to Stock("AAPL", "APPLE"),
        "GOOGLE" to Stock("GOOGL", "GOOGLE"),
        "AMAZON" to Stock("AMZN", "AMAZON"),
        "MICROSOFT" to Stock("MSFT", "MICROSOFT"),
        "TESLA" to Stock("TSLA", "TESLA"),
        "META" to Stock("META", "META"),
        "NETFLIX" to Stock("NFLX", "NETFLIX"),
        "NVIDIA" to Stock("NVDA", "NVIDIA"),
        "TWITTER" to Stock("X", "TWITTER"),
        "INTEL" to Stock("INTC", "INTEL"),
        "ADOBE" to Stock("ADBE", "ADOBE"),
        "SPOTIFY" to Stock("SPOT", "SPOTIFY"),
        "ORACLE" to Stock("ORCL", "ORACLE"),
        "IBM" to Stock("IBM", "IBM"),
        "AIRBNB" to Stock("ABNB", "AIRBNB"),
        "LYFT" to Stock("LYFT", "LYFT"),
        "SNAP" to Stock("SNAP", "SNAP"),
        "ZOOM" to Stock("ZM", "ZOOM")
    )

    var selectedCompany by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Choose company to analyse:",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                style = MaterialTheme.typography.h6.copy(
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(companies.size) { index ->
                    val company = companies[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCompany = company }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(company, style = MaterialTheme.typography.body1)
                    }
                }
            }

            //Spacer(modifier = Modifier.height(16.dp))

            selectedCompany?.let { company ->
                DisplayCompanyDetails(stocks[company]!!)
            }
        }
    }
}

@Composable
fun DisplayCompanyDetails(stock: Stock) {
    Text(
        "Choose Time Period",
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        style = MaterialTheme.typography.h6.copy(
            textAlign = TextAlign.Center
        )
    )
    val options = listOf("2 years" to 1, "5 years" to 2, "10 years" to 3, "20 years" to 4)
    var selectedOption by remember { mutableStateOf(4) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        options.forEach { (label, value) ->
            Button(
                onClick = { selectedOption = value },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedOption == value) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                )
            ) {
                Text(label)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    val plotPath = stock.get(selectedOption)

    val imageBitmap = remember(plotPath) { loadImageFromFile(plotPath) }

    imageBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = "Loaded Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        )
    } ?: Text("Error loading image", color = MaterialTheme.colors.error)
}

fun deletePngFilesInImagesFolder() {
    val imagesFolder = File(System.getProperty("user.dir"), "images")
    imagesFolder.listFiles { _, name -> name.endsWith(".png", ignoreCase = true) }
        ?.forEach { it.delete() }
}
fun main() = application {
    deletePngFilesInImagesFolder()
    Window(onCloseRequest = ::exitApplication, title = "Desktop Application") {
        App()
    }
}