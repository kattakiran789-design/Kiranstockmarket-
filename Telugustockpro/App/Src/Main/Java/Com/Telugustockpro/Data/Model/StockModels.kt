package com.telugustockpro.data.model

import java.time.LocalDateTime

// Market Index (Nifty 50, Sensex, Bank Nifty)
data class MarketIndex(
    val name: String,
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val isPositive: Boolean = change >= 0
)

// Individual Stock
data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val volume: Long,
    val high: Double,
    val low: Double,
    val open: Double,
    val previousClose: Double,
    val isPositive: Boolean = change >= 0,
    val sector: String = ""
)

// Candlestick data for charts
data class CandleData(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val isBullish: Boolean get() = close >= open
    val bodyTop: Double get() = maxOf(open, close)
    val bodyBottom: Double get() = minOf(open, close)
}

// OHLCV data point
data class OHLCV(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

// Technical Indicators
data class IndicatorData(
    val name: String,
    val values: List<Double?>,
    val color: Long
)

// Moving Average types
enum class MovingAverageType(val period: Int, val displayName: String) {
    EMA_9(9, "EMA 9"),
    EMA_21(21, "EMA 21"),
    EMA_50(50, "EMA 50"),
    SMA_20(20, "SMA 20"),
    SMA_200(200, "SMA 200")
}

// Analysis result
data class MarketAnalysis(
    val direction: MarketDirection,
    val confidence: Float,
    val upsideProbability: Float,
    val downsideProbability: Float,
    val suggestions: List<TradingSuggestion>,
    val indicators: Map<String, String>,
    val supportLevels: List<Double>,
    val resistanceLevels: List<Double>
)

enum class MarketDirection(val displayName: String, val emoji: String) {
    BULLISH("Bullish", "🟢"),
    BEARISH("Bearish", "🔴"),
    SIDEWAYS("Sideways", "🟡")
}

data class TradingSuggestion(
    val type: SuggestionType,
    val stock: String,
    val entry: Double,
    val target: Double,
    val stopLoss: Double,
    val riskReward: String,
    val reason: String
)

enum class SuggestionType {
    BUY, SELL, HOLD, AVOID
}

// Watchlist item
data class WatchlistItem(
    val stock: Stock,
    val addedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

// Time period for chart data
enum class ChartTimePeriod(val displayName: String, val hours: Int) {
    ONE_DAY("1D", 24),
    ONE_WEEK("1W", 168),
    ONE_MONTH("1M", 720),
    THREE_MONTHS("3M", 2160),
    SIX_MONTHS("6M", 4320),
    ONE_YEAR("1Y", 8760),
    ALL("ALL", 87600)
}

// Option Chain
data class OptionChainData(
    val strikePrice: Double,
    val callLtp: Double,
    val callChange: Double,
    val callVolume: Long,
    val callOi: Long,
    val putLtp: Double,
    val putChange: Double,
    val putVolume: Long,
    val putOi: Long,
    val isAtm: Boolean = false
)
