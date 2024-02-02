package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Typeface
import android.hardware.usb.UsbDevice
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.JSONPath
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.*
import com.jiangdg.ausbc.widget.*
import com.rockchip.iva.RockIva
import com.rockchip.iva.RockIvaCallback
import com.rockchip.iva.RockIvaImage
import com.rockchip.iva.RockIvaImage.TransformMode
import com.rockchip.iva.face.RockIvaFaceFeature
import com.rockchip.iva.face.RockIvaFaceInfo
import com.rockchip.iva.face.RockIvaFaceLibrary
import com.test.facerecognitionbyusbcamera.databinding.FragmentRecognitionBinding
import com.test.facerecognitionbyusbcamera.utils.ImageBufferQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class RecognitionFragment : CameraFragment(), View.OnClickListener {
    private var mMoreMenu: PopupWindow? = null

    private lateinit var mViewBinding: FragmentRecognitionBinding
    private lateinit var callBack: IPreviewDataCallBack

    private var mTrackedFaceArray: SparseArray<FaceResult>? = null
    private var ivaFaceManager: IvaFaceManager? = null
    private var ivaFaceLibrary: RockIvaFaceLibrary? = null

    private var mTrackResultBitmap: Bitmap? = null
    private var mTrackResultCanvas: Canvas? = null
    private var mTrackResultPaint: Paint? = null
    private var mTrackResultTextPaint: Paint? = null

    private var mPorterDuffXfermodeClear: PorterDuffXfermode? = null
    private var mPorterDuffXfermodeSRC: PorterDuffXfermode? = null

    companion object {
        var mActivity: FragmentActivity? = null
        var bufferQueue: ImageBufferQueue? = null
        var rockIva: RockIva? = null
    }

    override fun initView() {
        // 4
        super.initView()
        mActivity = activity
        callBack = Callback()
        mViewBinding.changeBtn.setOnClickListener(this)

        //初始化库
        mTrackedFaceArray = SparseArray()
        ivaFaceManager = IvaFaceManager(MyApplication.mContext)
        ivaFaceManager?.initForRecog()
        ivaFaceManager?.setCallback(mIvaCallback)
        bufferQueue = ivaFaceManager?.bufferQueue
        rockIva = ivaFaceManager?.iva
        ivaFaceLibrary = ivaFaceManager?.ivaFaceLibrary
    }

    override fun onDestroy() {
        super.onDestroy()
        ivaFaceManager?.release()
        bufferQueue = null
        rockIva = null
        ivaFaceLibrary = null
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        // 6
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

    class Callback : IPreviewDataCallBack {
        private var frameId = 0
        override fun onPreviewData(
            data: ByteArray?,
            width: Int,
            height: Int,
            format: IPreviewDataCallBack.DataFormat
        ) {
            MainScope().launch(Dispatchers.IO) {
                if (bufferQueue != null || rockIva != null) {
                    frameId += 1
                    val imageBuffer = bufferQueue?.freeBuffer
                    if (imageBuffer != null) {
                        imageBuffer.mImage.setImageData(data)
                        imageBuffer.mImage.frameId = frameId
                        rockIva?.pushFrame(imageBuffer.mImage)
                        bufferQueue?.postBuffer(imageBuffer)
                    }
                }
                println("预览数据流  ${data?.size}  $width   $height  $format")
            }
        }
    }


    override fun getCameraView(): IAspectRatio {
        // 2
        return AspectRatioTextureView(requireContext())
    }

    override fun getCameraViewContainer(): ViewGroup {
        // 3
        return mViewBinding.cameraViewContainer
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        // 1
        mViewBinding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun getGravity(): Int = Gravity.CENTER

    override fun getCameraRequest(): CameraRequest {
        // 5
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


    val handler = Handler(Looper.getMainLooper())
    val runnableRed = Runnable { setRed() }
    val runnableGreen = Runnable { setGreen() }
    val runnableOff = Runnable { setOff() }
    val runnableBeep = Runnable { setBeep() }
    val runnableBeepOff = Runnable { setBeepOff() }
    var canLightRed = true
    var canLightOff = true
    var canLightGreen = true
    var hasFaceVerified = false
    var listFaceOfCheckSuccess = mutableListOf<Int>()
    private var mIvaCallback: RockIvaCallback = object : RockIvaCallback {
        override fun onResultCallback(result: String, execureState: Int) {
            Log.d(Configs.TAG, "$result  execureState =  $execureState")
            val jobj = JSONObject.parseObject(result)
            val faceList = ArrayList<RockIvaFaceInfo>()
            if (JSONPath.contains(jobj, "$.faceDetResult")) {
                val num = JSONPath.eval(jobj, "$.faceDetResult.objNum") as Int
                println("MHJZ    这里识别到人脸了吗？？？？  $num ")
                if (num == 0) listFaceOfCheckSuccess.clear()
                hasFaceVerified = false
                for (i in 0 until num) {
                    val faceInfoJobj =
                        JSONPath.eval(jobj, String.format("$.faceDetResult.faceInfo[%d]", i))
                    val faceInfoId = JSONPath.eval(faceInfoJobj, "$.objId") as Int
                    println("")
                    if (listFaceOfCheckSuccess.size != 0) {
                        if (listFaceOfCheckSuccess.contains(faceInfoId)) hasFaceVerified = true
                    }
                    println("MHJZ   是否有人脸通过认证？ $hasFaceVerified   通过认证的数量？   ${if (listFaceOfCheckSuccess.size != 0) listFaceOfCheckSuccess.size else 0}")
                    val faceInfo = JSONObject.parseObject(
                        faceInfoJobj.toString(),
                        RockIvaFaceInfo::class.java
                    )
                    faceList.add(faceInfo)
                }
                //当检测到人脸且没有人脸通过识别，亮红灯
                if (num != 0 && !hasFaceVerified && canLightRed) {
                    canLightRed = false
                    canLightOff = true
                    canLightGreen = true
                    handler.removeCallbacks(runnableGreen)
                    handler.removeCallbacks(runnableOff)
                    handler.postDelayed(runnableRed, 300)
                }
                //当检测到人脸且其中有人脸通过识别，亮绿灯
                if (hasFaceVerified && canLightGreen) {
                    canLightGreen = false
                    canLightOff = true
                    canLightRed = true
                    handler.removeCallbacks(runnableRed)
                    handler.removeCallbacks(runnableOff)
                    handler.postDelayed(runnableGreen, 300)
                }
                //当没有检测到人脸时，熄灯
                if (num == 0 && canLightOff) {
                    canLightOff = false
                    canLightRed = true
                    canLightGreen = true
                    handler.removeCallbacks(runnableRed)
                    handler.removeCallbacks(runnableGreen)
                    handler.postDelayed(runnableOff, 300)
                }
                updateCurFaceList(faceList)
                checkFaceRecognition()
                MainScope().launch(Dispatchers.Main) { showResults() }
            } else if (JSONPath.contains(jobj, "$.faceCapResults")) {
                val num = JSONPath.eval(jobj, "$.faceCapResults.num") as Int
                for (i in 0 until num) {
                    val faceCapResultObj = JSONPath.eval(
                        jobj,
                        String.format("$.faceCapResults.faceResults[%d]", i)
                    ) as JSONObject
                    val qualityResult = JSONPath.eval(faceCapResultObj, "$.qualityResult") as Int
                    if (qualityResult == 0) {
                        val featureStr =
                            JSONPath.eval(faceCapResultObj, "$.faceAnalyseInfo.feature") as String
                        val faceFeature = RockIvaFaceFeature(featureStr)
                        val searchResults = ivaFaceLibrary!!.search(faceFeature, 5)
                        if (searchResults != null) {
                            val id = JSONPath.eval(faceCapResultObj, "$.faceInfo.objId") as Int
                            for (searchResult in searchResults) {
                                println("MHJZ  执行这里了吗？？？？？找到人脸信息，，，，   ${searchResult.faceId}    ${searchResult.score}")
                            }
                            if (searchResults.size > 0 && searchResults[0].score > Configs.IVA_FACE_RECOG_SCORE_THRESHOLD) {

                                listFaceOfCheckSuccess.add(id)

                                val trackedFace = mTrackedFaceArray!![id]
                                trackedFace?.setName(
                                    searchResults[0].faceId,
                                    searchResults[0].score
                                )
                            }
                        }
                    }
                }
            }
        }

        override fun onReleaseCallback(images: List<RockIvaImage>) {
            for (image in images) {
                bufferQueue?.releaseBuffer(image)
            }
        }
    }

    private fun updateCurFaceList(faceInfoList: List<RockIvaFaceInfo>) {
        val newFaceList = SparseArray<FaceResult>()
        for (faceInfo in faceInfoList) {
            val trackId = faceInfo.objId
            var face = mTrackedFaceArray!![trackId]
            if (face == null) face = FaceResult()
            face.faceInfo = faceInfo
            newFaceList.append(trackId, face)
        }
        mTrackedFaceArray = newFaceList
    }

    private fun checkFaceRecognition() {
        for (i in 0 until mTrackedFaceArray!!.size()) {
            val n = mTrackedFaceArray!!.keyAt(i)
            val face = mTrackedFaceArray!![n]
            if (face.name == null || face.name.isEmpty()) {
                val faceInfo = face.faceInfo
                if (faceInfo != null) {
                    if (faceInfo.faceQuality.score > 60) {
                        rockIva?.setAnalyseFace(face.faceInfo.objId)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
//            mViewBinding.resolutionBtn -> {
//                showResolutionDialog()
//            }

            mViewBinding.changeBtn -> {
                getCurrentCamera()?.let { strategy ->
                    if (strategy is CameraUVC) {
                        showUsbDevicesDialog(getDeviceList(), strategy.getUsbDevice())
                        return
                    }
                }
            }

            else -> {
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun showUsbDevicesDialog(
        usbDeviceList: MutableList<UsbDevice>?,
        curDevice: UsbDevice?
    ) {
        if (usbDeviceList.isNullOrEmpty()) {
            ToastUtils.show("Get usb device failed")
            return
        }
        val list = arrayListOf<String>()
        var selectedIndex: Int = -1
        for (index in (0 until usbDeviceList.size)) {
            val dev = usbDeviceList[index]
            val devName = if (!dev.productName.isNullOrEmpty()) {
                "${dev.productName}(${curDevice?.deviceId})"
            } else dev.deviceName
            val curDevName = if (!curDevice?.productName.isNullOrEmpty()) {
                "${curDevice!!.productName}(${curDevice.deviceId})"
            } else curDevice?.deviceName
            if (devName == curDevName) selectedIndex = index
            list.add(devName)
        }
        MaterialDialog(requireContext()).show {
            listItemsSingleChoice(
                items = list,
                initialSelection = selectedIndex
            ) { _, index, _ ->
                if (selectedIndex == index) return@listItemsSingleChoice
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
                    if (width == w && height == h) selectedIndex = index
                }
                list.add("$w x $h")
            }
            MaterialDialog(requireContext()).show {
                listItemsSingleChoice(
                    items = list,
                    initialSelection = selectedIndex
                ) { _, index, _ ->
                    if (selectedIndex == index) return@listItemsSingleChoice
                    updateResolution(previewSizes[index].width, previewSizes[index].height)
                }
            }
        }
    }

    private fun sp2px(spValue: Float): Int {
        val r = Resources.getSystem()
        val scale = r.displayMetrics.scaledDensity
        return (spValue * scale + 0.5f).toInt()
    }

    @SuppressLint("DefaultLocale")
    private fun showResults() {
        val width = mViewBinding.canvasView.width
        val height = mViewBinding.canvasView.height
        if (mTrackResultBitmap == null) {
            mTrackResultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mTrackResultCanvas = Canvas(mTrackResultBitmap!!)

            //用于画线
            mTrackResultPaint = Paint()
            mTrackResultPaint?.color = Color.YELLOW
            mTrackResultPaint?.strokeJoin = Paint.Join.ROUND
            mTrackResultPaint?.strokeCap = Paint.Cap.ROUND
            mTrackResultPaint?.strokeWidth = 3f
            mTrackResultPaint?.style = Paint.Style.STROKE
            mTrackResultPaint?.textAlign = Paint.Align.LEFT
            mTrackResultPaint?.textSize = sp2px(10f).toFloat()
            mTrackResultPaint?.setTypeface(Typeface.SANS_SERIF)
            mTrackResultPaint?.isFakeBoldText = false

            //用于文字
            mTrackResultTextPaint = Paint()
            mTrackResultTextPaint?.color = -0xf91401
            mTrackResultTextPaint?.strokeWidth = 2f
            mTrackResultTextPaint?.textAlign = Paint.Align.LEFT
            mTrackResultTextPaint?.textSize = sp2px(20f).toFloat()
            mTrackResultTextPaint?.setTypeface(Typeface.SANS_SERIF)
            mTrackResultTextPaint?.isFakeBoldText = false
            mPorterDuffXfermodeClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            mPorterDuffXfermodeSRC = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }

        // clear canvas
        mTrackResultPaint?.setXfermode(mPorterDuffXfermodeClear)
        mTrackResultCanvas?.drawPaint(mTrackResultPaint!!)
        mTrackResultPaint?.setXfermode(mPorterDuffXfermodeSRC)

        //detect result
        val faceList = mTrackedFaceArray!!.clone()
        if (faceList.size() > 0) {
            for (n in 0 until faceList.size()) {
                val key = faceList.keyAt(n)
                val face = faceList[key]
                val mode = TransformMode.NONE
                val drawRect =
                    RockIva.convertRectRatioToPixel(width, height, face.faceInfo.faceRect, mode)
                mTrackResultCanvas?.drawRect(drawRect, mTrackResultPaint!!)
                var drawStr = ""
                if (face.name != null && face.name.isNotEmpty()) {
                    drawStr += String.format("姓名:%s", face.name)
                }
                mTrackResultCanvas!!.drawText(
                    drawStr, drawRect.left.toFloat(),
                    (drawRect.top - 20).toFloat(), mTrackResultTextPaint!!
                )
            }
        }
        mViewBinding.canvasView.scaleType = ImageView.ScaleType.FIT_XY
        mViewBinding.canvasView.setImageBitmap(mTrackResultBitmap)
    }
}
