package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.test.dkread.DkReadMainActivity
import com.test.ledtest.LedTestMainActivity
import com.test.lkread.LkReadMainActivity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ItemChoose : AppCompatActivity(), View.OnClickListener {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_choose)
        findViewById<TextView>(R.id.face_camera).setOnClickListener(this)
        findViewById<TextView>(R.id.dk_read).setOnClickListener(this)
        findViewById<TextView>(R.id.lk_read).setOnClickListener(this)
        findViewById<TextView>(R.id.led_test).setOnClickListener(this)
        findViewById<TextView>(R.id.zw_test).setOnClickListener(this)
        findViewById<TextView>(R.id.version).text =
            BuildConfig.BUILD_TYPE + "-" + BuildConfig.VERSION_NAME + "[" + convertUtcTimestampToLocalDateTime(
                BuildConfig.BUILD_TIME) + "]"
    }

    private fun convertUtcTimestampToLocalDateTime(utcTimestamp: Long): String {
        // 将毫秒值转换为Instant
        val instant = Instant.ofEpochMilli(utcTimestamp)
        // 获取系统默认的时区或指定时区，例如：ZoneId.of("Asia/Shanghai")
        val zoneId = ZoneId.systemDefault()
        // 将Instant转换为指定时区的ZonedDateTime
        val zonedDateTime = instant.atZone(zoneId)
        // 定义日期时间格式
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        // 格式化ZonedDateTime为日期时间字符串
        return zonedDateTime.format(formatter)
    }

    override fun onClick(p0: View?) {
        var intent: Intent? = null
        when (p0?.id) {
            R.id.face_camera -> intent = Intent(applicationContext, MainActivity::class.java)
            R.id.dk_read -> intent = Intent(applicationContext, DkReadMainActivity::class.java)
            R.id.lk_read -> intent = Intent(applicationContext, LkReadMainActivity::class.java)
            R.id.led_test -> intent = Intent(applicationContext, LedTestMainActivity::class.java)
            R.id.zw_test -> intent = Intent(applicationContext, MainActivity::class.java)
        }
        startActivity(intent)
    }
}