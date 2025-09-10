package com.blannonnetwork.currencyconveter.widget

import android.util.Log
import com.blannonnetwork.currencyconveter.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlin.random.Random

object WidgetRepository {
    @Serializable
    data class WidgetExchange(
        val conversion_result: Double,
    )

    private val httpClient: HttpClient = com.blannonnetwork.currencyconveter.data.network.KtorClientProvider.create()

    private const val BASE_URL = "https://v6.exchangerate-api.com/v6"
    private val API_KEY = BuildConfig.API_KEY
    private const val MAX_RETRY_ATTEMPTS = 3
    private const val TIMEOUT_DURATION = 15000L

    suspend fun convert(
        fromCurrency: String,
        toCurrency: String,
        amount: Double,
    ): Double {
        return withTimeout(TIMEOUT_DURATION) {
            var lastException: Exception? = null

            repeat(MAX_RETRY_ATTEMPTS) { attempt ->
                try {
                    Log.d("WidgetRepository", "Attempting conversion: $fromCurrency -> $toCurrency (attempt ${attempt + 1})")

                    val result: WidgetExchange =
                        httpClient.get(
                            "$BASE_URL/$API_KEY/pair/$fromCurrency/$toCurrency/$amount",
                        ).body()

                    Log.d("WidgetRepository", "Conversion successful: ${result.conversion_result}")
                    return@withTimeout result.conversion_result
                } catch (e: Exception) {
                    lastException = e
                    Log.w("WidgetRepository", "Conversion attempt ${attempt + 1} failed: ${e.message}")

                    if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                        // Exponential backoff with jitter
                        val backoffDelay = (1000L * (attempt + 1)) + Random.nextLong(0, 1000)
                        Log.d("WidgetRepository", "Retrying in ${backoffDelay}ms...")
                        delay(backoffDelay)
                    }
                }
            }

            Log.e("WidgetRepository", "All conversion attempts failed", lastException)
            throw Exception("Currency conversion failed after $MAX_RETRY_ATTEMPTS attempts: ${lastException?.message}")
        }
    }

    fun cleanup() {
        try {
            httpClient.close()
            Log.d("WidgetRepository", "HTTP client closed successfully")
        } catch (e: Exception) {
            Log.w("WidgetRepository", "Error closing HTTP client: ${e.message}")
        }
    }
}
