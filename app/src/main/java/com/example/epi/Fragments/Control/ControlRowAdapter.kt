package com.example.epi.Fragments.Control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.R

class ControlRowAdapter(
    private val onEditClick: (ControlRow, Int) -> Unit,
    private val onDeleteClick: (ControlRow) -> Unit
) : ListAdapter<ControlRow, ControlRowAdapter.ControlRowViewHolder>(ControlRowDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControlRowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_control_row, parent, false)
        return ControlRowViewHolder(view)
    }

    override fun onBindViewHolder(holder: ControlRowViewHolder, position: Int) {
        val row = getItem(position)
        holder.bind(row, position)
    }

    inner class ControlRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEquipmentName: TextView = itemView.findViewById(R.id.tvEquipmentName)

        private val tvWorkComplex: TextView = itemView.findViewById(R.id.tvComplexOfWork)
        private val tvWorkType: TextView = itemView.findViewById(R.id.tvTypeOfWork)
        private val tvOrderNumber: TextView = itemView.findViewById(R.id.tvOrderNumber)
        private val tvReport: TextView = itemView.findViewById(R.id.tvReport)
        private val tvRemarks: TextView = itemView.findViewById(R.id.tvRemarks)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(row: ControlRow, position: Int) {
            tvEquipmentName.text = row.equipmentName
            tvWorkComplex.text = row.complexOfWork
            tvWorkType.text = row.typeOfWork
            tvOrderNumber.text = row.orderNumber
            tvReport.text = row.report
            tvRemarks.text = row.remarks

            btnEdit.setOnClickListener {
                onEditClick(row, position)
            }
            btnDelete.setOnClickListener {
                onDeleteClick(row)
            }
        }
    }

    class ControlRowDiffCallback : DiffUtil.ItemCallback<ControlRow>() {
        override fun areItemsTheSame(oldItem: ControlRow, newItem: ControlRow): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ControlRow, newItem: ControlRow): Boolean {
            return oldItem == newItem
        }
    }
}