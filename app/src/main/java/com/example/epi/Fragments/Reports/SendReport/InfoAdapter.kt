package com.example.epi.Fragments.Reports.SendReport

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.Fragments.Control.Model.ControlRow
import com.example.epi.Fragments.FixingVolumes.Model.FixVolumesRow
import com.example.epi.Fragments.Reports.SendReport.Model.InfoItem
import com.example.epi.databinding.InfoItemBinding

class InfoAdapter : ListAdapter<InfoItem, InfoAdapter.ViewHolder>(DiffCallback()) {

    private val nestedAdapter = NestedInfoAdapter()

    class ViewHolder(private val binding: InfoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InfoItem, nestedAdapter: NestedInfoAdapter) {
            binding.keyTextView.text = item.key
            binding.valueTextView.visibility = View.VISIBLE
            binding.nestedRecyclerView.visibility = View.GONE

            when (val value = item.value) {
                is String -> {
                    binding.valueTextView.text = value
                }
                is List<*> -> {
                    binding.valueTextView.visibility = View.GONE
                    val nestedRecycler = binding.nestedRecyclerView
                    if (nestedRecycler != null) {
                        nestedRecycler.layoutManager = LinearLayoutManager(nestedRecycler.context)
                        // Очищаем адаптер перед привязкой
                        nestedRecycler.adapter = null
                        nestedRecycler.adapter = nestedAdapter
                        Log.d("InfoAdapter", "Processing key: ${item.key}, value size: ${value.size}, first item type: ${value.firstOrNull()?.javaClass}")
                        when (item.key) {
                            "Полевой контроль" -> {
                                if (value.isNotEmpty() && value.all { it is ControlRow }) {
                                    nestedAdapter.submitControlRows(value as List<ControlRow>)
                                    nestedRecycler.visibility = View.VISIBLE
                                } else {
                                    Log.e("InfoAdapter", "Invalid type for Полевой контроль: ${value.firstOrNull()?.javaClass}")
                                }
                            }
                            "Фиксация объема" -> {
                                if (value.isNotEmpty() && value.all { it is FixVolumesRow }) {
                                    nestedAdapter.submitFixVolumesRows(value as List<FixVolumesRow>)
                                    nestedRecycler.visibility = View.VISIBLE
                                } else {
                                    Log.e("InfoAdapter", "Invalid type for Фиксация объема: ${value.firstOrNull()?.javaClass}")
                                }
                            }
                            else -> nestedRecycler.visibility = View.GONE
                        }
                        // Принудительное обновление после привязки данных
                        nestedRecycler.adapter?.notifyDataSetChanged()
                    } else {
                        Log.e("InfoAdapter", "nestedRecyclerView not found!")
                    }
                }
                else -> {
                    binding.valueTextView.text = value?.toString() ?: "Нет данных"
                    binding.nestedRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InfoItem>() {
        override fun areItemsTheSame(oldItem: InfoItem, newItem: InfoItem): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: InfoItem, newItem: InfoItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), nestedAdapter)

    }
}