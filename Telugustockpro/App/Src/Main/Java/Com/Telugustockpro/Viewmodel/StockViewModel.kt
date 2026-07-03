package com.telugustockpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telugustockpro.data.model.*
import com.telugustockpro.data.repository.SampleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockUiState(
    val marketIndices: List<MarketIndex> = emptyList(),
    val topGainers: List<Stock> = emptyList(),
    val topLosers: List<Stock> = emptyList(),
    val allStocks: List<Stock> = emptyList(),
    val watchlist: List<Stock> = emptyList(),
    val selectedStock: Stock? = null,
    val searchQuery: String = "",
    val searchResults: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedTimePeriod: ChartTimePeriod = ChartTimePeriod.ONE_DAY,
    val candleData: List<CandleData> = emptyList(),
    val activeIndicators: Set<String> = setOf("MA_9", "MA_21", "Volume"),
    val marketAnalysis: MarketAnalysis? = null,
    val optionChain: List<OptionChainData> = emptyList()
)

@HiltViewModel
class StockViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load market data
            _uiState.value = _uiState.value.copy(
                marketIndices = SampleData.marketIndices,
                topGainers = SampleData.topGainers,
                topLosers = SampleData.topLosers,
                allStocks = SampleData.allStocks,
                watchlist = SampleData.topGainers.take(5),
                isLoading = false
            )
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            // Simulate API call delay
            kotlinx.coroutines.delay(1500)

            // In real app, this would fetch from API
            _uiState.value = _uiState.value.copy(
                marketIndices = SampleData.marketIndices,
                topGainers = SampleData.topGainers,
                topLosers = SampleData.topLosers,
                allStocks = SampleData.allStocks,
                isRefreshing = false
            )
        }
    }

    fun searchStocks(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        val results = _uiState.value.allStocks.filter { stock ->
            stock.symbol.contains(query, ignoreCase = true) ||
                    stock.name.contains(query, ignoreCase = true)
        }
        _uiState.value = _uiState.value.copy(searchResults = results)
    }

    fun selectStock(symbol: String) {
        viewModelScope.launch {
            val stock = _uiState.value.allStocks.find { it.symbol == symbol }
                ?: _uiState.value.topGainers.find { it.symbol == symbol }
                ?: _uiState.value.topLosers.find { it.symbol == symbol }

            if (stock != null) {
                _uiState.value = _uiState.value.copy(selectedStock = stock)
                loadCandleData(symbol)
                loadOptionChain(stock.price)
            }
        }
    }

    fun loadCandleData(symbol: String, period: ChartTimePeriod = ChartTimePeriod.ONE_DAY) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedTimePeriod = period
            )

            val stock = _uiState.value.selectedStock
            val basePrice = stock?.price ?: 22456.80

            val candleData = when (period) {
                ChartTimePeriod.ONE_DAY -> SampleData.generateIntradayData(basePrice)
                ChartTimePeriod.ONE_WEEK -> SampleData.generateCandleData(basePrice, 7)
                ChartTimePeriod.ONE_MONTH -> SampleData.generateCandleData(basePrice, 30)
                ChartTimePeriod.THREE_MONTHS -> SampleData.generateCandleData(basePrice, 90)
                ChartTimePeriod.SIX_MONTHS -> SampleData.generateCandleData(basePrice, 180)
                ChartTimePeriod.ONE_YEAR -> SampleData.generateCandleData(basePrice, 365)
                ChartTimePeriod.ALL -> SampleData.generateCandleData(basePrice, 730)
            }

            _uiState.value = _uiState.value.copy(
                candleData = candleData,
                isLoading = false
            )
        }
    }

    fun loadOptionChain(currentPrice: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                optionChain = SampleData.generateOptionChainData(currentPrice)
            )
        }
    }

    fun toggleIndicator(indicator: String) {
        val current = _uiState.value.activeIndicators.toMutableSet()
        if (current.contains(indicator)) {
            current.remove(indicator)
        } else {
            current.add(indicator)
        }
        _uiState.value = _uiState.value.copy(activeIndicators = current)
    }

    fun loadAnalysis(symbol: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val stock = _uiState.value.allStocks.find { it.symbol == symbol }
            if (stock != null) {
                val analysis = SampleData.generateAnalysis(stock)
                _uiState.value = _uiState.value.copy(
                    marketAnalysis = analysis,
                    isLoading = false
                )
            }
        }
    }

    fun addToWatchlist(stock: Stock) {
        val current = _uiState.value.watchlist.toMutableList()
        if (current.none { it.symbol == stock.symbol }) {
            current.add(stock)
            _uiState.value = _uiState.value.copy(watchlist = current)
        }
    }

    fun removeFromWatchlist(symbol: String) {
        val current = _uiState.value.watchlist.toMutableList()
        current.removeAll { it.symbol == symbol }
        _uiState.value = _uiState.value.copy(watchlist = current)
    }
}
