package com.blannonnetwork.currencyconveter.data.network

import com.blannonnetwork.currencyconveter.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientProvider {
    fun create(): HttpClient =
        HttpClient(CIO) {
            expectSuccess = true

            engine {
                endpoint {
                    keepAliveTime = 5000
                    connectTimeout = 5000
                    connectAttempts = 5
                }
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                val filteredMessage =
                                    message.replace(
                                        Regex("v6/[a-f0-9]+/"),
                                        "v6/****/",
                                    )
                                println(filteredMessage)
                            }
                        }
                    level = LogLevel.INFO
                }
            }
        }
}
