package com.telugustockpro.data.remote

import com.telugustockpro.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

// Market data response
data class MarketDataResponse(
    val indices: List<MarketIndex>,
    val timestamp: Long
)

// Stock list response
data class StockListResponse(
    val stocks: List<Stock>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

// Historical data response
data class HistoricalDataResponse(
    val symbol: String,
    val candles: List<CandleData>,
    val interval: String
)

// Option chain response
data class OptionChainResponse(
    val symbol: String,
    val expiry: String,
    val data: List<OptionChainData>
)

interface StockApiService {

    @GET("market/indices")
    suspend fun getMarketIndices(): Response<ApiResponse<MarketDataResponse>>

    @GET("market/stocks")
    suspend fun getAllStocks(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null
    ): Response<ApiResponse<StockListResponse>>

    @GET("market/gainers")
    suspend fun getTopGainers(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<Stock>>>

    @GET("market/losers")
    suspend fun getTopLosers(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<Stock>>>

    @GET("stock/{symbol}")
    suspend fun getStockDetails(
        @Path("symbol") symbol: String
    ): Response<ApiResponse<Stock>>

    @GET("stock/{symbol}/historical")
    suspend fun getHistoricalData(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "1d",
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null
    ): Response<ApiResponse<HistoricalDataResponse>>

    @GET("stock/{symbol}/option-chain")
    suspend fun getOptionChain(
        @Path("symbol") symbol: String,
        @Query("expiry") expiry: String? = null
    ): Response<ApiResponse<OptionChainResponse>>

    @GET("market/search")
    suspend fun searchStocks(
        @Query("q") query: String
    ): Response<ApiResponse<List<Stock>>>

    @GET("market/most-active")
    suspend fun getMostActiveStocks(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<Stock>>>

    companion object {
        const val BASE_URL = "https://api.telugustockpro.com/v1/"

        // Example base URLs for different data providers:
        // const val BASE_URL = "https://www.google.com/finance/api/v1/"  // Google Finance
        // const val BASE_URL = "https://query1.finance.yahoo.com/v8/"    // Yahoo Finance
        // const val BASE_URL = "https://api.stockdata.org/v1/"           // StockData.org
        // const val BASE_URL = "https://financialmodelingprep.com/api/v3/" // FMP
    }
}
