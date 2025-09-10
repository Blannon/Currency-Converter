package com.blannonnetwork.currencyconveter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blannonnetwork.currencyconveter.presentation.ui.theme.currencyConverterTheme
import org.koin.androidx.compose.koinViewModel
import com.blannonnetwork.currencyconveter.domain.Currency as AppCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun exchangeScreenCore(viewModel: ExchangeViewModel = koinViewModel()) {
    ExchangeScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
    )
}

private fun calculateExchangeRate(state: ExchangeState): String {
    return if (state.result.isNotEmpty() && state.amount.isNotEmpty()) {
        try {
            val rate = state.result.toDouble() / state.amount.toDouble()
            String.format("%.4f", rate)
        } catch (e: NumberFormatException) {
            "0.0000"
        }
    } else {
        "0.0000"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangeScreen(
    state: ExchangeState,
    onAction: (ExchangeAction) -> Unit,
) {
    var isSelectingFromCurrency by rememberSaveable { mutableStateOf(true) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val exchangeRateText by remember(state.amount, state.result) {
        derivedStateOf {
            "1 ${state.from.code} = ${calculateExchangeRate(state)} ${state.to.code}"
        }
    }

    if (showBottomSheet) {
        currencyPickerBottomSheet(
            currencies = state.allCurrencies,
            favoriteCurrencies = state.favoriteCurrencies,
            onCurrencySelected = { currency ->
                val index = state.allCurrencies.indexOf(currency)
                if (index != -1) {
                    if (isSelectingFromCurrency) {
                        onAction(ExchangeAction.SelectedFrom(index))
                    } else {
                        onAction(ExchangeAction.SelectedTo(index))
                    }
                }
            },
            onToggleFavorite = { currencyCode ->
                onAction(ExchangeAction.ToggleFavorite(currencyCode))
            },
            onDismiss = { showBottomSheet = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.blannonnetwork.currencyconveter.R.string.currency_converter),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                    )
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        tryPinWidget(context)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add widget")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isLoading && state.allCurrencies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.blannonnetwork.currencyconveter.R.string.loading_currencies),
                        modifier = Modifier.padding(start = 40.dp),
                    )
                }
            } else if (state.error != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Last Updated Info
            if (!state.isLoading || state.allCurrencies.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text =
                            androidx.compose.ui.res.stringResource(
                                id = com.blannonnetwork.currencyconveter.R.string.last_updated,
                                getCurrentTimestamp(),
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // From Currency Card
                currencyInputCard(
                    currency = state.from,
                    amount = state.amount,
                    onCurrencyClick = {
                        isSelectingFromCurrency = true
                        showBottomSheet = true
                    },
                    onAmountChange = { newAmount ->
                        onAction(ExchangeAction.SetAmount(newAmount))
                    },
                )
                currencyDisplayCard(
                    currency = state.to,
                    amount = state.result,
                    onCurrencyClick = {
                        isSelectingFromCurrency = false
                        showBottomSheet = true
                    },
                    exchangeRate = exchangeRateText,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(getQuickAccessCurrencies(state.allCurrencies, state.favoriteCurrencies)) { currency ->
                        quickCurrencyItem(
                            currency = currency,
                            fromCurrency = state.from,
                            conversionRate = state.quickAccessRates[currency.code] ?: "",
                            isFavorite = state.favoriteCurrencies.contains(currency.code),
                            onClick = {
                                onAction(ExchangeAction.SelectedTo(state.allCurrencies.indexOf(currency)))
                            },
                            onToggleFavorite = {
                                onAction(ExchangeAction.ToggleFavorite(currency.code))
                            },
                        )
                    }
                    item {
                        Card(
                            modifier =
                                Modifier
                                    .width(120.dp)
                                    .clickable {
                                        isSelectingFromCurrency = false
                                        showBottomSheet = true
                                    },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .padding(12.dp)
                                        .height(56.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add more currencies",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Add More",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }

//                Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier.width(170.dp),
//                        .background(Color(0xFFFF6D00)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = androidx.compose.ui.res.stringResource(com.blannonnetwork.currencyconveter.R.string.add_currency))
                }
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = androidx.compose.ui.res.stringResource(com.blannonnetwork.currencyconveter.R.string.mid_market_rates),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun currencyInputCard(
    currency: AppCurrency,
    amount: String,
    onCurrencyClick: () -> Unit,
    onAmountChange: (String) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onCurrencyClick),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(getCurrencyColor(currency.code)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = currency.code.take(1),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currency.code,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select currency",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = {
                    Text(
                        "0.0",
                        fontSize = 20.sp,
                        color = Color.Gray,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(120.dp),
                singleLine = true,
                textStyle =
                    androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = TextAlign.End,
                    ),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun currencyDisplayCard(
    currency: AppCurrency,
    amount: String,
    onCurrencyClick: () -> Unit,
    exchangeRate: String,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onCurrencyClick),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(getCurrencyColor(currency.code)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = currency.code.take(1),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = currency.code,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select currency",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray,
                    )
                }
                Text(
                    text = if (amount.isNotEmpty()) "$amount" else "0.00",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exchangeRate,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
    }
}

@Composable
fun quickCurrencyItem(
    currency: AppCurrency,
    fromCurrency: AppCurrency,
    conversionRate: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .width(200.dp)
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(getCurrencyColor(currency.code)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = currency.code.take(1),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currency.code,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    if (conversionRate.isNotEmpty()) {
                        "≈ $conversionRate ${currency.code}"
                    } else {
                        androidx.compose.ui.res.stringResource(
                            com.blannonnetwork.currencyconveter.R.string.loading,
                        )
                    },
                style = MaterialTheme.typography.bodySmall,
                color = if (conversionRate.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

fun getQuickAccessCurrencies(
    allCurrencies: List<AppCurrency>,
    favoriteCodes: List<String>,
): List<AppCurrency> {
    return allCurrencies.filter { it.code in favoriteCodes }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun currencyPickerBottomSheet(
    currencies: List<AppCurrency>,
    favoriteCurrencies: List<String>,
    onCurrencySelected: (AppCurrency) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchText by remember { mutableStateOf("") }

    val filteredCurrencies by remember(searchText, currencies) {
        derivedStateOf {
            if (searchText.isEmpty()) {
                currencies
            } else {
                currencies.filter { currency ->
                    currency.name.contains(searchText, ignoreCase = true) ||
                        currency.code.contains(searchText, ignoreCase = true)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.LightGray),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search currencies...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )

            if (currencies.isEmpty()) {
                Text(
                    "No currencies available. Please check your connection.",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                )
            } else if (filteredCurrencies.isEmpty()) {
                Text(
                    "No matching currencies found",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray,
                )
            } else {
                LazyColumn {
                    // Show favorites first if no search
                    if (searchText.isEmpty()) {
                        item {
                            Text(
                                "Favorites",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }

                        items(currencies.filter { it.code in favoriteCurrencies }) { currency ->
                            currencyItem(
                                currency = currency,
                                isFavorite = true,
                                onToggleFavorite = { onToggleFavorite(currency.code) },
                                onClick = {
                                    onCurrencySelected(currency)
                                    onDismiss()
                                },
                            )
                        }

                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                "All Currencies",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }

                    items(filteredCurrencies) { currency ->
                        currencyItem(
                            currency = currency,
                            isFavorite = favoriteCurrencies.contains(currency.code),
                            onToggleFavorite = { onToggleFavorite(currency.code) },
                            onClick = {
                                onCurrencySelected(currency)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun currencyItem(
    currency: AppCurrency,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCurrencyColor(currency.code)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = currency.code.take(1),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${currency.code} - ${currency.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        IconButton(
            onClick = {
                onToggleFavorite()
                // Don't close the sheet when toggling favorite
            },
        ) {
            Icon(
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) Color.Red else Color.Gray,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            Icons.Default.ArrowForward,
            contentDescription = "Select",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// Helper Functions
@Composable
fun getCurrentTimestamp(): String {
    // Compatible with minSdk 24: use java.text.SimpleDateFormat
    val locale = java.util.Locale.getDefault()
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", locale)
    return sdf.format(java.util.Date())
}

fun getQuickAccessCurrencies(allCurrencies: List<AppCurrency>): List<AppCurrency> {
    val quickCodes = listOf("AUD", "CAD", "AMD", "ANG")
    return allCurrencies.filter { it.code in quickCodes }
}

fun tryPinWidget(context: android.content.Context) {
    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
    val provider = android.content.ComponentName(context, com.blannonnetwork.currencyconveter.widget.CurrencyWidgetReceiver::class.java)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val successCallback = null as android.app.PendingIntent?
            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
        } else {
            // Fallback: open widget picker
            val intent =
                android.content.Intent("android.intent.action.MAIN").apply {
                    addCategory("android.intent.category.HOME")
                }
            context.startActivity(intent)
        }
    } else {
        // Pre-O fallback
        val intent =
            android.content.Intent("android.intent.action.MAIN").apply {
                addCategory("android.intent.category.HOME")
            }
        context.startActivity(intent)
    }
}

fun getCurrencyColor(code: String): Color {
    return when (code) {
        "USD" -> Color(0xFF1E88E5)
        "EUR" -> Color(0xFF1976D2)
        "GBP" -> Color(0xFFD32F2F)
        "CAD" -> Color(0xFFD32F2F)
        "AUD" -> Color(0xFF1976D2)
        "AMD" -> Color(0xFFFFB74D)
        "ANG" -> Color(0xFF42A5F5)
        else -> Color(0xFFFF6D00).copy(alpha = 0.7f)
    }
}

@Preview(showBackground = true)
@Composable
private fun exchangeScreenPreview() {
    currencyConverterTheme {
        ExchangeScreen(
            state =
                ExchangeState(
                    allCurrencies =
                        listOf(
                            AppCurrency("United States Dollar", "USD"),
                            AppCurrency("Euro", "EUR"),
                            AppCurrency("Japanese Yen", "JPY"),
                            AppCurrency("British Pound", "GBP"),
                            AppCurrency("Canadian Dollar", "CAD"),
                            AppCurrency("Australian Dollar", "AUD"),
                            AppCurrency("Swiss Franc", "CHF"),
                            AppCurrency("Chinese Yuan", "CNY"),
                            AppCurrency("Indian Rupee", "INR"),
                            AppCurrency("Russian Ruble", "RUB"),
                        ),
                    from = AppCurrency("United States Dollar", "USD"),
                    to = AppCurrency("Euro", "EUR"),
                    amount = "100",
                    result = "85.00",
                    isLoading = false,
                ),
            onAction = {},
        )
    }
}
