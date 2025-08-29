package com.example.epi.Fragments.General.ChangePlanValues

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.DataBase.PlanValue.PlanValue
import com.example.epi.R

class PlanValueAdapter(
    private val onEditClicked: (PlanValue) -> Unit,
    private val onDeleteClicked: (PlanValue) -> Unit
) : ListAdapter<PlanValue, PlanValueAdapter.PlanValueViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanValueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_changeplanvalue_row, parent, false)
        return PlanValueViewHolder(view, onEditClicked, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: PlanValueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlanValueViewHolder(
        itemView: View,
        private val onEditClicked: (PlanValue) -> Unit,
        private val onDeleteClicked: (PlanValue) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvObjectId: TextView = itemView.findViewById(R.id.tv_cpv_objectId)
        private val tvComplexWork: TextView = itemView.findViewById(R.id.tv_cpv_ComplexOfWork)
        private val tvTypeOfWork: TextView = itemView.findViewById(R.id.tv_cpv_TypeOfWork)
        private val tvPlanValue: TextView = itemView.findViewById(R.id.tv_cpv_PlanValue)
        private val tvMeasures: TextView = itemView.findViewById(R.id.tv_cpv_Measures)
        private val btnEdit: Button = itemView.findViewById(R.id.btn_cpv_EditRow)
        private val btnDelete: Button = itemView.findViewById(R.id.btn_cpv_DeleteRow)

        fun bind(item: PlanValue) {
            tvObjectId.text = item.objectId
            tvComplexWork.text = item.complexWork
            tvTypeOfWork.text = item.typeOfWork
            tvPlanValue.text = item.planValue.toString()
            tvMeasures.text = item.measures

            btnEdit.setOnClickListener { onEditClicked(item) }
            btnDelete.setOnClickListener { onDeleteClicked(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlanValue>() {
        override fun areItemsTheSame(oldItem: PlanValue, newItem: PlanValue) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PlanValue, newItem: PlanValue) =
            oldItem == newItem
    }
}
