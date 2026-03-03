package com.example.myspt

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
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
        val ivMore: ImageView = view.findViewById(R.id.ivMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    // ในไฟล์ NotificationAdapter.kt
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = notifications[position]
        val type = doc.getString("type")

        when (activeTab) {
            "FRIEND" -> {
                // ดึงชื่อคนที่ส่งมาหาเรา
                val fromName = doc.getString("from_name") ?: "Someone"
                holder.tvName.text = fromName
                holder.tvMessage.text = "sent you a friend request."
                holder.btnAccept.visibility = View.VISIBLE
                holder.btnDelete.text = "Decline"
            }
            "GROUP" -> {
                val groupName = doc.getString("groupName") ?: "Unknown Group"
                holder.tvName.text = groupName
                holder.tvMessage.text = "invited you to join."
                holder.btnAccept.visibility = View.VISIBLE
            }
            "REQUEST" -> {
                // 🌟 แท็บนี้ต้องโชว์ชื่อคนที่เราส่งไปหา (to_name)
                val toName = doc.getString("to_name") ?: "Waiting for user..."
                holder.tvName.text = toName
                holder.tvMessage.text = "Waiting for approval..."
                holder.btnAccept.visibility = View.GONE // ซ่อนปุ่ม Accept ตามที่พี่ต้องการ
                holder.btnDelete.text = "Cancel" // เปลี่ยนเป็นปุ่ม Cancel แทน
            }
        }

        // กรณีทวงหนี้ (Debt) ให้จัดการแยกต่างหากตามเดิม
        if (type == "debt_reminder") {
            holder.tvName.text = "Debt Reminder"
            holder.btnAccept.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmation(context: Context, doc: DocumentSnapshot) {
        AlertDialog.Builder(context)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ -> onDelete(doc) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount() = notifications.size

    fun updateData(newList: List<DocumentSnapshot>, tab: String) {
        this.notifications = newList
        this.activeTab = tab
        notifyDataSetChanged()
    }
}