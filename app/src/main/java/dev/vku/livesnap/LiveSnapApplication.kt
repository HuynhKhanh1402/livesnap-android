package dev.vku.livesnap

import android.app.Application
import dev.vku.livesnap.data.AppContainer
import dev.vku.livesnap.data.DefaultAppContainer

class LiveSnapApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}