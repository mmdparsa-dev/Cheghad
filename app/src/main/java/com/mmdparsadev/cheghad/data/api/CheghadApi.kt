package com.mmdparsadev.cheghad.data.api

import com.mmdparsadev.cheghad.data.models.CurrencyResponse
import retrofit2.http.GET

interface CheghadApi {
    @GET("api/currencies")
    suspend fun GetLivePrices(): CurrencyResponse

    @GET("api/currencies/history")
    suspend fun GetHistory(
        @retrofit2.http.Query("symbol") symbol: String,
        @retrofit2.http.Query("range") range: String
    ): List<Double>
}
