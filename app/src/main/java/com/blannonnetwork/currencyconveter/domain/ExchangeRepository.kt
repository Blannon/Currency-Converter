package com.blannonnetwork.currencyconveter.domain

interface ExchangeRepository {
    suspend fun convert(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): Double

    suspend fun getAllCurrencies(): List<Currency>
}