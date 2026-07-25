package com.mmdparsadev.cheghad.data.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mmdparsadev.cheghad.BuildConfig
import com.mmdparsadev.cheghad.MainActivity
import java.util.concurrent.TimeUnit

class UpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentVersion = BuildConfig.VERSION_NAME
            val result = UpdateManager.checkForUpdate(currentVersion)
            val release = result.getOrNull()

            if (release != null) {
                val prefs = appContext.getSharedPreferences("cheghad_update_prefs", Context.MODE_PRIVATE)
                val lastNotifiedTag = prefs.getString("last_notified_tag", "")

                // Notify if this version tag hasn't been notified yet
                if (lastNotifiedTag != release.tagName) {
                    sendUpdateNotification(appContext, release)
                    prefs.edit().putString("last_notified_tag", release.tagName).apply()
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val CHANNEL_ID = "app_updates_channel"
        const val NOTIFICATION_ID = 2026

        fun schedulePeriodicCheck(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateWorker>(
                    6, TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "CheghadPeriodicUpdateCheck",
                    ExistingPeriodicWorkPolicy.KEEP,
                    updateWorkRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun sendUpdateNotification(context: Context, release: GitHubRelease) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "بروزرسانی‌های برنامه چقد",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "اطلاع‌رسانی انتشار نسخه‌های جدید برنامه"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("SHOW_UPDATE_DIALOG", true)
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("نسخه جدید چقد منتشر شد (${release.tagName})")
                    .setContentText("برای بروزرسانی و دریافت تغییرات جدید کلیک کنید.")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("نسخه جدید ${release.tagName} آماده دریافت است.\n\nتغییرات:\n${release.body ?: "بهبود عملکرد و رفع مشکلات"}")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
