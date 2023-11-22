package com.kimminh.moneysense.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kimminh.moneysense.databinding.DashboardItemBinding

class DashboardAdapter(
    private val historyList: List<History>
): RecyclerView.Adapter<DashboardAdapter.HistoryHolder>() {
    inner class HistoryHolder(private val binding: DashboardItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(history: History) {
            binding.noteName.text = history.name
            binding.dateCreated.text = history.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardAdapter.HistoryHolder {
        val binding = DashboardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryHolder(binding)
    }

    override fun onBindViewHolder(historyHolder: DashboardAdapter.HistoryHolder, position: Int) {
        val history: History = historyList[position]
        historyHolder.bind(history)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}

