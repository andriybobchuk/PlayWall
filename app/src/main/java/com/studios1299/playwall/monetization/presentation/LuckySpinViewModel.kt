package com.studios1299.playwall.monetization.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.domain.CoreRepository
import kotlinx.coroutines.launch

class LuckySpinViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    fun addDevils(count: Int) {
        viewModelScope.launch {
            repository.addDevils(count)
        }
    }
}