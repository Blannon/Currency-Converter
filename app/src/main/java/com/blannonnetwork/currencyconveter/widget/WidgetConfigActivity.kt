package com.blannonnetwork.currencyconveter.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.blannonnetwork.currencyconveter.domain.Currency
import com.blannonnetwork.currencyconveter.presentation.ui.theme.currencyConverterTheme
import kotlinx.coroutines.launch

class GlanceWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("GlanceWidgetConfig", "GlanceWidgetConfigActivity onCreate started")
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.d("GlanceWidgetConfig", "Widget ID: $appWidgetId")
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("GlanceWidgetConfig", "Invalid widget ID, finishing activity")
            finish()
            return
        }
        val widgetPrefs = com.blannonnetwork.currencyconveter.widget.data.WidgetPrefs(this)
        val existingFrom = widgetPrefs.getFrom(appWidgetId, "USD")
        val existingTo = widgetPrefs.getTo(appWidgetId, "EUR")
        val existingAmount = widgetPrefs.getAmount(appWidgetId, "1.00")

        try {
            setContent {
                currencyConverterTheme {
                    WidgetConfigScreen(
                        initialFromCurrency = existingFrom,
                        initialToCurrency = existingTo,
                        initialAmount = existingAmount,
                        onConfigComplete = { fromCurrency, toCurrency, amount ->
                            Log.d("GlanceWidgetConfig", "Configuration completed: $fromCurrency -> $toCurrency, amount: $amount")

                            try {
                                saveWidgetConfig(fromCurrency, toCurrency, amount)
                                updateGlanceWidget()
                            } catch (e: Exception) {
                                Log.e("GlanceWidgetConfig", "Error in configuration completion", e)
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        },
                    )
                }
            }
            Log.d("GlanceWidgetConfig", "setContent completed successfully")
        } catch (e: Exception) {
            Log.e("GlanceWidgetConfig", "Error setting content", e)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun saveWidgetConfig(
        fromCurrency: String,
        toCurrency: String,
        amount: String,
    ) {
        try {
            val widgetPrefs = com.blannonnetwork.currencyconveter.widget.data.WidgetPrefs(this)
            widgetPrefs.setConfig(appWidgetId, fromCurrency, toCurrency, amount)
            widgetPrefs.setLoadingState(appWidgetId, false)

            Log.d("GlanceWidgetConfig", "Widget preferences saved successfully")
        } catch (e: Exception) {
            Log.e("GlanceWidgetConfig", "Error saving widget config", e)
            throw e
        }
    }

    private fun updateGlanceWidget() {
        lifecycleScope.launch {
            try {
                val glanceManager = GlanceAppWidgetManager(this@GlanceWidgetConfigActivity)
                val glanceIds = glanceManager.getGlanceIds(CurrencyGlanceWidget::class.java)
                val glanceId =
                    glanceIds.firstOrNull { gid ->
                        try {
                            glanceManager.getAppWidgetId(gid) == appWidgetId
                        } catch (e: Exception) {
                            false
                        }
                    }

                if (glanceId != null) {
                    Log.d("GlanceWidgetConfig", "Updating Glance widget with ID: $glanceId")
                    CurrencyGlanceWidget().update(this@GlanceWidgetConfigActivity, glanceId)
                } else {
                    Log.w("GlanceWidgetConfig", "Could not find matching Glance ID for app widget ID: $appWidgetId")
                }

                val resultValue =
                    Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                setResult(RESULT_OK, resultValue)
                Log.d("GlanceWidgetConfig", "Widget configuration completed successfully")
                finish()
            } catch (e: Exception) {
                Log.e("GlanceWidgetConfig", "Error updating Glance widget", e)
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    initialFromCurrency: String = "USD",
    initialToCurrency: String = "EUR",
    initialAmount: String = "1.00",
    onConfigComplete: (String, String, String) -> Unit,
) {
    var fromCurrency by remember { mutableStateOf(initialFromCurrency) }
    var toCurrency by remember { mutableStateOf(initialToCurrency) }
    var amount by remember { mutableStateOf(TextFieldValue(initialAmount)) }
    var showFromCurrencyPicker by remember { mutableStateOf(false) }
    var showToCurrencyPicker by remember { mutableStateOf(false) }

    val commonCurrencies =
        remember {
            listOf(
                Currency("United States Dollar", "USD"),
                Currency("Euro", "EUR"),
                Currency("Japanese Yen", "JPY"),
                Currency("British Pound Sterling", "GBP"),
                Currency("Canadian Dollar", "CAD"),
                Currency("Kenyan Shilling", "KES"),
                Currency("Australian Dollar", "AUD"),
                Currency("Swiss Franc", "CHF"),
                Currency("Chinese Yuan", "CNY"),
                Currency("Indian Rupee", "INR"),
                Currency("Brazilian Real", "BRL"),
                Currency("South African Rand", "ZAR"),
                Currency("Mexican Peso", "MXN"),
                Currency("Korean Won", "KRW"),
                Currency("Singapore Dollar", "SGD"),
                Currency("Hong Kong Dollar", "HKD"),
                Currency("Norwegian Krone", "NOK"),
                Currency("Swedish Krona", "SEK"),
                Currency("Danish Krone", "DKK"),
                Currency("New Zealand Dollar", "NZD"),
                Currency("Russian Ruble", "RUB"),
            )
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Configure Currency Widget") },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Widget Configuration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Choose the currencies and amount to display on your widget:",
                fontSize = 16.sp,
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    val text = newValue.text
                    if (text.isEmpty() || text.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = newValue
                    }
                },
                label = { Text("Amount") },
                placeholder = { Text("1.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = fromCurrency,
                onValueChange = { },
                label = { Text("From Currency") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showFromCurrencyPicker = true }) {
                        Text("Select")
                    }
                },
            )

            OutlinedTextField(
                value = toCurrency,
                onValueChange = { },
                label = { Text("To Currency") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showToCurrencyPicker = true }) {
                        Text("Select")
                    }
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountText = amount.text.ifEmpty { "1.00" }
                    onConfigComplete(fromCurrency, toCurrency, amountText)
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                enabled = fromCurrency.isNotEmpty() && toCurrency.isNotEmpty() && amount.text.isNotEmpty(),
            ) {
                Text("Create Widget", fontSize = 16.sp)
            }
        }
    }

    if (showFromCurrencyPicker) {
        CurrencyPickerDialog(
            currencies = commonCurrencies,
            selectedCurrency = fromCurrency,
            onCurrencySelected = { currency ->
                fromCurrency = currency.code
                showFromCurrencyPicker = false
            },
            onDismiss = { showFromCurrencyPicker = false },
        )
    }

    if (showToCurrencyPicker) {
        CurrencyPickerDialog(
            currencies = commonCurrencies,
            selectedCurrency = toCurrency,
            onCurrencySelected = { currency ->
                toCurrency = currency.code
                showToCurrencyPicker = false
            },
            onDismiss = { showToCurrencyPicker = false },
        )
    }
}

@Composable
fun CurrencyPickerDialog(
    currencies: List<Currency>,
    selectedCurrency: String = "",
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
            ) {
                Text(
                    text = "Select Currency",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                LazyColumn {
                    items(currencies) { currency ->
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            onClick = { onCurrencySelected(currency) },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (currency.code == selectedCurrency) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                ),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                            ) {
                                Text(
                                    text = currency.code,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (currency.code == selectedCurrency) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                                Text(
                                    text = currency.name,
                                    fontSize = 14.sp,
                                    color =
                                        if (currency.code == selectedCurrency) {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
