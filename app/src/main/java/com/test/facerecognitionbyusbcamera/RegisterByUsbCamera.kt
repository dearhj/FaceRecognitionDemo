package com.test.facerecognitionbyusbcamera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.test.facerecognitionbyusbcamera.databinding.ActivityRegisterByUsbCameraBinding

class RegisterByUsbCamera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityRegisterByUsbCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterByUsbCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        replaceDemoFragment(RegisterFragment())
    }


    private fun replaceDemoFragment(fragment: Fragment) {
        println("执行这里了吗》》》》》》》》    应该是这里")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commitAllowingStateLoss()
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            0 -> {
//                val hasCameraPermission = androidx.core.content.PermissionChecker.checkSelfPermission(this,
//                    Manifest.permission.CAMERA
//                )
//                if (hasCameraPermission == androidx.core.content.PermissionChecker.PERMISSION_DENIED) {
//                    Toast.makeText(this, "权限拒绝", Toast.LENGTH_SHORT).show()
//                    return
//                }
//                replaceDemoFragment(RegisterFragment())
//            }
//            1 -> {
//                val hasCameraPermission =
//                    androidx.core.content.PermissionChecker.checkSelfPermission(this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    )
//                if (hasCameraPermission == androidx.core.content.PermissionChecker.PERMISSION_DENIED) {
//                    Toast.makeText(this, "权限拒绝", Toast.LENGTH_SHORT).show()
//                    return
//                }
//                // todo
//            }
//            else -> {
//            }
//        }
//    }
}