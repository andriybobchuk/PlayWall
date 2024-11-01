package com.studios1299.playwall.monetization.presentation

import com.studios1299.playwall.monetization.presentation.screens.DailyCheckinData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppState {
    private val _devilCount = MutableStateFlow(0)
    val devilCount: StateFlow<Int> = _devilCount.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _nextDiamondSheetShow = MutableStateFlow(false)
    val nextDiamondSheetShow: StateFlow<Boolean> = _nextDiamondSheetShow.asStateFlow()

    private val _nextSpinSheetShow = MutableStateFlow(false)
    val nextSpinSheetShow: StateFlow<Boolean> = _nextSpinSheetShow.asStateFlow()

    private val _isNextSpinLoading = MutableStateFlow(false)
    val isNextSpinLoading: StateFlow<Boolean> = _isNextSpinLoading.asStateFlow()

    private val _dailyCheckinState = MutableStateFlow<List<DailyCheckinData>>(emptyList())
    val dailyCheckinState: StateFlow<List<DailyCheckinData>> = _dailyCheckinState.asStateFlow()

    private val _hasCheckedInToday = MutableStateFlow(false)
    val hasCheckedInToday: StateFlow<Boolean> = _hasCheckedInToday.asStateFlow()

    private val _consecutiveDays = MutableStateFlow(0)
    val consecutiveDays: StateFlow<Int> = _consecutiveDays.asStateFlow()

    private val _lastCheckinDate = MutableStateFlow("2024-10-31")
    val lastCheckinDate: StateFlow<String> = _lastCheckinDate.asStateFlow()

    // Methods to update state
    fun updateDevilCount(newCount: Int) {
        _devilCount.value = newCount
    }

    fun updatePremiumStatus(isPremiumUser: Boolean) {
        _isPremium.value = isPremiumUser
    }

    fun updateNextDiamondSheetShow(shouldShow: Boolean) {
        _nextDiamondSheetShow.value = shouldShow
    }

    fun updateNextSpinSheetShow(shouldShow: Boolean) {
        _nextSpinSheetShow.value = shouldShow
    }

    fun updateIsNextSpinLoading(isLoading: Boolean) {
        _isNextSpinLoading.value = isLoading
    }

    fun updateDailyCheckinState(newCheckinData: List<DailyCheckinData>) {
        _dailyCheckinState.value = newCheckinData
    }

    fun updateHasCheckedInToday(hasCheckedIn: Boolean) {
        _hasCheckedInToday.value = hasCheckedIn
    }

    fun updateConsecutiveDays(newValue: Int) {
        _consecutiveDays.value = newValue
    }

    fun updateLastCheckinDate(date: String) {
        _lastCheckinDate.value = date
    }
}
