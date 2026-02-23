package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeGroupAdapter(
    private var groupList: List<CircleItem>, // เปลี่ยนเป็น var เพื่อให้เปลี่ยนข้อมูลตอน Search ได้
    private val isListView: Boolean = false,
    private val onClick: (CircleItem) -> Unit
) : RecyclerView.Adapter<HomeGroupAdapter.ViewHolder>() {

    // ส่ง isListView เข้ามาใน ViewHolder ด้วย เพื่อให้ดึง ID ได้ถูกไฟล์
    class ViewHolder(itemView: View, isListView: Boolean) : RecyclerView.ViewHolder(itemView) {
        // ใช้ ImageView ธรรมดาเป็นตัวรับ เพื่อไม่ให้เกิด Error ตอนแปลงเป็น ShapeableImageView ในโหมด List
        val imgGroup: ImageView = itemView.findViewById(if (isListView) R.id.ivGroupIcon else R.id.imgItem)
        val tvGroupName: TextView = itemView.findViewById(if (isListView) R.id.tvGroupName else R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (isListView) R.layout.item_group_list else R.layout.item_circle
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view, isListView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = groupList[position]

        if (currentItem.isAddButton) {
            holder.tvGroupName.text = "Add"
            holder.imgGroup.setImageResource(android.R.drawable.ic_input_add)
            holder.imgGroup.setPadding(40, 40, 40, 40)
        } else {
            holder.tvGroupName.text = currentItem.name
            holder.imgGroup.setPadding(0, 0, 0, 0)
            holder.imgGroup.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener { onClick(currentItem) }
    }

    override fun getItemCount(): Int = groupList.size

    // เพิ่มฟังก์ชันนี้เพื่อให้หน้า Grouplist เรียกใช้ตอนพิมพ์ค้นหา (Search)
    fun updateData(newList: List<CircleItem>) {
        groupList = newList
        notifyDataSetChanged()
    }
}