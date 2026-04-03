// LiquidacionDespidoActivity.kt
// Calculadora de Liquidación por Despido Sin Justa Causa

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class LiquidacionDespidoActivity : AppCompatActivity() {

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

    private var fechaInicioContrato: Calendar = Calendar.getInstance()
    private var fechaFinContrato: Calendar = Calendar.getInstance()
    private var fechaVencimientoFijo: Calendar = Calendar.getInstance()
    private var diasLaborados: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_liquidacion_despido)

            etFechaInicio = findViewById(R.id.et_fecha_inicio_despido)
            etFechaFin = findViewById(R.id.et_fecha_fin_despido)
            tvDiasLaborados = findViewById(R.id.tv_dias_laborados_despido)
            etSalario = findViewById(R.id.et_salario_despido)
            spinnerVacaciones = findViewById(R.id.spinner_vacaciones_despido)
            radioGroupTipoContrato = findViewById(R.id.radio_group_tipo_contrato_despido)
            tvLabelFechaVencimientoFijo = findViewById(R.id.tv_label_fecha_vencimiento_fijo_despido)
            etFechaVencimientoFijo = findViewById(R.id.et_fecha_vencimiento_fijo_despido)
            btnCalcular = findViewById(R.id.btn_calcular_liquidacion_despido)
            btnRegresar = findViewById(R.id.btn_regresar_inicio_liquidacion_despido)

            configurarSpinnerVacaciones()

            etFechaInicio.setOnClickListener { mostrarSelectorFecha(etFechaInicio, true) }
            etFechaFin.setOnClickListener { mostrarSelectorFecha(etFechaFin, false) }
            etFechaVencimientoFijo.setOnClickListener { mostrarSelectorFecha(etFechaVencimientoFijo, false, true) }

            configurarRadioGroup()

            btnCalcular.setOnClickListener { realizarCalculos() }
            btnRegresar.setOnClickListener { finish() }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar la calculadora: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun configurarSpinnerVacaciones() {
        val periodos = arrayOf("1", "2", "3", "4")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text, periodos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerVacaciones.adapter = adapter
    }

    private fun configurarRadioGroup() {
        radioGroupTipoContrato.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_termino_fijo_despido -> {
                    tvLabelFechaVencimientoFijo.visibility = TextView.VISIBLE
                    etFechaVencimientoFijo.visibility = EditText.VISIBLE
                }
                R.id.rb_termino_indefinido_despido -> {
                    tvLabelFechaVencimientoFijo.visibility = TextView.GONE
                    etFechaVencimientoFijo.visibility = EditText.GONE
                }
            }
        }
    }

    private fun mostrarSelectorFecha(editText: EditText, esFechaInicio: Boolean, esFechaVencimiento: Boolean = false) {
        val calendar = if (esFechaInicio) fechaInicioContrato
        else if (esFechaVencimiento) fechaVencimientoFijo
        else fechaFinContrato

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

            if (diasLaborados < 0) {
                Toast.makeText(this, "La fecha de retiro no puede ser anterior a la de ingreso.", Toast.LENGTH_LONG).show()
                diasLaborados = 0
                etFechaFin.text.clear()
            }
        } else {
            diasLaborados = 0
        }
        tvDiasLaborados.text = getString(R.string.dias_laborados_format, diasLaborados.toInt())
    }

    private fun realizarCalculos() {
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

        val periodosVacacionesNoTomadas = spinnerVacaciones.selectedItem.toString().toInt()
        val esTerminoFijo = radioGroupTipoContrato.checkedRadioButtonId == R.id.rb_termino_fijo_despido

        if (esTerminoFijo && etFechaVencimientoFijo.text.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar la fecha de vencimiento para Contrato Fijo.", Toast.LENGTH_LONG).show()
            return
        }

        // ── Cálculos ──────────────────────────────────────────────
        val (indemnizacion, diasIndemnizacion) = calcularIndemnizacionSinJustaCausa(esTerminoFijo, salario)
        val vacaciones   = calcularVacaciones(salario, periodosVacacionesNoTomadas)
        val cesantias    = calcularCesantias(salario, diasLaborados)
        val totalLiquidacion = indemnizacion + vacaciones + cesantias

        val intent = Intent(this, ResultadosLiquidacionActivity::class.java).apply {
            putExtra("FECHA_INICIO",       etFechaInicio.text.toString())
            putExtra("FECHA_FIN",          etFechaFin.text.toString())
            putExtra("DIAS_LABORADOS",     diasLaborados)
            putExtra("SALARIO",            salario)
            putExtra("PERIODOS_VACACIONES",periodosVacacionesNoTomadas)
            putExtra("TIPO_CONTRATO",      if (esTerminoFijo) "Término Fijo" else "Término Indefinido")
            putExtra("INDEMNIZACION",      indemnizacion)
            putExtra("VACACIONES",         vacaciones)
            putExtra("CESANTIAS",          cesantias)
            putExtra("TOTAL",              totalLiquidacion)
            putExtra("DIAS_INDEMNIZACION", diasIndemnizacion)
            // Indica al Activity de resultados que es despido sin justa causa
            putExtra("TIPO_CALCULO",       "DESPIDO_SIN_JUSTA_CAUSA")
        }
        startActivity(intent)
    }

    /**
     * Indemnización por DESPIDO SIN JUSTA CAUSA (Art. 64 CST):
     *   - Término Indefinido:
     *       • Primer año (o fracción): 30 días de salario
     *       • Por cada año adicional:  20 días de salario (proporcional)
     *   - Término Fijo:
     *       • Días de salario equivalentes al tiempo que falta para el vencimiento
     */
    private fun calcularIndemnizacionSinJustaCausa(esTerminoFijo: Boolean, salario: Double): Pair<Double, Int> {
        val salarioDiario = salario / 30.0

        return if (esTerminoFijo) {
            val diff = fechaVencimientoFijo.timeInMillis - fechaFinContrato.timeInMillis
            var diasFaltantes = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            if (diasFaltantes < 0) diasFaltantes = 0

            Pair(salarioDiario * diasFaltantes, diasFaltantes)

        } else {
            val anosLaborados = diasLaborados / 365.25

            val diasIndemnizacion: Int
            val indemnizacion: Double

            if (anosLaborados <= 1.0) {
                // Menos de 1 año completo → 30 días proporcionales
                diasIndemnizacion = (30.0 * anosLaborados).toInt().coerceAtLeast(30)
                indemnizacion = salarioDiario * diasIndemnizacion
            } else {
                // Primer año: 30 días fijos
                // Años adicionales: 20 días por año (proporcional)
                val anosAdicionales = anosLaborados - 1.0
                val diasAdicionales = (anosAdicionales * 20.0).toInt()
                diasIndemnizacion = 30 + diasAdicionales
                indemnizacion = salarioDiario * diasIndemnizacion
            }
            Pair(indemnizacion, diasIndemnizacion)
        }
    }

    private fun calcularVacaciones(salario: Double, periodosPendientes: Int): Double {
        return (salario / 30.0) * 15.0 * periodosPendientes
    }

    private fun calcularCesantias(salario: Double, dias: Long): Double {
        return (salario * dias.toDouble()) / 360.0
    }
}