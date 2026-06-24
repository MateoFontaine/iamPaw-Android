package com.example.iampaw.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PetReportLocal::class],
    version = 3,
    exportSchema = false
)
abstract class PawDatabase : RoomDatabase() {

    abstract fun pawDao(): IPawDao

    companion object {
        @Volatile
        private var instance: PawDatabase? = null

        fun getInstance(context: Context): PawDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context): PawDatabase =
            Room.databaseBuilder(context, PawDatabase::class.java, "iampaw_db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
