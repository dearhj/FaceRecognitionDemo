package com.test.facerecognitionbyusbcamera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.test.facerecognitionbyusbcamera.databinding.ActivityRegisterByUsbCameraBinding
import java.io.File

class StartFragmentTool : AppCompatActivity() {
    private lateinit var viewBinding: ActivityRegisterByUsbCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterByUsbCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val extras = intent.extras
        if (extras?.getString("flag").equals("register")) {
            val fileIva = File(applicationContext.externalCacheDir.toString() + "/iva/")
            if (!fileIva.exists()) fileIva.mkdir()
            MyApplication.savePath =
                applicationContext.externalCacheDir.toString() + "/iva/" + System.currentTimeMillis() + ".jpg"
            val file = File(MyApplication.savePath)
            if (file.exists()) file.delete()
            file.createNewFile()
            replaceDemoFragment(RegisterFragment())
        } else replaceDemoFragment(RecognitionFragment())
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        println("执行这里。关闭此页面")
    }


    private fun replaceDemoFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commitAllowingStateLoss()
    }
}