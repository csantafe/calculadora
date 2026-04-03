package com.example.legalcalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DerechoLaboralActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_derecho_laboral)

        findViewById<CardView>(R.id.card_justa_causa).setOnClickListener {
            startActivity(Intent(this, LiquidacionActivity::class.java))
        }

        findViewById<CardView>(R.id.card_sin_justa_causa).setOnClickListener {
            startActivity(Intent(this, LiquidacionDespidoActivity::class.java))
        }

        findViewById<Button>(R.id.btn_regresar_laboral).setOnClickListener {
            finish()
        }
    }
}