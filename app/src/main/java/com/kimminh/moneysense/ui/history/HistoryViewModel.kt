package com.kimminh.moneysense.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    val getAllHistory: LiveData<List<HistoryEntity>>
    private val repository: HistoryRepository
    init{
        val historyDao = HistoryDatabase.getDatabase(application).historyDao()
        repository = HistoryRepository(historyDao)
        getAllHistory = repository.getAllHistory
    }

    fun addHistory(history : HistoryEntity){
        viewModelScope.launch( Dispatchers.IO) {
            repository.addHistory(history )
        }
    }

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}