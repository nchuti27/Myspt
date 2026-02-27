package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

class NotificationAdapter(
    private var notifications: List<DocumentSnapshot>,
    private val onAccept: (DocumentSnapshot) -> Unit,
    private val onDelete: (DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        val myUid = FirebaseAuth.getInstance().currentUser?.uid

        // 1. ตรวจสอบว่าเป็นคำขอ "ส่งออก" หรือ "รับเข้า" [cite: 2026-02-27]
        val fromUid = doc.getString("from_uid")
        val isSentByMe = fromUid == myUid

        // 2. ตั้งค่าชื่อที่จะแสดง
        // ถ้าเราส่งเอง ให้โชว์ชื่อคนรับ (to_name) ถ้าเขาส่งมา ให้โชว์ชื่อคนส่ง (from_name) [cite: 2026-02-27]
        val displayName = if (isSentByMe) {
            doc.getString("to_name") ?: "Unknown User"
        } else {
            doc.getString("from_name") ?: "Unknown User"
        }
        holder.tvName.text = displayName

        // 3. จัดการปุ่มตามประเภทคำขอ [cite: 2026-02-27]
        if (isSentByMe) {
            // กรณี Tab Request (เราส่งเอง): ซ่อน Accept และเปลี่ยน Delete เป็น Cancel [cite: 2026-02-27]
            holder.btnAccept.visibility = View.GONE
            holder.btnDelete.text = "Cancel"
        } else {
            // กรณี Tab Friend/Group (คนอื่นส่งมา): โชว์ปุ่มตามปกติ [cite: 2026-02-27]
            holder.btnAccept.visibility = View.VISIBLE
            holder.btnDelete.text = "Delete"
        }

        // 4. จัดการเหตุการณ์กดปุ่ม
        holder.btnAccept.setOnClickListener { onAccept(doc) }
        holder.btnDelete.setOnClickListener { onDelete(doc) }
    }

    override fun getItemCount() = notifications.size

    fun updateData(newList: List<DocumentSnapshot>) {
        this.notifications = newList
        notifyDataSetChanged()
    }
}