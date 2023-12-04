package com.kimminh.moneysense.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel: ViewModel() {
    private val _recognizedMoney = MutableStateFlow("")
    val recognizedMoney = _recognizedMoney.asStateFlow()

    private val _convertedMoney = MutableStateFlow("")
    val convertedMoney = _convertedMoney.asStateFlow()

    fun onRecognized(money: String) {
        _recognizedMoney.update { money }
    }

    fun onConverted(money: String) {
        _convertedMoney.update { money }
    }
}