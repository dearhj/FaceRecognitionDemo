package com.test.facerecognitionbyusbcamera.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FaceData::class],
    version = 1,
    exportSchema = false
)
abstract class FaceDataBase : RoomDatabase() {
    abstract fun faceDataDao(): FaceDataDao

    companion object {
        @Volatile
        var INSTANCE: FaceDataBase? = null
        fun create(context: Context): FaceDataBase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                FaceDataBase::class.java,
                "dataFace.db"
            ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }
}