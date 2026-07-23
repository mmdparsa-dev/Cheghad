package com.mmdparsadev.cheghad.data.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class CurrencyType {
    @SerialName("currency")
    Currency,
    @SerialName("gold_and_coin")
    GoldAndCoin,
    @SerialName("crypto")
    Crypto
}

@Serializable
enum class PriceDirection {
    @SerialName("up")
    Up,
    @SerialName("down")
    Down,
    @SerialName("unchanged")
    Unchanged
}

@Entity(tableName = "currencies")
@Serializable
data class CurrencyItem(
    @PrimaryKey
    @SerialName("id")
    val Id: String,
    
    @SerialName("symbol")
    val Symbol: String,
    
    @SerialName("title")
    val Title: String,
    
    @SerialName("current_price")
    val CurrentPrice: Double,
    
    @SerialName("previous_price")
    val PreviousPrice: Double,
    
    @SerialName("change_amount")
    val ChangeAmount: Double,
    
    @SerialName("change_percentage")
    val ChangePercentage: Double,
    
    @SerialName("price_direction")
    val PriceDirection: PriceDirection,
    
    @SerialName("last_updated_timestamp")
    val LastUpdatedTimestamp: Long,
    
    @SerialName("icon_url")
    val IconUrl: String,
    
    @SerialName("category")
    val Category: CurrencyType,

    @SerialName("hidden_until")
    val HiddenUntil: Long = 0
)

@Serializable
data class PriceHistoryPoint(
    @SerialName("timestamp")
    val Timestamp: Long,
    
    @SerialName("price_value")
    val PriceValue: Double
)

@Serializable
data class CurrencyResponse(
    @SerialName("items")
    val Items: List<CurrencyItem>
)
