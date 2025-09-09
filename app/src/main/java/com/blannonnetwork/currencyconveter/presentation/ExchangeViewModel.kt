package com.blannonnetwork.currencyconveter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blannonnetwork.currencyconveter.domain.ConvertUseCase
import com.blannonnetwork.currencyconveter.domain.ExchangeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExchangeViewModel(
    private val convertUseCase: ConvertUseCase,
    private val exchangeRepository: ExchangeRepository
): ViewModel() {

    var state by mutableStateOf(ExchangeState())
        private set

    private var conversionJob: Job? = null

    init {
        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            try {
                val currencies = exchangeRepository.getAllCurrencies()
                // Set defaults based on device country (fallback USD -> EUR)
                val country = java.util.Locale.getDefault().country
                val countryToCurrency = mapOf(
                    "US" to "USD", "GB" to "GBP", "EU" to "EUR", "DE" to "EUR", "FR" to "EUR",
                    "IT" to "EUR", "ES" to "EUR", "NL" to "EUR", "BE" to "EUR", "IE" to "EUR",
                    "PT" to "EUR", "AT" to "EUR", "FI" to "EUR", "GR" to "EUR", "SK" to "EUR",
                    "SI" to "EUR", "EE" to "EUR", "LV" to "EUR", "LT" to "EUR", "CY" to "EUR",
                    "MT" to "EUR", "LU" to "EUR",
                    "CA" to "CAD", "AU" to "AUD", "NZ" to "NZD", "JP" to "JPY", "CN" to "CNY",
                    "IN" to "INR", "RU" to "RUB", "BR" to "BRL", "ZA" to "ZAR", "MX" to "MXN",
                    "CH" to "CHF", "SE" to "SEK", "NO" to "NOK", "DK" to "DKK", "PL" to "PLN"
                )
                val detectedFromCode = countryToCurrency[country] ?: "USD"
                val defaultFrom = currencies.firstOrNull { it.code == detectedFromCode } ?: currencies.firstOrNull { it.code == "USD" } ?: currencies.first()
                val defaultTo = currencies.firstOrNull { it.code == "EUR" } ?: currencies.getOrElse(1) { currencies.first() }

                state = state.copy(
                    allCurrencies = currencies,
                    from = defaultFrom,
                    to = defaultTo,
                    isLoading = false
                )

                // Now we can safely convert if there's an amount
                if (state.amount.isNotBlank()) {
                    debouncedConvert()
                }
            } catch (e: Exception) {
                state = state.copy(
                    allCurrencies = emptyList(),
                    error = "Failed to load currencies: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun onAction(action: ExchangeAction) {
        when(action) {
            ExchangeAction.Clear -> {
                state = state.copy(
                    amount = "",
                    result = ""
                )
                // Cancel any pending conversion
                conversionJob?.cancel()
            }
            ExchangeAction.Delete -> {
                if (state.amount.isBlank()) return
                state = state.copy(
                    amount = state.amount.dropLast(1)
                )
                debouncedConvert()
            }
            is ExchangeAction.Input -> {
                // Only allow a single decimal point and max 2 fraction digits
                val candidate = state.amount + action.value
                val pattern = Regex("^(\\d{0,9})(?:\\.(\\d{0,2}))?$")
                if (pattern.matches(candidate)) {
                    state = state.copy(amount = candidate)
                    debouncedConvert()
                }
            }
            is ExchangeAction.SelectedFrom -> {
                if (action.index in state.allCurrencies.indices) {
                    state = state.copy(
                        from = state.allCurrencies[action.index]
                    )
                    debouncedConvert()
                }
            }
            is ExchangeAction.SelectedTo -> {
                if (action.index in state.allCurrencies.indices) {
                    state = state.copy(
                        to = state.allCurrencies[action.index]
                    )
                    debouncedConvert()
                }
            }
            is ExchangeAction.SetAmount -> {
                val pattern = Regex("^(\\d{0,9})(?:\\.(\\d{0,2}))?$")
                if (action.amount.isEmpty() || pattern.matches(action.amount)) {
                    state = state.copy(amount = action.amount)
                    debouncedConvert()
                }
            }
            is ExchangeAction.ToggleFavorite -> {
                val currentFavorites = state.favoriteCurrencies.toMutableList()
                if (currentFavorites.contains(action.currencyCode)) {
                    currentFavorites.remove(action.currencyCode)
                } else {
                    if (currentFavorites.size < 8) { // Limit to 8 favorites
                        currentFavorites.add(action.currencyCode)
                    }
                }
                state = state.copy(favoriteCurrencies = currentFavorites)
                updateQuickAccessRates()
            }
            is ExchangeAction.ReorderFavorites -> {
                state = state.copy(favoriteCurrencies = action.newOrder)
            }
        }
    }

    private fun updateQuickAccessRates() {
        if (state.amount.isEmpty() || state.amount.toDoubleOrNull() == null) return

        viewModelScope.launch {
            val amount = state.amount.toDoubleOrNull() ?: return@launch
            try {
                // Single call to Latest endpoint for base currency
                // Limit API requests to avoid overuse; compute top favorites only
                val limitedFavorites = state.favoriteCurrencies.take(5)
                val temp = mutableMapOf<String, String>()
                for (code in limitedFavorites) {
                    val res = convertUseCase(state.from.code, code, state.amount)
                    temp[code] = res.getOrElse { "Error" }
                }
                state = state.copy(quickAccessRates = temp)
            } catch (e: Exception) {
                // keep previous if fails
            }
        }
    }

    private fun debouncedConvert() {
        // Cancel previous conversion job
        conversionJob?.cancel()

        // Only convert if we have valid data
        if (state.allCurrencies.isEmpty() || state.amount.isEmpty()) return

        // Start new conversion job with delay
        conversionJob = viewModelScope.launch {
            delay(500) // Wait 500ms before making API call
            convert()
        }
    }

    private fun convert() {
        // Only convert if we have valid currencies and amount
        if (state.allCurrencies.isEmpty() || state.amount.isEmpty()) return

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            try {
                val result = convertUseCase(
                    fromCurrency = state.from.code,
                    toCurrency = state.to.code,
                    amount = state.amount
                )
                result.fold(
                    onSuccess = { value ->
                        state = state.copy(result = value)
                    },
                    onFailure = { error ->
                        state = state.copy(error = mapError(error))
                    }
                )
            } catch (e: Exception) {
                state = state.copy(
                    error = "Conversion failed: ${e.message}"
                )
            } finally {
                state = state.copy(
                    isLoading = false
                )
            }
        }
    }

    private fun mapError(error: Throwable): String {
        return when (error) {
            is com.blannonnetwork.currencyconveter.data.network.ApiErrorException -> {
                when (error.errorType) {
                    "unsupported-code" -> "Unsupported currency code. Please select a valid ISO 4217 code."
                    "malformed-request" -> "Malformed request. Please try again."
                    "invalid-key" -> "Invalid API key. Please verify your key."
                    "inactive-account" -> "Inactive account. Please confirm your email."
                    "quota-reached" -> "API quota reached. Try again later."
                    else -> "API error: ${error.errorType}"
                }
            }
            is java.io.IOException -> "Network error. Check your connection."
            is io.ktor.client.plugins.ServerResponseException -> "Server error. Please try again later."
            is io.ktor.client.plugins.ClientRequestException -> {
                val status = error.response.status.value
                if (status == 429) "Too many requests. Please slow down." else "Request error ($status)."
            }
            else -> "Unexpected error: ${error.message ?: "Unknown"}"
        }
    }
}
