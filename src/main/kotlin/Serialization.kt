import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*

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