package com.kimminh.moneysense.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
}

data class History(
    var name: String = "New Collection",
    private var notes: String? = null
) {
    var date: String = date()
        private set


    private fun date(): String {
        val time = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return time.format(formatter)
    }

    fun editNotes(text: String) {
        notes = text
        date = date()
    }

    //TODO implement actual logic for displaying history
    companion object {
        private var lastHistoryId = 0
        fun createHistoryList(numHistory: Int): ArrayList<History> {
            val contacts = ArrayList<History>()
            for (i in 1..numHistory) {
                contacts.add(History("History " + ++lastHistoryId))
            }
            return contacts
        }
    }
}