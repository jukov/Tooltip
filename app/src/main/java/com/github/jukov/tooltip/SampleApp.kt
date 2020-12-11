package com.github.jukov.tooltip

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SampleApp: Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}