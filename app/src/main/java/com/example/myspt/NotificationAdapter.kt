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
import android.content.Intent

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

        // 1. จัดการการแสดงผลตามแท็บที่เลือก (Active Tab)
        when (activeTab) {
            "FRIEND" -> {
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
                holder.btnDelete.text = "Decline"
            }
            "REQUEST" -> {
                // แท็บคำขอที่เราส่ง: โชว์ชื่อคนรับ (to_name) และซ่อนปุ่ม Accept
                val toName = doc.getString("to_name") ?: "Waiting for user..."
                holder.tvName.text = toName
                holder.tvMessage.text = "Waiting for approval..."
                holder.btnAccept.visibility = View.GONE
                holder.btnDelete.text = "Cancel"
            }
        }

        // 2. กรณีแจ้งเตือนทวงเงิน (Debt Reminder)
        if (type == "debt_reminder") {
            holder.tvName.text = "Debt Reminder"
            holder.tvMessage.text = doc.getString("message") ?: "You have a pending debt."
            holder.btnAccept.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }

        // 🌟 3. ปุ่ม 3 จุดสำหรับดูโปรไฟล์ (View Profile) - ซ่อน QR Code
        holder.ivMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("View Profile")

            popup.setOnMenuItemClickListener { item ->
                if (item.title == "View Profile") {
                    val intent = Intent(view.context, FriendProfile::class.java).apply {
                        if (activeTab == "REQUEST") {
                            putExtra("FRIEND_UID", doc.getString("to_uid"))
                            putExtra("FRIEND_NAME", doc.getString("to_name"))
                        } else {
                            putExtra("FRIEND_UID", doc.getString("from_uid"))
                            putExtra("FRIEND_NAME", doc.getString("from_name"))
                        }
                        putExtra("IS_FRIEND", false) // ส่ง false เสมอเพื่อซ่อน QR
                    }
                    view.context.startActivity(intent)
                }
                true
            }
            popup.show()
        }

        // 4. ผูกการทำงานปุ่มกด (OnClickListener)
        holder.btnAccept.setOnClickListener { onAccept(doc) }
        // ใน NotificationAdapter.kt ตรงปุ่มลบรายบุคคล
        holder.btnDelete.setOnClickListener {
            // 🌟 เรียก onDelete ที่ส่งมาจาก Activity
            onDelete(doc)
        }
    } // 🌟 ปิดฟังก์ชัน onBindViewHolder (จุดที่พี่มักจะลืม)

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