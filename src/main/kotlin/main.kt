/*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*

import org.knowm.xchart.XYChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.style.Styler

import java.text.SimpleDateFormat
import java.util.Date

val client = HttpClient()

suspend fun fetchStockData(symbol: String, apiKey: String): String {
    val url = "https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY&symbol=$symbol&apikey=$apiKey"
    return client.get(url).bodyAsText()
}

@Serializable
data class MonthlyTimeSeriesResponse(
        val `Meta Data`: Map<String, String>,
        @SerialName("Monthly Time Series")
        val monthlyData: Map<String, StockData>
)

@Serializable
data class StockData(
        @SerialName("1. open") val open: String,
        @SerialName("2. high") val high: String,
        @SerialName("3. low") val low: String,
        @SerialName("4. close") val close: String,
        @SerialName("5. volume") val volume: String
)

fun draw(averages: List<Pair<String, Double>>) {
    val filteredAverages = averages.take(12).reversed()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    val dates = filteredAverages.map { dateFormat.parse(it.first) }
    val values = filteredAverages.map { it.second }

    val chart = XYChart(800, 600).apply {
        title = "Average Price in time"
        xAxisTitle = "Date"
        yAxisTitle = "Price"

        styler.apply {
            //chartTitleVisible = true
            xAxisLabelRotation = 45
            legendPosition = Styler.LegendPosition.InsideNW
        }

        addSeries("Average Price", dates, values)
    }

    SwingWrapper(chart).displayChart()
}

class Stock(val symbol: String) {
    var averages: List<Pair<String, Double>> = emptyList()

    init {
        // Uruchomienie korutyny w głównym wątku
        runBlocking {
            val jsonResponse = fetchStockData(symbol, "ZYDCWO2NI4MP4WUT")
            val json = Json { ignoreUnknownKeys = true }
            val parsedResponse = json.decodeFromString<MonthlyTimeSeriesResponse>(jsonResponse)
            averages = parsedResponse.monthlyData.map { (date, data) ->
                val high = data.high.toDouble()
                val low = data.low.toDouble()
                val average = (high + low) / 2
                date to average
            }
        }
    }
}

//suspend

fun main() {
    /*

    val apiKey = "ZYDCWO2NI4MP4WUT"
    val symbol = "IBM"

    val jsonResponse = fetchStockData(symbol, apiKey)

    val json = Json { ignoreUnknownKeys = true }
    val parsedResponse = json.decodeFromString<MonthlyTimeSeriesResponse>(jsonResponse)

    val averages = parsedResponse.monthlyData.map { (date, data) ->
        val high = data.high.toDouble()
        val low = data.low.toDouble()
        val average = (high + low) / 2
        date to average
    }

    draw(averages)
     */

    val stocks = mapOf(
            "APPLE" to Stock("AAPL"),
            "GOOGLE" to Stock("GOOGL"),
            "AMAZON" to Stock("AMZN"),
            "MICROSOFT" to Stock("MSFT"),
            "TESLA" to Stock("TSLA"),
            "NETFLIX" to Stock("NFLX"),
            "META" to Stock("META"),
            "NVIDIA" to Stock("NVDA"),
            "SPY" to Stock("SPY"),
            "IBM" to Stock("IBM")
    )
    val stock = stocks["APPLE"]!!
    draw(stock.averages)
    /*
    for(i in 0..4){
        val input = readLine()
        if (stocks.containsKey(input)) {
            val stock = stocks[input]!!
            draw(stock.averages)
        } else {
            println("Nie znaleziono takiego klucza w mapie.")
        }
    }
     */
*/

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*


val client = HttpClient()

suspend fun fetchStockData(symbol: String, apiKey: String): String {
    val url = "https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY&symbol=$symbol&apikey=$apiKey"
    return client.get(url).bodyAsText()
}

@Serializable
data class MonthlyTimeSeriesResponse(
        val `Meta Data`: Map<String, String>,
        @SerialName("Monthly Time Series")
        val monthlyData: Map<String, StockData>
)

@Serializable
data class StockData(
        @SerialName("1. open") val open: String,
        @SerialName("2. high") val high: String,
        @SerialName("3. low") val low: String,
        @SerialName("4. close") val close: String,
        @SerialName("5. volume") val volume: String
)


class Stock(val symbol: String) {
    var averages: List<Pair<String, Double>> = emptyList()
    init {
        runBlocking {
            val jsonResponse = fetchStockData(symbol, "33ZQ4YW7O1VKD01X")
            println(jsonResponse)
            val json = Json { ignoreUnknownKeys = true }
            val parsedResponse = json.decodeFromString<MonthlyTimeSeriesResponse>(jsonResponse)
            averages = parsedResponse.monthlyData.map { (date, data) ->
                val high = data.high.toDouble()
                val low = data.low.toDouble()
                val average = (high + low) / 2
                date to average
            }
        }
    }
     fun getFirst(): Double? {
        return averages.firstOrNull()?.second
    }
}

@Composable
fun App() {
    val companies = listOf(
        "APPLE", "GOOGLE", "AMAZON", "MICROSOFT"
    )

    val stocks = mapOf(
            "APPLE" to Stock("AAPL"),
            "GOOGLE" to Stock("GOOGL"),
            "AMAZON" to Stock("AMZN"),
            "MICROSOFT" to Stock("MSFT")
    )

    var selectedCompany by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            Text("Choose company to analyse:", style = MaterialTheme.typography.h6)

            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            LazyColumn {
                items(companies) { company ->
                    Row(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .clickable { selectedCompany = company }
                            .padding(8.dp)
                    ) {
                        Text(company, style = MaterialTheme.typography.body1)
                    }
                }
            }

            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            selectedCompany?.let {
                val stock = stocks[it]!!

                Text("Wybrano: ${stock.getFirst()}", style = MaterialTheme.typography.h5)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Lista Firm") {
        App()
    }
}