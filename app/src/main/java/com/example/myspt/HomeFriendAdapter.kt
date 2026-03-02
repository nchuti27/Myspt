package com.example.myspt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class HomeFriendAdapter(private val friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<HomeFriendAdapter.HomeFriendViewHolder>() {

    class HomeFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ShapeableImageView = itemView.findViewById(R.id.ivFriendProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvFriendName)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_list, parent, false)
        return HomeFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFriendViewHolder, position: Int) {
        val currentItem = friendList[position]
        val context = holder.itemView.context

        holder.tvName.text = currentItem.name

        // โหลดรูปวงกลมด้วย Glide
        Glide.with(context)
            .load(currentItem.profileUrl ?: R.drawable.outline_person)
            .placeholder(R.drawable.outline_person)
            .circleCrop()
            .into(holder.ivProfile)

        // 🌟 ระบบ PopupMenu สำหรับดูโปรไฟล์และลบเพื่อน
        holder.btnMore.setOnClickListener { view ->
            val popupMenu = PopupMenu(context, view)
            popupMenu.menu.add("View Profile")
            popupMenu.menu.add("Remove Friend")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "View Profile" -> {
                        val intent = Intent(context, FriendProfile::class.java).apply {
                            putExtra("FRIEND_UID", currentItem.uid)
                            putExtra("FRIEND_NAME", currentItem.name)
                            putExtra("FRIEND_IMG", currentItem.profileUrl)
                        }
                        context.startActivity(intent)
                        true
                    }
                    "Remove Friend" -> {
                        AlertDialog.Builder(context)
                            .setTitle("Remove Friend")
                            .setMessage("Do you want to remove ${currentItem.name}?")
                            .setPositiveButton("Remove") { _, _ ->
                                removeFriendFromFirestore(currentItem.uid, context, holder.bindingAdapterPosition)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // กดที่ตัว Item เพื่อไปหน้าโปรไฟล์โดยตรง
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FriendProfile::class.java).apply {
                putExtra("FRIEND_UID", currentItem.uid)
                putExtra("FRIEND_NAME", currentItem.name)
                putExtra("FRIEND_IMG", currentItem.profileUrl)
            }
            context.startActivity(intent)
        }
    }

    // ฟังก์ชันลบเพื่อนออกจาก Firestore
    private fun removeFriendFromFirestore(friendUid: String, context: android.content.Context, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(myUid)
            .update("friends", FieldValue.arrayRemove(friendUid))
            .addOnSuccessListener {
                if (position != RecyclerView.NO_POSITION && position < friendList.size) {
                    friendList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, friendList.size)
                }
                Toast.makeText(context, "Removed friend successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = friendList.size
}