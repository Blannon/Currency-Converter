package com.blannonnetwork.currencyconveter.data.network

class ApiErrorException(
    val errorType: String,
    message: String = errorType,
) : RuntimeException(message)
