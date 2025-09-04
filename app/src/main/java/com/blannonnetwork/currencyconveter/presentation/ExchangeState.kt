package com.blannonnetwork.currencyconveter.presentation

import com.blannonnetwork.currencyconveter.domain.Currency

data class ExchangeState(
    val allCurrencies: List<Currency> = emptyList(),
    val favoriteCurrencies: List<String> = listOf("EUR", "GBP", "JPY", "CAD"),
    val quickAccessRates: Map<String, String> = emptyMap(),
    val from: Currency = Currency("", ""),
    val to: Currency = Currency("", ""),
    val amount: String = "",
    val result: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)