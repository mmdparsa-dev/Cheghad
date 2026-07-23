package com.mmdparsadev.cheghad.data.database

import androidx.room3.ColumnTypeConverter
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.PriceDirection

object AppTypeConverters {
    @ColumnTypeConverter
    @JvmStatic
    fun fromCurrencyType(value: CurrencyType): String = value.name

    @ColumnTypeConverter
    @JvmStatic
    fun toCurrencyType(value: String): CurrencyType = CurrencyType.valueOf(value)

    @ColumnTypeConverter
    @JvmStatic
    fun fromPriceDirection(value: PriceDirection): String = value.name

    @ColumnTypeConverter
    @JvmStatic
    fun toPriceDirection(value: String): PriceDirection = PriceDirection.valueOf(value)
}
