package com.blannonnetwork.currencyconveter.domain

@JvmInline
value class CurrencyCode(val value: String) {
    init {
        require(isValid(value)) { "Invalid ISO 4217 currency code: $value" }
    }

    override fun toString(): String = value

    companion object {
        private val CODE_REGEX = Regex("^[A-Z]{3}$")

        fun isValid(code: String?): Boolean = code != null && CODE_REGEX.matches(code)
    }
}
