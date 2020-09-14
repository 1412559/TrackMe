package com.toantran.trackme

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        mApplication = this
    }

    companion object {
        private lateinit var  mApplication: Context
        fun getInstance() = mApplication
    }

}