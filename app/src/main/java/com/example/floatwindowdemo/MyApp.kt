package com.example.floatwindowdemo

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppFrontBackHelper.instance.init(this@MyApp)
    }
}