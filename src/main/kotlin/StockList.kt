val companies = listOf(
    "APPLE", "GOOGLE", "AMAZON", "MICROSOFT",
    "TESLA", "META", "NETFLIX", "NVIDIA",
    "TWITTER", "INTEL", "ADOBE", "SPOTIFY",
    "ORACLE", "IBM", "AIRBNB",
    "LYFT", "SNAP", "ZOOM"
)

val stocks = mapOf(
    "APPLE" to Stock("AAPL", "APPLE", "Technology"),
    "GOOGLE" to Stock("GOOGL", "GOOGLE", "Technology"),
    "AMAZON" to Stock("AMZN", "AMAZON", "E-commerce"),
    "MICROSOFT" to Stock("MSFT", "MICROSOFT", "Technology"),
    "TESLA" to Stock("TSLA", "TESLA", "Automotive"),
    "META" to Stock("META", "META", "Technology"),
    "NETFLIX" to Stock("NFLX", "NETFLIX", "Entertainment"),
    "NVIDIA" to Stock("NVDA", "NVIDIA", "Semiconductors"),
    "TWITTER" to Stock("X", "TWITTER", "Social Media"),
    "INTEL" to Stock("INTC", "INTEL", "Semiconductors"),
    "ADOBE" to Stock("ADBE", "ADOBE", "Software"),
    "SPOTIFY" to Stock("SPOT", "SPOTIFY", "Entertainment"),
    "ORACLE" to Stock("ORCL", "ORACLE", "Technology"),
    "IBM" to Stock("IBM", "IBM", "Technology"),
    "AIRBNB" to Stock("ABNB", "AIRBNB", "Travel"),
    "LYFT" to Stock("LYFT", "LYFT", "Transportation"),
    "SNAP" to Stock("SNAP", "SNAP", "Social Media"),
    "ZOOM" to Stock("ZM", "ZOOM", "Communication")
)

// Initialize data for all stocks
suspend fun initializeAllStocks() {
    stocks.values.forEach { stock ->
        stock.initializeData()
    }
}