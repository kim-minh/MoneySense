package com.kimminh.moneysense.ui.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val date: String,
    val totalMoney: String,
    val moneyTypes: String

)
