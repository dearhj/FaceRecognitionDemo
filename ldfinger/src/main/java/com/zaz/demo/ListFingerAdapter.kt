package com.zaz.demo

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.zaz.demo.db.FingerData

class ListFingerAdapter(layoutRes: Int = R.layout.item_finger_list, val updateOption: (FingerData) -> Unit, val delOption: (FingerData) -> Unit) :
    BaseQuickAdapter<FingerData, BaseViewHolder>(layoutRes) {

    override fun convert(holder: BaseViewHolder, item: FingerData) {
        holder.setText(R.id.finger_name, "name: " + item.name)
        holder.setText(R.id.finger_id, "fingerID: " + item.fingerId.toString())
        val update = holder.getView<TextView>(R.id.update)
        val del = holder.getView<TextView>(R.id.del)
        update.setOnClickListener { updateOption(item) }
        del.setOnClickListener { delOption(item) }
    }

}