package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ✅ ใช้ Glide โหลดรูป [cite: 2026-02-23]
import com.google.android.material.imageview.ShapeableImageView

class GroupAdapter(
    private val groups: List<Group>,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ✅ เปลี่ยน ID ให้ตรงกับไฟล์ item_friend_list.xml (หรือ item_group_list)
        val imgProfile: ShapeableImageView = view.findViewById(R.id.ivFriendProfile)
        val groupName: TextView = view.findViewById(R.id.tvFriendName)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        // ✅ เปลี่ยนจาก simple_list_item_1 เป็นไฟล์ XML ของพี่เอง
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_list, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.groupName.text = group.name

        // ✅ โหลดรูปภาพให้เป็นวงกลม
        // ✅ แก้เป็นแบบนี้ครับ
        // ✅ แก้ไข Glide ให้ถูกต้องตามนี้ครับ
        Glide.with(holder.itemView.context)
            .load(group.profileUrl ?: R.drawable.ic_launcher_background) // ลบ resourceId = ออก
            .placeholder(R.drawable.ic_launcher_background) // 🌟 ต้องใช้ R.drawable เท่านั้น ห้ามใช้ R.id
            .circleCrop()
            .into(holder.imgProfile)

        holder.itemView.setOnClickListener { onClick(group) }

        // จัดการคลิกปุ่ม 3 จุด (ถ้ามี)
        holder.btnMore.setOnClickListener {
            // โค้ดสำหรับโชว์เมนู ลบ หรือ แก้ไข
        }
    }

    override fun getItemCount() = groups.size
}