package com.blannonnetwork.currencyconveter.data

import com.blannonnetwork.currencyconveter.BuildConfig
import com.blannonnetwork.currencyconveter.data.network.dto.LatestResponse
import com.blannonnetwork.currencyconveter.domain.RatesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RatesRepositoryImpl(
    private val httpClient: HttpClient,
    private val apiConfig: com.blannonnetwork.currencyconveter.di.ApiConfig
) : RatesRepository {

    private val baseUrl = apiConfig.baseUrl
    private val apiKey = apiConfig.apiKey

    override suspend fun latestRates(base: String): Map<String, Double> {
        val response: LatestResponse = httpClient.get(
            "$baseUrl/$apiKey/latest/$base"
        ).body()
        if (response.result == "error") {
            throw com.blannonnetwork.currencyconveter.data.network.ApiErrorException(response.errorType ?: "unknown-error")
        }
        return response.conversionRates
    }
}
