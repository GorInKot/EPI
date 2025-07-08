package com.example.epi.Fragments.Reports.SendReport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.R

class ExpandableAdapter(private val data: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PARENT = 0
        private const val TYPE_CHILD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is ParentItem -> TYPE_PARENT
            is ChildItem -> TYPE_CHILD
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PARENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_parent_send_report, parent, false)
            ParentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_child_send_report, parent, false)
            ChildViewHolder(view)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = data[position]) {
            is ParentItem -> (holder as ParentViewHolder).bind(item, position)
            is ChildItem -> (holder as ChildViewHolder).bind(item)
        }
    }

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.textView_date)
        private val objectText: TextView = itemView.findViewById(R.id.textView_object)

        fun bind(item: ParentItem, position: Int) {
            dateText.text = "Дата: ${item.date}"
            objectText.text = "Объект: ${item.obj}"

            itemView.setOnClickListener {
                if (item.isExpanded) {
                    collapse(position, item)
                } else {
                    expand(position, item)
                }
                item.isExpanded = !item.isExpanded
            }
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workType: TextView = itemView.findViewById(R.id.textView_workType)
        private val customer: TextView = itemView.findViewById(R.id.textView_customer)
        private val contractor: TextView = itemView.findViewById(R.id.textView_contractor)
        private val transportCustomer: TextView = itemView.findViewById(R.id.textView_transportCustomer)

        fun bind(item: ChildItem) {
            workType.text = "Тип работ: ${item.workType}"
            customer.text = "Заказчик: ${item.customer}"
            contractor.text = "Генподрядчик: ${item.contractor}"
            transportCustomer.text = "Транспорт заказчика: ${item.transportCustomer}"
        }
    }

    private fun expand(position: Int, parentItem: ParentItem) {
        data.addAll(position + 1, parentItem.children)
        notifyItemRangeInserted(position + 1, parentItem.children.size)
    }

    private fun collapse(position: Int, parentItem: ParentItem) {
        data.subList(position + 1, position + 1 + parentItem.children.size).clear()
        notifyItemRangeRemoved(position + 1, parentItem.children.size)
    }
}