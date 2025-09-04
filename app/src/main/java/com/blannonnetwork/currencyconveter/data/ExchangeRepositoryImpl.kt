package com.blannonnetwork.currencyconveter.data

import com.blannonnetwork.currencyconveter.BuildConfig
import com.blannonnetwork.currencyconveter.domain.Currency
import com.blannonnetwork.currencyconveter.domain.ExchangeRepository
import com.blannonnetwork.currencyconveter.data.network.dto.PairResponse
import com.blannonnetwork.currencyconveter.data.mappers.toConversionResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay

class ExchangeRepositoryImpl(
    private val httpClient: HttpClient,
    private val apiConfig: com.blannonnetwork.currencyconveter.di.ApiConfig
): ExchangeRepository {

    private val tag ="ExchangeRepositoryImpl: "
    private val BASE_URL = apiConfig.baseUrl
    private val ApiKey = apiConfig.apiKey

    private val rateCache = mutableMapOf<Pair<String, String>, Double>()

    override suspend fun convert(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): Result<Double> {
        val pairKey = fromCurrency to toCurrency
        return try {
            val result = retryWithBackoff {
                val response: PairResponse = httpClient.get(
                    "$BASE_URL/$ApiKey/pair/$fromCurrency/$toCurrency/$amount"
                ).body()
                if (response.result == "error") {
                    throw com.blannonnetwork.currencyconveter.data.network.ApiErrorException(response.errorType ?: "unknown-error")
                }
                response.toConversionResult()
            }
            // Cache rate per 1 unit if amount > 0
            if (amount > 0) {
                val rate = result / amount
                if (rate.isFinite()) rateCache[pairKey] = rate
            }
            println(tag + result)
            Result.success(result)
        } catch (e: Exception) {
            val cachedRate = rateCache[pairKey]
            if (cachedRate != null) {
                val fallback = cachedRate * amount
                Result.success(fallback)
            } else {
                Result.failure(e)
            }
        }
    }

    private suspend fun <T> retryWithBackoff(
        times: Int = 3,
        initialDelayMs: Long = 200,
        maxDelayMs: Long = 1500,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var attempt = 0
        var lastError: Throwable? = null
        while (attempt < times) {
            try {
                return block()
            } catch (e: Exception) {
                if (!isTransient(e) || attempt == times - 1) throw e
                lastError = e
                delay(currentDelay)
                currentDelay = kotlin.math.min(maxDelayMs, (currentDelay * factor).toLong())
                attempt++
            }
        }
        throw lastError ?: IllegalStateException("Unknown error")
    }

    private fun isTransient(e: Exception): Boolean {
        return when (e) {
            is java.io.IOException -> true
            is io.ktor.client.plugins.ServerResponseException -> true // 5xx
            is io.ktor.client.plugins.ClientRequestException -> {
                // treat 429 and some 408 as transient
                val status = e.response.status
                status.value == 429 || status == HttpStatusCode.RequestTimeout
            }
            else -> false
        }
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