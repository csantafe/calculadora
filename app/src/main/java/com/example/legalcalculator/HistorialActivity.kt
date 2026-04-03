package com.example.legalcalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legalcalculator.adapter.HistorialAdapter
import com.example.legalcalculator.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorialActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvHistorialVacio: TextView
    private lateinit var btnLimpiarHistorial: Button
    private lateinit var btnRegresarHistorial: Button
    private lateinit var adapter: HistorialAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        db = AppDatabase.getDatabase(this)

        initViews()
        setupRecyclerView()
        setupButtons()
        observarHistorial()
    }

    private fun initViews() {
        rvHistorial = findViewById(R.id.rv_historial)
        tvHistorialVacio = findViewById(R.id.tv_historial_vacio)
        btnLimpiarHistorial = findViewById(R.id.btn_limpiar_historial)
        btnRegresarHistorial = findViewById(R.id.btn_regresar_historial)
    }

    private fun setupRecyclerView() {
        adapter = HistorialAdapter()
        rvHistorial.layoutManager = LinearLayoutManager(this)
        rvHistorial.adapter = adapter
    }

    private fun setupButtons() {
        btnRegresarHistorial.setOnClickListener { finish() }

        btnLimpiarHistorial.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Limpiar Historial")
                .setMessage("¿Estás seguro de que deseas eliminar todo el historial?")
                .setPositiveButton("Eliminar") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        db.calculoDao().deleteAll()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun observarHistorial() {
        lifecycleScope.launch {
            db.calculoDao().getAllCalculos().collect { lista ->
                withContext(Dispatchers.Main) {
                    adapter.submitList(lista)
                    if (lista.isEmpty()) {
                        tvHistorialVacio.visibility = View.VISIBLE
                        rvHistorial.visibility = View.GONE
                    } else {
                        tvHistorialVacio.visibility = View.GONE
                        rvHistorial.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
