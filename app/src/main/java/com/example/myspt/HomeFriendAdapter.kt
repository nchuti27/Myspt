package com.example.myspt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ✅ เปลี่ยนมาใช้ Glide เพื่อทำรูปวงกลมได้ง่าย
import com.google.android.material.imageview.ShapeableImageView

class HomeFriendAdapter(private val friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<HomeFriendAdapter.HomeFriendViewHolder>() {

    class HomeFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ 1. เปลี่ยน ID ให้ตรงกับไฟล์ item_friend_list.xml ที่คุณส่งมาล่าสุด
        val ivProfile: ShapeableImageView = itemView.findViewById(R.id.ivFriendProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvFriendName)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFriendViewHolder {
        // ✅ 2. เปลี่ยน Layout จาก item_contact เป็น item_friend_list เพื่อให้โครงสร้างเหมือนหน้า Group
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_list, parent, false)
        return HomeFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFriendViewHolder, position: Int) {
        val currentItem = friendList[position]
        val context = holder.itemView.context

        holder.tvName.text = currentItem.name

        // ✅ 3. ใช้ Glide โหลดรูปภาพให้เป็นวงกลม (ต้องมั่นใจว่า FriendData มี profileUrl)
        Glide.with(context)
            .load(currentItem.profileUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .circleCrop() // 🌟 ทำรูปวงกลมให้เหมือนหน้า Group
            .into(holder.ivProfile)

        // ตั้งค่าการคลิกปุ่ม 3 จุด (ถ้าต้องการ)
        holder.btnMore.setOnClickListener {
            // ใส่ลอจิกเมนู เช่น ลบเพื่อน หรือ ดูโปรไฟล์
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, FriendProfile::class.java).apply {
                putExtra("FRIEND_UID", currentItem.uid)
                putExtra("FRIEND_NAME", currentItem.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = friendList.size
}