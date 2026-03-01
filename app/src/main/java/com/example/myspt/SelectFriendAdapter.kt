package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

// ✅ เปลี่ยนจาก FriendData เป็น FriendItem ให้ตรงกับไฟล์ที่พี่มี
class SelectFriendAdapter(private val friends: List<FriendItem>) :
    RecyclerView.Adapter<SelectFriendAdapter.ViewHolder>() {

    private val selectedUids = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ✅ แมตช์ ID ให้ตรงกับ XML (imgAvatar)
        val imgAvatar: ShapeableImageView = view.findViewById(R.id.imgAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvName.text = friend.name

        // 1. แก้ไขจุดที่แดง: ใส่ 'friend.' หน้า profileUrl และเติมจุดหน้า load/into
        Glide.with(holder.itemView.context)
            .load(friend.profileUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgAvatar)

        // 2. ล้าง Listener เก่าป้องกัน CheckBox รวน
        holder.cbSelect.setOnCheckedChangeListener(null)

        // 3. ตั้งค่าสถานะ CheckBox
        holder.cbSelect.isChecked = selectedUids.contains(friend.uid)

        // 4. จัดการการเลือกผ่าน CheckBox
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            toggleSelection(friend.uid, isChecked)
        }

        // 5. กดที่ CardView แล้วให้สลับสถานะติ๊กถูก
        holder.itemView.setOnClickListener {
            holder.cbSelect.toggle()
        }
    }

    private fun toggleSelection(uid: String, isSelected: Boolean) {
        if (isSelected) {
            selectedUids.add(uid)
        } else {
            selectedUids.remove(uid)
        }
    }

    override fun getItemCount() = friends.size

    fun getSelectedFriendUids(): List<String> = selectedUids.toList()
}