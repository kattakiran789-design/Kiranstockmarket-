package com.telugustockpro.ui.components

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.telugustockpro.data.model.CandleData
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ChartMarkerView(
    context: Context,
    private val candleData: List<CandleData>
) : MarkerView(context, android.R.layout.simple_list_item_1) {

    private val priceFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var tvContent: TextView? = null

    init {
        // Find the TextView
        tvContent = findViewById(android.R.id.text1)
        tvContent?.apply {
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#21262D"))
            setPadding(16, 8, 16, 8)
            textSize = 11f
        }
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is CandleEntry) {
            val index = e.x.toInt()
            val candle = candleData.getOrNull(index)

            if (candle != null) {
                val dateStr = if (candleData.size > 30) {
                    dateFormat.format(Date(candle.timestamp))
                } else {
                    timeFormat.format(Date(candle.timestamp))
                }

                val change = candle.close - candle.open
                val changePercent = (change / candle.open) * 100
                val isBullish = candle.close >= candle.open
                val arrow = if (isBullish) "▲" else "▼"
                val colorHex = if (isBullish) "#26A69A" else "#EF5350"

                val text = buildString {
                    appendLine(dateStr)
                    appendLine("─────────────")
                    appendLine("O: ${priceFormat.format(candle.open)}")
                    appendLine("H: ${priceFormat.format(candle.high)}")
                    appendLine("L: ${priceFormat.format(candle.low)}")
                    appendLine("C: ${priceFormat.format(candle.close)}")
                    appendLine("─────────────")
                    appendLine("$arrow ${String.format("%+.2f", change)} (${String.format("%+.2f", changePercent)}%)")
                    append("Vol: ${formatVolume(candle.volume)}")
                }

                tvContent?.text = text
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height - 20f)
    }

    private fun formatVolume(volume: Long): String {
        return when {
            volume >= 10_000_000 -> "${"%.2f".format(volume / 10_000_000.0)}Cr"
            volume >= 100_000 -> "${"%.2f".format(volume / 100_000.0)}L"
            volume >= 1_000 -> "${"%.1f".format(volume / 1_000.0)}K"
            else -> volume.toString()
        }
    }
}
