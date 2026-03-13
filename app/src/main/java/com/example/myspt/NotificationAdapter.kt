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
        val type = doc.getString("type") ?: ""

        holder.btnAccept.visibility = View.GONE
        holder.btnDelete.visibility = View.GONE
        holder.ivMore.visibility = View.VISIBLE
        holder.imgAvatar.setOnClickListener(null) // ✅ reset ก่อนทุกครั้ง

        when (type) {
            "debt_reminder" -> {
                holder.tvName.text = "Payment Reminder"
                holder.tvMessage.text = doc.getString("message") ?: "You have a pending debt"
                holder.imgAvatar.setImageResource(R.drawable.outline_money)
            }
            "PAYMENT_RECEIVED" -> {
                holder.tvName.text = "Payment Received"
                holder.tvMessage.text = doc.getString("message") ?: ""
                holder.imgAvatar.setImageResource(R.drawable.outline_money)
            }
            "friend_accepted" -> {
                holder.tvName.text = doc.getString("from_name") ?: "Someone"
                holder.tvMessage.text = "accepted your friend request"
                holder.imgAvatar.setImageResource(R.drawable.outline_person)
                holder.imgAvatar.setOnClickListener {
                    val fromUid = doc.getString("from_uid") ?: return@setOnClickListener
                    val fromName = doc.getString("from_name") ?: "Unknown"
                    val myUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(myUid).get()
                        .addOnSuccessListener { userDoc ->
                            @Suppress("UNCHECKED_CAST")
                            val friends = userDoc.get("friends") as? List<String> ?: listOf()
                            val intent = Intent(holder.itemView.context, FriendProfile::class.java)
                            intent.putExtra("FRIEND_UID", fromUid)
                            intent.putExtra("FRIEND_NAME", fromName)
                            intent.putExtra("IS_FRIEND", friends.contains(fromUid))
                            holder.itemView.context.startActivity(intent)
                        }
                }
            }
            else -> {
                when (activeTab) {
                    "FRIEND" -> {
                        holder.tvName.text = doc.getString("from_name") ?: "Someone"
                        holder.tvMessage.text = "sent you a friend request."
                        holder.btnAccept.visibility = View.VISIBLE
                        holder.btnDelete.visibility = View.VISIBLE
                        holder.btnDelete.text = "Decline"
                        holder.imgAvatar.setOnClickListener {
                            val fromUid = doc.getString("from_uid") ?: return@setOnClickListener
                            val fromName = doc.getString("from_name") ?: "Unknown"
                            val myUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(myUid).get()
                                .addOnSuccessListener { userDoc ->
                                    @Suppress("UNCHECKED_CAST")
                                    val friends = userDoc.get("friends") as? List<String> ?: listOf()
                                    val intent = Intent(holder.itemView.context, FriendProfile::class.java)
                                    intent.putExtra("FRIEND_UID", fromUid)
                                    intent.putExtra("FRIEND_NAME", fromName)
                                    intent.putExtra("IS_FRIEND", friends.contains(fromUid))
                                    holder.itemView.context.startActivity(intent)
                                }
                        }
                    }
                    "GROUP" -> {
                        holder.tvName.text = doc.getString("groupName") ?: "Unknown Group"
                        holder.tvMessage.text = "invited you to join."
                        holder.btnAccept.visibility = View.VISIBLE
                        holder.btnDelete.visibility = View.VISIBLE
                        holder.btnDelete.text = "Decline"
                        holder.imgAvatar.setOnClickListener {
                            val groupId = doc.getString("groupId") ?: return@setOnClickListener
                            android.util.Log.d("NOTI", "groupId = $groupId")  // ✅ เช็ค log ก่อน
                            val intent = Intent(holder.itemView.context, GroupDetail::class.java)
                            intent.putExtra("GROUP_ID", groupId)
                            intent.putExtra("READ_ONLY", true)
                            holder.itemView.context.startActivity(intent)
                        }
                    }
                    "REQUEST" -> {
                        holder.tvName.text = doc.getString("to_name") ?: "Waiting..."
                        holder.tvMessage.text = "Waiting for approval..."
                        holder.btnDelete.visibility = View.VISIBLE
                        holder.btnDelete.text = "Cancel"
                        holder.imgAvatar.setOnClickListener(null)
                    }
                }
                val profileUrl = doc.getString("from_profileUrl")
                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(holder.itemView.context).load(profileUrl).into(holder.imgAvatar)
                }
            }
        }

        holder.btnAccept.setOnClickListener { onAccept(doc) }
        holder.btnDelete.setOnClickListener { onDelete(doc) }

        holder.ivMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Delete")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Delete") onDelete(doc)
                true
            }
            popup.show()
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