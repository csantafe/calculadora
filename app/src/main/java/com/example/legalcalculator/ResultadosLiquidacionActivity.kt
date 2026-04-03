// ResultadosLiquidacionActivity.kt
// Esta actividad recibe los resultados de la liquidación, los muestra
// y permite generar un PDF con la información.

package com.example.legalcalculator

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.legalcalculator.data.database.AppDatabase
import com.example.legalcalculator.data.entity.CalculoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ResultadosLiquidacionActivity : AppCompatActivity() {

    // Referencias a los TextViews para mostrar los resultados
    private lateinit var tvInputFechaInicio: TextView
    private lateinit var tvInputFechaFin: TextView
    private lateinit var tvInputDiasLaborados: TextView
    private lateinit var tvInputSalario: TextView
    private lateinit var tvInputVacacionesPeriodos: TextView
    private lateinit var tvInputTipoContrato: TextView
    private lateinit var tvInputFechaVencimientoFijo: TextView

    private lateinit var tvResultadoIndemnizacion: TextView
    private lateinit var tvResultadoVacaciones: TextView
    private lateinit var tvResultadoCesantias: TextView
    private lateinit var tvTotalLiquidacion: TextView
    private lateinit var btnDescargarPdf: Button
    private lateinit var tvExplicacionDespido: TextView // Nueva referencia para el texto explicativo

    // Variables para almacenar los datos recibidos
    private var fechaInicioContrato: String = ""
    private var fechaFinContrato: String = ""
    private var diasLaborados: Double = 0.0
    private var salario: Double = 0.0
    private var periodosVacaciones: Int = 0
    private var tipoContrato: String = ""
    private var fechaVencimientoFijo: String = ""

    private var indemnizacion: Double = 0.0
    private var vacaciones: Double = 0.0
    private var cesantias: Double = 0.0
    private var totalLiquidacion: Double = 0.0

    // Texto explicativo sobre el despido sin justa causa
    private val explicacionDespidoTexto = """
        El despido sin justa causa:
        Artículo 64 del CST:
        Establece que el empleador debe pagar una indemnización al trabajador cuando la terminación del contrato de trabajo sea unilateral y sin justa causa. 
        Ley 50 de 1990:
        Define las condiciones de pago de la indemnización, que generalmente se calcula con base en los años de servicio y el salario del trabajador. 
        Ley 789 de 2002:
        Modifica ciertos aspectos de la indemnización por despido sin justa causa, especialmente en lo relacionado con trabajadores que tienen más de 10 años de servicio.
    """.trimIndent() // trimIndent() para limpiar espacios en blanco al inicio de cada línea

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_liquidacion)

        // Inicializar vistas
        tvInputFechaInicio = findViewById(R.id.tv_input_fecha_inicio)
        tvInputFechaFin = findViewById(R.id.tv_input_fecha_fin)
        tvInputDiasLaborados = findViewById(R.id.tv_input_dias_laborados)
        tvInputSalario = findViewById(R.id.tv_input_salario)
        tvInputVacacionesPeriodos = findViewById(R.id.tv_input_vacaciones_periodos)
        tvInputTipoContrato = findViewById(R.id.tv_input_tipo_contrato)
        tvInputFechaVencimientoFijo = findViewById(R.id.tv_input_fecha_vencimiento_fijo)

        tvResultadoIndemnizacion = findViewById(R.id.tv_resultado_indemnizacion)
        tvResultadoVacaciones = findViewById(R.id.tv_resultado_vacaciones)
        tvResultadoCesantias = findViewById(R.id.tv_resultado_cesantias)
        tvTotalLiquidacion = findViewById(R.id.tv_total_liquidacion)
        btnDescargarPdf = findViewById(R.id.btn_descargar_pdf)
        tvExplicacionDespido = findViewById(R.id.tv_explicacion_despido) // Inicializar la nueva referencia

        // Obtener datos del Intent
        intent.extras?.let {
            fechaInicioContrato = it.getString("fechaInicioContrato", "")
            fechaFinContrato = it.getString("fechaFinContrato", "")
            diasLaborados = it.getDouble("diasLaborados", 0.0)
            salario = it.getDouble("salario", 0.0)
            periodosVacaciones = it.getInt("periodosVacaciones", 0)
            tipoContrato = it.getString("tipoContrato", "")
            fechaVencimientoFijo = it.getString("fechaVencimientoFijo", "")

            indemnizacion = it.getDouble("indemnizacion", 0.0)
            vacaciones = it.getDouble("vacaciones", 0.0)
            cesantias = it.getDouble("cesantias", 0.0)
            totalLiquidacion = it.getDouble("totalLiquidacion", 0.0)
        }

        // Mostrar los datos y resultados en la UI
        mostrarResultados()

        //Guardar en el historial
        guardarEnHistorial()

        // Asignar el texto explicativo al TextView
        tvExplicacionDespido.text = explicacionDespidoTexto

        // Configurar el botón de descarga de PDF
        btnDescargarPdf.setOnClickListener {
            crearPdf()
        }
    }
    private fun guardarEnHistorial() {
        val db = AppDatabase.getDatabase(this)
        val df = DecimalFormat("#,##0.00")

        val calculo = CalculoEntity(
            tipoCalculo = "⚖️ Liquidación Laboral",
            descripcion = "Salario: $${df.format(salario)} | $tipoContrato | $diasLaborados días",
            resultado = totalLiquidacion
        )

        lifecycleScope.launch(Dispatchers.IO) {
            db.calculoDao().insertCalculo(calculo)
        }
    }

    /**
     * Muestra los datos de entrada y los resultados calculados en los TextViews.
     */
    private fun mostrarResultados() {
        val currencyFormat = DecimalFormat("$#,##0.00")

        // Las siguientes líneas generan advertencias de Lint sobre concatenación de strings literales.
        // Para una aplicación profesional, se deberían usar strings de recursos con placeholders.
        // Ej: getString(R.string.fecha_inicio_contrato, fechaInicioContrato)
        tvInputFechaInicio.text = "Fecha de Inicio: $fechaInicioContrato"
        tvInputFechaFin.text = "Fecha de Finalización: $fechaFinContrato"
        tvInputDiasLaborados.text = "Días Laborados: ${diasLaborados.toLong()} días"
        tvInputSalario.text = "Salario Mensual: ${currencyFormat.format(salario)}"
        tvInputVacacionesPeriodos.text = "Periodos de Vacaciones No Tomadas: $periodosVacaciones"
        tvInputTipoContrato.text = "Tipo de Contrato: $tipoContrato"

        // Mostrar fecha de vencimiento solo si es término fijo
        if (tipoContrato == "Término Fijo" && fechaVencimientoFijo.isNotEmpty()) {
            tvInputFechaVencimientoFijo.visibility = View.VISIBLE
            tvInputFechaVencimientoFijo.text = "Fecha de Vencimiento Contrato Fijo: $fechaVencimientoFijo"
        } else {
            tvInputFechaVencimientoFijo.visibility = View.GONE
        }

        tvResultadoIndemnizacion.text = "Indemnización: ${currencyFormat.format(indemnizacion)}"
        tvResultadoVacaciones.text = "Vacaciones: ${currencyFormat.format(vacaciones)}"
        tvResultadoCesantias.text = "Cesantías: ${currencyFormat.format(cesantias)}"
        tvTotalLiquidacion.text = "Total Liquidación: ${currencyFormat.format(totalLiquidacion)}"
    }

    /**
     * Crea un documento PDF con los resultados de la liquidación y lo guarda.
     * Esta función inicia el proceso de creación del PDF y la solicitud de guardar.
     */
    private fun crearPdf() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            putExtra(Intent.EXTRA_TITLE, "LiquidacionContrato_$timestamp.pdf")
        }
        // startActivityForResult está deprecated, pero lo mantendremos por compatibilidad simple.
        // En un proyecto más nuevo, usarías ActivityResultLauncher.
        @Suppress("DEPRECATION")
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    /**
     * Callback para manejar el resultado de la selección de archivo para guardar el PDF.
     * Aquí es donde realmente se dibuja y escribe el PDF al archivo seleccionado.
     */
    @Suppress("DEPRECATION") // Para suprimir la advertencia de deprecación de onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                var pdfDocument: PdfDocument? = null // Declara pdfDocument aquí para que sea accesible en finally
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument = PdfDocument() // Inicializa aquí
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size (approx)
                        val page = pdfDocument!!.startPage(pageInfo) // Usa !! porque sabemos que no es null aquí
                        val canvas: Canvas = page.canvas
                        val paint = Paint()
                        val currencyFormat = DecimalFormat("$#,##0.00")

                        var yPos = 40f

                        // Dibuja el contenido del PDF
                        paint.textSize = 24f
                        paint.color = Color.BLACK
                        paint.isFakeBoldText = true
                        canvas.drawText("Reporte de Liquidación de Contrato", 40f, yPos, paint)
                        yPos += 40f

                        paint.textSize = 16f
                        paint.isFakeBoldText = true
                        canvas.drawText("Datos de Entrada:", 40f, yPos, paint)
                        yPos += 25f

                        paint.isFakeBoldText = false
                        canvas.drawText("Fecha de Inicio: $fechaInicioContrato", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Fecha de Finalización: $fechaFinContrato", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Días Laborados: ${diasLaborados.toLong()} días", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Salario Mensual: ${currencyFormat.format(salario)}", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Periodos de Vacaciones No Tomadas: $periodosVacaciones", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Tipo de Contrato: $tipoContrato", 60f, yPos, paint)
                        yPos += 20f
                        if (tipoContrato == "Término Fijo" && fechaVencimientoFijo.isNotEmpty()) {
                            canvas.drawText("Fecha de Vencimiento Contrato Fijo: $fechaVencimientoFijo", 60f, yPos, paint)
                            yPos += 20f
                        }
                        yPos += 20f

                        paint.isFakeBoldText = true
                        canvas.drawText("Resultados de la Liquidación:", 40f, yPos, paint)
                        yPos += 25f

                        paint.isFakeBoldText = false
                        canvas.drawText("Indemnización: ${currencyFormat.format(indemnizacion)}", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Vacaciones: ${currencyFormat.format(vacaciones)}", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Cesantías: ${currencyFormat.format(cesantias)}", 60f, yPos, paint)
                        yPos += 30f

                        paint.isFakeBoldText = true
                        paint.textSize = 18f
                        canvas.drawText("Total Liquidación: ${currencyFormat.format(totalLiquidacion)}", 60f, yPos, paint)
                        yPos += 40f

                        // Texto explicativo sobre el despido sin justa causa en el PDF
                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        val lines = explicacionDespidoTexto.split("\n")
                        for (line in lines) {
                            canvas.drawText(line, 40f, yPos, paint)
                            yPos += 15f
                        }

                        pdfDocument!!.finishPage(page) // Usa !!
                        pdfDocument!!.writeTo(outputStream) // Escribe el PDF al flujo de salida
                        Toast.makeText(this, "PDF guardado exitosamente.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(this, "Error al guardar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                } finally {
                    pdfDocument?.close() // Asegura que pdfDocument se cierre, si se inicializó
                }
            }
        } else if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Guardar PDF cancelado.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CREATE_FILE_REQUEST_CODE = 1
    }
}
    