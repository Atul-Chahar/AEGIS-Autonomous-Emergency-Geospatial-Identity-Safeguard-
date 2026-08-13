package com.example.aegis.sensors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryInfoProvider(private val context: Context) {
  fun getBatteryPercent(): Int {
    return try {
      val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      val batteryStatus = context.registerReceiver(null, intentFilter)
      val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
      val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

      if (level >= 0 && scale > 0) {
        ((level / scale.toFloat()) * 100).toInt()
      } else {
        100
      }
    } catch (e: Exception) {
      100
    }
  }
}
