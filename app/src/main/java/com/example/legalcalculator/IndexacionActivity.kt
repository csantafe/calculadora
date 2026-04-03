// IndexacionActivity.kt
// Actividad para la calculadora de indexación (traer a valor presente) usando IPC.

package com.example.legalcalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.legalcalculator.data.database.AppDatabase
import com.example.legalcalculator.data.entity.CalculoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class IndexacionActivity : AppCompatActivity() {

    // Constante con los valores del IPC (Índice Acumulado) desde 2003
    // Nota: Estos son los valores acumulados del índice, no los porcentajes de variación.
    private val ipcHistorico = mapOf(
        2003 to 64.95, 2004 to 68.34, 2005 to 72.07, 2006 to 75.83,
        2007 to 79.82, 2008 to 86.26, 2009 to 88.75, 2010 to 91.56,
        2011 to 94.62, 2012 to 97.59, 2013 to 100.56, 2014 to 104.14,
        2015 to 110.16, 2016 to 118.06, 2017 to 122.25, 2018 to 125.75,
        2019 to 130.54, 2020 to 132.64, 2021 to 140.09, 2022 to 158.46,
        2023 to 173.19, 2024 to 179.35, 2025 to 183.56 // Valor proyectado o parcial
    )

    // Variables de UI
    private lateinit var etValorHistorico: EditText
    private lateinit var etAnioInicial: EditText
    private lateinit var etAnioFinal: EditText
    private lateinit var tvResultadoIndexacion: TextView
    private lateinit var btnCalcular: Button
    private lateinit var btnRegresar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_indexacion)

            // 1. Inicializar Vistas (Binding)
            etValorHistorico = findViewById(R.id.et_valor_historico)
            etAnioInicial = findViewById(R.id.et_anio_inicial)
            etAnioFinal = findViewById(R.id.et_anio_final)
            tvResultadoIndexacion = findViewById(R.id.tv_resultado_indexacion)
            btnCalcular = findViewById(R.id.btn_calcular_indexacion)
            btnRegresar = findViewById(R.id.btn_regresar_inicio_indexacion)

            // 2. Configurar Botones
            btnCalcular.setOnClickListener { realizarCalculoIndexacion() }
            btnRegresar.setOnClickListener { finish() }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar la calculadora de indexación: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun realizarCalculoIndexacion() {
        // 1. Validar y Obtener Entradas
        val valorHistorico = etValorHistorico.text.toString().toDoubleOrNull()
        val anioInicial = etAnioInicial.text.toString().toIntOrNull()
        val anioFinal = etAnioFinal.text.toString().toIntOrNull()

        if (valorHistorico == null || anioInicial == null || anioFinal == null) {
            Toast.makeText(this, "Por favor, complete todos los campos numéricos.", Toast.LENGTH_LONG).show()
            return
        }
        if (valorHistorico <= 0) {
            Toast.makeText(this, "El valor a indexar debe ser positivo.", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Obtener Valores IPC
        val ipcInicial = ipcHistorico[anioInicial]
        val ipcFinal = ipcHistorico[anioFinal]

        if (ipcInicial == null || ipcFinal == null) {
            Toast.makeText(this, "Datos IPC no disponibles para los años seleccionados. Rango: 2003 - 2025.", Toast.LENGTH_LONG).show()
            return
        }

        // 3. Aplicar la Fórmula y Calcular
        // Fórmula: Valor Presente = Valor Histórico * (IPC Final / IPC Inicial)
        val valorPresente = valorHistorico * (ipcFinal / ipcInicial)

        // 4. Formatear y Mostrar Resultado
        val df = DecimalFormat("#,###.00")
        val resultadoFormateado = df.format(valorPresente)

        tvResultadoIndexacion.text = "Valor Presente Indexado: $$resultadoFormateado"
        guardarEnHistorial(valorHistorico!!, anioInicial!!, anioFinal!!, valorPresente)
    }
    private fun guardarEnHistorial(
        valorOriginal: Double,
        anioInicial: Int,
        anioFinal: Int,
        valorPresente: Double
    ) {
        val db = AppDatabase.getDatabase(this)
        val df = DecimalFormat("#,##0.00")

        val calculo = CalculoEntity(
            tipoCalculo = "📊 Indexación",
            descripcion = "Valor: $${df.format(valorOriginal)} | $anioInicial → $anioFinal",
            resultado = valorPresente
        )

        lifecycleScope.launch(Dispatchers.IO) {
            db.calculoDao().insertCalculo(calculo)
        }
    }
}