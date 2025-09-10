package com.blannonnetwork.currencyconveter.data

import kotlinx.serialization.Serializable

@Serializable
data class Exchange(
    val conversion_result: Double,
)
