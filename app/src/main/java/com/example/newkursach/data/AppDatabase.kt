package com.example.newkursach.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AudioRecord::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao
    companion object {
        private var audioDB: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var databaseInstance = audioDB
                if (databaseInstance == null) {
                    databaseInstance = Room.databaseBuilder(
                        context, AppDatabase::class.java, "audioDB"
                    ).fallbackToDestructiveMigration().build()
                    audioDB = databaseInstance
                }
                return databaseInstance
            }
        }
    }

}