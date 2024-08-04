package com.example.memry.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.memry.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val title = intent.getStringExtra("title") ?: "Recordatorio"
        //val reason = intent.getStringExtra("reason") ?: "fecha especial"
        showNotification(context, title)
    }

    private fun showNotification(
        context: Context,
        title : String
    ) {
        val channelId = "alarm_channel_id"
        val notificationId = 1

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_alarm)  // Icono para la notificación
            .setContentTitle(title)
            .setContentText("¡No te olvides de una fecha tan importante!")
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
