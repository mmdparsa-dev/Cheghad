package com.mmdparsadev.cheghad.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mmdparsadev.cheghad.MainActivity
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.api.ApiClient
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.repository.CurrencyRepository
import com.mmdparsadev.cheghad.data.repository.NetworkResult

import com.mmdparsadev.cheghad.widget.updateAllWidgets

class CurrencySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = com.mmdparsadev.cheghad.data.database.AppDatabase.getDatabase(applicationContext)
            val repository = CurrencyRepository(
                ApiClient.CheghadApiService,
                ApiClient.KifpoolApiService,
                database.currencyDao()
            )

            when (val networkResult = repository.fetchLivePrices()) {
                is NetworkResult.Success -> {
                    val items = networkResult.data
                    // Check active price alarms in background
                    checkAndTriggerAlarms(applicationContext, database, items)
                    updateAllWidgets(applicationContext)
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

    private suspend fun checkAndTriggerAlarms(
        context: Context,
        database: com.mmdparsadev.cheghad.data.database.AppDatabase,
        items: List<CurrencyItem>
    ) {
        val alarmDao = database.alarmDao()
        val activeAlarms = alarmDao.getActiveAlarms()

        for (alarm in activeAlarms) {
            val currentItem = items.find { it.symbol == alarm.symbol } ?: continue
            val currentPrice = currentItem.currentPrice

            var isTriggered = false
            if ((alarm.isAbove && currentPrice >= alarm.targetPrice) || (!alarm.isAbove && currentPrice <= alarm.targetPrice)) {
                isTriggered = true
            }

            if (isTriggered) {
                // Deactivate alarm in database
                alarmDao.updateAlarm(alarm.copy(isActive = false))

                // Send notification
                sendPriceAlarmNotification(context, alarm, currentPrice)
            }
        }
    }

    private fun sendPriceAlarmNotification(
        context: Context,
        alarm: com.mmdparsadev.cheghad.data.models.AlarmEntity,
        currentPrice: Double
    ) {
        try {
            val channelId = "price_alerts_channel"
            val channelName = "Price Alerts"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "اطلاع‌رسانی رسیدن به هشدار قیمت"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val directionText = if (alarm.isAbove) "بالاتر از" else "پایین‌تر از"
            val message = "قیمت ${alarm.title} (${alarm.symbol}) به $directionText ${alarm.targetPrice.toLong()} تومان رسید (قیمت فعلی: ${currentPrice.toLong()} تومان)"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                alarm.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.alarm_triggered_title))
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(alarm.id.toInt(), notification)
        } catch (e: Exception) {
        }
    }
}

