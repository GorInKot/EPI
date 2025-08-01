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
) : ListAdapter<ControlRow, ControlRowAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEquipment: TextView = view.findViewById(R.id.tvEquipmentName)
        private val tvWorkType: TextView = view.findViewById(R.id.tvWorkType)
        private val tvOrderNumber: TextView = view.findViewById(R.id.tvOrderNumber)
        private val tvReport: TextView = view.findViewById(R.id.tvReport)
        private val tvRemarks: TextView = view.findViewById(R.id.tvRemarks)
        private val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(row: ControlRow, position: Int, onEdit: (ControlRow, Int) -> Unit, onDelete: (ControlRow) -> Unit) {
            tvEquipment.text = row.equipmentName
            tvWorkType.text = row.workType
            tvOrderNumber.text = row.orderNumber
            tvReport.text = row.report
            tvRemarks.text = row.remarks

            btnEdit.setOnClickListener { onEdit(row, position) }
            btnDelete.setOnClickListener { onDelete(row) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_control_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position, onEditClick, onDeleteClick)
    }

    private class DiffCallback : DiffUtil.ItemCallback<ControlRow>() {
        override fun areItemsTheSame(oldItem: ControlRow, newItem: ControlRow): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ControlRow, newItem: ControlRow): Boolean {
            return oldItem == newItem
        }
    }
}