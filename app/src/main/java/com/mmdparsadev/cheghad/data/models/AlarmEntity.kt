package com.mmdparsadev.cheghad.data.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val title: String,
    val targetPrice: Double,
    val isAbove: Boolean, // true if trigger when price >= targetPrice, false if <= targetPrice
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
