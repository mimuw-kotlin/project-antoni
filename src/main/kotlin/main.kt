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

// import androidx.compose.ui.text.input.KeyboardOptions
// import androidx.compose.ui.text.input.KeyboardType

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
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.ChartUtils

import java.awt.Color

import java.io.FileWriter
import java.io.FileReader
import java.io.IOException
import java.io.BufferedWriter
import java.io.BufferedReader


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
            val yesterday = LocalDate.now().minusDays(1).toString()
            val regex = Regex("response_${symbol}_(\\d{4}-\\d{2}-\\d{2}).json")

            // && !file.name.contains(yesterday)

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
            //println(name)
            val json = Json { ignoreUnknownKeys = true }
            val parsedResponse = json.decodeFromString<MonthlyAdjustedTimeSeriesResponse>(jsonResponse)

            averages = parsedResponse.monthlyAdjustedData.entries
                .sortedBy { it.key } // Sortowanie rosnące po dacie
                .map { it.key to it.value.adjustedClose.toDouble() }
        }
    }

    fun getCurrent(): Double {
        return averages.lastOrNull()?.second ?: 0.0
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


class VirtualPortfolio(val name: String){
    val stockDataList = mutableListOf<Triple<String, Double, Double>>()
    val myStocks = mutableMapOf<String, Double>()
    var invested : Double
    var currentValue : Double

    init {
        invested = 0.0
        currentValue = 0.0
    }

    fun create(){
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"

        val directory = File(folderPath)
        directory.mkdirs()
        val dataFile = File(directory, "data.csv")
        try {
            dataFile.createNewFile()
            val writer = BufferedWriter(FileWriter(dataFile))
            writer.write("stock,price_start,owned\n")
            writer.close()
        } catch (e: IOException) {
            println("An error occurred while creating the file: ${e.message}")
        }
    }

    fun getData() {
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"
        val dataFile = File(folderPath, "data.csv")
        try {
            val reader = BufferedReader(FileReader(dataFile))
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

    fun calculate(stocks: Map<String, Stock>) {
        invested = 0.0
        currentValue = 0.0

        stockDataList.forEach { (symbol, priceStart, owned) ->
            val cur = stocks[symbol]?.getCurrent() ?: 0.0
            val value = owned * cur / priceStart

            invested += priceStart
            currentValue += value

            if (symbol !in myStocks) {
                myStocks[symbol] = 0.0
            }
            myStocks[symbol] = myStocks[symbol]?.plus(value) ?: value
        }
    }

    fun newPos(stocks: Map<String, Stock>, stockName: String, value: Double) {
        val priceStart = stocks[stockName]?.getCurrent() ?: throw IllegalArgumentException("Stock not found")
        val currentDir = System.getProperty("user.dir")
        val folderPath = "$currentDir/virtual/$name"
        val dataFile = File(folderPath, "data.csv")

        try {
            val writer = BufferedWriter(FileWriter(dataFile, true)) // 'true' pozwala na dopisywanie do pliku
            writer.write("$stockName,$priceStart,$value\n") 
            writer.close()
        } catch (e: IOException) {
            println("An error occurred while writing to the file: ${e.message}")
        }

        invested += value
        if (stockName !in myStocks) {
            myStocks[stockName] = 0.0
        }
        myStocks[stockName] = myStocks[stockName]?.plus(value) ?: value
        currentValue += value
    }

    fun getStockValue(stockName: String): Double {
        if (stockName !in myStocks) {
            return 0.0
        }
        return myStocks[stockName]!!
    }

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
            "Portfolio Distribution", // Chart title
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

fun listOfPortfolios(): List<String> {
    val currentDir = System.getProperty("user.dir")
    val virtualFolder = File("$currentDir/virtual")
    if (virtualFolder.exists() && virtualFolder.isDirectory) {
        return virtualFolder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
    }
    return emptyList()
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

    var portfoliosList = listOfPortfolios()
    var portfoliosList1 by remember { mutableStateOf(portfoliosList.toMutableList())}
    var portfolios = portfoliosList.associateWith { name -> VirtualPortfolio(name) }
    for ((_, portfolio) in portfolios) {
        portfolio.getData()
        portfolio.calculate(stocks)
    }
    var selectedCompany by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var newPortfolioName by remember { mutableStateOf("") }
    var showValue by remember { mutableStateOf<String?>(null) }
    var showBuyDialog by remember { mutableStateOf(false) }
    var stockName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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

            // Companies Grid
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

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(portfolios.size) { index ->
                    Button(onClick = { showValue = portfolios.keys.toList()[index] }) {
                        Text(text = portfolios.keys.toList()[index]) 
                    }
                }
                item {
                    Button(onClick = { showDialog = true }) {
                        Text("Add new portfolio")
                    }
                }
            }

            showValue?.let { portfolioName ->
                var portfolioo = portfolios[portfolioName]!!
                var portfolio_path = portfolioo.createPieChart()
                Text(
                    text = "Portfolio Value: $${String.format("%.2f", portfolioo.currentValue)}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(8.dp)
                )

                //W tym miejscu wyswietl ten wykres
                val imageBitmap = remember(portfolio_path) { loadImageFromFile(portfolio_path) }
                imageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Loaded Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .aspectRatio(1f)   
                            .padding(start = 16.dp) 
                    )
                } ?: Text(
                    "Error loading image",
                    color = MaterialTheme.colors.error,
                    modifier = Modifier
                        .padding(start = 16.dp)
                )

                Button(onClick = { showBuyDialog = true }) {
                    Text("Buy")
                }
                if (showBuyDialog) {
                    AlertDialog(
                        onDismissRequest = { showBuyDialog = false },
                        title = { Text("Buy Stock") },
                        text = {
                            Column {
                                TextField(
                                    value = stockName,
                                    onValueChange = { stockName = it },
                                    label = { Text("Stock Name") }
                                )
                                /*
                                TextField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    label = { Text("Amount") },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Number
                                    )
                                )
                                */
                                TextField(
                                    value = amount,
                                    onValueChange = { input ->
                                        amount = input.filter { it.isDigit() || it == '.' } // Akceptuje tylko cyfry i kropkę
                                    },
                                    label = { Text("Amount") }
                                )
                                if (errorMessage.isNotEmpty()) {
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val enteredAmount = amount.toDoubleOrNull()
                                    if (stockName.isBlank() || enteredAmount == null || enteredAmount < 1.0 || enteredAmount > 1_000_000_000.0) {
                                        errorMessage = when {
                                            stockName.isBlank() -> "Stock name cannot be empty."
                                            enteredAmount == null -> "Amount must be a valid number."
                                            enteredAmount < 1.0 || enteredAmount > 1_000_000_000.0 ->
                                                "Amount must be between 1.0 and 1,000,000,000.0."
                                            else -> ""
                                        }
                                    } else if (!stocks.containsKey(stockName)) {
                                        errorMessage = "Stock not found in the list."
                                    } else {
                                        errorMessage = ""
                                        var port = portfolios[portfolioName]!!
                                        port.newPos(stocks, stockName, enteredAmount)
                                        showBuyDialog = false
                                    }
                                }
                            ) {
                                Text("Buy")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showBuyDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            var errorMessage1 by remember { mutableStateOf("") }

            // Dialog for Adding New Portfolio
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add Portfolio") },
                    text = {
                        Column {
                            Text("Enter new portfolio name:")
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = newPortfolioName,
                                onValueChange = { newPortfolioName = it },
                                singleLine = true
                            )
                            // Jeśli istnieje komunikat o błędzie, wyświetlamy go
                            if (errorMessage1.isNotEmpty()) {
                                Text(
                                    text = errorMessage1,
                                    color = MaterialTheme.colors.error,
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            // Sprawdzamy, czy liczba portfeli nie przekracza 4
                            if (portfoliosList1.size >= 4) {
                                errorMessage1 = "You can only add up to 4 portfolios"
                            } else if (newPortfolioName.isNotBlank()) {
                                portfolios = portfolios + (newPortfolioName to VirtualPortfolio(newPortfolioName))
                                portfolios[newPortfolioName]!!.create()
                                portfoliosList1.add(newPortfolioName)
                                newPortfolioName = ""
                                showDialog = false
                                errorMessage1 = "" // Wyczyść komunikat o błędzie, jeśli dodano portfel
                            } else {
                                errorMessage1 = "Portfolio name cannot be empty"
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
            //selectedCompany?.let { company ->
            //    DisplayCompanyDetails(stocks[company]!!)
            //}

@Composable
fun DisplayCompanyDetails(stock: Stock) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nagłówek wyrównany po lewej stronie
        Text(
            "Choose Time Period",
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 275.dp, bottom = 16.dp), // Odstęp od lewej strony
            style = MaterialTheme.typography.h6
        )

        // Przyciski w jednym rzędzie obok siebie
        val options = listOf("2 years" to 1, "5 years" to 2, "10 years" to 3, "20 years" to 4)
        var selectedOption by remember { mutableStateOf(4) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 120.dp, bottom = 16.dp), // Odstęp całego rzędu od lewej strony
            horizontalArrangement = Arrangement.Start // Wszystkie przyciski po lewej stronie
        ) {
            options.forEach { (label, value) ->
                Button(
                    onClick = { selectedOption = value },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedOption == value) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                    ),
                    modifier = Modifier.padding(end = 50.dp) // Odstęp między przyciskami w rzędzie
                ) {
                    Text(label)
                }
            }
        }

        // Obrazek zajmujący połowę szerokości ekranu
        val plotPath = stock.get(selectedOption)
        val imageBitmap = remember(plotPath) { loadImageFromFile(plotPath) }

        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Loaded Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)   
                    .padding(start = 16.dp) 
            )
        } ?: Text(
            "Error loading image",
            color = MaterialTheme.colors.error,
            modifier = Modifier
                .padding(start = 16.dp) // Odstęp na wypadek błędu ładowania obrazka
        )
    }
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