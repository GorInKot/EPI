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
        private val repGenContractor: TextView = itemView.findViewById(R.id.textView_repGenContractor)
        private val repSSKGp: TextView = itemView.findViewById(R.id.textView_repSSKGp)
        private val subContractor: TextView = itemView.findViewById(R.id.textView_subContractor)
        private val repSubContractor: TextView = itemView.findViewById(R.id.textView_repSubContractor)
        private val repSSKSub: TextView = itemView.findViewById(R.id.textView_repSSKSub)
        private val transportContract: TextView = itemView.findViewById(R.id.textView_transportContract)
        private val transportExecutor: TextView = itemView.findViewById(R.id.textView_transportExecutor)
        private val stateNumber: TextView = itemView.findViewById(R.id.textView_stateNumber)
        private val startDate: TextView = itemView.findViewById(R.id.textView_startDate)
        private val startTime: TextView = itemView.findViewById(R.id.textView_startTime)
        private val endDate: TextView = itemView.findViewById(R.id.textView_endDate)
        private val endTime: TextView = itemView.findViewById(R.id.textView_endTime)

        fun bind(item: ChildItem) {
            customer.text = "Заказчик:\n${item.customer}"
            contract.text = "Договор СК:\n${item.contract}"
            genContractor.text = "Генподрядчик:\n${item.genContractor}"
            repGenContractor.text = "Представитель генподрядчика:\n${item.repGenContractor}"
            repSSKGp.text = "Представитель ССК ПО (ГП):\n${item.repSSKGp}"
            subContractor.text = "Субподрядчик:\n${item.subContractor}"
            repSubContractor.text = "Представитель субподрядчика:\n${item.repSubContractor}"
            repSSKSub.text = "Представитель ССК ПО (Суб):\n${item.repSSKSub}"
            transportContract.text = "Договор по транспорту:\n${item.transportCustomer}"
            transportExecutor.text = "Исполнитель по транспорту:\n${item.transportExecutor}"
            stateNumber.text = "Госномер:\n${item.stateNumber}"
            startDate.text = "Дата начала поездки:\n${item.startDate}"
            startTime.text = "Время начала поездки:\n${item.startTime}"
            endDate.text = "Дата завершения поездки:\n${item.endDate}"
            endTime.text = "Время завершения поездки:\n${item.endTime}"

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