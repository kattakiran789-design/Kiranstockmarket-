package com.telugustockpro.ui.screens.chart

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.telugustockpro.data.model.ChartTimePeriod
import com.telugustockpro.data.model.OptionChainData
import com.telugustockpro.ui.components.MPAndroidCandlestickChart
import com.telugustockpro.ui.theme.TradingViewColors
import com.telugustockpro.viewmodel.StockViewModel
import java.text.DecimalFormat

private val priceFormat = DecimalFormat("#,##0.00")
private val percentFormat = DecimalFormat("0.00")
private val volumeFormat = DecimalFormat("##.##")

// ═══════════════════════════════════════════════════════════════
// CHART SCREEN - COMPLETE IMPLEMENTATION
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    stockSymbol: String,
    onBackClick: () -> Unit,
    onAnalysisClick: (String) -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showIndicators by remember { mutableStateOf(false) }
    var showOptionChain by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(true) }
    var chartRefreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(stockSymbol) {
        viewModel.selectStock(stockSymbol)
    }

    val stock = uiState.selectedStock

    Scaffold(
        topBar = {
            ChartTopBar(
                stock = stock,
                stockSymbol = stockSymbol,
                onBackClick = onBackClick,
                onIndicatorsToggle = { showIndicators = !showIndicators },
                onVolumeToggle = { showVolume = !showVolume },
                onOptionChainToggle = { showOptionChain = !showOptionChain },
                onAnalysisClick = { onAnalysisClick(stockSymbol) },
                showIndicators = showIndicators,
                showVolume = showVolume,
                showOptionChain = showOptionChain
            )
        },
        containerColor = TradingViewColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ─── PRICE HEADER ───
            stock?.let { s ->
                PriceHeaderSection(stock = s)
            }

            // ─── TIME PERIOD SELECTOR ───
            TimePeriodSelector(
                selectedPeriod = uiState.selectedTimePeriod,
                onPeriodSelect = { period ->
                    viewModel.loadCandleData(stockSymbol, period)
                }
            )

            // ─── INDICATORS PANEL ───
            AnimatedVisibility(
                visible = showIndicators,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                IndicatorsPanelSection(
                    activeIndicators = uiState.activeIndicators,
                    onToggleIndicator = { viewModel.toggleIndicator(it) }
                )
            }

            // ─── MAIN CANDLESTICK CHART ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(TradingViewColors.Background)
            ) {
                if (uiState.candleData.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TradingViewColors.Blue,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    }
                } else {
                    MPAndroidCandlestickChart(
                        candleData = uiState.candleData,
                        activeIndicators = uiState.activeIndicators,
                        showVolume = showVolume,
                        key = chartRefreshTrigger
                    )
                }
            }

            // ─── CHART INFO & VOLUME LEGEND ───
            ChartInfoBar(
                candleData = uiState.candleData,
                activeIndicators = uiState.activeIndicators
            )

            // ─── OPTION CHAIN PANEL ───
            AnimatedVisibility(
                visible = showOptionChain,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OptionChainPanelSection(
                    optionChain = uiState.optionChain,
                    currentPrice = stock?.price ?: 0.0
                )
            }

            // ─── BOTTOM ACTIONS ───
            ChartActionButtons(
                onAnalysisClick = { onAnalysisClick(stockSymbol) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CHART TOP BAR
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartTopBar(
    stock: Stock?,
    stockSymbol: String,
    onBackClick: () -> Unit,
    onIndicatorsToggle: () -> Unit,
    onVolumeToggle: () -> Unit,
    onOptionChainToggle: () -> Unit,
    onAnalysisClick: () -> Unit,
    showIndicators: Boolean,
    showVolume: Boolean,
    showOptionChain: Boolean
) {
    TopAppBar(
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stock?.symbol ?: stockSymbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = TradingViewColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    // Change badge
                    stock?.let { s ->
                        val accentColor = if (s.isPositive) TradingViewColors.Green else TradingViewColors.Red
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = if (s.isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${if (s.isPositive) "+" else ""}${percentFormat.format(s.changePercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }
                        }
                    }
                }

                stock?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TradingViewColors.TextSecondary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TradingViewColors.TextPrimary
                )
            }
        },
        actions = {
            // Indicators
            IconButton(onClick = onIndicatorsToggle) {
                Icon(
                    imageVector = Icons.Filled.InsertChart,
                    contentDescription = "Indicators",
                    tint = if (showIndicators) TradingViewColors.Blue else TradingViewColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Volume
            IconButton(onClick = onVolumeToggle) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = "Volume",
                    tint = if (showVolume) TradingViewColors.Blue else TradingViewColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Option Chain
            IconButton(onClick = onOptionChainToggle) {
                Icon(
                    imageVector = Icons.Filled.TableChart,
                    contentDescription = "Option Chain",
                    tint = if (showOptionChain) TradingViewColors.Blue else TradingViewColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // AI Analysis
            IconButton(onClick = onAnalysisClick) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = "AI Analysis",
                    tint = TradingViewColors.Purple,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TradingViewColors.Background,
            navigationIconContentColor = TradingViewColors.TextPrimary,
            actionIconContentColor = TradingViewColors.TextSecondary
        )
    )
}

// ═══════════════════════════════════════════════════════════════
// PRICE HEADER SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PriceHeaderSection(stock: Stock) {
    val accentColor = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Price
            Column {
                Text(
                    text = priceFormat.format(stock.price),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OHLCItem("O", stock.open)
                    OHLCItem("H", stock.high)
                    OHLCItem("L", stock.low)
                    OHLCItem("C", stock.price)
                }
            }

            // Change Column
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (stock.isPositive) "+" else ""}${priceFormat.format(stock.change)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (stock.isPositive) "+" else ""}${percentFormat.format(stock.changePercent)}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OHLCItem(label: String, value: Double) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
        Text(
            text = priceFormat.format(value),
            style = MaterialTheme.typography.labelMedium,
            color = TradingViewColors.TextSecondary
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// TIME PERIOD SELECTOR
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TimePeriodSelector(
    selectedPeriod: ChartTimePeriod,
    onPeriodSelect: (ChartTimePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ChartTimePeriod.values().forEach { period ->
            val isSelected = selectedPeriod == period
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPeriodSelect(period) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) TradingViewColors.Blue
                else TradingViewColors.Surface
            ) {
                Text(
                    text = period.displayName,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) TradingViewColors.TextPrimary
                    else TradingViewColors.TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// INDICATORS PANEL
// ═══════════════════════════════════════════════════════════════

@Composable
private fun IndicatorsPanelSection(
    activeIndicators: Set<String>,
    onToggleIndicator: (String) -> Unit
) {
    val indicators = listOf(
        Triple("MA_9", "EMA 9", TradingViewColors.EMA9),
        Triple("MA_21", "EMA 21", TradingViewColors.EMA21),
        Triple("MA_50", "EMA 50", TradingViewColors.EMA50),
        Triple("BB", "Bollinger", TradingViewColors.Bands),
        Triple("RSI", "RSI (14)", TradingViewColors.RSI),
        Triple("MACD", "MACD", TradingViewColors.MACD),
        Triple("Volume", "Volume", TradingViewColors.GreenVolume)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TradingViewColors.Surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Technical Indicators",
                style = MaterialTheme.typography.titleSmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${activeIndicators.size} active",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            indicators.forEach { (key, name, color) ->
                val isActive = activeIndicators.contains(key)

                FilterChip(
                    selected = isActive,
                    onClick = { onToggleIndicator(key) },
                    label = {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2f),
                        containerColor = TradingViewColors.CardBackground
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = TradingViewColors.Border,
                        selectedBorderColor = color,
                        enabled = true,
                        selected = isActive
                    )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CHART INFO BAR
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ChartInfoBar(
    candleData: List<com.telugustockpro.data.model.CandleData>,
    activeIndicators: Set<String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TradingViewColors.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeIndicators.contains("MA_9")) {
                IndicatorLegend("EMA 9", TradingViewColors.EMA9)
            }
            if (activeIndicators.contains("MA_21")) {
                IndicatorLegend("EMA 21", TradingViewColors.EMA21)
            }
            if (activeIndicators.contains("MA_50")) {
                IndicatorLegend("EMA 50", TradingViewColors.EMA50)
            }
        }

        if (candleData.isNotEmpty()) {
            Text(
                text = "Vol: ${formatVolume(candleData.last().volume)}",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextSecondary
            )
        }
    }
}

@Composable
private fun IndicatorLegend(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// OPTION CHAIN PANEL
// ═══════════════════════════════════════════════════════════════

@Composable
private fun OptionChainPanelSection(
    optionChain: List<OptionChainData>,
    currentPrice: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(TradingViewColors.Surface)
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋 Option Chain",
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = TradingViewColors.Blue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "NIFTY",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.Blue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Spot",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
                Text(
                    text = priceFormat.format(currentPrice),
                    style = MaterialTheme.typography.labelMedium,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Column Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TradingViewColors.CardBackground)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "LTP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
                Text(
                    "Vol",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
            }
            Text(
                "CALLS",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "STRIKE",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.Yellow,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "PUTS",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "LTP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
                Text(
                    "Vol",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
            }
        }

        HorizontalDivider(color = TradingViewColors.Border, thickness = 0.5.dp)

        // Option Rows
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(optionChain) { option ->
                OptionChainRow(option = option, isAtm = option.isAtm)
            }
        }
    }
}

@Composable
private fun OptionChainRow(
    option: OptionChainData,
    isAtm: Boolean
) {
    val bgColor = if (isAtm) TradingViewColors.Yellow.copy(alpha = 0.1f)
    else TradingViewColors.Background

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Call LTP
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = priceFormat.format(option.callLtp),
                style = MaterialTheme.typography.bodySmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatVolume(option.callVolume),
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary
            )
        }

        Box(
            modifier = Modifier
                .width(2.dp)
                .height(24.dp)
                .background(TradingViewColors.Green.copy(alpha = 0.3f))
        )

        // Strike Price
        Column(
            modifier = Modifier.weight(0.6f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = priceFormat.format(option.strikePrice),
                style = MaterialTheme.typography.bodySmall,
                color = if (isAtm) TradingViewColors.Yellow else TradingViewColors.TextSecondary,
                fontWeight = if (isAtm) FontWeight.Bold else FontWeight.Normal
            )
            if (isAtm) {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = TradingViewColors.Yellow.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "ATM",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.Yellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(2.dp)
                .height(24.dp)
                .background(TradingViewColors.Red.copy(alpha = 0.3f))
        )

        // Put LTP
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = priceFormat.format(option.putLtp),
                style = MaterialTheme.typography.bodySmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatVolume(option.putVolume),
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ACTION BUTTONS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ChartActionButtons(
    onAnalysisClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // BUY Button
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = TradingViewColors.Green
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                Icons.Filled.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "BUY",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            )
        }

        // SELL Button
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = TradingViewColors.Red
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                Icons.Filled.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "SELL",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            )
        }

        // Analysis Button
        OutlinedButton(
            onClick = onAnalysisClick,
            modifier = Modifier.weight(0.5f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TradingViewColors.Purple
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(TradingViewColors.Purple)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                Icons.Filled.Psychology,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════════════════

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 100_000_000 -> "${volumeFormat.format(volume / 10_000_000.0)}Cr"
        volume >= 1_000_000 -> "${volumeFormat.format(volume / 100_000.0)}M"
        volume >= 1_000 -> "${volumeFormat.format(volume / 1_000.0)}K"
        else -> volume.toString()
    }
}

import com.telugustockpro.data.model.Stock
