package com.test.facerecognitionbyusbcamera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager

class AlarmLightDeviceReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val action = p1?.action
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
            println("TEST 有设备插入了。。。。")
            if (!hasAlarmDevice) {
                device = getUsbDevices()
                device?.let { flag = openPort(it) }
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
            println("TEST 有设备拔出了。。。。")
            if (getUsbDevices() == null) {
                println("TEST 报警灯被拔出了")
                hasAlarmDevice = false
                device = null
                flag = false
            }
        }
    }
}