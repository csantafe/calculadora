package com.example.legalcalculator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.legalcalculator.data.dao.CalculoDao
import com.example.legalcalculator.data.entity.CalculoEntity

@Database(entities = [CalculoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calculoDao(): CalculoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calculadora_juridica_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

