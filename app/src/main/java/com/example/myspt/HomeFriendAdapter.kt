package com.example.myspt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class HomeFriendAdapter(private val friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<HomeFriendAdapter.HomeFriendViewHolder>() {

    class HomeFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ตรวจสอบ ID ในไฟล์ item_contact.xml ให้ตรงกัน
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
        val context = holder.itemView.context

        // 1. แสดงชื่อเพื่อน
        holder.tvName.text = currentItem.name

        // 2. โหลดรูปภาพโปรไฟล์ (ถ้าใน FriendData มี field imageUrl)
        // ถ้ายังไม่มี URL ระบบจะใช้รูป ic_launcher_background เป็นรูปเริ่มต้น
        // holder.ivAvatar.load("URL_ของรูปภาพ") {
        //     placeholder(R.drawable.ic_launcher_background)
        //     crossfade(true)
        // }

        // 3. ตั้งค่าการคลิกเพื่อไปหน้า FriendProfile
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FriendProfile::class.java).apply {
                // ส่งข้อมูลสำคัญไปยังหน้าโปรไฟล์
                putExtra("FRIEND_UID", currentItem.uid)
                putExtra("FRIEND_NAME", currentItem.name)
                // ตรวจสอบว่า FriendData ของคุณมี field username หรือไม่
                // putExtra("FRIEND_USERNAME", currentItem.username)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}