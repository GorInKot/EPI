package com.example.epi.Fragments.Reports.Reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.epi.R

class ExpandableAdapter(private val data: MutableList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PARENT = 0
        private const val TYPE_CHILD = 1
    }

    private val expandedPositions = mutableSetOf<Int>()

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
        private val timeText: TextView = itemView.findViewById(R.id.textView_time)
        private val objectText: TextView = itemView.findViewById(R.id.textView_object)

        fun bind(item: ParentItem, position: Int) {
            dateText.text = "Дата: ${item.date}"
            timeText.text = "Время: ${item.time}"
            objectText.text = "Объект: ${item.obj}"

            itemView.setOnClickListener {
                if (expandedPositions.contains(position)) {
                    collapse(position, item)
                } else {
                    expand(position, item)
                }
            }
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customer: TextView = itemView.findViewById(R.id.textView_customer)
        private val contract: TextView = itemView.findViewById(R.id.textView_contract)
        private val genContractor: TextView = itemView.findViewById(R.id.textView_genContractor)
        private val subContractor: TextView = itemView.findViewById(R.id.textView_subContractor)
        private val transportCustomer: TextView = itemView.findViewById(R.id.textView_transportCustomer)

        fun bind(item: ChildItem) {
            customer.text = "Заказчик: ${item.customer}"
            contract.text = "Договор СК: ${item.contract}"
            genContractor.text = "Генподрядчик: ${item.genContractor}"
            subContractor.text = "Субподрядчик: ${item.subContractor}"
            transportCustomer.text = "Транспорт заказчика: ${item.transportCustomer}"
        }
    }

    private fun expand(position: Int, parentItem: ParentItem) {
        val insertPosition = position + 1 + countExpandedChildrenBefore(position)
        data.addAll(insertPosition, parentItem.children)
        expandedPositions.add(position)
        notifyItemRangeInserted(insertPosition, parentItem.children.size)
    }

    private fun collapse(position: Int, parentItem: ParentItem) {
        val startPosition = position + 1 + countExpandedChildrenBefore(position)
        val itemCount = parentItem.children.size
        data.subList(startPosition, startPosition + itemCount).clear()
        expandedPositions.remove(position)
        notifyItemRangeRemoved(startPosition, itemCount)
    }

    private fun countExpandedChildrenBefore(position: Int): Int {
        var count = 0
        for (i in 0 until position) {
            if (data[i] is ParentItem && expandedPositions.contains(i)) {
                count += (data[i] as ParentItem).children.size
            }
        }
        return count
    }
}