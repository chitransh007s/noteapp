package com.cs007s.live.notes.application

import android.app.Application

/**
 * Created by Chitransh Shrivastav on 10/10/2020 for Notes
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {

        val TAG: String = BaseApplication::class.java
            .simpleName

        private var instance: BaseApplication? = null

        @Synchronized
        fun getInstance(): BaseApplication? {
            return instance
        }
    }
}
