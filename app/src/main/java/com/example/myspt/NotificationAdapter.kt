package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

class NotificationAdapter(
    private var notifications: List<DocumentSnapshot>,
    private val onAccept: (DocumentSnapshot) -> Unit,
    private val onDelete: (DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var activeTab: String = "FRIEND"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ShapeableImageView = view.findViewById(R.id.ivFriendProfile)
        val tvName: TextView = view.findViewById(R.id.tvFriendName)
        val tvMessage: TextView = view.findViewById(R.id.tvNotiMessage)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = notifications[position]
        val type = doc.getString("type") // เช็คว่าเป็นทวงหนี้หรือไม่

        if (type == "debt_reminder") {
            // --- UI สำหรับแจ้งเตือนทวงหนี้ ---
            holder.tvName.text = "แจ้งเตือนการทวงหนี้"
            holder.tvMessage.text = doc.getString("message") ?: "คุณมีหนี้ค้างชำระ"
            holder.btnAccept.visibility = View.GONE // ซ่อนปุ่ม Accept
            holder.btnDelete.text = "ลบ"
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background)
        } else {
            // --- UI เดิมสำหรับขอเป็นเพื่อน/กลุ่ม ---
            val fromName = doc.getString("from_name") ?: "Someone"
            holder.tvName.text = fromName
            holder.tvMessage.text = "sent you a friend request."
            holder.btnAccept.visibility = View.VISIBLE
            // ... (โค้ดโหลดรูปเดิมของคุณ) ...
        }

        holder.btnAccept.setOnClickListener { onAccept(doc) }
        holder.btnDelete.setOnClickListener { onDelete(doc) }
    }

    override fun getItemCount() = notifications.size

    fun updateData(newList: List<DocumentSnapshot>, tab: String) {
        this.notifications = newList
        this.activeTab = tab
        notifyDataSetChanged()
    }
}