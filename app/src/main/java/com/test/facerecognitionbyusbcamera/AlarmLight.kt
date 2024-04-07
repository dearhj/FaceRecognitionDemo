package com.test.facerecognitionbyusbcamera

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager


private var manager: UsbManager? = null
var device: UsbDevice? = null
private lateinit var usbInterface: UsbInterface
private lateinit var usbConnection: UsbDeviceConnection
private lateinit var usbEndpointIn: UsbEndpoint
private lateinit var usbEndpointOut: UsbEndpoint
var flag = false
var hasAlarmDevice = false

fun String.hexToByteArray(): ByteArray {
    var hex = replace(" ", "")
    if (hex.length % 2 != 0) hex = "0${hex}"
    val result = ByteArray(hex.length / 2)
    for (i in result.indices) {
        val index = i * 2
        val hexByte = hex.substring(index, index + 2)
        result[i] = hexByte.toInt(16).toByte()
    }
    return result
}

fun initAlarmLight(context: Context) {
    if (manager == null) {
        manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        device = getUsbDevices()
        device?.let { flag = openPort(it) }
    }
}

fun setRed() {
    val cmd = "FF040101AA".hexToByteArray()
//    val cmd = "CMD=1;PM=6+FF0000+FF0000+FF0000+FF0000+FF0000+FF0000".toByteArray()
    checkDevice()
    if (flag) usbConnection.bulkTransfer(usbEndpointOut, cmd, cmd.size, 500)
}

fun setBeep() {
    val cmd = "BEEP=1+5000+15+200+500".toByteArray()
    checkDevice()
    if (flag) usbConnection.bulkTransfer(usbEndpointOut, cmd, cmd.size, 500)
}

fun setBeepOff() {
    val cmd = "BEEP=0".toByteArray()
    checkDevice()
    if (flag) usbConnection.bulkTransfer(usbEndpointOut, cmd, cmd.size, 500)
}

fun setGreen() {
    val cmd = "FF020101AA".hexToByteArray()
//    val cmd = "CMD=1;PM=6+00FF00+00FF00+00FF00+00FF00+00FF00+00FF00".toByteArray()
    checkDevice()
    if (flag) usbConnection.bulkTransfer(usbEndpointOut, cmd, cmd.size, 500)
}

fun setOff() {
//    val cmd = "CMD=0".toByteArray()
    val cmd = "FF010101AA".hexToByteArray()
    checkDevice()
    if (flag) usbConnection.bulkTransfer(usbEndpointOut, cmd, cmd.size, 500)
}

private fun checkDevice() {
    if (device != null) {
        if (!flag) device?.let { flag = openPort(it) }
    } else {
        device = getUsbDevices()
        device?.let { flag = openPort(it) }
    }
}

fun getUsbDevices(): UsbDevice? {
    val deviceList = manager?.deviceList
    deviceList?.forEach {
        if (it.value.productId == 29987 && it.value.vendorId == 6790) {
            hasAlarmDevice = true
            return deviceList[it.value.deviceName]
        }
    }
    return null
}

private fun hasPermission(device: UsbDevice?): Boolean? {
    return manager?.hasPermission(device)
}

fun openPort(device: UsbDevice): Boolean {
    usbInterface = device.getInterface(0)

    if (hasPermission(device)!!) {
        usbConnection = manager!!.openDevice(device)
        if (usbConnection.claimInterface(usbInterface, true)) {
            println("TEST 找到了设备接口")
        } else {
            usbConnection.close()
            println("TEST 很遗憾，没有找到设备接口")
            return false
        }
    } else {
        println("TEST 很遗憾，没有USB权限")
        return false
    }
    for (i in 0 until usbInterface.endpointCount) {
        val end: UsbEndpoint = usbInterface.getEndpoint(i)
        if (end.direction == UsbConstants.USB_DIR_IN) {
            usbEndpointIn = end
            println("TEST 找到发送节点")
        } else {
            usbEndpointOut = end
            println("TEST 找到接收节点")
        }
    }
    return true
}

