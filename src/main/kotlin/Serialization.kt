import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.*

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
    @SerialName("7. dividend amount") val dividendAmount: Double
)