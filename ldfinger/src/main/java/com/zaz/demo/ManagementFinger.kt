package com.zaz.demo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zaz.demo.LdFingerMainActivity.dao
import com.zaz.demo.db.FingerData

class ManagementFinger : AppCompatActivity() {
    private var listData: MutableList<FingerData>? = null
    private var adapter: ListFingerAdapter? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_management_finger)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ListFingerAdapter(updateOption = { fingerData -> showDialogUpdate(fingerData) },
            delOption = { fingerData -> showDialogDel(fingerData) })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        listData = dao.selectAllData()
        adapter?.setNewInstance(listData)
        adapter?.notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun showDialogDel(fingerData: FingerData) {
        AlertDialog.Builder(this)
            .setTitle("删除：")
            .setMessage("确认删除？")
            .setNegativeButton("取消", null)
            .setPositiveButton("确定") { _, _ ->
                val ret = LdFingerMainActivity.zaclient.ZAZDelChar(
                    LdFingerMainActivity.DEV_ADDR,
                    fingerData.fingerId, 1
                )
                if (ret == 0) {
                    dao.deleteData(fingerData)
                    showToast(this, "删除成功！")
                    listData?.clear()
                    listData = dao.selectAllData()
                    adapter?.setNewInstance(listData)
                    adapter?.notifyDataSetChanged()
                } else showToast(this, "删除失败！")
            }
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDialogUpdate(fingerData: FingerData) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.finger_insert, null)
        builder.setView(view)
        val inputEditText = view.findViewById<EditText>(R.id.inputEditText)
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
                    fingerData.name = inputText
                    dao.updateData(fingerData)
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