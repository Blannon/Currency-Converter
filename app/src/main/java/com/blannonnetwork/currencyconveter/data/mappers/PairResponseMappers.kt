package com.blannonnetwork.currencyconveter.data.mappers

import com.blannonnetwork.currencyconveter.data.network.dto.PairResponse

internal fun PairResponse.toConversionResult(): Double = this.conversionResult
