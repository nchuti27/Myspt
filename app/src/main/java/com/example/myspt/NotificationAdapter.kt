package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot

class NotificationAdapter(
    private var notifications: List<DocumentSnapshot>,
    private val onAccept: (DocumentSnapshot) -> Unit,
    private val onDelete: (DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // ไอดีตรงกับใน item_notification.xml ที่เราแก้ไขล่าสุด [cite: 2026-02-21]
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = notifications[position]

        // ดึงชื่อผู้ส่งคำขอ: ลำดับความสำคัญคือ senderName -> username -> Unknown [cite: 2026-02-09]
        val name = doc.getString("senderName")
            ?: doc.getString("username")
            ?: "Unknown User"

        holder.tvName.text = name

        // ส่ง Document ของรายการนั้นๆ กลับไปให้ Activity จัดการผ่าน Callback
        holder.btnAccept.setOnClickListener {
            onAccept(doc)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(doc)
        }
    }

    override fun getItemCount() = notifications.size

    // ฟังก์ชันสำหรับอัปเดตข้อมูลเมื่อมีการเปลี่ยนแปลงใน Firestore [cite: 2026-02-21]
    fun updateData(newList: List<DocumentSnapshot>) {
        notifications = newList
        notifyDataSetChanged()
    }
}