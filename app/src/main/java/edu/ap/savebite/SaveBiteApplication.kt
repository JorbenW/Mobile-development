package edu.ap.savebite

import android.app.Application
import edu.ap.savebite.data.AppContainer

class SaveBiteApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
