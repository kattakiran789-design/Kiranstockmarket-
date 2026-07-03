package com.telugustockpro.ui.screens.analysis

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.telugustockpro.data.model.*
import com.telugustockpro.ui.components.*
import com.telugustockpro.ui.theme.*
import com.telugustockpro.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    stockSymbol: String,
    onBackClick: () -> Unit,
    onTradeClick: (String) -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(stockSymbol) {
        viewModel.selectStock(stockSymbol)
        viewModel.loadAnalysis(stockSymbol)
    }

    val stock = uiState.selectedStock
    val analysis = uiState.marketAnalysis

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Analysis",
                            style = MaterialTheme.typography.titleLarge,
                            color = TradingViewColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stock?.symbol ?: stockSymbol,
                            style = MaterialTheme.typography.bodySmall,
                            color = TradingViewColors.Purple
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TradingViewColors.Background
                )
            )
        },
        containerColor = TradingViewColors.Background
    ) { paddingValues ->
        if (uiState.isLoading || analysis == null) {
            LoadingIndicator(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Market Direction Card
                item {
                    DirectionCard(analysis = analysis, stock = stock)
                }

                // Probability Bars
                item {
                    ProbabilityCard(analysis = analysis)
                }

                // Technical Indicators
                item {
                    IndicatorsCard(analysis = analysis)
                }

                // Support & Resistance
                item {
                    SupportResistanceCard(analysis = analysis, stock = stock)
                }

                // Trading Suggestions
                item {
                    Text(
                        text = "📌 Trading Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        color = TradingViewColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(analysis.suggestions) { suggestion ->
                    SuggestionCard(suggestion = suggestion)
                }

                // Disclaimer
                item {
                    DisclaimerCard()
                }

                // Action Button
                item {
                    Button(
                        onClick = { onTradeClick(stockSymbol) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TradingViewColors.Blue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Chart & Trade",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionCard(
    analysis: MarketAnalysis,
    stock: Stock?
) {
    val directionColor = when (analysis.direction) {
        MarketDirection.BULLISH -> BullishColor
        MarketDirection.BEARISH -> BearishColor
        MarketDirection.SIDEWAYS -> SidewaysColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            directionColor.copy(alpha = 0.15f),
                            TradingViewColors.CardBackground
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Direction Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(directionColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = analysis.direction.emoji,
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Market Direction",
                style = MaterialTheme.typography.bodyMedium,
                color = TradingViewColors.TextSecondary
            )

            Text(
                text = analysis.direction.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = directionColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence Meter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Confidence:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TradingViewColors.TextSecondary
                )
                LinearProgressIndicator(
                    progress = { analysis.confidence },
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = directionColor,
                    trackColor = TradingViewColors.Surface
                )
                Text(
                    text = "${(analysis.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = directionColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProbabilityCard(analysis: MarketAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Probability Analysis",
                style = MaterialTheme.typography.titleSmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Upside Probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = TradingViewColors.Green,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Upside",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.TextSecondary
                    )
                }
                Text(
                    text = "${(analysis.upsideProbability * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = TradingViewColors.Green,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { analysis.upsideProbability },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = TradingViewColors.Green,
                trackColor = TradingViewColors.GreenBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Downside Probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = TradingViewColors.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Downside",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.TextSecondary
                    )
                }
                Text(
                    text = "${(analysis.downsideProbability * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = TradingViewColors.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { analysis.downsideProbability },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = TradingViewColors.Red,
                trackColor = TradingViewColors.RedBackground
            )
        }
    }
}

@Composable
private fun IndicatorsCard(analysis: MarketAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Technical Indicators",
                style = MaterialTheme.typography.titleSmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            analysis.indicators.forEach { (name, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.TextSecondary
                    )

                    val valueColor = when {
                        value.contains("Above", ignoreCase = true) -> TradingViewColors.Green
                        value.contains("Below", ignoreCase = true) -> TradingViewColors.Red
                        value.contains("Bullish", ignoreCase = true) -> TradingViewColors.Green
                        value.contains("Bearish", ignoreCase = true) -> TradingViewColors.Red
                        else -> TradingViewColors.TextPrimary
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = valueColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = value,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = valueColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                HorizontalDivider(color = TradingViewColors.BorderLight)
            }
        }
    }
}

@Composable
private fun SupportResistanceCard(
    analysis: MarketAnalysis,
    stock: Stock?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Support & Resistance",
                style = MaterialTheme.typography.titleSmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current Price Level
            stock?.let { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TradingViewColors.Blue.copy(alpha = 0.1f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Price",
                        style = MaterialTheme.typography.labelMedium,
                        color = TradingViewColors.Blue
                    )
                    Text(
                        text = priceFormat.format(s.price),
                        style = MaterialTheme.typography.labelMedium,
                        color = TradingViewColors.Blue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resistance Levels
            Text(
                text = "Resistance Levels",
                style = MaterialTheme.typography.labelMedium,
                color = TradingViewColors.Red
            )
            analysis.resistanceLevels.forEach { level ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "R${analysis.resistanceLevels.indexOf(level) + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = priceFormat.format(level),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.Red
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = TradingViewColors.Border
            )

            // Support Levels
            Text(
                text = "Support Levels",
                style = MaterialTheme.typography.labelMedium,
                color = TradingViewColors.Green
            )
            analysis.supportLevels.forEach { level ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "S${analysis.supportLevels.indexOf(level) + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = priceFormat.format(level),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.Green
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: TradingSuggestion) {
    val typeColor = when (suggestion.type) {
        SuggestionType.BUY -> SuggestionBuy
        SuggestionType.SELL -> SuggestionSell
        SuggestionType.HOLD -> SuggestionHold
        SuggestionType.AVOID -> SuggestionAvoid
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = suggestion.type.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = typeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = suggestion.stock,
                    style = MaterialTheme.typography.labelMedium,
                    color = TradingViewColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Entry, Target, Stop Loss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = priceFormat.format(suggestion.entry),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = priceFormat.format(suggestion.target),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.Green,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "Stop Loss",
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = priceFormat.format(suggestion.stopLoss),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.Red,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                        text = "R:R",
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextTertiary
                    )
                    Text(
                        text = suggestion.riskReward,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TradingViewColors.Yellow,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = TradingViewColors.TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = suggestion.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TradingViewColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Yellow.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = TradingViewColors.Yellow,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Disclaimer: AI analysis is for educational purposes only. Markets are volatile. Always do your own research before trading. Past performance doesn't guarantee future results.",
                style = MaterialTheme.typography.bodySmall,
                color = TradingViewColors.TextSecondary
            )
        }
    }
}
