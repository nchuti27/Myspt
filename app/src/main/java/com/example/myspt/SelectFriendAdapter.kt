package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectFriendAdapter(private val friends: List<FriendData>) :
    RecyclerView.Adapter<SelectFriendAdapter.ViewHolder>() {

    private val selectedUids = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvName.text = friend.name // แสดงชื่อจาก FriendData

        // 1. ล้าง Listener เก่าออกก่อนเพื่อป้องกันสถานะ CheckBox รวน
        holder.cbSelect.setOnCheckedChangeListener(null)

        // 2. ตั้งค่าสถานะ CheckBox ตามลิสต์ที่เราเลือกไว้
        holder.cbSelect.isChecked = selectedUids.contains(friend.uid)

        // 3. จัดการการเลือกเมื่อกดที่ตัว CheckBox
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            toggleSelection(friend.uid, isChecked)
        }

        // 4. เพิ่มความสะดวก: กดที่ชื่อเพื่อนแล้วให้ติ๊ก CheckBox ด้วย
        holder.itemView.setOnClickListener {
            holder.cbSelect.toggle() // สลับสถานะติ๊กถูก
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

    // ดึง UIDs ของเพื่อนที่ถูกเลือกทั้งหมดส่งกลับไปที่หน้า SelectFriend
    fun getSelectedFriendUids(): List<String> = selectedUids.toList()
}