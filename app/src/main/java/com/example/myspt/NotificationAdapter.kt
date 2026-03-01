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
        val type = doc.getString("type") ?: "friend" // แยกประเภท: friend, group, request

        // ตั้งค่าข้อความตามประเภท
        when (type) {
            "group" -> {
                val groupName = doc.getString("group_name") ?: "Unknown Group"
                val inviter = doc.getString("from_name") ?: "Someone"
                holder.tvName.text = "$inviter invited you to $groupName" // โชว์ชื่อคนเชิญ+ชื่อกลุ่ม
            }
            "bill_nudge" -> {
                val billName = doc.getString("bill_name") ?: "Bill"
                val amount = doc.getDouble("amount") ?: 0.0
                holder.tvName.text = "Don't forget to pay $billName (฿$amount)" // แจ้งเตือนสะกิดจ่ายเงิน
            }
            else -> {
                // Friend Request ตามเดิมที่พี่เขียนไว้
                val fromName = doc.getString("from_name") ?: "Unknown User"
                holder.tvName.text = fromName
            }
        }
    }

    override fun getItemCount() = notifications.size

    fun updateData(newList: List<DocumentSnapshot>) {
        this.notifications = newList
        notifyDataSetChanged()
    }
}