package com.telugustockpro.data.repository

import com.telugustockpro.data.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.random.Random

object SampleData {

    // Major Indian Market Indices
    val marketIndices = listOf(
        MarketIndex("NIFTY 50", "NIFTY", 22456.80, 187.35, 0.84),
        MarketIndex("SENSEX", "SENSEX", 73890.45, 562.20, 0.77),
        MarketIndex("BANK NIFTY", "BANKNIFTY", 48234.90, -245.60, -0.51),
        MarketIndex("NIFTY IT", "NIFTYIT", 34567.25, 423.10, 1.24),
        MarketIndex("NIFTY FINANCIAL", "FINNIFTY", 20123.45, 89.30, 0.45),
        MarketIndex("INDIA VIX", "INDIAVIX", 13.45, -0.82, -5.74)
    )

    // Top 10 Gainers
    val topGainers = listOf(
        Stock("TATASTEEL", "Tata Steel Ltd", 156.75, 8.92, 6.04, 45_230_000, 158.20, 147.83, 148.50, 147.83, sector = "Metals"),
        Stock("ADANIENT", "Adani Enterprises Ltd", 3245.60, 167.85, 5.44, 12_450_000, 3268.00, 3077.75, 3090.00, 3077.75, sector = "Conglomerates"),
        Stock("BAJFINANCE", "Bajaj Finance Ltd", 7234.90, 312.45, 4.52, 8_920_000, 7289.00, 6922.45, 6950.00, 6922.45, sector = "Finance"),
        Stock("RELIANCE", "Reliance Industries Ltd", 2890.35, 108.70, 3.90, 23_456_000, 2912.00, 2781.65, 2795.00, 2781.65, sector = "Oil & Gas"),
        Stock("HCLTECH", "HCL Technologies Ltd", 1456.80, 48.90, 3.47, 15_670_000, 1478.50, 1407.90, 1415.00, 1407.90, sector = "IT"),
        Stock("LT", "Larsen & Toubro Ltd", 3567.45, 102.30, 2.96, 9_870_000, 3589.00, 3465.15, 3478.00, 3465.15, sector = "Infrastructure"),
        Stock("ASIANPAINT", "Asian Paints Ltd", 2876.30, 78.45, 2.80, 6_540_000, 2895.00, 2797.85, 2810.00, 2797.85, sector = "Consumer"),
        Stock("MARUTI", "Maruti Suzuki India Ltd", 11234.50, 287.60, 2.62, 4_320_000, 11289.00, 10946.90, 10980.00, 10946.90, sector = "Auto"),
        Stock("INFY", "Infosys Ltd", 1567.85, 36.70, 2.39, 34_560_000, 1589.00, 1531.15, 1540.00, 1531.15, sector = "IT"),
        Stock("WIPRO", "Wipro Ltd", 456.20, 9.85, 2.20, 28_900_000, 462.50, 446.35, 448.00, 446.35, sector = "IT")
    )

    // Top 10 Losers
    val topLosers = listOf(
        Stock("DRREDDY", "Dr. Reddy's Laboratories", 5678.90, -234.50, -3.96, 7_890_000, 5913.40, 5656.00, 5900.00, 5913.40, sector = "Pharma"),
        Stock("SUNPHARMA", "Sun Pharmaceutical Industries", 1123.45, -42.30, -3.63, 18_230_000, 1165.75, 1118.90, 1158.00, 1165.75, sector = "Pharma"),
        Stock("CIPLA", "Cipla Ltd", 1345.60, -45.80, -3.28, 12_450_000, 1391.40, 1338.70, 1385.00, 1391.40, sector = "Pharma"),
        Stock("TECHM", "Tech Mahindra Ltd", 1234.75, -36.90, -2.91, 15_670_000, 1271.65, 1228.50, 1268.00, 1271.65, sector = "IT"),
        Stock("TATAMOTORS", "Tata Motors Ltd", 789.45, -21.30, -2.63, 32_450_000, 810.75, 784.20, 805.00, 810.75, sector = "Auto"),
        Stock("BAJAJFINSV", "Bajaj Finserv Ltd", 1567.80, -38.45, -2.40, 9_870_000, 1606.25, 1559.30, 1598.00, 1606.25, sector = "Finance"),
        Stock("HINDALCO", "Hindalco Industries Ltd", 567.30, -12.85, -2.21, 21_340_000, 580.15, 562.40, 578.00, 580.15, sector = "Metals"),
        Stock("TATAPOWER", "Tata Power Company Ltd", 423.60, -8.90, -2.06, 18_900_000, 432.50, 419.70, 430.00, 432.50, sector = "Power"),
        Stock("ONGC", "Oil & Natural Gas Corp", 278.45, -5.60, -1.97, 25_670_000, 284.05, 275.80, 282.00, 284.05, sector = "Oil & Gas"),
        Stock("NTPC", "NTPC Limited", 345.20, -6.45, -1.83, 19_450_000, 351.65, 342.10, 349.00, 351.65, sector = "Power")
    )

    // All Stocks for Markets Screen
    val allStocks = topGainers + topLosers + listOf(
        Stock("TCS", "Tata Consultancy Services", 3890.75, 45.30, 1.18, 5_430_000, 3912.00, 3845.45, 3855.00, 3845.45, sector = "IT"),
        Stock("HDFCBANK", "HDFC Bank Ltd", 1678.90, 23.45, 1.42, 18_760_000, 1692.00, 1655.45, 1660.00, 1655.45, sector = "Banking"),
        Stock("ICICIBANK", "ICICI Bank Ltd", 1123.45, 18.70, 1.69, 22_340_000, 1138.00, 1104.75, 1108.00, 1104.75, sector = "Banking"),
        Stock("SBIN", "State Bank of India", 789.60, -4.30, -0.54, 45_670_000, 798.90, 785.20, 795.00, 793.90, sector = "Banking"),
        Stock("KOTAKBANK", "Kotak Mahindra Bank", 1845.30, 28.90, 1.59, 12_340_000, 1862.00, 1816.40, 1820.00, 1816.40, sector = "Banking"),
        Stock("BHARTIARTL", "Bharti Airtel Limited", 1456.75, 32.45, 2.27, 15_670_000, 1478.00, 1424.30, 1428.00, 1424.30, sector = "Telecom"),
        Stock("ITC", "ITC Limited", 467.85, 3.20, 0.69, 34_560_000, 472.50, 464.65, 465.00, 464.65, sector = "FMCG"),
        Stock("ASIANPAINT", "Asian Paints Ltd", 2876.30, 78.45, 2.80, 6_540_000, 2895.00, 2797.85, 2810.00, 2797.85, sector = "Consumer"),
        Stock("ULTRACEMCO", "UltraTech Cement Ltd", 9876.50, 156.30, 1.61, 3_450_000, 9920.00, 9720.20, 9745.00, 9720.20, sector = "Cement"),
        Stock("NESTLEIND", "Nestle India Limited", 24567.80, -234.50, -0.94, 890_000, 24802.30, 24456.00, 24789.00, 24802.30, sector = "FMCG"),
        Stock("POWERGRID", "Power Grid Corp", 298.45, 2.30, 0.78, 28_900_000, 302.50, 296.15, 297.00, 296.15, sector = "Power"),
        Stock("TITAN", "Titan Company Limited", 3456.70, 67.80, 2.00, 7_890_000, 3489.00, 3388.90, 3400.00, 3388.90, sector = "Consumer")
    )

    // Generate realistic candlestick data
    fun generateCandleData(basePrice: Double, days: Int = 365): List<CandleData> {
        val candles = mutableListOf<CandleData>()
        var currentPrice = basePrice * 0.85
        val zoneId = ZoneId.of("Asia/Kolkata")
        val startDate = LocalDate.now().minusDays(days.toLong())

        for (i in 0 until days) {
            val date = startDate.plusDays(i.toLong())
            val timestamp = date.atTime(9, 15).atZone(zoneId).toInstant().toEpochMilli()

            val volatility = 0.02 + Random.nextDouble() * 0.02
            val trend = when {
                i < days * 0.3 -> 0.001
                i < days * 0.6 -> -0.0005
                else -> 0.0008
            }

            val open = currentPrice
            val change = currentPrice * (trend + (Random.nextDouble() - 0.5) * volatility)
            val close = open + change

            val highExtra = abs(Random.nextDouble() * currentPrice * volatility * 0.5)
            val lowExtra = abs(Random.nextDouble() * currentPrice * volatility * 0.5)

            val high = maxOf(open, close) + highExtra
            val low = minOf(open, close) - lowExtra

            val baseVolume = (basePrice * 100_000).toLong()
            val volume = baseVolume + Random.nextLong(-baseVolume / 2, baseVolume / 2)

            candles.add(CandleData(timestamp, open, high, low, close, volume))
            currentPrice = close
        }
        return candles
    }

    // Generate intraday data (1-minute candles for 1 day)
    fun generateIntradayData(basePrice: Double): List<CandleData> {
        val candles = mutableListOf<CandleData>()
        var currentPrice = basePrice * 0.998
        val zoneId = ZoneId.of("Asia/Kolkata")
        val today = LocalDate.now()

        // Market hours: 9:15 AM to 3:30 PM (375 minutes)
        for (minute in 0 until 375) {
            val hour = 9 + (minute + 15) / 60
            val min = (minute + 15) % 60
            val timestamp = today.atTime(hour, min).atZone(zoneId).toInstant().toEpochMilli()

            val volatility = 0.002 + Random.nextDouble() * 0.003
            val open = currentPrice
            val change = currentPrice * (Random.nextDouble() - 0.48) * volatility
            val close = open + change
            val high = maxOf(open, close) + abs(Random.nextDouble() * currentPrice * volatility * 0.3)
            val low = minOf(open, close) - abs(Random.nextDouble() * currentPrice * volatility * 0.3)
            val volume = Random.nextLong(50_000, 500_000)

            candles.add(CandleData(timestamp, open, high, low, close, volume))
            currentPrice = close
        }
        return candles
    }

    // Generate Option Chain data for Nifty
    fun generateOptionChainData(currentPrice: Double): List<OptionChainData> {
        val chain = mutableListOf<OptionChainData>()
        val strikeSpacing = 50.0
        val startStrike = (currentPrice / strikeSpacing).toInt() * strikeSpacing - 500

        for (i in 0 until 21) {
            val strike = startStrike + i * strikeSpacing
            val distanceFromAtm = abs(strike - currentPrice)
            val isAtm = abs(strike - currentPrice) < strikeSpacing / 2

            val callBase = maxOf(0.05, (currentPrice - strike) * 0.01 + Random.nextDouble() * 50)
            val putBase = maxOf(0.05, (strike - currentPrice) * 0.01 + Random.nextDouble() * 50)

            chain.add(
                OptionChainData(
                    strikePrice = strike,
                    callLtp = callBase,
                    callChange = (Random.nextDouble() - 0.5) * callBase * 0.1,
                    callVolume = Random.nextLong(10_000, 500_000),
                    callOi = Random.nextLong(100_000, 2_000_000),
                    putLtp = putBase,
                    putChange = (Random.nextDouble() - 0.5) * putBase * 0.1,
                    putVolume = Random.nextLong(10_000, 500_000),
                    putOi = Random.nextLong(100_000, 2_000_000),
                    isAtm = isAtm
                )
            )
        }
        return chain
    }

    // Generate market analysis
    fun generateAnalysis(stock: Stock): MarketAnalysis {
        val random = Random.nextFloat()
        val direction = when {
            random < 0.4f -> MarketDirection.BULLISH
            random < 0.7f -> MarketDirection.BEARISH
            else -> MarketDirection.SIDEWAYS
        }

        val confidence = 0.55f + Random.nextFloat() * 0.35f
        val upsideProb = when (direction) {
            MarketDirection.BULLISH -> 0.6f + Random.nextFloat() * 0.25f
            MarketDirection.BEARISH -> 0.25f + Random.nextFloat() * 0.15f
            else -> 0.4f + Random.nextFloat() * 0.2f
        }
        val downsideProb = 1f - upsideProb

        val suggestions = when (direction) {
            MarketDirection.BULLISH -> listOf(
                TradingSuggestion(
                    SuggestionType.BUY, stock.symbol,
                    entry = stock.price,
                    target = stock.price * 1.08,
                    stopLoss = stock.price * 0.96,
                    riskReward = "1:2",
                    reason = "Strong momentum with bullish indicators aligned"
                ),
                TradingSuggestion(
                    SuggestionType.HOLD, stock.symbol,
                    entry = stock.price * 0.97,
                    target = stock.price * 1.12,
                    stopLoss = stock.price * 0.93,
                    riskReward = "1:3",
                    reason = "Pullback entry for medium-term upside"
                )
            )
            MarketDirection.BEARISH -> listOf(
                TradingSuggestion(
                    SuggestionType.SELL, stock.symbol,
                    entry = stock.price,
                    target = stock.price * 0.93,
                    stopLoss = stock.price * 1.03,
                    riskReward = "1:1.5",
                    reason = "Breaking support with bearish divergence"
                ),
                TradingSuggestion(
                    SuggestionType.AVOID, stock.symbol,
                    entry = stock.price,
                    target = stock.price * 0.90,
                    stopLoss = stock.price * 1.05,
                    riskReward = "N/A",
                    reason = "High risk - wait for trend reversal confirmation"
                )
            )
            MarketDirection.SIDEWAYS -> listOf(
                TradingSuggestion(
                    SuggestionType.HOLD, stock.symbol,
                    entry = stock.price,
                    target = stock.price * 1.03,
                    stopLoss = stock.price * 0.97,
                    riskReward = "1:1",
                    reason = "Range-bound trading with strict levels"
                )
            )
        }

        return MarketAnalysis(
            direction = direction,
            confidence = confidence,
            upsideProbability = upsideProb,
            downsideProbability = downsideProb,
            suggestions = suggestions,
            indicators = mapOf(
                "RSI (14)" to "${30 + Random.nextFloat() * 40}",
                "MACD" to if (direction == MarketDirection.BULLISH) "Bullish Crossover" else "Bearish Signal",
                "EMA 9" to if (stock.price > stock.price * 0.98) "Above" else "Below",
                "EMA 21" to if (stock.price > stock.price * 0.96) "Above" else "Below",
                "Volume" to if (Random.nextFloat() > 0.5f) "Above Average" else "Below Average",
                "Bollinger Band" to if (Random.nextFloat() > 0.5f) "Upper Band" else "Lower Band"
            ),
            supportLevels = listOf(
                stock.price * 0.97,
                stock.price * 0.94,
                stock.price * 0.90
            ),
            resistanceLevels = listOf(
                stock.price * 1.03,
                stock.price * 1.06,
                stock.price * 1.10
            )
        )
    }
}
