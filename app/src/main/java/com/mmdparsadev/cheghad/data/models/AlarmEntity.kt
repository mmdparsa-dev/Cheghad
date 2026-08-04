package com.mmdparsadev.cheghad.data.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.ColumnInfo

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) 
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "symbol")
    val symbol: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "targetPrice")
    val targetPrice: Double,
    
    @ColumnInfo(name = "isAbove")
    val isAbove: Boolean, // true if trigger when price >= targetPrice, false if <= targetPrice
    
    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
