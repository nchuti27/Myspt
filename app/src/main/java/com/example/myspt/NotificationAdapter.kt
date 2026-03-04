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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = notifications[position]
        val type = doc.getString("type")

        holder.btnAccept.visibility = View.VISIBLE
        holder.btnDelete.visibility = View.VISIBLE
        holder.btnDelete.text = "Delete"
        holder.ivMore.visibility = View.VISIBLE
        holder.imgAvatar.setImageResource(R.drawable.outline_person)

        if (type == "debt_reminder" || type == "PAYMENT_RECEIVED") {

            holder.tvName.text = if (type == "debt_reminder") "Debt Reminder" else "Payment Received"
            holder.tvMessage.text = doc.getString("message") ?: ""


            holder.btnAccept.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
            holder.imgAvatar.setImageResource(R.drawable.outline_money)

        } else {
            when (activeTab) {
                "FRIEND" -> {
                    holder.tvName.text = doc.getString("from_name") ?: "Someone"
                    holder.tvMessage.text = "sent you a friend request."
                    holder.btnDelete.text = "Decline"
                }
                "GROUP" -> {
                    holder.tvName.text = doc.getString("groupName") ?: "Unknown Group"
                    holder.tvMessage.text = "invited you to join."
                    holder.btnDelete.text = "Decline"
                }
                "REQUEST" -> {
                    holder.tvName.text = doc.getString("to_name") ?: "Waiting for user..."
                    holder.tvMessage.text = "Waiting for approval..."
                    holder.btnAccept.visibility = View.GONE
                    holder.btnDelete.text = "Cancel"
                }
            }


            val profileUrl = doc.getString("from_profileUrl") ?: doc.getString("to_profileUrl")
            if (!profileUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context).load(profileUrl).into(holder.imgAvatar)
            }
        }

        holder.ivMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)

            if (type == "debt_reminder" || type == "PAYMENT_RECEIVED") {
                popup.menu.add("Delete")
            } else {
                popup.menu.add("View Profile")
                popup.menu.add("Delete")
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Delete" -> onDelete(doc)
                    "View Profile" -> {
                        val intent = Intent(view.context, FriendProfile::class.java).apply {
                            val uid = if (activeTab == "REQUEST") doc.getString("to_uid") else doc.getString("from_uid")
                            val name = if (activeTab == "REQUEST") doc.getString("to_name") else doc.getString("from_name")
                            putExtra("FRIEND_UID", uid)
                            putExtra("FRIEND_NAME", name)
                            putExtra("IS_FRIEND", false)
                        }
                        view.context.startActivity(intent)
                    }
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