package com.mmdparsadev.cheghad.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mmdparsadev.cheghad.data.api.ApiClient
import com.mmdparsadev.cheghad.data.repository.CurrencyRepository
import com.mmdparsadev.cheghad.data.repository.NetworkResult

class CurrencySyncWorker(
    AppContext: Context,
    WorkerParams: WorkerParameters
) : CoroutineWorker(AppContext, WorkerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = com.mmdparsadev.cheghad.data.database.AppDatabase.getDatabase(applicationContext)
            val Repository = CurrencyRepository(ApiClient.CheghadApiService, ApiClient.KifpoolApiService, database.currencyDao())
            when (val NetworkResult = Repository.FetchLivePrices()) {
                is NetworkResult.Success -> {
                    Repository.saveCurrenciesToCache(NetworkResult.Data)
                    Result.success()
                }
                is NetworkResult.Error -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
