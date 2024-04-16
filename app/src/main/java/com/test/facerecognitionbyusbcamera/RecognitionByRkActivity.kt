package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.test.facerecognitionbyusbcamera.ManageActivity.FacePassHandlerHasInit
import com.test.facerecognitionbyusbcamera.ManageActivity.group_name
import com.test.facerecognitionbyusbcamera.ManageActivity.isLocalGroupExist
import com.test.facerecognitionbyusbcamera.ManageActivity.mFacePassHandler
import com.test.facerecognitionbyusbcamera.RkCameraManager.CameraListener
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

class RecognitionByRkActivity : AppCompatActivity(), CameraListener {
    private var mSurfaceView: SurfaceView? = null
    private var mCameraManager: CameraManager? = null

    companion object {
        private var mAndroidHandler: Handler? = null

        @SuppressLint("StaticFieldLeak")
        var mContext: Context? = null
        var mFaceView: FaceView? = null
        var mDetectResultQueue: ArrayBlockingQueue<RecognizeData>? = null

        val runnableRed = Runnable { setRed() }
        val runnableGreen = Runnable { setGreen() }
        val runnableOff = Runnable { setOff() }
        var faceNum = 0
        var isOpen = false
    }

    private var mRecognizeThread: RecognizeThread? = null
    private var mFeedFrameThread: FeedFrameThread? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_recognition_by_rk)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mSurfaceView = findViewById(R.id.surfaceViewCamera1)
        mFaceView = findViewById(R.id.faceView)
        mCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val list = mCameraManager!!.cameraIdList
            for (id in list) {
                println("这里获取到的id是？   $id")
            }
            if (list.isNotEmpty()) {
                val rkCameraManager = RkCameraManager.getInstance(this)
                rkCameraManager.initCamera(list[0], mSurfaceView)
                rkCameraManager.setListener(this)
                isOpen = true
                faceNum = mFacePassHandler.getLocalGroupInfo(group_name).size

                mDetectResultQueue = ArrayBlockingQueue<RecognizeData>(5)
                mAndroidHandler = Handler(Looper.getMainLooper())
                mFeedFrameThread = FeedFrameThread()
                mFeedFrameThread!!.start()
                mRecognizeThread = RecognizeThread()
                mRecognizeThread!!.start()
                mContext = this
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        isOpen = false
        setOff()
        mRecognizeThread?.isInterrupt = true
        mFeedFrameThread?.isInterrupt = true
        mFaceView?.clear()
        mFaceView?.invalidate()
        mFacePassHandler.reset()
        mDetectResultQueue?.clear()
        ComplexFrameHelper.complexUvcFrameQueue.clear()
        if (mAndroidHandler != null) {
            mAndroidHandler?.removeCallbacksAndMessages(null)
        }
        super.onDestroy()
    }


    override fun onCameraPreviewData(cameraPreviewData: ByteArray) {
        MainScope().launch(Dispatchers.IO) {
            ComplexFrameHelper.addUvcRgbFrame(cameraPreviewData)
        }
    }


    private class FeedFrameThread : Thread() {
        var isInterrupt = false
        override fun run() {
            while (!isInterrupt) {
                if (FacePassHandlerHasInit) {
                    val framePair: ByteArray? = try {
                        ComplexFrameHelper.takeComplexUvcFrame()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        continue
                    }
                    if (mFacePassHandler == null) {
                        continue
                    }
                    /* 将相机预览帧转成SDK算法所需帧的格式 FacePassImage */

                    val imageRGB: FacePassImage = try {
                        FacePassImage(
                            framePair,
                            1280,
                            720,
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
                            mFaceView?.clear()
                            mFaceView?.invalidate()
                        }
                        mAndroidHandler?.postDelayed(runnableOff, 500)
                    } else if (isOpen) {
                        /* 将识别到的人脸在预览界面中圈出，并在上方显示人脸位置及角度信息 */
                        val bufferFaceList = detectionResult.trackedFaces
                        (mContext as Activity).runOnUiThread {
                            ViewUtil.showFacePassFaceByUvc(
                                bufferFaceList,
                                mFaceView
                            )
                        }
                    }

                    /*离线模式，将识别到人脸的，message不为空的result添加到处理队列中*/
                    if (detectionResult != null) {
                        /*所有检测到的人脸框的属性信息*/
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
                    val recognizeData: RecognizeData = mDetectResultQueue!!.take()
                    if (isLocalGroupExist) {

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
                            //这里有人脸送入识别，准备亮红灯。
                            if (!recognizeResult.isNullOrEmpty() && isOpen) {
                                for (result in recognizeResult) {
                                    if (null == result.faceToken) {
                                        Log.d("人脸识别中", "result.faceToken is null.")
                                        continue
                                    }
                                    println("人脸识别中？？？？  有识别结果。。。。 result.recognitionState  " + result.recognitionState)
                                    val faceToken = String(result.faceToken)
                                    if (result.recognitionState != FacePassRecognitionState.RECOGNITION_PASS && result.recognitionState != FacePassRecognitionState.RECOGNITION_RETRY && isOpen) {
                                        mAndroidHandler?.removeCallbacks(runnableOff)
                                        mAndroidHandler?.post(runnableRed)
                                    }
                                    if (FacePassRecognitionState.RECOGNITION_PASS == result.recognitionState && isOpen) {
                                        //识别成功，亮绿灯
                                        mAndroidHandler?.removeCallbacks(runnableOff)
                                        mAndroidHandler?.post(runnableGreen)
                                        (mContext as Activity).runOnUiThread {
                                            ViewUtil.getFaceImageByFaceTokenByUvc(
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
