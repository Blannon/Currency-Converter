package com.blannonnetwork.currencyconveter.data

import com.blannonnetwork.currencyconveter.BuildConfig
import com.blannonnetwork.currencyconveter.domain.Currency
import com.blannonnetwork.currencyconveter.domain.ExchangeRepository
import com.blannonnetwork.currencyconveter.data.network.dto.PairResponse
import com.blannonnetwork.currencyconveter.data.mappers.toConversionResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ExchangeRepositoryImpl(
    private val httpClient: HttpClient
): ExchangeRepository {

    private val tag ="ExchangeRepositoryImpl: "
    private val BASE_URL = "https://v6.exchangerate-api.com/v6"
    private val ApiKey = BuildConfig.API_KEY

    override suspend fun convert(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): Double {
        val response: PairResponse = httpClient.get(
            "$BASE_URL/$ApiKey/pair/$fromCurrency/$toCurrency/$amount"
        ).body()

        val result = response.toConversionResult()
        println(tag + result)

        return result
    }

    // Simple in-memory cache for supported currencies during app session
    private var cachedCurrencies: List<Currency>? = null

    override suspend fun getAllCurrencies(): List<Currency> {
        cachedCurrencies?.let { return it }

        val codesResponse: com.blannonnetwork.currencyconveter.data.network.dto.CodesResponse = httpClient.get(
            "$BASE_URL/$ApiKey/codes"
        ).body()

        val currencies = codesResponse.supportedCodes
            .mapNotNull { pair ->
                val code = pair.getOrNull(0)
                val name = pair.getOrNull(1)
                if (com.blannonnetwork.currencyconveter.domain.CurrencyCode.isValid(code) && !name.isNullOrBlank()) {
                    Currency(name = name, code = code!!)
                } else null
            }

        cachedCurrencies = currencies
        return currencies
    }
}