import androidx.compose.desktop.ui.tooling.preview.Preview
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

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

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

// Main app function
@Composable
@Preview
fun App() {

    var selectedCompany by remember { mutableStateOf<String?>("APPLE") }

    MaterialTheme {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
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
            }
        
    

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
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
            }
        
        
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        DisplayVirtual(stocks)
                    }

                    selectedCompany?.let { company ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            DisplayCompanyDetails(stocks[company]!!)
                        }
                    }
                }
            }
        }
        
        /*
        LazyColumn(modifier = Modifier.padding(16.dp)) {
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

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    DisplayVirtual(stocks)
                }

                selectedCompany?.let { company ->
                    Column(
                        modifier = Modifier
                            .weight(1f) 
                            .fillMaxHeight()
                    ) {
                        DisplayCompanyDetails(stocks[company]!!)
                    }
                }
            }

        }
        */
    }
}

// Function to display virtual portfolios
@Composable
fun DisplayVirtual(stocks: Map<String, Stock>){
    var portfoliosList = listOfPortfolios()
    var portfoliosList1 by remember { mutableStateOf(portfoliosList.toMutableList())}
    var portfolios = portfoliosList.associateWith { name -> VirtualPortfolio(name) }
    for ((_, portfolio) in portfolios) {
        portfolio.getData()
        portfolio.calculatePortfolioValue(stocks)
    }

    var showDialog by remember { mutableStateOf(false) }
    var newPortfolioName by remember { mutableStateOf("") }
    var showValue by remember { mutableStateOf<String?>(null) }
    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    var stockName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var amount1 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var errorMessage2 by remember { mutableStateOf("") }
    /*
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        */

    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        portfolios.keys.forEach { portfolioName ->
            Button(onClick = { showValue = portfolioName }) {
                Text(text = portfolioName)
            }
        }
        Button(onClick = { showDialog = true }) {
            Text("Add new portfolio")
        }
    }
    
    val minimum = 1.0
    val maximum = 1_000_000_000.0
    showValue?.let { portfolioName ->
        val showPortfolio = portfolios[portfolioName]!!
        val portfolioPath = remember(portfolioName) { showPortfolio.createPieChart() }

        Text(
            text = "Portfolio Value: $${String.format("%.2f", showPortfolio.currentValue)}, Money Invested: \$${
                String.format("%.2f", showPortfolio.invested)
            }",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(8.dp)
        )

        val imageBitmap = remember(portfolioPath) { loadImageFromFile(portfolioPath) }
        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Loaded Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .aspectRatio(1f)
                    .padding(start = 16.dp)
            )
        } ?: Text(
            "Error loading image",
            color = MaterialTheme.colors.error,
            modifier = Modifier.padding(start = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = { showBuyDialog = true }) {
                Text("Buy")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showSellDialog = true }) {
                Text("Sell")
            }
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

                        TextField(
                            value = amount,
                            onValueChange = { input ->
                                amount = input.filter { it.isDigit() || it == '.' } 
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
                            if (stockName.isBlank() || enteredAmount == null || enteredAmount < minimum || enteredAmount > maximum) {
                                errorMessage = when {
                                    stockName.isBlank() -> "Stock name cannot be empty."
                                    enteredAmount == null -> "Amount must be a valid number."
                                    enteredAmount < minimum || enteredAmount > maximum ->
                                        "Amount must be between ${minimum} and ${maximum}."
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

        if (showSellDialog) {
            // User pressed Sell button
            AlertDialog(
                onDismissRequest = { showSellDialog = false },
                title = { Text("Sell Stock") },
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
                            value = amount1,
                            onValueChange = { input ->
                                amount1 = input.filter { it.isDigit() || it == '.' } 
                            },
                            label = { Text("Amount") }
                        )
                        if (errorMessage2.isNotEmpty()) {
                            Text(
                                text = errorMessage2,
                                color = MaterialTheme.colors.error,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val enteredAmount = amount1.toDoubleOrNull()
                            var stockAmount = portfolios[portfolioName]!!.getStockValue(stockName)
                            if (stockName.isBlank() || enteredAmount == null || enteredAmount < minimum || enteredAmount > stockAmount) {
                                errorMessage2 = when {
                                    stockName.isBlank() -> "Stock name cannot be empty."
                                    enteredAmount == null -> "Amount must be a valid number."
                                    enteredAmount < minimum || enteredAmount > stockAmount ->
                                        "Amount must be between ${minimum} and ${stockAmount}"
                                    else -> ""
                                }
                            } else if (!stocks.containsKey(stockName)) {
                                errorMessage2 = "Stock not found in the list."
                            } else {
                                errorMessage2 = ""
                                var port1 = portfolios[portfolioName]!!
                                port1.newPos(stocks, stockName, -enteredAmount)
                                showSellDialog = false
                            }
                        }
                    ) {
                        Text("Sell")
                    }
                },
                dismissButton = {
                    Button(onClick = { showSellDialog = false }) {
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
                    // Showing error message if it exists
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
                    // Check whether there are more than 4 portfolios
                    if (portfoliosList1.size >= 4) {
                        errorMessage1 = "You can only add up to 4 portfolios"
                    } else if (newPortfolioName.isNotBlank()) {
                        portfolios = portfolios + (newPortfolioName to VirtualPortfolio(newPortfolioName))
                        portfolios[newPortfolioName]!!.create()
                        portfoliosList1.add(newPortfolioName)
                        newPortfolioName = ""
                        showDialog = false
                        errorMessage1 = "" 
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

// Function to display charts showing the company's price over time
@Composable
fun DisplayCompanyDetails(stock: Stock) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center 
    ) {

        Text(
            "Choose Time Period",
            modifier = Modifier
                .padding(bottom = 16.dp), 
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center 
        )

        val options = listOf("2 years" to TimePeriod.LAST_24_MONTHS, "5 years" to TimePeriod.LAST_60_MONTHS, "10 years" to TimePeriod.LAST_120_MONTHS, "20 years" to TimePeriod.ALL_DATA)
        var selectedOption by remember { mutableStateOf(TimePeriod.ALL_DATA) }

        Row(
            modifier = Modifier
                .fillMaxWidth(), 
            horizontalArrangement = Arrangement.Center
        ) {
            options.forEach { (label, value) ->
                Button(
                    onClick = { selectedOption = value },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedOption == value) MaterialTheme.colors.primary else MaterialTheme.colors.surface
                    ),
                    modifier = Modifier.padding(end = 8.dp) 
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) 
        
        // Displaying the image
        val plotPath = stock.get(selectedOption)
        val imageBitmap = remember(plotPath) { loadImageFromFile(plotPath) }

        imageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Loaded Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.75f) 
                    .aspectRatio(1f)
                    .padding(top = 16.dp) 
            )
        } ?: Text(
            "Error loading image",
            color = MaterialTheme.colors.error,
            textAlign = TextAlign.Center, 
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

fun main() = application {
    deletePngFilesInImagesFolder()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Desktop Application",
        state = WindowState(width = 1280.dp, height = 720.dp)
    ) {
        App()
    }
}
