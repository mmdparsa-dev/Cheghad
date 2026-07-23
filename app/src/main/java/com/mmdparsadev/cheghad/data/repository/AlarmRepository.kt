package com.mmdparsadev.cheghad.data.repository

import com.mmdparsadev.cheghad.data.database.AlarmDao
import com.mmdparsadev.cheghad.data.models.AlarmEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarmsFlow: Flow<List<AlarmEntity>> = alarmDao.getAllAlarmsFlow()

    suspend fun getActiveAlarms(): List<AlarmEntity> = alarmDao.getActiveAlarms()

    suspend fun insertAlarm(alarm: AlarmEntity) = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)

    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)
}
