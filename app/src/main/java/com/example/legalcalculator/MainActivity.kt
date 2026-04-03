package com.example.legalcalculator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val cardDerechoLaboral: CardView = findViewById(R.id.card_derecho_laboral)
        val cardDerechoComercial: CardView = findViewById(R.id.card_derecho_comercial)
        val cardDerechoTributario: CardView = findViewById(R.id.card_derecho_tributario)
        val btnHistorial: Button = findViewById(R.id.btn_historial)

        cardDerechoLaboral.setOnClickListener {
            startActivity(Intent(this, DerechoLaboralActivity::class.java))
        }
        cardDerechoComercial.setOnClickListener {
            startActivity(Intent(this, InteresesActivity::class.java))
        }
        cardDerechoTributario.setOnClickListener {
            startActivity(Intent(this, IndexacionActivity::class.java))
        }
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
    }
}