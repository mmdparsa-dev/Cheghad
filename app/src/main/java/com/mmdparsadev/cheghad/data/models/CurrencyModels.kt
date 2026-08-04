package com.mmdparsadev.cheghad.data.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.ColumnInfo
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
    @ColumnInfo(name = "Id")
    val id: String,
    
    @SerialName("symbol")
    @ColumnInfo(name = "Symbol")
    val symbol: String,
    
    @SerialName("title")
    @ColumnInfo(name = "Title")
    val title: String,
    
    @SerialName("current_price")
    @ColumnInfo(name = "CurrentPrice")
    val currentPrice: Double,
    
    @SerialName("previous_price")
    @ColumnInfo(name = "PreviousPrice")
    val previousPrice: Double,
    
    @SerialName("change_amount")
    @ColumnInfo(name = "ChangeAmount")
    val changeAmount: Double,
    
    @SerialName("change_percentage")
    @ColumnInfo(name = "ChangePercentage")
    val changePercentage: Double,
    
    @SerialName("price_direction")
    @ColumnInfo(name = "PriceDirection")
    val priceDirection: PriceDirection,
    
    @SerialName("last_updated_timestamp")
    @ColumnInfo(name = "LastUpdatedTimestamp")
    val lastUpdatedTimestamp: Long,
    
    @SerialName("icon_url")
    @ColumnInfo(name = "IconUrl")
    val iconUrl: String,
    
    @SerialName("category")
    @ColumnInfo(name = "Category")
    val category: CurrencyType,

    @SerialName("hidden_until")
    @ColumnInfo(name = "HiddenUntil")
    val hiddenUntil: Long = 0
)

@Serializable
data class PriceHistoryPoint(
    @SerialName("timestamp")
    val timestamp: Long,
    
    @SerialName("price_value")
    val priceValue: Double
)

@Serializable
data class CurrencyResponse(
    @SerialName("items")
    val items: List<CurrencyItem>
)
