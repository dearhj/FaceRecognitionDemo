package com.test.facerecognitionbyusbcamera

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.test.lkread.LkReadMainActivity

class ItemChoose : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_choose)
        findViewById<TextView>(R.id.face_camera).setOnClickListener(this)
        findViewById<TextView>(R.id.dk_read).setOnClickListener(this)
        findViewById<TextView>(R.id.lk_read).setOnClickListener(this)
        findViewById<TextView>(R.id.led_test).setOnClickListener(this)
        findViewById<TextView>(R.id.zw_test).setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        var intent: Intent? = null
        when (p0?.id){
            R.id.face_camera -> intent = Intent(applicationContext, MainActivity::class.java)
            R.id.dk_read -> intent = Intent(applicationContext, MainActivity::class.java)
            R.id.lk_read -> intent = Intent(applicationContext, LkReadMainActivity::class.java)
            R.id.led_test -> intent = Intent(applicationContext, MainActivity::class.java)
            R.id.zw_test -> intent = Intent(applicationContext, MainActivity::class.java)
        }
        startActivity(intent)
    }
}