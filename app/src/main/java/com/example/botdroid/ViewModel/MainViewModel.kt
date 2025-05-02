package com.example.botoptimitationtest.ViewModel

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import com.example.botoptimitationtest.Broadcast.LaunchAppReceiver
import java.util.Calendar

class MainViewModel : ViewModel(){

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun ScheduleAppLaunch(context: Context) {
        val alamManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LaunchAppReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 4)
            set(Calendar.SECOND, 0)
        }

        alamManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}