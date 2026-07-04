package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_history")
data class MedicineHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val medicineName: String,
    val medicineType: String,
    val dosage: String,
    val time: String, // Scheduled time
    val status: String, // "Taken", "Missed"
    val date: String, // e.g. "2026-07-03"
    val timestamp: Long = System.currentTimeMillis()
)
