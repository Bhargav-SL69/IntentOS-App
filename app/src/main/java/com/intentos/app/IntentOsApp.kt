package com.intentos.app

import android.app.Application
import com.intentos.app.core.utils.CrashHandler
import com.intentos.app.core.utils.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IntentOsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Core Utilities
        Logger.setup()
        CrashHandler.setup()
        
        Logger.i("IntentOsApp", "IntentOS Application initialized.")
    }
}
