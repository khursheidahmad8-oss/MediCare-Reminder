package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Medicine
import com.example.data.MedicineHistory
import com.example.data.MedicineRepository
import com.example.utils.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MedicineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicineRepository
    private val sharedPrefs = application.getSharedPreferences("medicare_settings", Context.MODE_PRIVATE)

    val medicines: StateFlow<List<Medicine>>
    val history: StateFlow<List<MedicineHistory>>

    private val _darkModeEnabled = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", false))
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _reminderBeforeMinutes = MutableStateFlow(sharedPrefs.getInt("reminder_before", 10))
    val reminderBeforeMinutes: StateFlow<Int> = _reminderBeforeMinutes.asStateFlow()

    private val _hydrationCount = MutableStateFlow(sharedPrefs.getInt("hydration_count_${getTodayDateString()}", 0))
    val hydrationCount: StateFlow<Int> = _hydrationCount.asStateFlow()

    val computedStreak: StateFlow<Int>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MedicineRepository(database.medicineDao())

        medicines = repository.allMedicines
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        history = repository.allHistory
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        computedStreak = repository.allHistory.map { historyList ->
            if (historyList.isEmpty()) return@map 1
            val takenDates = historyList.filter { it.status == "Taken" }.map { it.date }.toSet()
            var streak = 0
            val cal = Calendar.getInstance()
            val todayStr = getTodayDateString()
            if (takenDates.contains(todayStr)) {
                streak++
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
            for (i in 1..100) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                if (takenDates.contains(dateStr)) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
            if (streak == 0) 1 else streak
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

        // Perform daily reset and logging on initialization
        viewModelScope.launch {
            checkAndResetDailyStatus()
        }
    }

    // Automatically check if a new day has started and update active medicine statuses
    private suspend fun checkAndResetDailyStatus() {
        val today = getTodayDateString()
        Log.d("MedicineViewModel", "Checking daily status for today: $today")

        // We must inspect the current state of medicines.
        // Flow collection can be done once using .first()
        val currentMedicines = repository.allMedicines.first()

        currentMedicines.forEach { medicine ->
            if (medicine.lastUpdatedDate.isNotEmpty() && medicine.lastUpdatedDate != today) {
                // The date has changed since this medicine was last processed.
                // If the medicine was left as "Pending", it counts as "Missed" for that past day!
                if (medicine.status == "Pending") {
                    val missedHistory = MedicineHistory(
                        medicineId = medicine.id,
                        medicineName = medicine.name,
                        medicineType = medicine.type,
                        dosage = medicine.dosage,
                        time = medicine.time,
                        status = "Missed",
                        date = medicine.lastUpdatedDate
                    )
                    repository.insertHistory(missedHistory)
                    Log.d("MedicineViewModel", "Logged missed medicine for date ${medicine.lastUpdatedDate}: ${medicine.name}")
                }

                // Reset the medicine status to "Pending" for the new day
                val resetMedicine = medicine.copy(
                    status = "Pending",
                    lastUpdatedDate = today
                )
                repository.updateMedicine(resetMedicine)
                
                // Reschedule alarm for the new day
                ReminderScheduler.scheduleReminder(getApplication(), resetMedicine)
            } else if (medicine.lastUpdatedDate.isEmpty()) {
                // Initialize last updated date for newly added medicines if empty
                val initializedMedicine = medicine.copy(lastUpdatedDate = today)
                repository.updateMedicine(initializedMedicine)
                ReminderScheduler.scheduleReminder(getApplication(), initializedMedicine)
            }
        }
    }

    // Database CRUD Operations

    fun addMedicine(
        name: String,
        dosage: String,
        time: String,
        type: String,
        notes: String
    ) {
        viewModelScope.launch {
            val today = getTodayDateString()
            val newMedicine = Medicine(
                name = name,
                dosage = dosage,
                time = time,
                type = type,
                notes = notes,
                status = "Pending",
                lastUpdatedDate = today
            )
            val id = repository.insertMedicine(newMedicine)
            val medicineWithId = newMedicine.copy(id = id.toInt())
            
            // Schedule the system reminder alarm
            ReminderScheduler.scheduleReminder(getApplication(), medicineWithId)
        }
    }

    fun updateMedicine(
        id: Int,
        name: String,
        dosage: String,
        time: String,
        type: String,
        notes: String,
        status: String
    ) {
        viewModelScope.launch {
            val existing = repository.getMedicineById(id)
            val today = getTodayDateString()
            val updatedMedicine = Medicine(
                id = id,
                name = name,
                dosage = dosage,
                time = time,
                type = type,
                notes = notes,
                status = status,
                lastUpdatedDate = existing?.lastUpdatedDate ?: today
            )
            repository.updateMedicine(updatedMedicine)
            
            // Cancel old and schedule updated alarm
            ReminderScheduler.cancelReminder(getApplication(), updatedMedicine)
            ReminderScheduler.scheduleReminder(getApplication(), updatedMedicine)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), medicine)
            repository.deleteMedicine(medicine)
        }
    }

    fun markAsTaken(medicine: Medicine) {
        viewModelScope.launch {
            val today = getTodayDateString()
            val updated = medicine.copy(
                status = "Taken",
                lastUpdatedDate = today
            )
            repository.updateMedicine(updated)

            // Insert into history
            val historyItem = MedicineHistory(
                medicineId = medicine.id,
                medicineName = medicine.name,
                medicineType = medicine.type,
                dosage = medicine.dosage,
                time = medicine.time,
                status = "Taken",
                date = today
            )
            repository.insertHistory(historyItem)
            Log.d("MedicineViewModel", "Marked ${medicine.name} as Taken and added history entry.")
        }
    }

    fun markAsPending(medicine: Medicine) {
        viewModelScope.launch {
            val today = getTodayDateString()
            val updated = medicine.copy(
                status = "Pending",
                lastUpdatedDate = today
            )
            repository.updateMedicine(updated)
            Log.d("MedicineViewModel", "Reset ${medicine.name} status back to Pending.")
        }
    }

    fun getMedicineByIdFlow(id: Int): Flow<Medicine?> {
        return repository.getMedicineByIdFlow(id)
    }

    fun deleteHistory(history: MedicineHistory) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }

    // Settings adjustments

    fun toggleDarkMode() {
        val newMode = !_darkModeEnabled.value
        _darkModeEnabled.value = newMode
        sharedPrefs.edit().putBoolean("dark_mode", newMode).apply()
    }

    fun setReminderBefore(minutes: Int) {
        _reminderBeforeMinutes.value = minutes
        sharedPrefs.edit().putInt("reminder_before", minutes).apply()
    }

    fun incrementHydration() {
        val today = getTodayDateString()
        val current = _hydrationCount.value
        val next = if (current >= 8) 0 else current + 1
        _hydrationCount.value = next
        sharedPrefs.edit().putInt("hydration_count_$today", next).apply()
    }

    fun resetData() {
        viewModelScope.launch {
            // Cancel all scheduled alarms
            val all = repository.allMedicines.first()
            all.forEach { medicine ->
                ReminderScheduler.cancelReminder(getApplication(), medicine)
            }
            repository.resetAllData()
            Log.d("MedicineViewModel", "Wiped all medicine database data successfully.")
        }
    }

    // Utility methods for dates

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    fun getYesterdayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    fun formatDisplayDate(dateString: String): String {
        return try {
            val today = getTodayDateString()
            val yesterday = getYesterdayDateString()
            if (dateString == today) {
                "Today"
            } else if (dateString == yesterday) {
                "Yesterday"
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                if (date != null) outputFormat.format(date) else dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
