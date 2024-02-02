package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MyApplication: Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
        lateinit var savePath: String
        var takePhotoFlag = false
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        initAlarmLight()
    }
}