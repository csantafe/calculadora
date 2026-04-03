package com.example.legalcalculator.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.legalcalculator.data.entity.CalculoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculoDao {

    @Insert
    suspend fun insertCalculo(calculo: CalculoEntity)

    @Query("SELECT * FROM calculos ORDER BY fecha DESC")
    fun getAllCalculos(): Flow<List<CalculoEntity>>

    @Query("DELETE FROM calculos")
    suspend fun deleteAll()
}
