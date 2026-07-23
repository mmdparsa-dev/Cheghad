package com.mmdparsadev.cheghad.data.api

import com.mmdparsadev.cheghad.data.models.UdfHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KifpoolApi {
    @GET("api/udf/history")
    suspend fun GetHistory(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long
    ): UdfHistoryResponse
}
