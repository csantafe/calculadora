// LiquidacionActivity.kt
// Actividad para la calculadora de liquidación por terminación de contrato.

package com.example.legalcalculator

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class LiquidacionActivity : AppCompatActivity() {

    // Variables de UI y Datos
    private lateinit var etFechaInicio: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var tvDiasLaborados: TextView
    private lateinit var etSalario: EditText
    private lateinit var spinnerVacaciones: Spinner
    private lateinit var radioGroupTipoContrato: RadioGroup
    private lateinit var tvLabelFechaVencimientoFijo: TextView
    private lateinit var etFechaVencimientoFijo: EditText
    private lateinit var btnCalcular: Button
    private lateinit var btnRegresar: Button

    // Variables de fecha (Calendar)
    private var fechaInicioContrato: Calendar = Calendar.getInstance()
    private var fechaFinContrato: Calendar = Calendar.getInstance()
    private var fechaVencimientoFijo: Calendar = Calendar.getInstance()
    private var diasLaborados: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usamos un bloque try-catch en el inicio para atrapar cualquier error de referencia
        // o inicialización que cause el cierre inmediato (crash) de la app.
        try {
            setContentView(R.layout.activity_liquidacion)

            // 1. Inicializar Vistas
            etFechaInicio = findViewById(R.id.et_fecha_inicio)
            etFechaFin = findViewById(R.id.et_fecha_fin)
            tvDiasLaborados = findViewById(R.id.tv_dias_laborados)
            etSalario = findViewById(R.id.et_salario)
            spinnerVacaciones = findViewById(R.id.spinner_vacaciones)
            radioGroupTipoContrato = findViewById(R.id.radio_group_tipo_contrato)
            tvLabelFechaVencimientoFijo = findViewById(R.id.tv_label_fecha_vencimiento_fijo)
            etFechaVencimientoFijo = findViewById(R.id.et_fecha_vencimiento_fijo)
            btnCalcular = findViewById(R.id.btn_calcular_liquidacion)
            btnRegresar = findViewById(R.id.btn_regresar_inicio_liquidacion)

            // 2. Configurar Spinner de Vacaciones
            configurarSpinnerVacaciones()

            // 3. Configurar Selectores de Fecha
            etFechaInicio.setOnClickListener { mostrarSelectorFecha(etFechaInicio, true) }
            etFechaFin.setOnClickListener { mostrarSelectorFecha(etFechaFin, false) }
            etFechaVencimientoFijo.setOnClickListener { mostrarSelectorFecha(etFechaVencimientoFijo, false, true) }

            // 4. Configurar RadioGroup (Término Fijo/Indefinido)
            configurarRadioGroup()

            // 5. Configurar Botones
            btnCalcular.setOnClickListener { realizarCalculos() }
            btnRegresar.setOnClickListener { finish() }

        } catch (e: Exception) {
            // Muestra un Toast de error si ocurre un crash al iniciar la actividad.
            Toast.makeText(this, "Error al cargar la calculadora: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            // Esto ayuda a diagnosticar errores de 'Unresolved reference' que causan crasheos silenciosos.
        }
    }

    private fun configurarSpinnerVacaciones() {
        val periodos = arrayOf("1", "2", "3", "4")

        // CORRECCIÓN: Usamos R.layout.spinner_item_text (nuestro layout personalizado)
        // para la vista del elemento seleccionado (asegurando color negro).
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text, periodos)

        // Para la vista desplegable, usamos un layout de Android más estándar
        // que maneja mejor los fondos de las listas.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerVacaciones.adapter = adapter
    }

    private fun configurarRadioGroup() {
        radioGroupTipoContrato.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_termino_fijo -> {
                    tvLabelFechaVencimientoFijo.visibility = TextView.VISIBLE
                    etFechaVencimientoFijo.visibility = EditText.VISIBLE
                }
                R.id.rb_termino_indefinido -> {
                    tvLabelFechaVencimientoFijo.visibility = TextView.GONE
                    etFechaVencimientoFijo.visibility = EditText.GONE
                }
            }
        }
    }

    private fun mostrarSelectorFecha(editText: EditText, esFechaInicio: Boolean, esFechaVencimiento: Boolean = false) {
        val calendar = if (esFechaInicio) fechaInicioContrato else if (esFechaVencimiento) fechaVencimientoFijo else fechaFinContrato

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(sdf.format(calendar.time))
                calcularDiasLaborados()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun calcularDiasLaborados() {
        if (etFechaInicio.text.isNotEmpty() && etFechaFin.text.isNotEmpty()) {
            val diff = fechaFinContrato.timeInMillis - fechaInicioContrato.timeInMillis
            diasLaborados = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)

            // Validar que la fecha de inicio no sea posterior a la fecha de fin
            if (diasLaborados < 0) {
                Toast.makeText(this, "La fecha de retiro no puede ser anterior a la fecha de ingreso.", Toast.LENGTH_LONG).show()
                diasLaborados = 0
                etFechaFin.text.clear()
            }
        } else {
            diasLaborados = 0
        }
        tvDiasLaborados.text = getString(R.string.dias_laborados_format, diasLaborados.toInt())
    }


    private fun realizarCalculos() {
        // 1. Validaciones
        val salarioText = etSalario.text.toString()
        if (etFechaInicio.text.isEmpty() || etFechaFin.text.isEmpty() || salarioText.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos requeridos.", Toast.LENGTH_LONG).show()
            return
        }

        val salario = salarioText.toDoubleOrNull() ?: 0.0
        if (salario <= 0.0) {
            Toast.makeText(this, "El salario debe ser un valor numérico positivo.", Toast.LENGTH_LONG).show()
            return
        }

        // El cálculo de días laborados ya se hace en calcularDiasLaborados()

        // Variables de entrada
        val periodosVacacionesNoTomadas = spinnerVacaciones.selectedItem.toString().toInt()
        val esTerminoFijo = radioGroupTipoContrato.checkedRadioButtonId == R.id.rb_termino_fijo

        // Validar Fecha de Vencimiento para Término Fijo
        if (esTerminoFijo && etFechaVencimientoFijo.text.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar la fecha de vencimiento para Contrato Fijo.", Toast.LENGTH_LONG).show()
            return
        }


        // 2. Cálculos Principales
        val (indemnizacion, diasIndemnizacion) = calcularIndemnizacion(esTerminoFijo, salario)
        val vacaciones = calcularVacaciones(esTerminoFijo, salario, periodosVacacionesNoTomadas)
        val cesantias = calcularCesantias(salario, diasLaborados)
        val totalLiquidacion = indemnizacion + vacaciones + cesantias

        // 3. Enviar a Resultados
        val intent = Intent(this, ResultadosLiquidacionActivity::class.java).apply {
            // Datos de Entrada
            putExtra("FECHA_INICIO", etFechaInicio.text.toString())
            putExtra("FECHA_FIN", etFechaFin.text.toString())
            putExtra("DIAS_LABORADOS", diasLaborados)
            putExtra("SALARIO", salario)
            putExtra("PERIODOS_VACACIONES", periodosVacacionesNoTomadas)
            putExtra("TIPO_CONTRATO", if (esTerminoFijo) "Término Fijo" else "Término Indefinido")

            // Resultados
            putExtra("INDEMNIZACION", indemnizacion)
            putExtra("VACACIONES", vacaciones)
            putExtra("CESANTIAS", cesantias)
            putExtra("TOTAL", totalLiquidacion)
            putExtra("DIAS_INDEMNIZACION", diasIndemnizacion) // Para mostrar en resultados
        }
        startActivity(intent)
    }

    private fun calcularIndemnizacion(esTerminoFijo: Boolean, salario: Double): Pair<Double, Int> {
        return if (esTerminoFijo) {
            // Indemnización Término Fijo: Salario / 365 * Días faltantes para el vencimiento.
            val diff = fechaVencimientoFijo.timeInMillis - fechaFinContrato.timeInMillis
            var diasFaltantesParaVencimiento = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()

            // La indemnización solo se paga si la terminación es ANTES del vencimiento
            if (diasFaltantesParaVencimiento < 0) {
                diasFaltantesParaVencimiento = 0 // No hay indemnización si se termina después del vencimiento
            }

            val indemnizacion = (salario / 30.0) * diasFaltantesParaVencimiento // Usar 30 días para salario diario

            Pair(indemnizacion, diasFaltantesParaVencimiento)

        } else {
            // Indemnización Término Indefinido
            val anosLaborados = diasLaborados / 365.25 // Usar 365.25 para incluir bisiestos
            val salarioDiario = salario / 30.0

            val indemnizacion: Double
            val diasIndemnizacion: Int

            if (anosLaborados <= 1.0) {
                // Menos de 1 año: 30 días de salario
                diasIndemnizacion = 30
                indemnizacion = salarioDiario * 30.0
            } else {
                // Más de 1 año
                val anosAdicionales = anosLaborados - 1

                // Primer año: 30 días
                var diasTotal = 30

                // Años adicionales: 20 días por año
                // Convertimos la fracción de año a días de indemnización proporcional
                val diasAdicionales = (anosAdicionales * 20.0).toInt()
                diasTotal += diasAdicionales

                indemnizacion = salarioDiario * diasTotal
                diasIndemnizacion = diasTotal
            }
            Pair(indemnizacion, diasIndemnizacion)
        }
    }

    private fun calcularVacaciones(esTerminoFijo: Boolean, salario: Double, periodosPendientes: Int): Double {
        // Vacaciones: 15 días de salario por cada año trabajado
        // Si hay periodos pendientes: Salario / 30 * 15 * Periodos
        val salarioDiario = salario / 30.0
        return salarioDiario * 15.0 * periodosPendientes
    }

    private fun calcularCesantias(salario: Double, dias: Long): Double {
        // Cesantías: Salario * Días Laborados / 360
        // (Se asume que la fórmula es para la liquidación total de la fracción de año)
        return (salario * dias.toDouble()) / 360.0
    }
}