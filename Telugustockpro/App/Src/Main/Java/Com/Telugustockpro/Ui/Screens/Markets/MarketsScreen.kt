package com.telugustockpro.ui.screens.markets

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.telugustockpro.data.model.Stock
import com.telugustockpro.ui.theme.TradingViewColors
import com.telugustockpro.viewmodel.StockViewModel
import java.text.DecimalFormat

private val priceFormat = DecimalFormat("#,##0.00")
private val percentFormat = DecimalFormat("0.00")
private val volumeFormat = DecimalFormat("##.##")

enum class MarketFilter(val displayName: String, val icon: @Composable () -> Unit) {
    ALL("All", { Icon(Icons.Filled.Apps, null, modifier = Modifier.size(14.dp)) }),
    GAINERS("Gainers", { Icon(Icons.Filled.TrendingUp, null, modifier = Modifier.size(14.dp), tint = TradingViewColors.Green) }),
    LOSERS("Losers", { Icon(Icons.Filled.TrendingDown, null, modifier = Modifier.size(14.dp), tint = TradingViewColors.Red) }),
    MOST_ACTIVE("Volume", { Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(14.dp), tint = TradingViewColors.Yellow) }),
    MY_WATCHLIST("Watchlist", { Icon(Icons.Filled.Star, null, modifier = Modifier.size(14.dp), tint = TradingViewColors.Orange) })
}

enum class SortOption(val displayName: String) {
    SYMBOL("A-Z"),
    PRICE_HIGH("Price ↓"),
    PRICE_LOW("Price ↑"),
    CHANGE_HIGH("Gain %"),
    CHANGE_LOW("Loss %"),
    VOLUME("Volume")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    onStockClick: (String) -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MarketFilter.ALL) }
    var selectedSort by remember { mutableStateOf(SortOption.SYMBOL) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Filter and sort stocks
    val displayStocks = remember(uiState, searchQuery, selectedFilter, selectedSort) {
        val baseList = when {
            searchQuery.isNotBlank() -> {
                uiState.allStocks.filter { stock ->
                    stock.symbol.contains(searchQuery, ignoreCase = true) ||
                            stock.name.contains(searchQuery, ignoreCase = true) ||
                            stock.sector.contains(searchQuery, ignoreCase = true)
                }
            }
            selectedFilter == MarketFilter.MY_WATCHLIST -> uiState.watchlist
            selectedFilter == MarketFilter.GAINERS -> uiState.allStocks.filter { it.isPositive }
            selectedFilter == MarketFilter.LOSERS -> uiState.allStocks.filter { !it.isPositive }
            selectedFilter == MarketFilter.MOST_ACTIVE -> uiState.allStocks
            else -> uiState.allStocks
        }

        // Sort
        when (selectedSort) {
            SortOption.SYMBOL -> baseList.sortedBy { it.symbol }
            SortOption.PRICE_HIGH -> baseList.sortedByDescending { it.price }
            SortOption.PRICE_LOW -> baseList.sortedBy { it.price }
            SortOption.CHANGE_HIGH -> baseList.sortedByDescending { it.changePercent }
            SortOption.CHANGE_LOW -> baseList.sortedBy { it.changePercent }
            SortOption.VOLUME -> baseList.sortedByDescending { it.volume }
        }
    }

    // Focus search on activate
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            MarketsTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchToggle = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) {
                        searchQuery = ""
                        focusManager.clearFocus()
                    }
                },
                onQueryChange = { searchQuery = it },
                onClearQuery = { searchQuery = "" },
                focusRequester = focusRequester
            )
        },
        containerColor = TradingViewColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ─── Market Summary Banner ───
            MarketSummaryBanner(
                totalStocks = uiState.allStocks.size,
                advances = uiState.allStocks.count { it.isPositive },
                declines = uiState.allStocks.count { !it.isPositive }
            )

            // ─── Filter Chips ───
            FilterChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelect = { selectedFilter = it }
            )

            // ─── Sort Options ───
            SortOptionsRow(
                selectedSort = selectedSort,
                onSortSelect = { selectedSort = it },
                stockCount = displayStocks.size
            )

            HorizontalDivider(
                color = TradingViewColors.Border,
                thickness = 0.5.dp
            )

            // ─── Column Headers ───
            StockListHeader()

            // ─── Stock List ───
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshData()
                }
            ) {
                LaunchedEffect(uiState.isRefreshing) {
                    if (!uiState.isRefreshing) isRefreshing = false
                }

                if (displayStocks.isEmpty()) {
                    EmptyState(
                        searchQuery = searchQuery,
                        filter = selectedFilter
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(
                            items = displayStocks,
                            key = { _, stock -> stock.symbol }
                        ) { index, stock ->
                            StockListRow(
                                stock = stock,
                                rank = index + 1,
                                onClick = { onStockClick(stock.symbol) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TOP BAR WITH SEARCH
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketsTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchToggle: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "search_transition"
            ) { isActive ->
                if (isActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                "Search stocks, sectors...",
                                color = TradingViewColors.TextTertiary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                null,
                                tint = TradingViewColors.TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearQuery,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Clear,
                                        "Clear",
                                        tint = TradingViewColors.TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TradingViewColors.Blue,
                            unfocusedBorderColor = TradingViewColors.Border,
                            focusedContainerColor = TradingViewColors.Surface,
                            unfocusedContainerColor = TradingViewColors.Surface,
                            cursorColor = TradingViewColors.Blue,
                            focusedTextColor = TradingViewColors.TextPrimary,
                            unfocusedTextColor = TradingViewColors.TextPrimary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                        }),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column {
                        Text(
                            text = "Markets",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TradingViewColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NSE • BSE • All Segments",
                            style = MaterialTheme.typography.labelSmall,
                            color = TradingViewColors.TextTertiary
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        "Back",
                        tint = TradingViewColors.TextPrimary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = if (isSearchActive) TradingViewColors.Blue else TradingViewColors.TextSecondary
                )
            }
            if (!isSearchActive) {
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Filled.FilterList,
                        "Filter",
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
// MARKET SUMMARY BANNER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MarketSummaryBanner(
    totalStocks: Int,
    advances: Int,
    declines: Int
) {
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
            SummaryItem(
                label = "Total",
                value = "$totalStocks",
                color = TradingViewColors.Blue,
                icon = Icons.Filled.Apps
            )
            VerticalDivider(modifier = Modifier.height(36.dp), color = TradingViewColors.Border)
            SummaryItem(
                label = "Advances",
                value = "$advances",
                color = TradingViewColors.Green,
                icon = Icons.Filled.TrendingUp
            )
            VerticalDivider(modifier = Modifier.height(36.dp), color = TradingViewColors.Border)
            SummaryItem(
                label = "Declines",
                value = "$declines",
                color = TradingViewColors.Red,
                icon = Icons.Filled.TrendingDown
            )
            VerticalDivider(modifier = Modifier.height(36.dp), color = TradingViewColors.Border)
            SummaryItem(
                label = "Unchanged",
                value = "${totalStocks - advances - declines}",
                color = TradingViewColors.Yellow,
                icon = Icons.Filled.Remove
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            fontSize = 9.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// FILTER CHIPS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FilterChipsRow(
    selectedFilter: MarketFilter,
    onFilterSelect: (MarketFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MarketFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val chipColor = when (filter) {
                MarketFilter.GAINERS -> TradingViewColors.Green
                MarketFilter.LOSERS -> TradingViewColors.Red
                MarketFilter.MOST_ACTIVE -> TradingViewColors.Yellow
                MarketFilter.MY_WATCHLIST -> TradingViewColors.Orange
                else -> TradingViewColors.Blue
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelect(filter) },
                label = {
                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    filter.icon()
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = chipColor,
                    selectedLeadingIconColor = chipColor,
                    containerColor = TradingViewColors.Surface,
                    labelColor = TradingViewColors.TextSecondary,
                    leadingIconColor = TradingViewColors.TextTertiary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = TradingViewColors.Border,
                    selectedBorderColor = chipColor,
                    enabled = true,
                    selected = isSelected
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SORT OPTIONS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SortOptionsRow(
    selectedSort: SortOption,
    onSortSelect: (SortOption) -> Unit,
    stockCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$stockCount stocks",
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SortOption.values().forEach { sort ->
                val isSelected = selectedSort == sort
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSortSelect(sort) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) TradingViewColors.Blue.copy(alpha = 0.15f)
                    else Color.Transparent
                ) {
                    Text(
                        text = sort.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) TradingViewColors.Blue else TradingViewColors.TextTertiary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STOCK LIST HEADER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StockListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TradingViewColors.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank + Stock
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(20.dp)
            )
            Text(
                text = "Stock",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Sector tag width
        Spacer(modifier = Modifier.width(36.dp))

        // Price
        Text(
            text = "Price",
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Change %
        Text(
            text = "Chg %",
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(72.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Volume
        Text(
            text = "Volume",
            style = MaterialTheme.typography.labelSmall,
            color = TradingViewColors.TextTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// STOCK LIST ROW
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StockListRow(
    stock: Stock,
    rank: Int,
    onClick: () -> Unit
) {
    val accentColor = if (stock.isPositive) TradingViewColors.Green else TradingViewColors.Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = TradingViewColors.Background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelSmall,
                color = TradingViewColors.TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Stock Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        accentColor.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.symbol.take(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Stock Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleSmall,
                        color = TradingViewColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TradingViewColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            // Sector Tag
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = TradingViewColors.SurfaceVariant
            ) {
                Text(
                    text = stock.sector.take(4),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextTertiary,
                    fontSize = 8.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Price
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = priceFormat.format(stock.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TradingViewColors.TextPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "₹${priceFormat.format(stock.change)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    textAlign = TextAlign.End,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Change %
            Surface(
                modifier = Modifier.width(72.dp),
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (stock.isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${if (stock.isPositive) "+" else ""}${percentFormat.format(stock.changePercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Volume
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = formatVolume(stock.volume),
                    style = MaterialTheme.typography.labelSmall,
                    color = TradingViewColors.TextSecondary,
                    textAlign = TextAlign.End
                )
                // Volume bar
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TradingViewColors.SurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(
                                (stock.volume.toFloat() / 50_000_000f).coerceIn(0.1f, 1f)
                            )
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(
    searchQuery: String,
    filter: MarketFilter
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when {
                searchQuery.isNotEmpty() -> Icons.Filled.SearchOff
                filter == MarketFilter.MY_WATCHLIST -> Icons.Outlined.StarOutline
                else -> Icons.Filled.Inventory2
            },
            contentDescription = null,
            tint = TradingViewColors.TextTertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                searchQuery.isNotEmpty() -> "No stocks found"
                filter == MarketFilter.MY_WATCHLIST -> "Your watchlist is empty"
                else -> "No data available"
            },
            style = MaterialTheme.typography.titleMedium,
            color = TradingViewColors.TextSecondary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                searchQuery.isNotEmpty() -> "Try searching with a different keyword"
                filter == MarketFilter.MY_WATCHLIST -> "Add stocks to your watchlist from the chart screen"
                else -> "Pull down to refresh"
            },
            style = MaterialTheme.typography.bodySmall,
            color = TradingViewColors.TextTertiary,
            textAlign = TextAlign.Center
        )
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
