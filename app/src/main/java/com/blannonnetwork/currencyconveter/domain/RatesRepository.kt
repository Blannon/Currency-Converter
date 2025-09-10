package com.blannonnetwork.currencyconveter.domain

interface RatesRepository {
    suspend fun latestRates(base: String): Map<String, Double>
}
