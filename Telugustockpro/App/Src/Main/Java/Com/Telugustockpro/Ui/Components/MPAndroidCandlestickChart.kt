package com.telugustockpro.ui.components

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.telugustockpro.data.model.CandleData
import java.text.SimpleDateFormat
import java.util.*

private var storedCandleData: List<CandleData> = emptyList()

@Composable
fun MPAndroidCandlestickChart(
    candleData: List<CandleData>,
    modifier: Modifier = Modifier,
    activeIndicators: Set<String> = setOf(),
    showVolume: Boolean = true,
    onCandleSelected: ((CandleData) -> Unit)? = null
) {
    LaunchedEffect(candleData) {
        storedCandleData = candleData
    }

    if (candleData.isEmpty()) return

    val greenColor = Color.parseColor("#26A69A")
    val redColor = Color.parseColor("#EF5350")
    val bgColor = Color.parseColor("#0D1117")
    val gridColor = Color.parseColor("#21262D")
    val textColor = Color.parseColor("#8B949E")
    val borderColor = Color.parseColor("#30363D")

    val ema9Color = Color.parseColor("#FFAB40")
    val ema21Color = Color.parseColor("#2196F3")
    val ema50Color = Color.parseColor("#E040FB")
    val bbandsColor = Color.parseColor("#78909C")

    val currentIndicators = activeIndicators
    val currentShowVolume = showVolume

    AndroidView(
        modifier = modifier,
        factory = { context ->
            CombinedChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                setBackgroundColor(bgColor)
                setDrawGridBackground(false)
                setDrawBorders(true)
                setBorderColor(borderColor)
                setBorderWidth(0.5f)

                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDoubleTapToZoomEnabled(true)
                setExtraOffsets(8f, 8f, 8f, 8f)

                legend.apply {
                    isEnabled = true
                    textColor = textColor
                    textSize = 10f
                    form = Legend.LegendForm.LINE
                    isWordWrapEnabled = true
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    xOffset = 5f
                    yOffset = 0f
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridColor = gridColor
                    gridLineWidth = 0.5f
                    textColor = textColor
                    textSize = 10f
                    setLabelCount(6, false)
                    labelRotationAngle = -45f
                    setAvoidFirstLastClipping(true)
                    valueFormatter = object : ValueFormatter() {
                        private val format = SimpleDateFormat("dd MMM", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index in storedCandleData.indices) {
                                format.format(Date(storedCandleData[index].timestamp))
                            } else ""
                        }
                    }
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = gridColor
                    gridLineWidth = 0.5f
                    textColor = textColor
                    textSize = 10f
                    setLabelCount(6, false)
                    position = YAxis.YAxisLabelPosition.OUTSIDE_CHART
                    setDrawAxisLine(true)
                    axisLineColor = borderColor
                    axisLineWidth = 0.5f
                    setDrawTopYLabelEntry(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    }
                    val allPrices = candleData.flatMap { listOf(it.high, it.low) }
                    val minPrice = allPrices.minOrNull() ?: 0.0
                    val maxPrice = allPrices.maxOrNull() ?: 100.0
                    val padding = (maxPrice - minPrice) * 0.05
                    axisMinimum = (minPrice - padding).toFloat()
                    axisMaximum = (maxPrice + padding).toFloat()
                }

                axisRight.apply {
                    setDrawGridLines(false)
                    setDrawLabels(false)
                    setDrawAxisLine(false)
                    isEnabled = false
                }

                setDrawOrder(
                    arrayOf(
                        DrawOrder.BAR,
                        DrawOrder.LINE,
                        DrawOrder.CANDLE
                    )
                )

                animateX(500)
                animateY(300)

                val markerView = ChartMarkerView(context, candleData)
                markerView.chartView = this
                marker = markerView
            }
        },
        update = { chart ->
            chart.clear()

            val combinedData = CombinedData()

            // Candlestick data
            val candleEntries = candleData.mapIndexed { index, candle ->
                CandleEntry(
                    index.toFloat(),
                    candle.high.toFloat(),
                    candle.low.toFloat(),
                    candle.open.toFloat(),
                    candle.close.toFloat()
                )
            }

            val candleDataSet = CandleDataSet(candleEntries, "Price").apply {
                setDrawValues(false)
                shadowColorSameAsCandle = true
                increasingColor = greenColor
                increasingPaintStyle = android.graphics.Paint.Style.FILL
                decreasingColor = redColor
                decreasingPaintStyle = android.graphics.Paint.Style.FILL
                neutralColor = textColor
                setShadowWidth(0.8f)
                barSpace = 0.1f
                setDrawCandleBar(true)
                highLightColor = Color.WHITE
                setDrawHighlightIndicators(true)
                highlightLineWidth = 1.2f
                isHighlightEnabled = true
                setHighLightColor(Color.WHITE)
            }
            combinedData.candleData = CandleData(candleDataSet)

            // Volume bars
            if (currentShowVolume) {
                val maxVol = candleData.maxOfOrNull { it.volume } ?: 1L
                val priceRange = chart.axisLeft.axisMaximum - chart.axisLeft.axisMinimum
                val volumeEntries = candleData.mapIndexed { index, candle ->
                    val volHeight = (candle.volume.toFloat() / maxVol.toFloat()) * priceRange * 0.15f
                    BarEntry(index.toFloat(), volHeight)
                }

                val volumeDataSet = BarDataSet(volumeEntries, "Volume").apply {
                    setDrawValues(false)
                    colors = candleData.map { candle ->
                        if (candle.close >= candle.open)
                            Color.parseColor("#4026A69A")
                        else
                            Color.parseColor("#40EF5350")
                    }
                    highlightAlpha = 0f
                }
                volumeDataSet.barSpacePercent = 30f
                combinedData.barData = BarData(volumeDataSet)
            }

            // Moving averages and indicators
            val lineData = LineData()

            if (currentIndicators.contains("MA_9")) {
                val entries = calculateMA(candleData, 9)
                    .mapIndexedNotNull { i, v ->
                        if (v != null) Entry(i.toFloat(), v.toFloat()) else null
                    }
                if (entries.isNotEmpty()) {
                    lineData.addDataSet(LineDataSet(entries, "EMA 9").apply {
                        color = ema9Color
                        lineWidth = 1.5f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        highLightColor = Color.TRANSPARENT
                    })
                }
            }

            if (currentIndicators.contains("MA_21")) {
                val entries = calculateMA(candleData, 21)
                    .mapIndexedNotNull { i, v ->
                        if (v != null) Entry(i.toFloat(), v.toFloat()) else null
                    }
                if (entries.isNotEmpty()) {
                    lineData.addDataSet(LineDataSet(entries, "EMA 21").apply {
                        color = ema21Color
                        lineWidth = 1.5f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        highLightColor = Color.TRANSPARENT
                    })
                }
            }

            if (currentIndicators.contains("MA_50")) {
                val entries = calculateMA(candleData, 50)
                    .mapIndexedNotNull { i, v ->
                        if (v != null) Entry(i.toFloat(), v.toFloat()) else null
                    }
                if (entries.isNotEmpty()) {
                    lineData.addDataSet(LineDataSet(entries, "EMA 50").apply {
                        color = ema50Color
                        lineWidth = 1.5f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        highLightColor = Color.TRANSPARENT
                    })
                }
            }

            if (currentIndicators.contains("BB")) {
                val bb = calculateBB(candleData, 20, 2.0)
                val upperEntries = bb.upper.mapIndexedNotNull { i, v ->
                    if (v != null) Entry(i.toFloat(), v.toFloat()) else null
                }
                val lowerEntries = bb.lower.mapIndexedNotNull { i, v ->
                    if (v != null) Entry(i.toFloat(), v.toFloat()) else null
                }

                if (upperEntries.isNotEmpty()) {
                    lineData.addDataSet(LineDataSet(upperEntries, "BB Upper").apply {
                        color = bbandsColor
                        lineWidth = 1f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        highLightColor = Color.TRANSPARENT
                        enableDashedLine(10f, 5f, 0f)
                    })
                }
                if (lowerEntries.isNotEmpty()) {
                    lineData.addDataSet(LineDataSet(lowerEntries, "BB Lower").apply {
                        color = bbandsColor
                        lineWidth = 1f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        cubicIntensity = 0.15f
                        highLightColor = Color.TRANSPARENT
                        enableDashedLine(10f, 5f, 0f)
                    })
                }
            }

            if (lineData.dataSetCount > 0) {
                combinedData.lineData = lineData
            }

            chart.data = combinedData

            // Default zoom: show ~60 candles
            val visibleCount = minOf(60, candleData.size)
            chart.setVisibleXRangeMaximum(visibleCount.toFloat())
            chart.moveViewToX(candleData.size.toFloat())

            chart.invalidate()
        }
    )
}

private fun calculateMA(data: List<CandleData>, period: Int): List<Double?> {
    return data.mapIndexed { index, _ ->
        if (index < period - 1) null
        else data.subList(index - period + 1, index + 1).map { it.close }.average()
    }
}

private data class BBData(
    val upper: List<Double?>,
    val middle: List<Double?>,
    val lower: List<Double?>
)

private fun calculateBB(data: List<CandleData>, period: Int, multiplier: Double): BBData {
    val upper = mutableListOf<Double?>()
    val middle = mutableListOf<Double?>()
    val lower = mutableListOf<Double?>()

    for (i in data.indices) {
        if (i < period - 1) {
            upper.add(null)
            middle.add(null)
            lower.add(null)
        } else {
            val closes = data.subList(i - period + 1, i + 1).map { it.close }
            val avg = closes.average()
            val variance = closes.map { (it - avg) * (it - avg) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            middle.add(avg)
            upper.add(avg + multiplier * stdDev)
            lower.add(avg - multiplier * stdDev)
        }
    }
    return BBData(upper, middle, lower)
}
