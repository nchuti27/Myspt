package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ✅ ใช้ Glide โหลดรูป [cite: 2026-02-23]
import com.google.android.material.imageview.ShapeableImageView

class GroupAdapter(
    private val groups: List<Group>,
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProfile: ShapeableImageView = view.findViewById(R.id.ivFriendProfile)
        val groupName: TextView = view.findViewById(R.id.tvFriendName)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_list, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.groupName.text = group.name

        Glide.with(holder.itemView.context)
            .load(group.profileUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(holder.imgProfile)

        holder.itemView.setOnClickListener { onClick(group) }

        holder.btnMore.setOnClickListener {

        }
    }

    override fun getItemCount() = groups.size
}