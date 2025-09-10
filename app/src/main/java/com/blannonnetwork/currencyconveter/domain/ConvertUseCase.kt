package com.blannonnetwork.currencyconveter.domain

class ConvertUseCase(
    private val exchangeRepository: ExchangeRepository,
) {
    suspend operator fun invoke(
        fromCurrency: String,
        toCurrency: String,
        amount: String,
    ): Result<String> {
        if (fromCurrency.isBlank() || toCurrency.isBlank() || amount.isBlank()) {
            return Result.success("")
        }
        if (fromCurrency == toCurrency) return Result.success(amount)

        val amountDouble = amount.toDoubleOrNull() ?: return Result.success("")

        return exchangeRepository.convert(fromCurrency, toCurrency, amountDouble)
            .map { it.toString() }
    }
}
