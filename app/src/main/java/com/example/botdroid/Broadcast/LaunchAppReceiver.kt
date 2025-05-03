package com.example.botoptimitationtest.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class LaunchAppReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.tiktrend.goviral")
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Log.e("LaunchAppReceiver", "No se encontró la app a lanzar")
        }
    }
}