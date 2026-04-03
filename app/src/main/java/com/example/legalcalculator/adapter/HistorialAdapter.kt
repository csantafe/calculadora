package com.example.legalcalculator.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.legalcalculator.R
import com.example.legalcalculator.data.entity.CalculoEntity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialAdapter : ListAdapter<CalculoEntity, HistorialAdapter.HistorialViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTipo: TextView = itemView.findViewById(R.id.tv_item_tipo)
        private val tvFecha: TextView = itemView.findViewById(R.id.tv_item_fecha)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tv_item_descripcion)
        private val tvTotal: TextView = itemView.findViewById(R.id.tv_item_total)

        fun bind(calculo: CalculoEntity) {
            val df = DecimalFormat("#,##0.00")
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            tvTipo.text = calculo.tipoCalculo
            tvFecha.text = sdf.format(Date(calculo.fecha))
            tvDescripcion.text = calculo.descripcion
            tvTotal.text = "Total: $${df.format(calculo.resultado)}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CalculoEntity>() {
        override fun areItemsTheSame(oldItem: CalculoEntity, newItem: CalculoEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CalculoEntity, newItem: CalculoEntity) =
            oldItem == newItem
    }
}
