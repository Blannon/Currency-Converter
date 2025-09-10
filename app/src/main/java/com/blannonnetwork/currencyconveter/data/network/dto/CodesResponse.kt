package com.blannonnetwork.currencyconveter.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CodesResponse(
    @SerialName("result") val result: String? = null,
    // API returns array of [code, name]
    @SerialName("supported_codes") val supportedCodes: List<List<String>> = emptyList(),
)
