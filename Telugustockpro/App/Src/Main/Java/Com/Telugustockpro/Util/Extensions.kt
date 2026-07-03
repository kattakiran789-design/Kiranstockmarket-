package com.telugustockpro.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

val priceFormat = DecimalFormat("#,##0.00")
val volumeFormat = DecimalFormat("#,##,###")
val percentFormat = DecimalFormat("0.00")
val croreFormat = DecimalFormat("#,##0.00 Cr")
val lakhFormat = DecimalFormat("#,##0.00 L")

fun Long.formatVolume(): String {
    return when {
        this >= 1_00_00_000 -> "${(this / 1_00_00_000.0).format(2)} Cr"
        this >= 1_00_000 -> "${(this / 1_00_000.0).format(2)} L"
        this >= 1_000 -> "${(this / 1_000.0).format(1)}K"
        else -> this.toString()
    }
}

fun Double.format(decimals: Int = 2): String {
    return String.format("%.${decimals}f", this)
}

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

// Extension to calculate EMA
fun List<Double>.calculateEMA(period: Int): List<Double?> {
    if (size < period) return List(size) { null }

    val ema = mutableListOf<Double?>()
    val multiplier = 2.0 / (period + 1)

    // First EMA is SMA
    val firstSma = take(period).average()
    repeat(period - 1) { ema.add(null) }
    ema.add(firstSma)

    // Calculate EMA
    for (i in period until size) {
        val value = (get(i) - ema.last()!!) * multiplier + ema.last()!!
        ema.add(value)
    }

    return ema
}

// Extension to calculate SMA
fun List<Double>.calculateSMA(period: Int): List<Double?> {
    if (size < period) return List(size) { null }

    return mapIndexed { index, _ ->
        if (index < period - 1) null
        else subList(index - period + 1, index + 1).average()
    }
}

// Extension to calculate RSI
fun List<Double>.calculateRSI(period: Int = 14): List<Double?> {
    if (size < period + 1) return List(size) { null }

    val changes = zipWithNext { a, b -> b - a }
    val rsi = mutableListOf<Double?>()

    repeat(period) { rsi.add(null) }

    var avgGain = changes.take(period).filter { it > 0 }.average().let { if (it.isNaN()) 0.0 else it }
    var avgLoss = changes.take(period).filter { it < 0 }.average().let { if (it.isNaN()) 0.0 else -it }

    val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
    rsi.add(100 - (100 / (1 + rs)))

    for (i in period until changes.size) {
        val gain = if (changes[i] > 0) changes[i] else 0.0
        val loss = if (changes[i] < 0) -changes[i] else 0.0

        avgGain = (avgGain * (period - 1) + gain) / period
        avgLoss = (avgLoss * (period - 1) + loss) / period

        val currentRs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
        rsi.add(100 - (100 / (1 + currentRs)))
    }

    return rsi
}

// Extension to calculate MACD
fun List<Double>.calculateMACD(
    fastPeriod: Int = 12,
    slowPeriod: Int = 26,
    signalPeriod: Int = 9
): Triple<List<Double?>, List<Double?>, List<Double?>> {
    val fastEma = calculateEMA(fastPeriod)
    val slowEma = calculateEMA(slowPeriod)

    val macdLine = fastEma.zip(slowEma) { fast, slow ->
        if (fast != null && slow != null) fast - slow else null
    }

    val signalLine = macdLine.filterNotNull().calculateEMA(signalPeriod)
    val histogram = macdLine.zip(signalLine) { macd, signal ->
        if (macd != null && signal != null) macd - signal else null
    }

    return Triple(macdLine, signalLine, histogram)
}

// Extension to calculate Bollinger Bands
fun List<Double>.calculateBollingerBands(
    period: Int = 20,
    stdDevMultiplier: Float = 2f
): Triple<List<Double?>, List<Double?>, List<Double?>> {
    if (size < period) return Triple(List(size) { null }, List(size) { null }, List(size) { null })

    val middle = calculateSMA(period)
    val upper = mutableListOf<Double?>()
    val lower = mutableListOf<Double?>()

    for (i in indices) {
        if (i < period - 1) {
            upper.add(null)
            lower.add(null)
        } else {
            val slice = subList(i - period + 1, i + 1)
            val mean = slice.average()
            val variance = slice.map { (it - mean) * (it - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)

            upper.add(mean + stdDevMultiplier * stdDev)
            lower.add(mean - stdDevMultiplier * stdDev)
        }
    }

    return Triple(upper, middle, lower)
}
