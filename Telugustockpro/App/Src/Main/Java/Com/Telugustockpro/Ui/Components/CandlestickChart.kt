package com.telugustockpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telugustockpro.data.model.CandleData
import com.telugustockpro.ui.theme.TradingViewColors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candleData: List<CandleData>,
    modifier: Modifier = Modifier,
    activeIndicators: Set<String> = setOf(),
    showCrosshair: Boolean = true,
    onCandleSelected: ((CandleData) -> Unit)? = null
) {
    if (candleData.isEmpty()) return

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var crosshairPosition by remember { mutableStateOf<Offset?>(null) }
    var selectedCandle by remember { mutableStateOf<CandleData?>(null) }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Calculate price range
    val priceData = remember(candleData) {
        val allHighs = candleData.map { it.high }
        val allLows = candleData.map { it.low }
        val minPrice = allLows.minOrNull() ?: 0.0
        val maxPrice = allHighs.maxOrNull() ?: 100.0
        val padding = (maxPrice - minPrice) * 0.1
        PriceRange(minPrice - padding, maxPrice + padding)
    }

    // Calculate volume range
    val volumeData = remember(candleData) {
        val maxVol = candleData.maxOfOrNull { it.volume } ?: 1L
        VolumeRange(0L, maxVol)
    }

    Box(
        modifier = modifier
            .background(TradingViewColors.Background)
            .pointerInput(candleData) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offsetX = (offsetX + pan.x).coerceIn(
                        -size.width * scale,
                        size.width
                    )
                }
            }
            .pointerInput(candleData, priceData) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (showCrosshair) {
                            crosshairPosition = offset
                            // Find nearest candle
                            val candleWidth = size.width / (candleData.size * scale).coerceAtLeast(1f)
                            val index = ((offset.x - offsetX) / candleWidth).toInt()
                                .coerceIn(0, candleData.size - 1)
                            selectedCandle = candleData.getOrNull(index)
                            onCandleSelected?.invoke(candleData[index])
                        }
                    },
                    onDrag = { change, _ ->
                        if (showCrosshair) {
                            crosshairPosition = change.position
                            val candleWidth = size.width / (candleData.size * scale).coerceAtLeast(1f)
                            val index = ((change.position.x - offsetX) / candleWidth).toInt()
                                .coerceIn(0, candleData.size - 1)
                            selectedCandle = candleData.getOrNull(index)
                            onCandleSelected?.invoke(candleData[index])
                        }
                    },
                    onDragEnd = {
                        crosshairPosition = null
                        selectedCandle = null
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val priceChartHeight = chartHeight * 0.75f
            val volumeChartHeight = chartHeight * 0.2f
            val volumeTop = priceChartHeight + chartHeight * 0.05f

            // Draw grid lines
            drawGridLines(
                chartWidth = chartWidth,
                chartHeight = priceChartHeight,
                priceRange = priceData,
                textMeasurer = textMeasurer
            )

            // Calculate visible candles
            val candleWidth = chartWidth / (candleData.size * scale).coerceAtLeast(1f)
            val visibleStart = (-offsetX / candleWidth).toInt().coerceAtLeast(0)
            val visibleEnd = ((chartWidth - offsetX) / candleWidth).toInt()
                .coerceAtMost(candleData.size - 1)

            // Draw volume bars
            for (i in visibleStart..visibleEnd) {
                val candle = candleData[i]
                val x = i * candleWidth + offsetX + candleWidth * 0.2f
                val volWidth = candleWidth * 0.6f
                val volHeight = (candle.volume.toFloat() / volumeData.maxVolume.toFloat()) * volumeChartHeight
                val volColor = if (candle.isBullish) TradingViewColors.GreenVolume else TradingViewColors.RedVolume

                drawRect(
                    color = volColor,
                    topLeft = Offset(x, volumeTop + volumeChartHeight - volHeight),
                    size = Size(volWidth, volHeight)
                )
            }

            // Draw moving averages
            if (activeIndicators.contains("MA_9")) {
                drawMovingAverage(
                    candleData = candleData,
                    period = 9,
                    color = TradingViewColors.EMA9,
                    chartWidth = chartWidth,
                    priceChartHeight = priceChartHeight,
                    priceRange = priceData,
                    candleWidth = candleWidth,
                    offsetX = offsetX,
                    visibleStart = visibleStart,
                    visibleEnd = visibleEnd
                )
            }

            if (activeIndicators.contains("MA_21")) {
                drawMovingAverage(
                    candleData = candleData,
                    period = 21,
                    color = TradingViewColors.EMA21,
                    chartWidth = chartWidth,
                    priceChartHeight = priceChartHeight,
                    priceRange = priceData,
                    candleWidth = candleWidth,
                    offsetX = offsetX,
                    visibleStart = visibleStart,
                    visibleEnd = visibleEnd
                )
            }

            if (activeIndicators.contains("MA_50")) {
                drawMovingAverage(
                    candleData = candleData,
                    period = 50,
                    color = TradingViewColors.EMA50,
                    chartWidth = chartWidth,
                    priceChartHeight = priceChartHeight,
                    priceRange = priceData,
                    candleWidth = candleWidth,
                    offsetX = offsetX,
                    visibleStart = visibleStart,
                    visibleEnd = visibleEnd
                )
            }

            // Draw Bollinger Bands
            if (activeIndicators.contains("BB")) {
                drawBollingerBands(
                    candleData = candleData,
                    period = 20,
                    stdDevMultiplier = 2f,
                    chartWidth = chartWidth,
                    priceChartHeight = priceChartHeight,
                    priceRange = priceData,
                    candleWidth = candleWidth,
                    offsetX = offsetX,
                    visibleStart = visibleStart,
                    visibleEnd = visibleEnd
                )
            }

            // Draw candles
            for (i in visibleStart..visibleEnd) {
                val candle = candleData[i]
                val x = i * candleWidth + offsetX

                val bodyColor = if (candle.isBullish) TradingViewColors.GreenCandle else TradingViewColors.RedCandle
                val wickColor = bodyColor

                // Draw wick (shadow)
                val wickX = x + candleWidth * 0.5f
                val wickTop = priceToY(candle.high, priceData, priceChartHeight)
                val wickBottom = priceToY(candle.low, priceData, priceChartHeight)

                drawLine(
                    color = wickColor,
                    start = Offset(wickX, wickTop),
                    end = Offset(wickX, wickBottom),
                    strokeWidth = 1.dp.toPx()
                )

                // Draw body
                val bodyTop = priceToY(candle.bodyTop, priceData, priceChartHeight)
                val bodyBottom = priceToY(candle.bodyBottom, priceData, priceChartHeight)
                val bodyHeight = max(bodyBottom - bodyTop, 1.dp.toPx())

                if (candle.isBullish) {
                    // Bullish candle - hollow or filled green
                    drawRect(
                        color = bodyColor,
                        topLeft = Offset(x + candleWidth * 0.15f, bodyTop),
                        size = Size(candleWidth * 0.7f, bodyHeight),
                        style = Fill
                    )
                } else {
                    // Bearish candle - filled red
                    drawRect(
                        color = bodyColor,
                        topLeft = Offset(x + candleWidth * 0.15f, bodyTop),
                        size = Size(candleWidth * 0.7f, bodyHeight),
                        style = Fill
                    )
                }
            }

            // Draw crosshair
            if (showCrosshair && crosshairPosition != null) {
                val pos = crosshairPosition!!

                // Vertical line
                drawLine(
                    color = TradingViewColors.Crosshair,
                    start = Offset(pos.x, 0f),
                    end = Offset(pos.x, chartHeight),
                    strokeWidth = 0.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )

                // Horizontal line
                drawLine(
                    color = TradingViewColors.Crosshair,
                    start = Offset(0f, pos.y),
                    end = Offset(chartWidth, pos.y),
                    strokeWidth = 0.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )

                // Price label
                val price = yToPrice(pos.y, priceData, priceChartHeight)
                drawRect(
                    color = TradingViewColors.ElevatedSurface,
                    topLeft = Offset(chartWidth - 70.dp.toPx(), pos.y - 10.dp.toPx()),
                    size = Size(70.dp.toPx(), 20.dp.toPx())
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = priceFormat.format(price),
                    topLeft = Offset(chartWidth - 68.dp.toPx(), pos.y - 8.dp.toPx()),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = TradingViewColors.TextPrimary
                    )
                )
            }

            // Draw Y-axis labels
            val numLabels = 6
            for (i in 0..numLabels) {
                val y = (i.toFloat() / numLabels) * priceChartHeight
                val price = yToPrice(y, priceData, priceChartHeight)

                drawText(
                    textMeasurer = textMeasurer,
                    text = priceFormat.format(price),
                    topLeft = Offset(4.dp.toPx(), y - 6.dp.toPx()),
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = TradingViewColors.AxisLabel
                    )
                )
            }
        }

        // Selected candle info overlay
        selectedCandle?.let { candle ->
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .background(
                        TradingViewColors.ElevatedSurface.copy(alpha = 0.9f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CandleInfoItem("O", priceFormat.format(candle.open))
                    CandleInfoItem("H", priceFormat.format(candle.high))
                    CandleInfoItem("L", priceFormat.format(candle.low))
                    CandleInfoItem("C", priceFormat.format(candle.close))
                }
                Text(
                    text = "Vol: ${volumeFormat.format(candle.volume)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
            }
        }
    }
}

@Composable
private fun CandleInfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = TradingViewColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun DrawScope.drawGridLines(
    chartWidth: Float,
    chartHeight: Float,
    priceRange: PriceRange,
    textMeasurer: TextMeasurer
) {
    val numLines = 5
    for (i in 0..numLines) {
        val y = (i.toFloat() / numLines) * chartHeight

        drawLine(
            color = TradingViewColors.GridLine,
            start = Offset(60.dp.toPx(), y),
            end = Offset(chartWidth, y),
            strokeWidth = 0.5.dp.toPx()
        )
    }
}

private fun DrawScope.drawMovingAverage(
    candleData: List<CandleData>,
    period: Int,
    color: Color,
    chartWidth: Float,
    priceChartHeight: Float,
    priceRange: PriceRange,
    candleWidth: Float,
    offsetX: Float,
    visibleStart: Int,
    visibleEnd: Int
) {
    if (candleData.size < period) return

    val path = Path()
    var started = false

    for (i in visibleStart..visibleEnd) {
        if (i < period - 1) continue

        val maValue = candleData.subList(i - period + 1, i + 1)
            .map { it.close }
            .average()

        val x = i * candleWidth + offsetX + candleWidth * 0.5f
        val y = priceToY(maValue, priceRange, priceChartHeight)

        if (!started) {
            path.moveTo(x, y)
            started = true
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawBollingerBands(
    candleData: List<CandleData>,
    period: Int,
    stdDevMultiplier: Float,
    chartWidth: Float,
    priceChartHeight: Float,
    priceRange: PriceRange,
    candleWidth: Float,
    offsetX: Float,
    visibleStart: Int,
    visibleEnd: Int
) {
    if (candleData.size < period) return

    val upperPath = Path()
    val lowerPath = Path()
    val middlePath = Path()
    var started = false

    for (i in visibleStart..visibleEnd) {
        if (i < period - 1) continue

        val closes = candleData.subList(i - period + 1, i + 1).map { it.close }
        val middle = closes.average()
        val stdDev = kotlin.math.sqrt(closes.map { (it - middle) * (it - middle) }.average())

        val upper = middle + stdDevMultiplier * stdDev
        val lower = middle - stdDevMultiplier * stdDev

        val x = i * candleWidth + offsetX + candleWidth * 0.5f
        val yUpper = priceToY(upper, priceRange, priceChartHeight)
        val yLower = priceToY(lower, priceRange, priceChartHeight)
        val yMiddle = priceToY(middle, priceRange, priceChartHeight)

        if (!started) {
            upperPath.moveTo(x, yUpper)
            lowerPath.moveTo(x, yLower)
            middlePath.moveTo(x, yMiddle)
            started = true
        } else {
            upperPath.lineTo(x, yUpper)
            lowerPath.lineTo(x, yLower)
            middlePath.lineTo(x, yMiddle)
        }
    }

    // Draw filled area between bands
    drawPath(
        path = upperPath,
        color = TradingViewColors.Bands.copy(alpha = 0.3f),
        style = Stroke(width = 1.dp.toPx())
    )
    drawPath(
        path = lowerPath,
        color = TradingViewColors.Bands.copy(alpha = 0.3f),
        style = Stroke(width = 1.dp.toPx())
    )
    drawPath(
        path = middlePath,
        color = TradingViewColors.Bands.copy(alpha = 0.5f),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
        )
    )
}

private fun priceToY(price: Double, range: PriceRange, height: Float): Float {
    val ratio = (price - range.min) / (range.max - range.min)
    return (1f - ratio.toFloat()) * height
}

private fun yToPrice(y: Float, range: PriceRange, height: Float): Double {
    val ratio = 1f - y / height
    return range.min + ratio * (range.max - range.min)
}

private data class PriceRange(val min: Double, val max: Double)
private data class VolumeRange(val min: Long, val max: Long)
