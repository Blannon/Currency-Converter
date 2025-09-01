package com.blannonnetwork.currencyconveter

import android.app.Application
import com.blannonnetwork.currencyconveter.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

    }
}