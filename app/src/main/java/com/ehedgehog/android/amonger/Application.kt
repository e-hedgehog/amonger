package com.ehedgehog.android.amonger

import android.app.Application
import com.ehedgehog.android.amonger.di.AppComponent
import com.ehedgehog.android.amonger.di.DaggerAppComponent
import com.ehedgehog.android.amonger.di.DataModule

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        val appComponent: AppComponent =
            DaggerAppComponent.builder()
                .dataModule(DataModule())
                .build()
    }

}