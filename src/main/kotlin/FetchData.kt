import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

val client = HttpClient()

suspend fun fetchStockData(symbol: String, apiKey: String): String {
    val url = "https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol=$symbol&apikey=$apiKey"
    return client.get(url).bodyAsText()
}