package com.mmdparsadev.cheghad.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UdfHistoryResponse(
    @SerialName("s") val status: String,
    @SerialName("t") val timestamps: List<Long>? = null,
    @SerialName("o") val open: List<Double>? = null,
    @SerialName("h") val high: List<Double>? = null,
    @SerialName("l") val low: List<Double>? = null,
    @SerialName("c") val close: List<Double>? = null,
    @SerialName("v") val volume: List<Double>? = null,
    @SerialName("errmsg") val errorMessage: String? = null
)
