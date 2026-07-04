package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.Medicine
import com.example.receiver.MedicineReminderReceiver
import java.util.Calendar

object ReminderScheduler {

    fun scheduleReminder(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MedicineReminderReceiver::class.java).apply {
            putExtra("medicine_id", medicine.id)
            putExtra("medicine_name", medicine.name)
            putExtra("medicine_dosage", medicine.dosage)
            putExtra("medicine_notes", medicine.notes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = parseTimeToCalendar(medicine.time) ?: return

        // If the scheduled time is in the past for today, schedule it for tomorrow
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        Log.d("ReminderScheduler", "Scheduling reminder for ${medicine.name} at ${calendar.time} (epoch: ${calendar.timeInMillis})")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Failed to schedule exact alarm: ${e.message}. Falling back to non-exact.")
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("ReminderScheduler", "Cancelled reminder for ${medicine.name}")
        }
    }

    private fun parseTimeToCalendar(timeString: String): Calendar? {
        try {
            // Sample timeString: "08:00 AM" or "09:30 PM"
            val parts = timeString.trim().split(" ")
            if (parts.size != 2) return null
            val timeParts = parts[0].split(":")
            if (timeParts.size != 2) return null

            var hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val isPm = parts[1].equals("PM", ignoreCase = true)

            if (isPm && hour < 12) {
                hour += 12
            } else if (!isPm && hour == 12) {
                hour = 0
            }

            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error parsing time string $timeString: ${e.message}")
            return null
        }
    }
}
