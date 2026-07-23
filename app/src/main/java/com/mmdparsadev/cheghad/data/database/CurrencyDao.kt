package com.mmdparsadev.cheghad.data.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies")
    fun getAllCurrenciesFlow(): Flow<List<CurrencyItem>>

    @Query("SELECT * FROM currencies WHERE HiddenUntil < :now")
    fun getVisibleCurrenciesFlow(now: Long): Flow<List<CurrencyItem>>

    @Query("SELECT * FROM currencies")
    suspend fun getAllCurrencies(): List<CurrencyItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currencies: List<CurrencyItem>)

    @Query("UPDATE currencies SET HiddenUntil = :until WHERE Id = :id")
    suspend fun updateHiddenUntil(id: String, until: Long)

    @Query("DELETE FROM currencies")
    suspend fun deleteAll()
}
