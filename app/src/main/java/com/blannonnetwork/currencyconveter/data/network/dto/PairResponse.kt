package com.blannonnetwork.currencyconveter.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairResponse(
    @SerialName("result") val result: String? = null,
    @SerialName("conversion_result") val conversionResult: Double = 0.0,
    @SerialName("base_code") val baseCode: String? = null,
    @SerialName("target_code") val targetCode: String? = null
)
