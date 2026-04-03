// ResultadosInteresesActivity.kt
// Esta actividad recibe los resultados del cálculo de intereses, los muestra
// y permite generar un PDF con la información, incluyendo el texto legal.

package com.example.legalcalculator

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
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


class ResultadosInteresesActivity : AppCompatActivity() {

    // Referencias a los TextViews para mostrar los resultados
    private lateinit var tvInputCapital: TextView
    private lateinit var tvInputDiasMora: TextView
    private lateinit var tvInputTasaCorrienteAnual: TextView
    private lateinit var tvInputTasaMoraMensual: TextView

    private lateinit var tvResultadoInteresCorriente: TextView
    private lateinit var tvResultadoInteresMoratorio: TextView
    private lateinit var tvExplicacionIntereses: TextView // Nueva referencia para el texto explicativo
    private lateinit var btnDescargarPdf: Button

    // Variables para almacenar los datos recibidos
    private var capital: Double = 0.0
    private var diasMora: Double = 0.0
    private var tasaCorrienteAnual: Double = 0.0
    private var tasaMoraMensual: Double = 0.0
    private var interesCorriente: Double = 0.0
    private var interesMoratorio: Double = 0.0

    // Texto explicativo sobre los intereses
    private val explicacionInteresesTexto = """
        Intereses Corrientes:
        Código de Comercio (Artículo 884):
        Define la tasa de interés remuneratorio, que es la que se aplica a los créditos o préstamos cuando se están al día con los pagos. 
        Intereses Moratorios:
        Código de Comercio (Artículo 884):
        El interés moratorio es el que se aplica cuando hay mora en el pago de las cuotas o de la obligación. 
        Contratos:
        Las partes pueden pactar libremente la tasa de interés moratorio en los contratos de financiación.
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_intereses)

        // Inicializar vistas. Los IDs deben coincidir EXACTAMENTE con los del XML.
        tvInputCapital = findViewById(R.id.tv_input_capital)
        tvInputDiasMora = findViewById(R.id.tv_input_dias_mora)
        tvInputTasaCorrienteAnual = findViewById(R.id.tv_input_tasa_corriente_anual)
        tvInputTasaMoraMensual = findViewById(R.id.tv_input_tasa_mora_mensual)

        tvResultadoInteresCorriente = findViewById(R.id.tv_resultado_interes_corriente)
        tvResultadoInteresMoratorio = findViewById(R.id.tv_resultado_interes_moratorio)
        tvExplicacionIntereses = findViewById(R.id.tv_explicacion_intereses)
        btnDescargarPdf = findViewById(R.id.btn_descargar_pdf_intereses)

        // Obtener datos del Intent
        intent.extras?.let {
            capital = it.getDouble("capital", 0.0)
            diasMora = it.getDouble("diasMora", 0.0)
            tasaCorrienteAnual = it.getDouble("tasaCorrienteAnual", 0.0)
            tasaMoraMensual = it.getDouble("tasaMoraMensual", 0.0)
            interesCorriente = it.getDouble("interesCorriente", 0.0)
            interesMoratorio = it.getDouble("interesMoratorio", 0.0)
        }

        // Mostrar los datos y resultados en la UI
        mostrarResultados()

        //Guarda el resultado en el historial
        guardarEnHistorial()

        // Asignar el texto explicativo al TextView
        tvExplicacionIntereses.text = explicacionInteresesTexto

        // Configurar el botón de descarga de PDF
        btnDescargarPdf.setOnClickListener {
            crearPdf()
        }
    }
    private fun guardarEnHistorial() {
        val db = AppDatabase.getDatabase(this)
        val df = DecimalFormat("#,##0.00")
        val totalIntereses = intent.getDoubleExtra("TOTAL_INTERESES", 0.0)
        val capital = intent.getDoubleExtra("CAPITAL", 0.0)
        val diasMora = intent.getIntExtra("DIAS_MORA", 0)

        val calculo = CalculoEntity(
            tipoCalculo = "💰 Intereses",
            descripcion = "Capital: $${df.format(capital)} | Días mora: $diasMora",
            resultado = totalIntereses
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
        val percentFormat = DecimalFormat("0.00'%'")

        tvInputCapital.text = "Capital: ${currencyFormat.format(capital)}"
        tvInputDiasMora.text = "Días en Mora: ${diasMora.toLong()} días"
        tvInputTasaCorrienteAnual.text = "Tasa Corriente Anual: ${percentFormat.format(tasaCorrienteAnual)}"
        tvInputTasaMoraMensual.text = "Tasa Mora Mensual: ${percentFormat.format(tasaMoraMensual)}"

        tvResultadoInteresCorriente.text = "Interés Corriente: ${currencyFormat.format(interesCorriente)}"
        tvResultadoInteresMoratorio.text = "Interés Moratorio: ${currencyFormat.format(interesMoratorio)}"
    }

    /**
     * Crea un documento PDF con los resultados de los intereses y lo guarda.
     * Esta función inicia el proceso de creación del PDF y la solicitud de guardar.
     */
    private fun crearPdf() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            putExtra(Intent.EXTRA_TITLE, "ReporteIntereses_$timestamp.pdf")
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
                        val percentFormat = DecimalFormat("0.00'%'")

                        var yPos = 40f

                        // Dibuja el contenido del PDF
                        paint.textSize = 24f
                        paint.color = Color.BLACK
                        paint.isFakeBoldText = true
                        canvas.drawText("Reporte de Cálculo de Intereses", 40f, yPos, paint)
                        yPos += 40f

                        paint.textSize = 16f
                        paint.isFakeBoldText = true
                        canvas.drawText("Datos de Entrada:", 40f, yPos, paint)
                        yPos += 25f

                        paint.isFakeBoldText = false
                        canvas.drawText("Capital: ${currencyFormat.format(capital)}", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Días en Mora: ${diasMora.toLong()} días", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Tasa Corriente Anual: ${percentFormat.format(tasaCorrienteAnual)}", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Tasa Mora Mensual: ${percentFormat.format(tasaMoraMensual)}", 60f, yPos, paint)
                        yPos += 20f

                        paint.isFakeBoldText = true
                        canvas.drawText("Resultados del Cálculo:", 40f, yPos, paint)
                        yPos += 25f

                        paint.isFakeBoldText = false
                        canvas.drawText("Interés Corriente: ${currencyFormat.format(interesCorriente)}", 60f, yPos, paint)
                        yPos += 20f
                        canvas.drawText("Interés Moratorio: ${currencyFormat.format(interesMoratorio)}", 60f, yPos, paint)
                        yPos += 30f

                        // Texto explicativo sobre los intereses en el PDF
                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        val lines = explicacionInteresesTexto.split("\n")
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
    