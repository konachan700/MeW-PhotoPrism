package com.mewhpm.mewphotoprism.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mewhpm.mewphotoprism.utils.X_ACTION
import com.mewhpm.mewphotoprism.utils.X_ACTION_START

class UniversalBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val intentSvc = Intent(context, UniversalBackgroundService::class.java)
                intentSvc.putExtra(X_ACTION, X_ACTION_START)
                context!!.startForegroundService(intentSvc)
            }
            null -> {
                Log.w("BrReceiver", "Strange broadcast without intent")
            }
        }
    }
}