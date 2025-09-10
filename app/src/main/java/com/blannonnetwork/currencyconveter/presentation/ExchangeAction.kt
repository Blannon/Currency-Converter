package com.blannonnetwork.currencyconveter.presentation

sealed interface ExchangeAction {
    data class Input(val value: String) : ExchangeAction

    data class SetAmount(val amount: String) : ExchangeAction

    data object Clear : ExchangeAction

    data object Delete : ExchangeAction

    data class SelectedFrom(val index: Int) : ExchangeAction

    data class SelectedTo(val index: Int) : ExchangeAction

    data class ToggleFavorite(val currencyCode: String) : ExchangeAction

    data class ReorderFavorites(val newOrder: List<String>) : ExchangeAction
}
