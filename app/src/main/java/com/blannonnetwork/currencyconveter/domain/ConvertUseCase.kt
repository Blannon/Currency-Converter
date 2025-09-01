package com.blannonnetwork.currencyconveter.domain

class ConvertUseCase(
    private val exchangeRepository: ExchangeRepository
) {
    suspend operator fun invoke(
        fromCurrency: String,
        toCurrency: String,
        amount: String
    ): String {
        if(fromCurrency.isBlank()) return ""
        if(toCurrency.isBlank()) return ""
        if(amount.isBlank()) return ""
        if (fromCurrency==toCurrency)return  amount

        val amountDouble = amount.toDoubleOrNull() ?: return ""

        val result = exchangeRepository.convert(
            fromCurrency, toCurrency, amountDouble
        )
        return result.toString()
    }
}