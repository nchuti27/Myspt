package com.example.myspt

import android.content.Context
import android.content.Intent
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
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
        val layoutActions: LinearLayout = itemView.findViewById(R.id.layoutActions)
        val divider: View = itemView.findViewById(R.id.divider)
        val btnFriendDetail: MaterialButton = itemView.findViewById(R.id.btnFriendDetail)
        val btnRemoveFriend: MaterialButton = itemView.findViewById(R.id.btnRemoveFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_list, parent, false)
        return HomeFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFriendViewHolder, position: Int) {
        val currentItem = friendList[position]
        val context = holder.itemView.context

        holder.tvName.text = currentItem.name

        // ✅ กางหรือซ่อนแผงตามสถานะในข้อมูล (เหมือน Group List)
        holder.layoutActions.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE
        holder.divider.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

        Glide.with(context)
            .load(currentItem.profileUrl ?: R.drawable.outline_person)
            .circleCrop()
            .into(holder.ivProfile)

        // ✅ ปุ่ม 3 จุด: สลับสถานะเปิด/ปิด (Toggle)
        holder.btnMore.setOnClickListener {
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(position)
        }

        // ✅ ปุ่ม Remove Friend: ค่อยเด้ง Dialog ในนี้
        holder.btnRemoveFriend.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Remove Friend")
                .setMessage("Do you want to remove ${currentItem.name}?")
                .setPositiveButton("Remove") { _, _ ->
                    removeFriendFromFirestore(currentItem.uid, context, holder.bindingAdapterPosition)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    currentItem.isExpanded = false
                    notifyItemChanged(position)
                    dialog.dismiss()
                }
                .show()
        }

        // ✅ ปุ่ม Friend Detail
        holder.btnFriendDetail.setOnClickListener {
            val intent = Intent(context, FriendProfile::class.java).apply {
                putExtra("FRIEND_UID", currentItem.uid)
                putExtra("FRIEND_NAME", currentItem.name)
                putExtra("FRIEND_IMG", currentItem.profileUrl)
                // 🌟 บรรทัดสำคัญที่ห้ามลืม! บอกหน้าโปรไฟล์ว่าเป็นเพื่อนกันแล้ว
                putExtra("IS_FRIEND", true)
            }
            context.startActivity(intent)
        }
    }

    fun updateData(newList: ArrayList<FriendData>) {
        friendList.clear()
        friendList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun removeFriendFromFirestore(friendUid: String, context: Context, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(myUid)
            .update("friends", FieldValue.arrayRemove(friendUid))
            .addOnSuccessListener {
                if (position != RecyclerView.NO_POSITION && position < friendList.size) {
                    friendList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, friendList.size)
                }
                Toast.makeText(context, "Removed successfully", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = friendList.size
}