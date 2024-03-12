package com.test.facerecognitionbyusbcamera

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.test.facerecognitionbyusbcamera.db.FaceData

class ListFaceAdapter(
    layoutRes: Int = R.layout.item_face_list,
    val updateOption: (FaceData) -> Unit,
    val delOption: (FaceData) -> Unit,
    val seePhoto: (FaceData) -> Unit
) :
    BaseQuickAdapter<FaceData, BaseViewHolder>(layoutRes) {

    override fun convert(holder: BaseViewHolder, item: FaceData) {
        holder.setText(R.id.face_name, "姓名: " + item.name)
        val update = holder.getView<TextView>(R.id.updateFaceName)
        val del = holder.getView<TextView>(R.id.delFaceName)
        val info = holder.getView<TextView>(R.id.face_photo)
        info.setOnClickListener { seePhoto(item) }
        update.setOnClickListener { updateOption(item) }
        del.setOnClickListener { delOption(item) }
    }

}