package com.blannonnetwork.currencyconveter.widget.data

import android.content.Context
import com.blannonnetwork.currencyconveter.widget.WidgetKeys

class WidgetPrefs(private val context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences(WidgetKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getFrom(
        appWidgetId: Int,
        default: String = "USD",
    ): String = prefs.getString(WidgetKeys.KEY_FROM + appWidgetId, default) ?: default

    fun getTo(
        appWidgetId: Int,
        default: String = "EUR",
    ): String = prefs.getString(WidgetKeys.KEY_TO + appWidgetId, default) ?: default

    fun getAmount(
        appWidgetId: Int,
        default: String = "1.00",
    ): String = prefs.getString(WidgetKeys.KEY_AMOUNT + appWidgetId, default) ?: default

    // Cache last successful conversion
    fun getLastConvertedAmount(appWidgetId: Int): String? = prefs.getString(WidgetKeys.KEY_LAST_CONVERTED + appWidgetId, null)

    fun getLastConvertedTimestamp(appWidgetId: Int): Long = prefs.getLong(WidgetKeys.KEY_LAST_TIMESTAMP + appWidgetId, 0L)

    fun isLoading(appWidgetId: Int): Boolean = prefs.getBoolean(WidgetKeys.KEY_LOADING + appWidgetId, false)

    fun setConfig(
        appWidgetId: Int,
        from: String,
        to: String,
        amount: String,
    ) {
        prefs.edit()
            .putString(WidgetKeys.KEY_FROM + appWidgetId, from)
            .putString(WidgetKeys.KEY_TO + appWidgetId, to)
            .putString(WidgetKeys.KEY_AMOUNT + appWidgetId, amount)
            .apply()
    }

    fun setLastConversion(
        appWidgetId: Int,
        convertedAmount: String,
    ) {
        prefs.edit()
            .putString(WidgetKeys.KEY_LAST_CONVERTED + appWidgetId, convertedAmount)
            .putLong(WidgetKeys.KEY_LAST_TIMESTAMP + appWidgetId, System.currentTimeMillis())
            .apply()
    }

    fun setLoadingState(
        appWidgetId: Int,
        isLoading: Boolean,
    ) {
        prefs.edit()
            .putBoolean(WidgetKeys.KEY_LOADING + appWidgetId, isLoading)
            .apply()
    }

    fun clearWidgetData(appWidgetId: Int) {
        prefs.edit()
            .remove(WidgetKeys.KEY_FROM + appWidgetId)
            .remove(WidgetKeys.KEY_TO + appWidgetId)
            .remove(WidgetKeys.KEY_AMOUNT + appWidgetId)
            .remove(WidgetKeys.KEY_LAST_CONVERTED + appWidgetId)
            .remove(WidgetKeys.KEY_LAST_TIMESTAMP + appWidgetId)
            .remove(WidgetKeys.KEY_LOADING + appWidgetId)
            .apply()
    }
}
