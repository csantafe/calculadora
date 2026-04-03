package com.example.legalcalculator.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculos")
data class CalculoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tipoCalculo: String,
    val descripcion: String,
    val resultado: Double,
    val fecha: Long = System.currentTimeMillis()
)

