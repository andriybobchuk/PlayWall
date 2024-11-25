package com.studios1299.playwall.monetization.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.domain.CoreRepository
import com.studios1299.playwall.monetization.presentation.screens.DailyCheckinData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiamondsViewModel(
    private val repository: CoreRepository
) : ViewModel() {

    companion object {
        private const val LOG_TAG = "DiamondsViewModel"
        val dailyCheckinData = listOf(
            DailyCheckinData(label = "Day 1", diamonds = 1, checked = false),
            DailyCheckinData(label = "Day 2", diamonds = 1, checked = false),
            DailyCheckinData(label = "Day 3", diamonds = 1, checked = false),
            DailyCheckinData(label = "Day 4", diamonds = 2, checked = false),
            DailyCheckinData(label = "Day 5", diamonds = 2, checked = false),
            DailyCheckinData(label = "Day 6", diamonds = 2, checked = false),
            DailyCheckinData(label = "Day 7", diamonds = 3, checked = false),
            DailyCheckinData(label = "Day 8", diamonds = 4, checked = false),
            DailyCheckinData(label = "Day 9", diamonds = 5, checked = false)
        )
    }

    // State from AppState
    val devilCount = AppState.devilCount.asLiveData()
    val isPremium = AppState.isPremium.asLiveData()
    val hasCheckedInToday = AppState.hasCheckedInToday.asLiveData()

    private val _nextDiamondSheetShow = MutableStateFlow(false)
    val nextDiamondSheetShow: StateFlow<Boolean> = _nextDiamondSheetShow.asStateFlow()

    private val _nextSpinSheetShow = MutableStateFlow(false)
    val nextSpinSheetShow: StateFlow<Boolean> = _nextSpinSheetShow.asStateFlow()

    private val _isNextSpinLoading = MutableStateFlow(false)
    val isNextSpinLoading: StateFlow<Boolean> = _isNextSpinLoading.asStateFlow()

    private val _dailyCheckinState = MutableStateFlow<List<DailyCheckinData>>(emptyList())
    val dailyCheckinState: StateFlow<List<DailyCheckinData>> = _dailyCheckinState.asStateFlow()


    init {
        checkIfCheckedInToday()
        loadDailyCheckinData()

        AppState.devilCount.onEach { newCount ->
            Log.e("AppState", "Devil Count Updated: $newCount")
        }.launchIn(viewModelScope)

        AppState.isPremium.onEach { isPremium ->
            Log.e("AppState", "Premium Status Updated: $isPremium")
        }.launchIn(viewModelScope)

        AppState.hasCheckedInToday.onEach { hasCheckedInToday ->
            Log.e("AppState", "Has Checked In Today Updated: $hasCheckedInToday")
        }.launchIn(viewModelScope)

    }

    fun addDevils(count: Int) {
        viewModelScope.launch {
            repository.addDevils(count)
        }
    }


    fun updatePremiumStatus(isPremium: Boolean) {
        viewModelScope.launch {
            repository.updatePremiumStatus(isPremium)
        }
    }


    private fun checkIfCheckedInToday() {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())

            val lastCheckInDate = repository.getLastCheckInDate()
            AppState.updateHasCheckedInToday(lastCheckInDate == today)
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            Log.e(LOG_TAG, "checkIn(), start")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Date() // Gets the current date
            val todayStr = sdf.format(today) // Formats today's date to a String
            Log.e(LOG_TAG, "checkIn(), todayStr=$todayStr")

            val lastCheckInDateStr = repository.getLastCheckInDate() ?: ""
            Log.e(LOG_TAG, "checkIn(), lastCheckInDateStr=$lastCheckInDateStr")

            if (lastCheckInDateStr != todayStr) {
                val lastCheckInDate = if (lastCheckInDateStr.isNotEmpty()) sdf.parse(lastCheckInDateStr) else null
                val yesterday = Date(today.time - 86400000) // Calculate yesterday's date

                val wasYesterday = lastCheckInDate != null && sdf.format(lastCheckInDate) == sdf.format(yesterday)
                val consecutiveDays = if (wasYesterday) {
                    repository.getConsecutiveDays() + 1
                } else {
                    1
                }
                Log.e(LOG_TAG, "checkIn(), consecutiveDays: $consecutiveDays")

                repository.setLastCheckInDate(todayStr)
                repository.setConsecutiveDays(consecutiveDays)

                val diamondsToAdd = when (consecutiveDays) {
                    in 1..3 -> 1
                    in 4..6 -> 2
                    7, 8 -> 3
                    9 -> 5
                    else -> 1
                }
                Log.e(LOG_TAG, "checkIn(), diamondsToAdd: $diamondsToAdd")

                repository.addDevils(diamondsToAdd)
                checkIfCheckedInToday()
                loadDailyCheckinData()
            }
        }
    }
    private fun loadDailyCheckinData() {
        Log.e(LOG_TAG, "loadDailyCheckinData(), start")
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Date()
            val todayStr = sdf.format(today)
            val yesterday = Date(today.time - 86400000) // Calculate yesterday's date

            val lastCheckInDateStr = repository.getLastCheckInDate() ?: ""
            val lastCheckInDate = if (lastCheckInDateStr.isNotEmpty()) sdf.parse(lastCheckInDateStr) else null

            // Check if streak is broken
            val isStreakBroken = lastCheckInDate != null && sdf.format(lastCheckInDate) != sdf.format(yesterday) && lastCheckInDateStr != todayStr

            if (isStreakBroken) {
                Log.e(LOG_TAG, "Streak is broken. Resetting to 0.")
                repository.setConsecutiveDays(0)
            }

            val numberOfConsecutiveDays = repository.getConsecutiveDays()
            val hasCheckedInToday = lastCheckInDateStr == todayStr

            Log.e(LOG_TAG, "loadDailyCheckinData(), consecutiveDaysCheckedIn: $numberOfConsecutiveDays")
            Log.e(LOG_TAG, "loadDailyCheckinData(), hasCheckedInToday: $hasCheckedInToday")

            val updatedDailyCheckinData = dailyCheckinData.mapIndexed { indexOfDay, data ->
                val dayNumber = indexOfDay + 1

                val isPastCheckin = dayNumber <= numberOfConsecutiveDays
                val isTodayCheckin = dayNumber == numberOfConsecutiveDays && hasCheckedInToday

                data.copy(checked = isPastCheckin || isTodayCheckin)
            }

            AppState.updateDailyCheckinState(updatedDailyCheckinData)

            // Log the updated data
            updatedDailyCheckinData.forEach { day ->
                Log.e(LOG_TAG, "Day: ${day.label}, Diamonds: ${day.diamonds}, Checked: ${day.checked}")
            }
        }
    }


//    private fun loadDailyCheckinData() {
//        Log.e(LOG_TAG, "loadDailyCheckinData(), start")
//        viewModelScope.launch {
//            val numberOfConsecutiveDays = repository.getConsecutiveDays()
//            val hasCheckedInToday = repository.hasCheckedInToday()
//
//            Log.e(LOG_TAG, "loadDailyCheckinData(), consecutiveDaysCheckedIn: $numberOfConsecutiveDays")
//            Log.e(LOG_TAG, "loadDailyCheckinData(), hasCheckedInToday: $hasCheckedInToday")
//
//            val updatedDailyCheckinData = dailyCheckinData.mapIndexed { indexOfDay, data ->
//                val dayNumber = indexOfDay + 1
//
//                val isPastCheckin = dayNumber <= numberOfConsecutiveDays
//                val isTodayCheckin = dayNumber == numberOfConsecutiveDays && hasCheckedInToday
//
//                data.copy(checked = isPastCheckin || isTodayCheckin)
//            }
//
//            // Ensure reset if the streak is broken
//            if (!hasCheckedInToday && numberOfConsecutiveDays == 0) {
//                updatedDailyCheckinData.forEach { day ->
//                    Log.e(LOG_TAG, "Streak Reset - Day: ${day.label}, Checked: ${day.checked}")
//                }
//            }
//
//            AppState.updateDailyCheckinState(updatedDailyCheckinData)
//
//            // Log the new state for verification
//            updatedDailyCheckinData.forEach { day ->
//                Log.e(LOG_TAG, "Day: ${day.label}, Diamonds: ${day.diamonds}, Checked: ${day.checked}")
//            }
//        }
//    }


}
