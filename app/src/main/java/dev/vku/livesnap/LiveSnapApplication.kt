package dev.vku.livesnap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.vku.livesnap.data.AppContainer
import dev.vku.livesnap.data.DefaultAppContainer

@HiltAndroidApp
class LiveSnapApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}