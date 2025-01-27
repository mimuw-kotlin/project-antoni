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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color

// Main app function
@Composable
fun App() {
    var selectedCompany by remember { mutableStateOf<String?>("APPLE") }
    var showBuySell by remember { mutableStateOf(true) }

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
                        val stockDecision = stocks[company]?.stockDecision ?: "NONE"
                        val backgroundColor = when {
                            showBuySell && stockDecision == "BUY" -> Color.Green
                            showBuySell && stockDecision == "SELL" -> Color.Red
                            showBuySell -> Color.Gray
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCompany = company }
                                .padding(8.dp)
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = company,
                                style = MaterialTheme.typography.body1,
                                color = if (showBuySell) Color.White else Color.Unspecified
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showBuySell = ! showBuySell }) {
                    if(showBuySell) {
                        Text(text = "Turn stock recommendation off")
                    } else{
                        Text(text = "Turn stock recommendation on")
                    }
                }
            }

            item {
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
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Screener:",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    style = MaterialTheme.typography.h6.copy(
                        textAlign = TextAlign.Center
                    )
                )
                DisplayScreener()
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
                CompanyObservationScreen()
            }
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Financial Ratio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                    style = MaterialTheme.typography.h6.copy(
                        textAlign = TextAlign.Center
                    )
                )
                FinancialRatio()
            }
        }
    }
}

// Function to display virtual portfolios
@Composable
fun DisplayVirtual(stocks: Map<String, Stock>){
    val portfoliosList = listOfPortfolios()
    //val portfoliosList1 by remember { mutableStateOf(portfoliosList.toMutableList())}
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
        val portfolioPath = showPortfolio.createPieChart()

        Text(
            text = "Portfolio Value: $${String.format("%.2f", showPortfolio.currentValue)}, Money Invested: \$${
                String.format("%.2f", showPortfolio.invested)
            }",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(8.dp)
        )

        val imageBitmap = loadImageFromFile(portfolioPath)
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
                                portfolios[portfolioName]!!.newPos(stocks, stockName, enteredAmount)
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
                            val stockAmount = portfolios[portfolioName]!!.getStockValue(stockName)
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
                                portfolios[portfolioName]!!.newPos(stocks, stockName, -enteredAmount)
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
                    if (portfoliosList.size >= 4) {
                        errorMessage1 = "You can only add up to 4 portfolios"
                    } else if (newPortfolioName.isNotBlank()) {
                        portfolios = portfolios + (newPortfolioName to VirtualPortfolio(newPortfolioName))
                        portfolios[newPortfolioName]!!.create()
                        portfoliosList.add(newPortfolioName)
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
        val plotPath = stock.getPlot(selectedOption)
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

@Composable
fun DisplayScreener() {
    var expandedIndustry by remember { mutableStateOf(false) }
    var expandedCriteria by remember { mutableStateOf(false) }
    var selectedIndustry by remember { mutableStateOf("Select Industry") }
    val selectedCriteriaList = remember { mutableStateListOf<String>() }
    val criteriaParameters = remember { mutableStateMapOf<String, Pair<Double, Double>>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Industry Window
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Industry",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box {
                    OutlinedButton(
                        onClick = { expandedIndustry = !expandedIndustry },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedIndustry)
                    }
                    DropdownMenu(
                        expanded = expandedIndustry,
                        onDismissRequest = { expandedIndustry = false }
                    ) {
                        industries.forEach { industry ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedIndustry = industry
                                    expandedIndustry = false
                                }
                            ) {
                                Text(industry)
                            }
                        }
                    }
                }
            }

            // Criteria Window
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Criteria",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box {
                    OutlinedButton(
                        onClick = { expandedCriteria = !expandedCriteria },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Criteria")
                    }
                    DropdownMenu(
                        expanded = expandedCriteria,
                        onDismissRequest = { expandedCriteria = false }
                    ) {
                        criteria.forEach { criterion ->
                            DropdownMenuItem(
                                onClick = {
                                    if (!selectedCriteriaList.contains(criterion)) {
                                        selectedCriteriaList.add(criterion)
                                        criteriaParameters[criterion] =
                                            Pair(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                                    }
                                    expandedCriteria = false
                                }
                            ) {
                                Text(criterion)
                            }
                        }
                    }
                }
            }
        }

        // Displayed options and parameters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Selected Options:",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Last Selected Industry
            if(selectedIndustry != "Select Industry"){
                Text(text = "Industry: $selectedIndustry")
            }

            // List of Criteria
            if (selectedCriteriaList.isNotEmpty()) {
                selectedCriteriaList.forEach { criterion ->
                    CriterionInput(
                        criterion = criterion,
                        criteriaParameters = criteriaParameters
                    )
                }
            }
        }
        // Reset and Search Button
        Button(
            onClick = {
                prepareRequirements(criteriaParameters, requirements, selectedIndustry)
                companyScreener = myScreener.findStocksMatchingRequirements(requirements)
                requirements = mutableListOf()
                selectedIndustry = "Select Industry"
                selectedCriteriaList.clear()
                criteriaParameters.clear()
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Search")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                "Results:",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                style = MaterialTheme.typography.h6.copy(
                    textAlign = TextAlign.Center
                )
            )
            companyScreener.forEach { company ->
                Text(
                    text = company,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CriterionInput(
    criterion: String,
    criteriaParameters: MutableMap<String, Pair<Double, Double>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = criterion)
        when (criterion) {
            "Current Value" -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = criteriaParameters[criterion]?.first
                            ?.takeIf { it != Double.NEGATIVE_INFINITY }
                            ?.toString() ?: "",
                        onValueChange = {
                            criteriaParameters[criterion] = Pair(
                                it.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY,
                                criteriaParameters[criterion]?.second ?: Double.POSITIVE_INFINITY
                            )
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = criteriaParameters[criterion]?.second
                            ?.takeIf { it != Double.POSITIVE_INFINITY }
                            ?.toString() ?: "",
                        onValueChange = {
                            criteriaParameters[criterion] = Pair(
                                criteriaParameters[criterion]?.first ?: Double.NEGATIVE_INFINITY,
                                it.toDoubleOrNull() ?: Double.POSITIVE_INFINITY
                            )
                        },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            "One Year Annual Growth", "Two Years Annual Growth", "Five Years Annual Growth" -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = criteriaParameters[criterion]?.first
                            ?.takeIf { it != Double.NEGATIVE_INFINITY }
                            ?.toString() ?: "",
                        onValueChange = { input ->
                            criteriaParameters[criterion] = Pair(
                                input.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY,
                                criteriaParameters[criterion]?.second ?: Double.POSITIVE_INFINITY
                            )
                        },
                        label = { Text("Min (%)") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = { Text("%") }
                    )
                    TextField(
                        value = criteriaParameters[criterion]?.second
                            ?.takeIf { it != Double.POSITIVE_INFINITY }
                            ?.toString() ?: "",
                        onValueChange = { input ->
                            criteriaParameters[criterion] = Pair(
                                criteriaParameters[criterion]?.first ?: Double.NEGATIVE_INFINITY,
                                input.toDoubleOrNull() ?: Double.POSITIVE_INFINITY
                            )
                        },
                        label = { Text("Max (%)") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = { Text("%") }
                    )
                }
            }
            "Dividend Amount" -> {
                TextField(
                    value = criteriaParameters[criterion]?.first
                        ?.takeIf { it != Double.NEGATIVE_INFINITY }
                        ?.toString() ?: "",
                    onValueChange = {
                        criteriaParameters[criterion] = Pair(
                            it.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY
                        )
                    },
                    label = { Text("Min") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CompanyObservationScreen() {
    var selectedCompany by remember { mutableStateOf<String?>(null) }
    var observationInput by remember { mutableStateOf(TextFieldValue("")) }
    var previousObservations by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Observations:",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            style = MaterialTheme.typography.h6.copy(
                textAlign = TextAlign.Center
            )
        )
        Text("Select a company:")

        Spacer(modifier = Modifier.height(8.dp))

        companies.forEach { company ->
            Text(
                text = company,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        selectedCompany = company
                        previousObservations = getPreviousObservation(stocks[company]!!)
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedCompany?.let { company ->
            Text("Selected Company: $company")

            Spacer(modifier = Modifier.height(8.dp))

            // Previous observations
            Text("Previous Observations:")
            Text(
                text = previousObservations.ifEmpty { "No observations available." },
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text field for adding a new observation
            Text("Add New Observation:")

            BasicTextField(
                value = observationInput,
                onValueChange = { observationInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(100.dp)
                    .border(1.dp, Color.Gray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Row {
                Button(
                    onClick = {
                        if (observationInput.text.isNotEmpty()) {
                            newObservation(stocks[company]!!, observationInput.text)
                            observationInput = TextFieldValue("")
                            previousObservations = getPreviousObservation(stocks[company]!!)
                        }
                    }
                ) {
                    Text("Save Observation")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { selectedCompany = null }) {
                    Text("Back to Companies")
                }
            }
        }
    }
}

@Composable
fun FinancialRatio() {
    var selectedCompany by remember { mutableStateOf<String?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(onClick = {
            isDialogOpen = true
        }) {
            Text(text = "Select Company")
        }

        // Show the dialog when isDialogOpen is true
        if (isDialogOpen) {
            CompanySelectionDialog(
                onCompanySelected = { company ->
                    selectedCompany = company
                    isDialogOpen = false
                },
                onDismiss = { isDialogOpen = false }
            )
        }

        // Display the selected company's financial ratios
        selectedCompany?.let { company ->
            stocks[company]?.let { stock ->
                val financialCalculator = stock.financial

                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(text = "Financial Ratios for $company:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Loop through all calculated financial ratios and display them
                    financialCalculator.calculateAllRatios().forEach { (ratioName, ratioValue) ->
                        Text(
                            text = "$ratioName: ${ratioValue?.let { "%.2f".format(it) } ?: "N/A"}",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompanySelectionDialog(
    onCompanySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // A dialog to display a list of companies
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select a Company") },
        text = {
            LazyColumn {
                items(companies) { company ->
                    // Display each company in the list, allowing the user to select one
                    Text(
                        text = company,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCompanySelected(company) }
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            // Close button to dismiss the dialog
            Button(onClick = onDismiss) {
                Text(text = "Close")
            }
        }
    )
}