package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class MedicineReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getIntExtra("medicine_id", -1)
        val medicineName = intent.getStringExtra("medicine_name") ?: "Medicine"
        val medicineDosage = intent.getStringExtra("medicine_dosage") ?: "1 Dose"
        val medicineNotes = intent.getStringExtra("medicine_notes") ?: ""

        Log.d("MedicineReminder", "Triggered alarm for medicine: $medicineName")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medicare_reminders"

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled medicines"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action when user clicks the notification: Open MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicine_id", medicineId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medicineId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback standard system drawable for small icon
            .setContentTitle("💊 Time for your Medicine!")
            .setContentText("Take $medicineName ($medicineDosage) now.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time to take your $medicineName ($medicineDosage).\nNotes: $medicineNotes"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(medicineId, notification)
    }
}
