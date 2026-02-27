package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParticipantAdapter(
    private val participantList: MutableList<ParticipantData>,
    private val onDeleteClick: (String) -> Unit // ส่ง UID กลับไปเพื่อลบออก
) : RecyclerView.Adapter<ParticipantAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // แก้ไขชื่อ ID และชนิดของตัวแปรให้ตรงกับ XML ของคุณ
        val tvMemberName: TextView = view.findViewById(R.id.tvMemberName)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
        val imgMemberAvatar: ImageView = view.findViewById(R.id.imgMemberAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // อ้างอิงชื่อไฟล์ XML ให้ตรงกับที่คุณใช้ในหน้า CreateGroup
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member_manage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participantList[position]

        // ใส่ชื่อเพื่อนลงใน TextView
        holder.tvMemberName.text = participant.name

        // เมื่อกดปุ่มกากบาทลบเพื่อน
        holder.btnRemove.setOnClickListener {
            onDeleteClick(participant.uid)
        }
    }

    override fun getItemCount(): Int = participantList.size
}