package com.zaz.demo

import android.content.Context
import android.widget.Toast
import java.io.FileInputStream
import java.io.FileOutputStream


var toast: Toast? = null
fun showToast(context: Context, string: String){
    if(toast != null) toast?.cancel()
    toast = Toast.makeText(context, string, Toast.LENGTH_SHORT)
    toast?.show()
}


fun writeToFile(upChar: ByteArray?, len: Int) {
    val filePath = "/sdcard/12.txt"
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(filePath)
        fos.write(upChar, 0, len)
        fos.flush() // 确保所有数据都写入到文件中
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (fos != null) {
            try {
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


fun readFromFile(downChar: ByteArray) {
    val filePath = "/sdcard/12.txt"
    var fis: FileInputStream? = null
    val bytesRead: Int
    try {
        fis = FileInputStream(filePath)
        bytesRead = fis.read(downChar) // 读取文件内容到 upChar 数组
        if (bytesRead < downChar.size) {
            // 如果文件大小小于 upChar 的大小，将剩余字节设置为 0
            for (i in bytesRead until downChar.size) {
                downChar[i] = 0
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        if (fis != null) {
            try {
                fis.close() // 关闭文件输入流
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}