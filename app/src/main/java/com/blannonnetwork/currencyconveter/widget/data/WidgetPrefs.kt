package com.blannonnetwork.currencyconveter.widget.data

import android.content.Context
import com.blannonnetwork.currencyconveter.widget.WidgetKeys

class WidgetPrefs(private val context: Context) {
    private val prefs by lazy {
        context.getSharedPreferences(WidgetKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getFrom(appWidgetId: Int, default: String = "USD"): String =
        prefs.getString(WidgetKeys.KEY_FROM + appWidgetId, default) ?: default

    fun getTo(appWidgetId: Int, default: String = "EUR"): String =
        prefs.getString(WidgetKeys.KEY_TO + appWidgetId, default) ?: default

    fun getAmount(appWidgetId: Int, default: String = "1.00"): String =
        prefs.getString(WidgetKeys.KEY_AMOUNT + appWidgetId, default) ?: default

    fun setConfig(appWidgetId: Int, from: String, to: String, amount: String) {
        prefs.edit()
            .putString(WidgetKeys.KEY_FROM + appWidgetId, from)
            .putString(WidgetKeys.KEY_TO + appWidgetId, to)
            .putString(WidgetKeys.KEY_AMOUNT + appWidgetId, amount)
            .apply()
    }
}
