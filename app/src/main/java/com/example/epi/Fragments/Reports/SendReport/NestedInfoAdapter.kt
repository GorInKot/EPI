package com.example.epi.Fragments.Reports.SendReport

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.databinding.NestedInfoItemBinding

class NestedInfoAdapter : ListAdapter<NestedInfoAdapter.Item, NestedInfoAdapter.ViewHolder>(DiffCallback()) {

    sealed class Item {
        data class ControlItem(val controlRow: ControlRow) : Item()
        data class FixVolumesItem(val fixVolumesRow: FixVolumesRow) : Item()
    }

    class ViewHolder(private val binding: NestedInfoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            Log.d("NestedInfoAdapter", "Binding item: ${item.javaClass.simpleName}, data: $item")
            when (item) {
                is Item.ControlItem -> {
                    binding.titleTextView.text = "- Наименование оборудования: ${item.controlRow.equipmentName}"
                    binding.detailsTextView.text = """
                        Комплекс работ: ${item.controlRow.complexOfWork}
                        Вид работ: ${item.controlRow.typeOfWork}
                        Номер предписания: ${item.controlRow.orderNumber}
                        Отчет: ${item.controlRow.report}
                        Замечания: ${item.controlRow.remarks}
                    """.trimIndent()
                }
                is Item.FixVolumesItem -> {
                    binding.titleTextView.text = "- Объект: ${item.fixVolumesRow.ID_object}"
                    binding.detailsTextView.text = """
                        Комплекс работ: ${item.fixVolumesRow.complexOfWork}
                        Вид работ: ${item.fixVolumesRow.projectWorkType}
                        План: ${item.fixVolumesRow.plan} ${item.fixVolumesRow.measure}
                        Факт: ${item.fixVolumesRow.fact} ${item.fixVolumesRow.measure}
                        Результат: ${item.fixVolumesRow.result} ${item.fixVolumesRow.measure}
                    """.trimIndent()
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return when {
                oldItem is Item.ControlItem && newItem is Item.ControlItem -> oldItem.controlRow.equipmentName == newItem.controlRow.equipmentName
                oldItem is Item.FixVolumesItem && newItem is Item.FixVolumesItem -> oldItem.fixVolumesRow.ID_object == newItem.fixVolumesRow.ID_object
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NestedInfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("NestedInfoAdapter", "Binding position: $position, item: $item")
        holder.bind(item)
    }

    fun submitControlRows(rows: List<ControlRow>) {
        val newList = rows.map { Item.ControlItem(it) }
        Log.d("NestedInfoAdapter", "Submitting ControlRows: $newList")
        submitList(newList)
    }

    fun submitFixVolumesRows(rows: List<FixVolumesRow>) {
        val newList = rows.map { Item.FixVolumesItem(it) }
        Log.d("NestedInfoAdapter", "Submitting FixVolumesRows: $newList")
        submitList(newList)
    }
}