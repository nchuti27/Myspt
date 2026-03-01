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
        val myUid = FirebaseAuth.getInstance().currentUser?.uid

        // 🌟 ดึงข้อมูลจาก Firestore
        val fromName = doc.getString("from_name") ?: "Someone"
        val fromProfileUrl = doc.getString("from_profileUrl")
        val toName = doc.getString("to_name") ?: "Someone"
        val toProfileUrl = doc.getString("to_profileUrl")
        val groupName = doc.getString("groupName")

        // 🌟 แยกการแสดงผลตาม Tab
        when (activeTab) {
            "REQUEST" -> {
                // หน้าเราส่งหาเขา: โชว์ชื่อคนรับ (toName)
                holder.tvName.text = toName
                holder.tvMessage.text = "Waiting for approval..."
                holder.btnAccept.visibility = View.GONE
                loadImg(holder, toProfileUrl)
            }
            "GROUP" -> {
                // หน้าเชิญเข้ากลุ่ม: โชว์ชื่อคนเชิญ (fromName) + ชื่อกลุ่ม
                holder.tvName.text = fromName
                holder.tvMessage.text = "invited you to join: ${groupName ?: "a group"}"
                holder.btnAccept.visibility = View.VISIBLE
                loadImg(holder, fromProfileUrl)
            }
            else -> { // Tab FRIEND
                // หน้าคนอื่นขอเรา: โชว์ชื่อคนส่ง (fromName)
                holder.tvName.text = fromName
                holder.tvMessage.text = "sent you a friend request."
                holder.btnAccept.visibility = View.VISIBLE
                loadImg(holder, fromProfileUrl)
            }
        }

        // กันพลาด: ถ้าเป็นรายการที่เราส่งเอง ให้ซ่อนปุ่ม Accept เสมอ
        if (doc.getString("from_uid") == myUid) holder.btnAccept.visibility = View.GONE

        holder.btnAccept.setOnClickListener { onAccept(doc) }
        holder.btnDelete.setOnClickListener { onDelete(doc) }
    }

    private fun loadImg(holder: ViewHolder, url: String?) {
        Glide.with(holder.itemView.context)
            .load(url ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(holder.imgAvatar)
    }

    override fun getItemCount() = notifications.size

    fun updateData(newList: List<DocumentSnapshot>, tab: String) {
        this.notifications = newList
        this.activeTab = tab
        notifyDataSetChanged()
    }
}