package com.blannonnetwork.currencyconveter.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blannonnetwork.currencyconveter.domain.ConvertUseCase
import com.blannonnetwork.currencyconveter.domain.ExchangeRepository
import kotlinx.coroutines.launch

class ExchangeViewModel(
    private val convertUseCase: ConvertUseCase,
    private val exchangeRepository: ExchangeRepository
): ViewModel(){

    var state by mutableStateOf(ExchangeState())
        private set

    init {
        convert()
    }

    private fun convert() {
        viewModelScope.launch {
            state = state.copy(
                result = convertUseCase(
                    fromCurrency = state.from.code,
                    toCurrency = state.to.code,
                    amount = state.amount
                )
            )
        }
    }
}