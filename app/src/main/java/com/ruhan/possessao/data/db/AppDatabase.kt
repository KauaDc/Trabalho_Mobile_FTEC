package com.ruhan.possessao.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruhan.possessao.data.model.EntityRecord
import com.ruhan.possessao.data.repo.EntityDao

@Database(entities = [EntityRecord::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entityDao(): EntityDao
}
