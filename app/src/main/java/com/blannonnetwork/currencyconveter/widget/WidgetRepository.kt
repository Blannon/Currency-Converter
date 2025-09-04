package com.blannonnetwork.currencyconveter.widget

import com.blannonnetwork.currencyconveter.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object WidgetRepository {

    @Serializable
    data class WidgetExchange(
        val conversion_result: Double
    )

    private val httpClient: HttpClient = com.blannonnetwork.currencyconveter.data.network.KtorClientProvider.create()

    private const val BASE_URL = "https://v6.exchangerate-api.com/v6"
    private val API_KEY = com.blannonnetwork.currencyconveter.BuildConfig.API_KEY

    suspend fun convert(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): Double {
        return withTimeout(10000) {
            try {
                val result: WidgetExchange = httpClient.get(
                    "$BASE_URL/$API_KEY/pair/$fromCurrency/$toCurrency/$amount"
                ).body()

                result.conversion_result
            } catch (e: Exception) {
                throw Exception("Conversion failed: ${e.message}")
            }
        }
    }

    fun cleanup() {
        httpClient.close()
    }
}