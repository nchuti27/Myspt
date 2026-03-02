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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = notifications[position]
        val type = doc.getString("type")

        if (type == "debt_reminder") {
            // --- ภาษาอังกฤษสำหรับ Debt ---
            holder.tvName.text = "Debt Reminder"
            val message = doc.getString("message") ?: "You have an outstanding debt"
            holder.tvMessage.text = message
                .replace("ทวงหนี้: บิล", "Debt: Bill")
                .replace("จำนวน", "Amount")
                .replace("คุณมีหนี้ค้างชำระ", "You have an outstanding debt")

            holder.btnAccept.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_background)
        } else {
            // --- ภาษาอังกฤษสำหรับ Friend/Group ---
            val fromName = doc.getString("from_name") ?: "Someone"
            holder.tvName.text = fromName
            holder.tvMessage.text = "sent you a friend request."

            holder.btnAccept.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        }

        // เมนูสามจุดสำหรับลบ
        holder.ivMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Delete")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Delete") {
                    showDeleteConfirmation(view.context, doc)
                }
                true
            }
            popup.show()
        }

        holder.btnAccept.setOnClickListener { onAccept(doc) }
        holder.btnDelete.setOnClickListener { onDelete(doc) }
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