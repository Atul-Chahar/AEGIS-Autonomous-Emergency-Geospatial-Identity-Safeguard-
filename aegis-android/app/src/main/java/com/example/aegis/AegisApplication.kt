package com.example.aegis

import android.app.Application

class AegisApplication : Application() {

  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    container = AppContainer(this)
    container.scheduleBackgroundWork()
  }
}
