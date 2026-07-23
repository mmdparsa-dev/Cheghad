package com.mmdparsadev.cheghad.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UdfHistoryResponse(
    @SerialName("s") val Status: String,
    @SerialName("t") val Timestamps: List<Long>? = null,
    @SerialName("o") val Open: List<Double>? = null,
    @SerialName("h") val High: List<Double>? = null,
    @SerialName("l") val Low: List<Double>? = null,
    @SerialName("c") val Close: List<Double>? = null,
    @SerialName("v") val Volume: List<Double>? = null,
    @SerialName("errmsg") val ErrorMessage: String? = null
)
