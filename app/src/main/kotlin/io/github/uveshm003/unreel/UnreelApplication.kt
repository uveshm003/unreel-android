package io.github.uveshm003.unreel

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UnreelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugTools.install()
    }
}
