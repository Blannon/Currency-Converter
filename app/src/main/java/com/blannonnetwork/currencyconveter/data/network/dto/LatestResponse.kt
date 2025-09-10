package com.blannonnetwork.currencyconveter.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LatestResponse(
    @SerialName("result") val result: String? = null,
    @SerialName("error-type") val errorType: String? = null,
    @SerialName("base_code") val baseCode: String? = null,
    @SerialName("conversion_rates") val conversionRates: Map<String, Double> = emptyMap(),
)
