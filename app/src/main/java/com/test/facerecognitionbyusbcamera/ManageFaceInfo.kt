package com.test.facerecognitionbyusbcamera

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.facerecognitionbyusbcamera.FileUtil.showToast
import com.test.facerecognitionbyusbcamera.ManageActivity.dao
import com.test.facerecognitionbyusbcamera.ManageActivity.group_name
import com.test.facerecognitionbyusbcamera.ManageActivity.mFacePassHandler
import com.test.facerecognitionbyusbcamera.db.FaceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageFaceInfo : AppCompatActivity() {
    private var listData: MutableList<FaceData>? = null
    private var adapter: ListFaceAdapter? = null
    private var iv: ImageView? = null
    private var runnable: Runnable? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_face_info)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFace)
        iv = findViewById<View>(R.id.im_view_face) as ImageView
        adapter = ListFaceAdapter(updateOption = { faceData -> showDialogUpdate(faceData) },
            delOption = { faceData -> showDialogDel(faceData) },
            seePhoto = { faceData -> showPhoto(faceData) })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        listData = mutableListOf()
        getFaceInfo()

        runnable = Runnable {
            iv?.visibility = View.GONE
            iv?.setImageBitmap(null)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getFaceInfo() {
        if (mFacePassHandler == null) return
        MainScope().launch(Dispatchers.IO) {
            try {
                val faceTokens: Array<ByteArray> = mFacePassHandler.getLocalGroupInfo(group_name)
                listData?.clear()
                if (faceTokens.isNotEmpty()) {
                    for (j in faceTokens.indices) {
                        if (faceTokens[j].isNotEmpty()) {
                            val faceInfo = dao.selectFaceDataByFaceToken(String(faceTokens[j]))
                            if (faceInfo.isNotEmpty()) listData?.add(faceInfo[0])
                            else {
                                val face = FaceData(0, "未知", String(faceTokens[j]))
                                dao.insertData(face)
                                listData?.add(face)
                            }
                        }
                    }
                } else dao.delAllData()
                withContext(Dispatchers.Main) {
                    adapter?.setNewInstance(listData)
                    adapter?.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val handler = Handler(Looper.getMainLooper())

    private fun showPhoto(faceData: FaceData) {
        if (mFacePassHandler == null) return
        try {
            runnable?.let { handler.removeCallbacks(it)}
            val bmp = mFacePassHandler.getFaceImage(faceData.faceToken.toByteArray())
            iv?.setImageBitmap(bmp)
            iv?.visibility = View.VISIBLE
            runnable?.let { handler.postDelayed(it, 2000)}
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDialogDel(faceData: FaceData) {
        AlertDialog.Builder(this)
            .setTitle("删除：")
            .setMessage("确认删除？")
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ ->
                if (mFacePassHandler != null) {
                    try {
                        val faceToken = faceData.faceToken.toByteArray()
                        val b = mFacePassHandler.unBindGroup(group_name, faceToken)
                        println("这里解绑成功了吗？   $b")
                        if (b) {
                            val c = mFacePassHandler.deleteFace(faceToken)
                            println("这里删除成功了吗? $c")
                            dao.deleteData(faceData)
                            showToast(this, "删除成功！")
                            listData?.clear()
                            listData = dao.selectAllData()
                            adapter?.setNewInstance(listData)
                            adapter?.notifyDataSetChanged()
                        } else showToast(this, "删除失败！")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            .show()
    }

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    private fun showDialogUpdate(faceData: FaceData) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.face_name_update, null)
        builder.setView(view)
        val inputEditText = view.findViewById<EditText>(R.id.inputName)
        val sure = view.findViewById<Button>(R.id.sure)
        val cancel = view.findViewById<Button>(R.id.cancel)
        builder.setTitle("输入信息：")
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        sure.setOnClickListener {
            val inputText = inputEditText.text.toString().trim { it <= ' ' }
            if (inputText != "") {
                val list = dao.selectDataByName(inputText)
                if (list.isNotEmpty()) showToast(this, "输入name已经存在，请重新输入")
                else {
                    val faceInfoByToken = dao.selectFaceDataByFaceToken(faceData.faceToken)
                    dao.updateData(
                        FaceData(
                            faceInfoByToken[0].id,
                            inputText,
                            faceInfoByToken[0].faceToken
                        )
                    )
                    showToast(this, "更新成功")
                    listData?.clear()
                    listData = dao.selectAllData()
                    adapter?.setNewInstance(listData)
                    adapter?.notifyDataSetChanged()
                    dialog.dismiss()
                }
            } else showToast(this, "输入的字符非法，请重新输入")
        }
        cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}