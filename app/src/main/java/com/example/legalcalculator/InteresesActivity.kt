// InteresesActivity.kt
// Actividad para la calculadora de intereses de mora y corrientes.

package com.example.legalcalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class InteresesActivity : AppCompatActivity() {

    private lateinit var etCapital: EditText
    private lateinit var etDiasMora: EditText
    private lateinit var etTasaCorrienteAnual: EditText
    private lateinit var etTasaMoraMensual: EditText
    private lateinit var btnCalcularIntereses: Button
    // ID corregido de btn_regresar_inicio_intereses
    private lateinit var btnRegresarInicioIntereses: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intereses)

        // Inicialización de vistas
        etCapital = findViewById(R.id.et_capital)
        etDiasMora = findViewById(R.id.et_dias_mora)
        etTasaCorrienteAnual = findViewById(R.id.et_tasa_interes_corriente_anual)
        etTasaMoraMensual = findViewById(R.id.et_tasa_mora_mensual)
        btnCalcularIntereses = findViewById(R.id.btn_calcular_intereses)
        // Uso del ID corregido: btn_regresar_inicio_intereses
        btnRegresarInicioIntereses = findViewById(R.id.btn_regresar_inicio_intereses)

        btnCalcularIntereses.setOnClickListener {
            realizarCalculos()
        }

        btnRegresarInicioIntereses.setOnClickListener {
            // Lógica para regresar a la actividad principal
            finish()
        }
    }

    private fun realizarCalculos() {
        val capitalStr = etCapital.text.toString()
        val diasMoraStr = etDiasMora.text.toString()
        val tasaCorrienteAnualStr = etTasaCorrienteAnual.text.toString()
        val tasaMoraMensualStr = etTasaMoraMensual.text.toString()

        if (capitalStr.isEmpty() || diasMoraStr.isEmpty() || tasaCorrienteAnualStr.isEmpty() || tasaMoraMensualStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val capital = capitalStr.toDoubleOrNull() ?: 0.0
        val diasMora = diasMoraStr.toDoubleOrNull() ?: 0.0
        // Convertir el porcentaje a decimal dividiendo por 100
        val tasaCorrienteAnual = (tasaCorrienteAnualStr.toDoubleOrNull() ?: 0.0) / 100.0
        val tasaMoraMensual = (tasaMoraMensualStr.toDoubleOrNull() ?: 0.0) / 100.0

        if (capital <= 0 || diasMora <= 0) {
            Toast.makeText(this, "Capital y Días en Mora deben ser mayores a cero.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fórmulas
        // 1. Interés Corriente Diario: (Capital * Tasa Corriente Anual) / 365
        // 2. Interés Corriente Total: Interés Diario * Días en Mora
        val interesCorrienteTotal = capital * tasaCorrienteAnual * diasMora / 365.0

        // 1. Interés Moratorio Diario: (Capital * Tasa Mora Mensual) / 30 (simplificando a 30 días/mes)
        // 2. Interés Moratorio Total: Interés Diario * Días en Mora
        val interesMoratorioTotal = capital * tasaMoraMensual * diasMora / 30.0

        val totalIntereses = interesCorrienteTotal + interesMoratorioTotal

        // Enviar resultados a la actividad de resultados
        val intent = Intent(this, ResultadosInteresesActivity::class.java).apply {
            putExtra("CAPITAL", capital)
            putExtra("DIAS_MORA", diasMora.toInt())
            putExtra("TASA_CORRIENTE_ANUAL", tasaCorrienteAnual * 100)
            putExtra("TASA_MORA_MENSUAL", tasaMoraMensual * 100)
            putExtra("RESULTADO_CORRIENTE", interesCorrienteTotal)
            putExtra("RESULTADO_MORATORIO", interesMoratorioTotal)
            putExtra("TOTAL_INTERESES", totalIntereses)
        }
        startActivity(intent)
    }
}