package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraActivity
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.test.facerecognitionbyusbcamera.ManageActivity.group_name
import com.test.facerecognitionbyusbcamera.ManageActivity.mFacePassHandler
import com.test.facerecognitionbyusbcamera.ViewUtil.getFaceImageByFaceTokenByUvc
import com.test.facerecognitionbyusbcamera.ViewUtil.showFacePassFaceByUvc
import com.test.facerecognitionbyusbcamera.databinding.ActivityRecognitionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mcv.facepass.FacePassException
import mcv.facepass.types.FacePassImage
import mcv.facepass.types.FacePassImageType
import mcv.facepass.types.FacePassLivenessMode
import mcv.facepass.types.FacePassRCAttribute
import mcv.facepass.types.FacePassRecogMode
import mcv.facepass.types.FacePassRecognitionState
import mcv.facepass.types.FacePassTrackOptions
import mcv.facepass.types.FacePassTrackResult
import java.util.concurrent.ArrayBlockingQueue


class RecognitionActivity : CameraActivity() {

    private lateinit var callBack: IPreviewDataCallBack

    companion object {
        private const val cameraWidth = 1280
        private const val cameraHeight = 720
        private var mAndroidHandler: Handler? = null

        @SuppressLint("StaticFieldLeak")
        var mContext: Context? = null
        var mDetectResultQueue: ArrayBlockingQueue<RecognizeData>? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var viewBinding: ActivityRecognitionBinding
        val runnableRed = Runnable { setRed() }
        val runnableGreen = Runnable { setGreen() }
        val runnableOff = Runnable { setOff() }
        var faceNum = 0
        var isOpen = false
    }

    private var mRecognizeThread: RecognizeThread? = null
    private var mFeedFrameThread: FeedFrameThread? = null

    override fun getCameraView(): IAspectRatio {
        return AspectRatioTextureView(this)
    }

    override fun getCameraViewContainer(): ViewGroup {
        return viewBinding.cameraViewContainer
    }

    override fun getRootView(layoutInflater: LayoutInflater): View {
        viewBinding = ActivityRecognitionBinding.inflate(layoutInflater)
        callBack = Callback()

        mDetectResultQueue = ArrayBlockingQueue<RecognizeData>(5)
        mAndroidHandler = Handler(Looper.getMainLooper())
        mFeedFrameThread = FeedFrameThread()
        mFeedFrameThread!!.start()
        mRecognizeThread = RecognizeThread()
        mRecognizeThread!!.start()
        mContext = this

        return viewBinding.root
    }

    override fun getGravity(): Int = Gravity.CENTER

    override fun getCameraRequest(): CameraRequest {
        // 5
        return CameraRequest.Builder()
            .setPreviewWidth(cameraWidth)
            .setPreviewHeight(cameraHeight)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .create()
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
        println("这里摄像头出错了")
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {
        println("这里摄像头关闭了")
        ToastUtils.show("camera closed success")
        removePreviewDataCallBack(callBack)
    }

    private fun handleCameraOpened() {
        println("这里摄像头打开了")
        isOpen = true
        faceNum = mFacePassHandler.getLocalGroupInfo(group_name).size
        ToastUtils.show("camera opened success")
        addPreviewDataCallBack(callBack)
    }

    override fun onDestroy() {
        isOpen = false
        setOff()
        mRecognizeThread!!.isInterrupt = true
        mFeedFrameThread!!.isInterrupt = true
        viewBinding.faceView.clear()
        viewBinding.faceView.invalidate()
        mFacePassHandler.reset()
        mDetectResultQueue!!.clear()
        ComplexFrameHelper.complexUvcFrameQueue.clear()
        if (mAndroidHandler != null) {
            mAndroidHandler!!.removeCallbacksAndMessages(null)
        }
        super.onDestroy()
    }


    class Callback : IPreviewDataCallBack {
        override fun onPreviewData(
            data: ByteArray?,
            width: Int,
            height: Int,
            format: IPreviewDataCallBack.DataFormat
        ) {
            MainScope().launch(Dispatchers.IO) {
                ComplexFrameHelper.addUvcRgbFrame(data)
            }
        }
    }


    class FeedFrameThread : Thread() {
        var isInterrupt = false
        override fun run() {
            while (!isInterrupt) {
                if (ManageActivity.FacePassHandlerHasInit) {
                    val framePair: ByteArray? = try {
                        ComplexFrameHelper.takeComplexUvcFrame()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        continue
                    }
                    if (mFacePassHandler == null) {
                        println("识别中  mFacePassHandler == null !")
                        continue
                    }
                    /* 将相机预览帧转成SDK算法所需帧的格式 FacePassImage */

                    val imageRGB: FacePassImage = try {
                        FacePassImage(
                            framePair,
                            cameraWidth,
                            cameraHeight,
                            0,
                            FacePassImageType.NV21
                        )
                    } catch (e: FacePassException) {
                        e.printStackTrace()
                        continue
                    }

                    /* 将每一帧FacePassImage 送入SDK算法， 并得到返回结果 */
                    var detectionResult: FacePassTrackResult? = null
                    try {
                        detectionResult = mFacePassHandler.feedFrame(imageRGB)
                    } catch (e: FacePassException) {
                        e.printStackTrace()
                    }

                    if (detectionResult == null || detectionResult.trackedFaces.isEmpty()) {

                        /* 当前帧没有检出人脸 */
                        MainScope().launch(Dispatchers.Main) {
                            viewBinding.faceView.clear()
                            viewBinding.faceView.invalidate()
                        }
                        mAndroidHandler?.postDelayed(runnableOff, 500)
                    } else if (isOpen) {
                        /* 将识别到的人脸在预览界面中圈出，并在上方显示人脸位置及角度信息 */
                        val bufferFaceList = detectionResult.trackedFaces
                        (mContext as Activity).runOnUiThread {
                            showFacePassFaceByUvc(bufferFaceList, viewBinding.faceView)
                        }
                    }

                    /*离线模式，将识别到人脸的，message不为空的result添加到处理队列中*/
                    if (detectionResult != null) {
                        /*所有检测到的人脸框的属性信息*/
                        Log.d(
                            "识别中",
                            "--------------------------------------------------------------------------------------------------------------------------------------------------"
                        )
                        println("人脸识别中，detectionResult.message是否不为空 " + (detectionResult.message.isNotEmpty()))
                        if (detectionResult.message.isNotEmpty() && isOpen) {
                            /*送识别的人脸框的属性信息*/
                            val trackOpts =
                                arrayOfNulls<FacePassTrackOptions>(detectionResult.images.size)
                            for (i in detectionResult.images.indices) {
                                if (detectionResult.images[i].rcAttr.respiratorType != FacePassRCAttribute.FacePassRespiratorType.NO_RESPIRATOR) {
                                    val searchThreshold = 60f
                                    val livenessThreshold =
                                        75f // -1.0f will not change the liveness threshold
                                    trackOpts[i] = FacePassTrackOptions(
                                        detectionResult.images[i].trackId,
                                        searchThreshold,
                                        livenessThreshold
                                    )
                                } else {
                                    trackOpts[i] = FacePassTrackOptions(
                                        detectionResult.images[i].trackId,
                                        -1f,
                                        -1f
                                    )
                                }
                            }
                            Log.d(
                                "识别中",
                                "mRecognizeDataQueue.offer(mRecData);"
                            )
                            val mRecData = RecognizeData(
                                detectionResult.message,
                                trackOpts
                            )
                            mDetectResultQueue?.offer(mRecData)
                        }
                    }
                }
            }
        }
    }


    private class RecognizeThread : Thread() {
        var isInterrupt = false
        override fun run() {
            while (!isInterrupt) {
                try {
                    println("人脸识别中，开始执行识别线程。。")
                    val recognizeData: RecognizeData = mDetectResultQueue!!.take()
                    println("人脸识别中，，，，，， MyApplication.isLocalGroupExist " + ManageActivity.isLocalGroupExist)
                    if (ManageActivity.isLocalGroupExist) {

                        val liveNessResult = recognizeData.trackOpt!![0]?.let {
                            mFacePassHandler.livenessClassify(recognizeData.message, it.trackId, FacePassLivenessMode.FP_REG_MODE_LIVENESS, it.livenessThreshold)
                        }
                        var liveNessStat = " Unkonw"
                        if (!liveNessResult.isNullOrEmpty() && isOpen) {
                            for(result in liveNessResult) {
                                if(result.livenessState == 0) liveNessStat = "LIVENESS_PASS"
                                if(result.livenessState == 1) liveNessStat = "LIVENESS_RETRY"
                                if(result.livenessState == 2) liveNessStat = "LIVENESS_RETRY_EXPIRED"
                                if(result.livenessState == 3) liveNessStat = "LIVENESS_TRACK_MISSING"
                                if(result.livenessState == 4) liveNessStat = "LIVENESS_UNPASS"
                                println("这里的活体结果是？？？？ " + result.livenessState)
                            }
                        }
                        if(liveNessStat == "LIVENESS_PASS" && isOpen) {
                            if (faceNum == 0) {
                                mAndroidHandler?.removeCallbacks(runnableOff)
                                mAndroidHandler?.removeCallbacks(runnableRed)
                                mAndroidHandler?.postDelayed(runnableRed, 500)
                            }
                            val recognizeResult = recognizeData.trackOpt?.get(0)?.let {
                                mFacePassHandler.recognize(
                                    group_name,
                                    recognizeData.message,
                                    1,
                                    FacePassRecogMode.FP_REG_MODE_FEAT_COMP
                                )
                            }
                            println("人脸识别中，这里的值是 ${recognizeData.trackOpt!![0]!!.livenessThreshold}    ${recognizeData.trackOpt!![0]!!.searchThreshold}")
                            //这里有人脸送入识别，准备亮红灯。
                            if (!recognizeResult.isNullOrEmpty() && isOpen) {
                                for (result in recognizeResult) {
                                    if (null == result.faceToken) {
                                        Log.d("人脸识别中", "result.faceToken is null.")
                                        continue
                                    }
                                    println("人脸识别中？？？？  有识别结果。。。。 result.recognitionState  " + result.recognitionState)
                                    val faceToken = String(result.faceToken)
                                    Log.d(
                                        "人脸识别中",
                                        "FacePassRecognitionState.RECOGNITION_PASS = " + result.recognitionState
                                    )
                                    if (result.recognitionState != FacePassRecognitionState.RECOGNITION_PASS && result.recognitionState != FacePassRecognitionState.RECOGNITION_RETRY && isOpen) {
                                        mAndroidHandler?.removeCallbacks(runnableOff)
                                        mAndroidHandler?.post(runnableRed)
                                    }
                                    if (FacePassRecognitionState.RECOGNITION_PASS == result.recognitionState && isOpen) {
                                        //识别成功，亮绿灯
                                        mAndroidHandler?.removeCallbacks(runnableOff)
                                        mAndroidHandler?.post(runnableGreen)
                                        (mContext as Activity).runOnUiThread {
                                            getFaceImageByFaceTokenByUvc(
                                                mContext,
                                                mContext as Activity,
                                                faceToken
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: FacePassException) {
                    e.printStackTrace()
                }
            }
        }
    }

    class RecognizeData(var message: ByteArray, opt: Array<FacePassTrackOptions?>) {
        var trackOpt: Array<FacePassTrackOptions?>? = opt
    }

}