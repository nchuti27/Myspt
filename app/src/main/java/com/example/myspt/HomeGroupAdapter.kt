package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class HomeGroupAdapter(
    private val groupList: List<CircleItem>,
    private val isListView: Boolean = false, // ถ้าส่ง true จะใช้ layout แบบรายชื่อ
    private val onClick: (CircleItem) -> Unit
) : RecyclerView.Adapter<HomeGroupAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ตรวจสอบให้แน่ใจว่าทั้ง item_circle และ item_group_list ใช้ ID เหล่านี้
        val imgGroup: ShapeableImageView = itemView.findViewById(R.id.imgItem)
        val tvGroupName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // เลือก Layout ตามโหมดที่สั่งมา
        val layout = if (isListView) R.layout.item_group_list else R.layout.item_circle
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = groupList[position]

        if (currentItem.isAddButton) {
            // ตั้งค่าสำหรับปุ่มบวก (+)
            holder.tvGroupName.text = "Add"
            holder.imgGroup.setImageResource(android.R.drawable.ic_input_add)
            holder.imgGroup.setPadding(40, 40, 40, 40)
            holder.imgGroup.strokeWidth = 0f
        } else {
            // ตั้งค่าสำหรับข้อมูลกลุ่มปกติ
            holder.tvGroupName.text = currentItem.name
            holder.imgGroup.setPadding(0, 0, 0, 0)
            holder.imgGroup.strokeWidth = 2f
            holder.imgGroup.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener { onClick(currentItem) }
    }

    override fun getItemCount(): Int = groupList.size
}