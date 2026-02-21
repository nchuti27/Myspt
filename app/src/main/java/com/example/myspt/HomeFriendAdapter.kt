package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// ใช้ข้อมูลจาก FriendData class ที่เราสร้างไว้ก่อนหน้า
class HomeFriendAdapter(private val friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<HomeFriendAdapter.HomeFriendViewHolder>() {

    class HomeFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ตรวจสอบ ID ในไฟล์ item_contact.xml ให้ตรงกันนะครับ
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFriendViewHolder {
        // ใช้ Layout item_contact สำหรับแสดงผลรายชื่อเพื่อนในหน้าหลัก
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return HomeFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFriendViewHolder, position: Int) {
        val currentItem = friendList[position]

        // แสดงชื่อเพื่อนที่ดึงมาจาก Firestore
        holder.tvName.text = currentItem.name

        // หากในอนาคตมี URL รูปภาพโปรไฟล์ สามารถใช้ Glide หรือ Coil โหลดรูปเข้า ivAvatar ได้ที่นี่
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}