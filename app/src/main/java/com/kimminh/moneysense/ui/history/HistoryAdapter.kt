package com.kimminh.moneysense.ui.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kimminh.moneysense.R
import com.kimminh.moneysense.databinding.HistoryItemBinding

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {

    private var historyList = emptyList<HistoryEntity>()
    class HistoryHolder(private val binding: HistoryItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(history: HistoryEntity) {
            binding.tvDateCreated.text = history.date
            binding.tvTotalMoney.text = history.totalMoney+" VNĐ"
            binding.tvMoneyTypes.text = binding.root.context.getString(R.string.money_types)+": " +history.moneyTypes
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.HistoryHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryHolder(binding)
    }

    override fun onBindViewHolder(historyHolder: HistoryAdapter.HistoryHolder, position: Int) {
        val history: HistoryEntity = historyList[position]
        historyHolder.bind(history)

    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    fun setData(history : List<HistoryEntity>){
        this.historyList = history
        notifyDataSetChanged()
    }
}

