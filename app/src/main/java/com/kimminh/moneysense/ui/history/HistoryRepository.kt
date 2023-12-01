package com.kimminh.moneysense.ui.history

import androidx.lifecycle.LiveData

class HistoryRepository(private val historyDao: HistoryDao) {

    val getAllHistory: LiveData<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun addHistory(historyEntity: HistoryEntity){
        historyDao.insert(historyEntity)
    }

    suspend fun deleteAll(){
        historyDao.deleteAll()
    }
}