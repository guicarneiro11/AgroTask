package com.guicarneirodev.agrotask.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.guicarneirodev.agrotask.data.local.converter.Converters
import com.guicarneirodev.agrotask.data.local.dao.ActivityRecordDao
import com.guicarneirodev.agrotask.data.local.dao.TaskDao
import com.guicarneirodev.agrotask.data.local.dao.WeatherCacheDao
import com.guicarneirodev.agrotask.data.local.entity.ActivityRecordEntity
import com.guicarneirodev.agrotask.data.local.entity.TaskEntity
import com.guicarneirodev.agrotask.data.local.entity.WeatherCacheEntity

@Database(
    entities = [
        TaskEntity::class,
        ActivityRecordEntity::class,
        WeatherCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun activityRecordDao(): ActivityRecordDao
    abstract fun weatherCacheDao(): WeatherCacheDao
}