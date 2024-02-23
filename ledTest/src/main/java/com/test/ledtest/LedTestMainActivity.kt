package com.test.ledtest

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


//GPIO1
//拉高
//echo 0 > /sys/class/EMDEBUG/custom_output_gpio
//拉低
//echo 1 > /sys/class/EMDEBUG/custom_output_gpio
//GPIO2
//拉高
//echo 2 > /sys/class/EMDEBUG/custom_output_gpio
//拉低
//echo 3 > /sys/class/EMDEBUG/custom_output_gpio
//GPIO3
//拉高
//echo 4 > /sys/class/EMDEBUG/custom_output_gpio
//拉低
//echo 5 > /sys/class/EMDEBUG/custom_output_gpio
//GPIO4
//拉高
//echo 6 > /sys/class/EMDEBUG/custom_output_gpio
//拉低
//echo 7 > /sys/class/EMDEBUG/custom_output_gpio

class LedTestMainActivity : AppCompatActivity() {
    private var gpio1: TextView? = null
    private var gpio2: TextView? = null
    private var gpio3: TextView? = null
    private var gpio4: TextView? = null
    private var result: TextView? = null

    private var button1: Button? = null
    private var button2: Button? = null
    private var button3: Button? = null
    private var button4: Button? = null
    private var buttonStart: Button? = null

    private var editTextForNum: EditText? = null
    private var editTextForTime: EditText? = null
    private var editTextForGpio: EditText? = null

    private var flagGpio1 = false
    private var flagGpio2 = false
    private var flagGpio3 = false
    private var flagGpio4 = false

    private var gpioPath = "/sys/class/EMDEBUG/custom_output_gpio"

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.led_activity_main)
        gpio1 = findViewById(R.id.gpio1)
        gpio2 = findViewById(R.id.gpio2)
        gpio3 = findViewById(R.id.gpio3)
        gpio4 = findViewById(R.id.gpio4)
        result = findViewById(R.id.result)

        button1 = findViewById(R.id.bt1)
        button2 = findViewById(R.id.bt2)
        button3 = findViewById(R.id.bt3)
        button4 = findViewById(R.id.bt4)
        buttonStart = findViewById(R.id.start)

        editTextForNum = findViewById(R.id.input_count)
        editTextForTime = findViewById(R.id.input_time)
        editTextForGpio = findViewById(R.id.input_gpio)

        val handler = Handler(Looper.getMainLooper())
        button1?.setOnClickListener {
            if (flagGpio1) {
                setGpio("0", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio1 = false
                            gpio1?.text = "GPIO拉高"
                        } else gpio1?.text = "操作失败"
                    }
                })
            } else {
                setGpio("1", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio1 = true
                            gpio1?.text = "GPIO拉低"
                        } else gpio1?.text = "操作失败"
                    }
                })
            }
        }
        button2?.setOnClickListener {
            if (flagGpio2) {
                setGpio("2", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio2 = false
                            gpio2?.text = "GPIO拉高"
                        } else gpio2?.text = "操作失败"
                    }
                })
            } else {
                setGpio("3", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio2 = true
                            gpio2?.text = "GPIO拉低"
                        } else gpio2?.text = "操作失败"
                    }
                })
            }
        }
        button3?.setOnClickListener {
            if (flagGpio3) {
                setGpio("4", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio3 = false
                            gpio3?.text = "GPIO拉高"
                        } else gpio3?.text = "操作失败"
                    }
                })
            } else {
                setGpio("5", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio3 = true
                            gpio3?.text = "GPIO拉低"
                        } else gpio3?.text = "操作失败"
                    }
                })
            }
        }
        button4?.setOnClickListener {
            if (flagGpio4) {
                setGpio("6", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio4 = false
                            gpio4?.text = "GPIO拉高"
                        } else gpio4?.text = "操作失败"
                    }
                })
            } else {
                setGpio("7", gpioPath, change = { result ->
                    handler.post {
                        if (result == "操作成功！") {
                            flagGpio4 = true
                            gpio4?.text = "GPIO拉低"
                        } else gpio4?.text = "操作失败"
                    }
                })
            }
        }


        var buttonStartFlag = true
        buttonStart?.setOnClickListener {
            try {
                if (buttonStartFlag) {
                    buttonStartFlag = false
                    var num = Int.MAX_VALUE
                    var time = 1000L
                    var whatGpio = "GPIO1"
                    if (editTextForNum?.text.toString() != "")
                        num = editTextForNum?.text.toString().toInt()
                    if (editTextForTime?.text.toString() != "")
                        time = editTextForTime?.text.toString().toLong()
                    if (editTextForGpio?.text.toString() != "")
                        whatGpio = editTextForGpio?.text.toString()
                    buttonStart?.text = "停止"
                    autoSetGpio(num, time, whatGpio, stopChange = {
                        buttonStartFlag = true
                        handler.post { buttonStart?.text = "开始" }
                    }, updateUI = {
                        if (whatGpio.contains("1")) handler.post { gpio1?.text = it }
                        if (whatGpio.contains("2")) handler.post { gpio2?.text = it }
                        if (whatGpio.contains("3")) handler.post { gpio3?.text = it }
                        if (whatGpio.contains("4")) handler.post { gpio4?.text = it }
                    }) {
                        if (it != "error") {
                            handler.post { result?.text = "已完成${it}次" }
                        } else {
                            buttonStartFlag = true
                            handler.post { buttonStart?.text = "开始" }
                            handler.post { result?.text = "操作失败！" }
                        }
                    }
                } else {
                    buttonStartFlag = true
                    countNum = Int.MAX_VALUE //主动停止循环。
                }
            } catch (_: Exception) {
            }
        }
    }

    private var countNum = 0


    private fun autoSetGpio(
        num: Int,
        time: Long,
        whatGpio: String,
        stopChange: () -> Unit,
        updateUI: (String) -> Unit,
        change: (String) -> Unit
    ) {
        countNum = 0
        var resultCount: Int
        var flag = false
        val high: String
        val low: String
        try {
            println("这里的结果是？？？？？ $whatGpio")
            if (whatGpio.contains("1")) {
                high = "0"
                low = "1"
            } else if (whatGpio.contains("2")) {
                high = "2"
                low = "3"
            } else if (whatGpio.contains("3")) {
                high = "4"
                low = "5"
            } else if (whatGpio.contains("4")) {
                high = "6"
                low = "7"
            } else {
                change("error")
                return
            }
            timer(time, num, end = { stopChange() }) {
                setGpio(if (flag) high else low, gpioPath, change = {
                    if (it == "操作成功！") {
                        updateUI(if (flag) "GPIO拉高" else "GPIO拉低")
                        flag = !flag
                    } else {
                        change("error")
                        countNum = Int.MAX_VALUE
                    }  //操作失败结束循环
                }) {
                    if (it == "完成一次设置") {
                        countNum++    //此处循环次数达标主动停止。
                        resultCount = countNum
                        change("$resultCount")
                    }
                }
            }
        } catch (e: Exception) {
            change("error")
            e.printStackTrace()
        }
    }


    private fun timer(delay: Long, num: Int, end: () -> Unit, block: suspend () -> Unit) {
        MainScope().launch(Dispatchers.IO) {
            while (true) {
                if (countNum >= num) break
                block()
                delay(delay)
            }
            end()
        }
    }


    private fun setGpio(
        str: String,
        path: String,
        change: (String) -> Unit,
        count: (String) -> Unit = {}
    ) {
        MainScope().launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val bufferedWriter = BufferedWriter(FileWriter(path))
                    bufferedWriter.write(str)
                    bufferedWriter.close()
                    change("操作成功！")
                    if (str == "0" || str == "2" || str == "4" || str == "6") count("完成一次设置")
                } else change("路径不存在！")
            } catch (e: Exception) {
                change("error ->  $e")
                count("失败")
                e.printStackTrace()
            }
        }
    }
}