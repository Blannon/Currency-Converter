package com.blannonnetwork.currencyconveter.data

import com.blannonnetwork.currencyconveter.BuildConfig
import com.blannonnetwork.currencyconveter.data.network.dto.LatestResponse
import com.blannonnetwork.currencyconveter.domain.RatesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RatesRepositoryImpl(
    private val httpClient: HttpClient
) : RatesRepository {

    private val baseUrl = "https://v6.exchangerate-api.com/v6"
    private val apiKey = BuildConfig.API_KEY

    override suspend fun latestRates(base: String): Map<String, Double> {
        val response: LatestResponse = httpClient.get(
            "$baseUrl/$apiKey/latest/$base"
        ).body()
        return response.conversionRates
    }
}
