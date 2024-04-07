package com.zaz.demo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FingerData::class],
    version = 1,
    exportSchema = false
)
abstract class FingerDataBase : RoomDatabase() {
    abstract fun fingerDataDao(): FingerDataDao

    companion object {
        @Volatile
        var INSTANCE: FingerDataBase? = null
        fun create(context: Context): FingerDataBase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                FingerDataBase::class.java,
                "data8800.db"
            ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }
}