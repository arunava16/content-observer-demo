package com.arunava.apps.contentobserverdemo

import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arunava.apps.contentobserverdemo.databinding.ItemCallLogBinding
import java.text.SimpleDateFormat
import java.util.*

class CallListAdapter : ListAdapter<MyCallLog, CallListAdapter.CallViewHolder>(comparator) {

    private val todayTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        return CallViewHolder(
            ItemCallLogBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CallViewHolder(
        private val binding: ItemCallLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyCallLog) {
            binding.tvDate.text = getDate(item.date)
            binding.tvNumber.text = item.number
            binding.ivCallType.setImageResource(getTypeBasedResource(item.type))
        }
    }

    private fun getTypeBasedResource(type: Int): Int {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> R.drawable.ic_call_received
            CallLog.Calls.OUTGOING_TYPE -> R.drawable.ic_call_made
            CallLog.Calls.MISSED_TYPE -> R.drawable.ic_call_missed
            else -> R.drawable.unknown_med
        }
    }

    private fun getDate(date: Long): String {
        val timeNow = System.currentTimeMillis()
        return if (timeNow - date < 84000000) {
            "Today ${todayTimeFormat.format(Date(date))}"
        } else {
            timeFormat.format(Date(date))
        }
    }

    companion object {

        private val comparator = object : DiffUtil.ItemCallback<MyCallLog>() {
            override fun areItemsTheSame(oldItem: MyCallLog, newItem: MyCallLog): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MyCallLog, newItem: MyCallLog): Boolean {
                return oldItem.id == newItem.id && oldItem.number == newItem.number && oldItem.date == newItem.date
            }
        }
    }
}
