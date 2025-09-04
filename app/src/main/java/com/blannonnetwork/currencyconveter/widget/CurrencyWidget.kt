package com.blannonnetwork.currencyconveter.widget

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.LocalContext
import com.blannonnetwork.currencyconveter.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

class CurrencyGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = abs(id.toString().hashCode())
        val widgetPrefs = com.blannonnetwork.currencyconveter.widget.data.WidgetPrefs(context)

        val fromCurrency = widgetPrefs.getFrom(appWidgetId)
        val toCurrency = widgetPrefs.getTo(appWidgetId)
        val amountStr = widgetPrefs.getAmount(appWidgetId)

        val (convertedAmount, isLoading) = try {
            val amt = amountStr.toDoubleOrNull() ?: 1.0
            val result = withContext(Dispatchers.IO) {
                WidgetRepository.convert(fromCurrency, toCurrency, amt)
            }
            String.format(Locale.US, "%.4f", result) to false
        } catch (_: Exception) {
            "Error" to false
        }

        provideContent {
            currencyWidgetContent(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                amount = amountStr,
                convertedAmount = convertedAmount,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun currencyWidgetContent(
    fromCurrency: String,
    toCurrency: String,
    amount: String,
    convertedAmount: String,
    isLoading: Boolean
) {
    val white: ColorProvider = ColorProvider(day = Color.White, night = Color.White)
    val dimWhite30 = ColorProvider(day = Color.White.copy(alpha = 0.3f), night = Color.White.copy(alpha = 0.3f))
    val dimWhite10 = ColorProvider(day = Color.White.copy(alpha = 0.1f), night = Color.White.copy(alpha = 0.1f))
    val dimBlack20 = ColorProvider(day = Color.Black.copy(alpha = 0.2f), night = Color.Black.copy(alpha = 0.2f))
    val textGreen = ColorProvider(day = Color(0xFF4CAF50), night = Color(0xFF4CAF50))
    val bg = ColorProvider(day = Color(0xFF1C1C1E), night = Color(0xFF1C1C1E))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bg)
            .cornerRadius(16.dp)
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalContext.current.getString(com.blannonnetwork.currencyconveter.R.string.currency_converter),
                style = TextStyle(
                    color = white,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Text(
                text = LocalContext.current.getString(com.blannonnetwork.currencyconveter.R.string.refresh),
                style = TextStyle(
                    color = white,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier
                    .padding(end = 8.dp)
                    .clickable(actionRunCallback<RefreshAction>())
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(dimWhite10)
                .cornerRadius(8.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fromCurrency,
                style = TextStyle(
                    color = white,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier
                    .background(dimBlack20)
                    .cornerRadius(4.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = amount,
                style = TextStyle(
                    color = white,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = LocalContext.current.getString(com.blannonnetwork.currencyconveter.R.string.swap),
            style = TextStyle(
                color = white,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier
                .padding(2.dp)
                .clickable(actionRunCallback<SwapAction>())
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(dimWhite10)
                .cornerRadius(8.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = toCurrency,
                style = TextStyle(
                    color = white,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier
                    .background(dimBlack20)
                    .cornerRadius(4.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = if (isLoading) LocalContext.current.getString(com.blannonnetwork.currencyconveter.R.string.loading) else convertedAmount,
                style = TextStyle(
                    color = textGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )


        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = LocalContext.current.getString(com.blannonnetwork.currencyconveter.R.string.tap_to_open_app),
            style = TextStyle(
                color = ColorProvider(day = Color.White.copy(alpha = 0.7f), night = Color.White.copy(alpha = 0.7f)),
                fontSize = 10.sp
            )
        )
    }
}

// Actions
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[longPreferencesKey(com.blannonnetwork.currencyconveter.widget.WidgetKeys.STATE_REFRESH_TIME)] = System.currentTimeMillis()
        }
        CurrencyGlanceWidget().update(context, glanceId)
    }
}

class SwapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetId = abs(glanceId.toString().hashCode())
        val prefs = com.blannonnetwork.currencyconveter.widget.data.WidgetPrefs(context)

        val from = prefs.getFrom(appWidgetId)
        val to = prefs.getTo(appWidgetId)

        prefs.setConfig(appWidgetId, to, from, prefs.getAmount(appWidgetId))

        CurrencyGlanceWidget().update(context, glanceId)
    }
}

class CurrencyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CurrencyGlanceWidget()
}
