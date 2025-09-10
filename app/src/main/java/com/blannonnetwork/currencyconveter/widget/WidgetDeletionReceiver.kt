package com.blannonnetwork.currencyconveter.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.blannonnetwork.currencyconveter.widget.data.WidgetPrefs

class WidgetDeletionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_DELETED -> {
                val appWidgetId =
                    intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID,
                    )

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    Log.d("WidgetDeletion", "Cleaning up widget data for ID: $appWidgetId")

                    val widgetPrefs = WidgetPrefs(context)
                    widgetPrefs.clearWidgetData(appWidgetId)

                    Log.d("WidgetDeletion", "Widget data cleaned up successfully")
                }
            }

            AppWidgetManager.ACTION_APPWIDGET_DISABLED -> {
                Log.d("WidgetDeletion", "Widget provider disabled - performing cleanup")
            }
        }
    }
}
