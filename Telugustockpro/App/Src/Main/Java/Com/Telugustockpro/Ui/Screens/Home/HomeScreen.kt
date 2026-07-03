package com.telugustockpro.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.telugustockpro.data.model.MarketIndex
import com.telugustockpro.data.model.Stock
import com.telugustockpro.ui.theme.TradingViewColors
import com.telugustockpro.viewmodel.StockViewModel
import java.text.DecimalFormat

private val priceFormat = DecimalFormat("#,##0.00")
private val percentFormat = DecimalFormat("0.00")
private val volumeFormat = DecimalFormat("##.##")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStockClick: (String) -> Unit,
    onSeeAllMarkets: () -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshData()
                },
                onNotifications = { }
            )
        },
        containerColor = TradingViewColors.Background
    ) { paddingValues ->
        if (uiState.marketIndices.isEmpty() && uiState.isLoading) {
            HomeLoadingShimmer(modifier = Modifier.padding(paddingValues))
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.refreshData()
                },
                modifier = Modifier.padding(paddingValues)
            ) {
                LaunchedEffect(uiState.isRefreshing) {
                    if (!uiState.isRefreshing) isRefreshing = false
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // ─── Market Status Banner ───
                    item {
                        MarketStatusBanner()
                    }

                    // ─── Market Indices Carousel ───
                    item {
                        SectionHeader(
                            title = "Market Indices",
                            icon = Icons.Filled.ShowChart
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MarketIndicesCarousel(
                            indices = uiState.marketIndices,
                            onIndexClick = { onStockClick(it.symbol) }
                        )
                    }

                    // ─── Quick Watchlist ───
                    if (uiState.watchlist.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader(
                                title = "My Watchlist",
                                icon = Icons.Filled.Star,
                                actionText = "Edit",
                                onActionClick = { }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(
                            items = uiState.watchlist.take(5),
                            key = { it.symbol }
                        ) { stock ->
                            WatchlistItem(
                                stock = stock,
                                onClick = { onStockClick(stock.symbol) },
                                onRemove = { viewModel.removeFromWatchlist(stock.symbol) }
                            )
                        }
                    }

                    // ─── Top Gainers ───
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = "Top Gainers",
                            icon = Icons.Filled.TrendingUp,
                            iconColor = TradingViewColors.Green,
                            actionText = "See All",
                            onActionClick = onSeeAllMarkets
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(
                        items = uiState.topGainers.take(5),
                        key = { _, stock -> stock.symbol }
                    ) { index, stock ->
                        StockListItem(
                            stock = stock,
                            rank = index + 1,
                            onClick = { onStockClick(stock.symbol) }
                        )
                    }

                    // ─── Top Losers ───
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = "Top Losers",
                            icon = Icons.Filled.TrendingDown,
                            iconColor = TradingViewColors.Red,
                            actionText = "See All",
                            onActionClick = onSeeAllMarkets
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(
                        items = uiState.topLosers.take(5),
                        key = { _, stock -> stock.symbol }
                    ) { index, stock ->
                        StockListItem(
                            stock = stock,
                            rank = index + 1,
                            onClick = { onStockClick(stock.symbol) }
                        )
                    }

                    // ─── Market Movers ───
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = "Market Movers",
                            icon = Icons.Filled.Bolt
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MarketMoversSection()
                    }

                    // ─── Sector Performance ───
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(
                            title = "Sector Performance",
                            icon = Icons.Filled.PieChart
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SectorPerformanceSection()
                    }

                    // ─── Quick Actions ───
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        QuickActionsSection(
                            onMarketsClick = onSeeAllMarkets,
                            onAnalysisClick = { onStockClick("NIFTY") },
                            onOptionChainClick = { onStockClick("NIFTY") }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onRefresh: () -> Unit,
    onNotifications: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TradingViewColors.Blue, TradingViewColors.Purple)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShowChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "Telugu Stock Pro",
                        style = MaterialTheme.typography.titleMedium,
                        color = TradingViewColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TradingViewColors.Green)
                        )
                        Text(
                            text = "Market Open • Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = TradingViewColors.Green
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = TradingViewColors.TextSecondary
                )
            }
            IconButton(onClick = onNotifications) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = TradingViewColors.Red,
                            contentColor = Color.White
                        ) {
                            Text("3", fontSize = 8.sp)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TradingViewColors.TextSecondary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TradingViewColors.Background
        )
    )
}

// ═══════════════════════════════════════════════════════════════
// MARKET STATUS BANNER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MarketStatusBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarketStatusItem(
                icon = Icons.Filled.AccessTime,
                label = "Market",
                value = "Open",
                color = TradingViewColors.Green
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = TradingViewColors.Border
            )
            MarketStatusItem(
                icon = Icons.Filled.Timer,
                label = "Closes",
                value = "3:30 PM",
                color = TradingViewColors.Yellow
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = TradingViewColors.Border
            )
            MarketStatusItem(
                icon = Icons.Filled.TrendingUp,
                label = "Advances",
                value = "1,847",
                color = TradingViewColors.Green
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = TradingViewColors.Border
            )
            MarketStatusItem(
                icon = Icons.Filled.TrendingDown,
                label = "Declines",
                value = "1,234",
                color = TradingViewColors.Red
            )
        }
    }
}

@Composable
private fun MarketStatusItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// MARKET INDICES CAROUSEL
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MarketIndicesCarousel(
    indices: List<MarketIndex>,
    onIndexClick: (MarketIndex) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        indices.take(4).forEach { index ->
            MarketIndexCard(
                index = index,
                onClick = { onIndexClick(index) }
            )
        }
    }
}

@Composable
private fun MarketIndexCard(
    index: MarketIndex,
    onClick: () -> Unit
) {
    val cardColor = if (index.isPositive) {
        Brush.verticalGradient(
            colors = listOf(
                TradingViewColors.GreenBackground,
                TradingViewColors.CardBackground
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                TradingViewColors.RedBackground,
                TradingViewColors.CardBackground
            )
        )
    }

    val accentColor = if (index.isPositive) TradingViewColors.Green else TradingViewColors.Red

    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
                .padding(16.dp)
        ) {
            // Index Name & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = index.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = TradingViewColors.TextSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (index.isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price
            Text(
                text = priceFormat.format(index.price),
                style = MaterialTheme.typography.headlineSmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Change & Percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (index.isPositive) "▲" else "▼"} ${if (index.isPositive) "+" else ""}${percentFormat.format(index.changePercent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${if (index.isPositive) "+" else ""}${priceFormat.format(index.change)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini Sparkline Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.1f))
            ) {
                // Simulated sparkline
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val heights = listOf(0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.7f, 0.9f, 0.6f, 0.8f, 1f)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(1.dp))
                                .background(accentColor.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION HEADER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconColor: Color = TradingViewColors.Blue,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TradingViewColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = TradingViewColors.Blue
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TradingViewColors.Blue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WATCHLIST ITEM
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WatchlistItem(
    stock: Stock,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stock Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        (if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.symbol.take(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stock Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TradingViewColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Price & Change
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = priceFormat.format(stock.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = (if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (stock.isPositive) "+" else ""}${percentFormat.format(stock.changePercent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = TradingViewColors.TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STOCK LIST ITEM (Gainers/Losers)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StockListItem(
    stock: Stock,
    rank: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        TradingViewColors.SurfaceVariant,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stock Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        (if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red).copy(alpha = 0.12f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (stock.isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stock Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TradingViewColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                // Volume
                Text(
                    text = "Vol: ${formatVolume(stock.volume)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price Column
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = priceFormat.format(stock.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Change badge
                val accentColor = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red
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
                            imageVector = if (stock.isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (stock.isPositive) "+" else ""}${percentFormat.format(stock.changePercent)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// MARKET MOVERS SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MarketMoversSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MoverCard(
            title = "Most Active",
            subtitle = "By Volume",
            items = listOf("SBIN" to "45.7M", "TCS" to "34.6M", "INFY" to "32.1M"),
            color = TradingViewColors.Blue
        )
        MoverCard(
            title = "52 Week High",
            subtitle = "New highs today",
            items = listOf("NESTLEIND" to "₹25,890", "TITAN" to "₹3,890", "BAJFIN" to "₹7,580"),
            color = TradingViewColors.Green
        )
        MoverCard(
            title = "52 Week Low",
            subtitle = "New lows today",
            items = listOf("TATASTEEL" to "₹89.50", "BANKBAR" to "₹145.20", "IDBI" to "₹52.30"),
            color = TradingViewColors.Red
        )
        MoverCard(
            title = "High Delivery",
            subtitle = "Strong hands buying",
            items = listOf("HDFCBANK" to "78%", "ICICI" to "72%", "SBIN" to "68%"),
            color = TradingViewColors.Purple
        )
    }
}

@Composable
private fun MoverCard(
    title: String,
    subtitle: String,
    items: List<Pair<String, String>>,
    color: Color
) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { (name, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextSecondary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTOR PERFORMANCE
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectorPerformanceSection() {
    val sectors = listOf(
        Triple("IT", 2.34, "💻"),
        Triple("Banking", 1.12, "🏦"),
        Triple("Pharma", -1.87, "💊"),
        Triple("Auto", 0.95, "🚗"),
        Triple("FMCG", 0.45, "🛒"),
        Triple("Metal", 3.21, "⚙️"),
        Triple("Energy", -0.78, "⚡"),
        Triple("Realty", 1.56, "🏗️")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sectors.forEach { (name, change, emoji) ->
            val isPositive = change >= 0
            val color = if (isPositive) TradingViewColors.Green else TradingViewColors.Red

            Card(
                modifier = Modifier.width(90.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TradingViewColors.CardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = TradingViewColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${if (isPositive) "+" else ""}${percentFormat.format(change)}%",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// QUICK ACTIONS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun QuickActionsSection(
    onMarketsClick: () -> Unit,
    onAnalysisClick: () -> Unit,
    onOptionChainClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Filled.ShowChart,
            label = "Markets",
            color = TradingViewColors.Blue,
            onClick = onMarketsClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.Psychology,
            label = "AI Analysis",
            color = TradingViewColors.Purple,
            onClick = onAnalysisClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.TableChart,
            label = "Options",
            color = TradingViewColors.Orange,
            onClick = onOptionChainClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LOADING SHIMMER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun HomeLoadingShimmer(modifier: Modifier = Modifier) {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha = shimmerTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shimmer for cards
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TradingViewColors.Surface.copy(alpha = alpha.value))
            )
        }

        // Shimmer for list items
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TradingViewColors.Surface.copy(alpha = alpha.value))
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UTILITY
// ═══════════════════════════════════════════════════════════════

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 100_000_000 -> "${volumeFormat.format(volume / 10_000_000.0)}Cr"
        volume >= 1_000_000 -> "${volumeFormat.format(volume / 100_000.0)}M"
        volume >= 1_000 -> "${volumeFormat.format(volume / 1_000.0)}K"
        else -> volume.toString()
    }
}
