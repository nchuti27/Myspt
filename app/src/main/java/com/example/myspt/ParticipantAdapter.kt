package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ✅ เพิ่ม Glide สำหรับโหลดรูป [cite: 2026-02-23]

class ParticipantAdapter(
    private val participantList: MutableList<ParticipantData>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ParticipantAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMemberName: TextView = view.findViewById(R.id.tvMemberName)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
        val imgMemberAvatar: ImageView = view.findViewById(R.id.imgMemberAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member_manage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participantList[position]

        holder.tvMemberName.text = participant.name

        // ✅ โหลดรูปโปรไฟล์เพื่อน (ถ้าใน ParticipantData มีฟิลด์ profileUrl)
        Glide.with(holder.itemView.context)
            .load(participant.profileUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(holder.imgMemberAvatar)

        holder.btnRemove.setOnClickListener {
            onDeleteClick(participant.uid)
        }
    }

    override fun getItemCount(): Int = participantList.size
}