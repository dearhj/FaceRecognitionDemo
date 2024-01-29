package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.hardware.usb.UsbDevice
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.ICaptureCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.*
import com.jiangdg.ausbc.widget.*
import com.test.facerecognitionbyusbcamera.databinding.FragmentRegisterBinding
import java.util.*

class RegisterFragment : CameraFragment(), View.OnClickListener {
    private var mMoreMenu: PopupWindow? = null

    private lateinit var mViewBinding: FragmentRegisterBinding
    private lateinit var callBack: IPreviewDataCallBack

    override fun initView() {
        super.initView()
        callBack = Callback()
        mViewBinding.resolutionBtn.setOnClickListener(this)
        mViewBinding.changeBtn.setOnClickListener(this)
        mViewBinding.picBtn.setOnClickListener(this)
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(msg)
        }
    }

    private fun handleCameraError(msg: String?) {
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {
        ToastUtils.show("camera closed success")
        removePreviewDataCallBack(callBack)
    }

    private fun handleCameraOpened() {
        ToastUtils.show("camera opened success")
        addPreviewDataCallBack(callBack)
    }

    class Callback: IPreviewDataCallBack {
        override fun onPreviewData(
            data: ByteArray?,
            width: Int,
            height: Int,
            format: IPreviewDataCallBack.DataFormat
        ) {
            println("预览数据流  ${data?.size}    $width   $height  $format")
        }
    }



    override fun getCameraView(): IAspectRatio {
        return AspectRatioTextureView(requireContext())
    }

    override fun getCameraViewContainer(): ViewGroup {
        return mViewBinding.cameraViewContainer
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        mViewBinding = FragmentRegisterBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun getGravity(): Int = Gravity.CENTER

    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1920)
            .setPreviewHeight(1080)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .create()
    }

    override fun onClick(v: View?) {
        when (v) {
            mViewBinding.resolutionBtn -> {
                showResolutionDialog()
            }
            mViewBinding.changeBtn -> {
                getCurrentCamera()?.let { strategy ->
                    if (strategy is CameraUVC) {
                        showUsbDevicesDialog(getDeviceList(), strategy.getUsbDevice())
                        return
                    }
                }
            }
            mViewBinding.picBtn -> {
                captureImage(object : ICaptureCallBack{
                    override fun onBegin() {
                        println("开始拍照  ")
                    }

                    override fun onComplete(path: String?) {
                        println("拍照完成     $path")
                    }

                    override fun onError(error: String?) {
                        println("拍照失败。。。   $error")
                    }
                })
            }
            else -> {
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun showUsbDevicesDialog(usbDeviceList: MutableList<UsbDevice>?, curDevice: UsbDevice?) {
        if (usbDeviceList.isNullOrEmpty()) {
            ToastUtils.show("Get usb device failed")
            return
        }
        val list = arrayListOf<String>()
        var selectedIndex: Int = -1
        for (index in (0 until usbDeviceList.size)) {
            val dev = usbDeviceList[index]
            val devName = if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP && !dev.productName.isNullOrEmpty()) {
                "${dev.productName}(${curDevice?.deviceId})"
            } else {
                dev.deviceName
            }
            val curDevName = if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP && !curDevice?.productName.isNullOrEmpty()) {
                "${curDevice!!.productName}(${curDevice.deviceId})"
            } else {
                curDevice?.deviceName
            }
            if (devName == curDevName) {
                selectedIndex = index
            }
            list.add(devName)
        }
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                items = list,
                initialSelection = selectedIndex
            ) { dialog, index, text ->
                if (selectedIndex == index) {
                    return@listItemsSingleChoice
                }
                switchCamera(usbDeviceList[index])
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun showResolutionDialog() {
        mMoreMenu?.dismiss()
        getAllPreviewSizes().let { previewSizes ->
            if (previewSizes.isNullOrEmpty()) {
                ToastUtils.show("Get camera preview size failed")
                return
            }
            val list = arrayListOf<String>()
            var selectedIndex: Int = -1
            for (index in (0 until previewSizes.size)) {
                val w = previewSizes[index].width
                val h = previewSizes[index].height
                getCurrentPreviewSize()?.apply {
                    if (width == w && height == h) {
                        selectedIndex = index
                    }
                }
                list.add("$w x $h")
            }
            MaterialDialog(requireContext()).show {
                listItemsSingleChoice(
                    items = list,
                    initialSelection = selectedIndex
                ) { _, index, _ ->
                    if (selectedIndex == index) {
                        return@listItemsSingleChoice
                    }
                    updateResolution(previewSizes[index].width, previewSizes[index].height)
                    println("这里数值是多少?????   ${previewSizes[index].width}   +   ${previewSizes[index].height}")
                }
            }
        }
    }
}
