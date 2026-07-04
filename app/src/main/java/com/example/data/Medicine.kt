package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val time: String, // e.g. "08:00 AM"
    val type: String, // e.g. "Tablet", "Capsule", "Syrup", "Injection"
    val notes: String = "",
    val status: String = "Pending", // "Pending", "Taken"
    val lastUpdatedDate: String = "" // e.g. "2026-07-03" to reset status daily
)
