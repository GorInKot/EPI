package com.example.epi.Fragments.FixingVolumes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.R

class FixVolumesRowAdapter(
    private val onEditClick: (FixVolumesRow, Int) -> Unit,
    private val onDeleteClick: (FixVolumesRow) -> Unit
) : ListAdapter<FixVolumesRow, FixVolumesRowAdapter.FixVolumesViewHolder>(FixVolumesRowDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FixVolumesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fixvolumes_row, parent, false)
        return FixVolumesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FixVolumesViewHolder, position: Int) {
        val row = getItem(position)
        holder.bind(row, position)
    }

    inner class FixVolumesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIdObject: TextView = itemView.findViewById(R.id.tvIdObject)
        private val tvProjectWorkType: TextView = itemView.findViewById(R.id.tvProjectWorkType)
        private val tvMeasure: TextView = itemView.findViewById(R.id.tvMeasure)
        private val tvPlan: TextView = itemView.findViewById(R.id.tvPlan)
        private val tvFact: TextView = itemView.findViewById(R.id.tvFact)
        private val tvResult: TextView = itemView.findViewById(R.id.tvResult)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(row: FixVolumesRow, position: Int) {
            tvIdObject.text = row.ID_object ?: ""
            tvProjectWorkType.text = row.projectWorkType ?: ""
            tvMeasure.text = row.measure ?: ""
            tvPlan.text = row.plan ?: ""
            tvFact.text = row.fact ?: ""
            tvResult.text = row.result ?: ""

            btnEdit.setOnClickListener { onEditClick(row, position) }
            btnDelete.setOnClickListener { onDeleteClick(row) }
        }
    }

    class FixVolumesRowDiffCallback : DiffUtil.ItemCallback<FixVolumesRow>() {
        override fun areItemsTheSame(oldItem: FixVolumesRow, newItem: FixVolumesRow): Boolean {
            return oldItem.ID_object == newItem.ID_object
        }

        override fun areContentsTheSame(oldItem: FixVolumesRow, newItem: FixVolumesRow): Boolean {
            return oldItem == newItem
        }
    }
}