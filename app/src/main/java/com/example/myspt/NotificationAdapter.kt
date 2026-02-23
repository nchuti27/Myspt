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

        // --- แก้ไขจุดนี้: เปลี่ยนมาดึงจากฟิลด์ from_name ที่เราบันทึกไว้ล่าสุด ---
        val name = doc.getString("from_name")
            ?: doc.getString("senderName") // กันเหนียวสำหรับข้อมูลเก่า
            ?: doc.getString("username")   // กันเหนียวสำหรับข้อมูลเก่า
            ?: "Unknown User"

        holder.tvName.text = name

        holder.btnAccept.setOnClickListener {
            onAccept(doc)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(doc)
        }
    }

    override fun getItemCount() = notifications.size

    fun updateData(newList: List<DocumentSnapshot>) {
        notifications = newList
        notifyDataSetChanged()
    }
}