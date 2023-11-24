package com.kimminh.moneysense.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _recognizedMoneyText = MutableStateFlow("")
    val recognizedMoneyText = _recognizedMoneyText.asStateFlow()

}