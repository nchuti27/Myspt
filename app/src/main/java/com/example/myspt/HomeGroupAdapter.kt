package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeGroupAdapter(
    private var groupList: List<CircleItem>,
    private val isListView: Boolean = false,
    private val onClick: (CircleItem) -> Unit,
    private val onLeaveClick: ((CircleItem) -> Unit)? = null
) : RecyclerView.Adapter<HomeGroupAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, isListView: Boolean) : RecyclerView.ViewHolder(itemView) {
        // ใช้รหัสที่ปลอดภัยขึ้นในการหา View
        val imgGroup: ImageView? = if (isListView)
            itemView.findViewById(R.id.ivGroupIcon) else itemView.findViewById(R.id.imgItem)

        // 2. ตรวจสอบ ID ของชื่อกลุ่ม
        val tvGroupName: TextView? = if (isListView)
            itemView.findViewById(R.id.tvGroupName) else itemView.findViewById(R.id.tvName)

        // กำหนดให้เป็น Optional (ใส่ ?) ทั้งหมดเพื่อป้องกันแอปเด้งหากหา ID ไม่เจอ
        val btnMore: ImageButton? = itemView.findViewById(R.id.btnMore)
        val layoutExpandOptions: LinearLayout? = itemView.findViewById(R.id.layoutExpandOptions)
        val btnGroupDetail: Button? = itemView.findViewById(R.id.btnGroupDetail)
        val btnLeaveGroup: Button? = itemView.findViewById(R.id.btnLeaveGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ตรวจสอบว่าไฟล์ XML ทั้งสองใบมี ID ครบถ้วนตามที่เรียกด้านบน
        val layout = if (isListView) R.layout.item_group_list else R.layout.item_circle
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view, isListView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = groupList[position]

        // ใช้ safe call ?. เพื่อป้องกัน NullPointerException
        holder.tvGroupName?.text = if (currentItem.isAddButton) "Add" else currentItem.name

        if (currentItem.isAddButton) {
            holder.imgGroup?.setImageResource(android.R.drawable.ic_input_add)
            holder.imgGroup?.setPadding(40, 40, 40, 40)
        } else {
            holder.imgGroup?.setImageResource(R.drawable.ic_launcher_background)
            holder.imgGroup?.setPadding(0, 0, 0, 0)
        }

        if (isListView) {
            // จัดการแสดงผลส่วนขยาย
            holder.layoutExpandOptions?.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

            holder.btnMore?.setOnClickListener {
                currentItem.isExpanded = !currentItem.isExpanded
                notifyItemChanged(position)
            }

            holder.btnGroupDetail?.setOnClickListener {
                currentItem.isExpanded = false
                notifyItemChanged(position)
                onClick(currentItem)
            }

            holder.btnLeaveGroup?.setOnClickListener {
                currentItem.isExpanded = false
                notifyItemChanged(position)
                onLeaveClick?.invoke(currentItem)
            }
        } else {
            holder.itemView.setOnClickListener { onClick(currentItem) }
        }
    }

    override fun getItemCount(): Int = groupList.size

    fun updateData(newList: List<CircleItem>) {
        // ล้างข้อมูลเก่าและแทนที่ด้วยข้อมูลใหม่เพื่อแก้ปัญหา "ข้อมูลเบิ้ล"
        this.groupList = newList
        notifyDataSetChanged()
    }
}