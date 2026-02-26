package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemberListAdapter(private val memberList: List<CircleItem>) : RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // อ้างอิง ID จากไฟล์ item_member_list.xml (หรือชื่อไฟล์ที่คุณตั้ง)
        val imgMember: ImageView = view.findViewById(R.id.imgMember)
        val tvMemberName: TextView = view.findViewById(R.id.tvMemberName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 🚨 ตรง R.layout.item_member_list ให้แก้เป็นชื่อไฟล์ XML ของคุณนะครับ
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = memberList[position]
        holder.tvMemberName.text = member.name
        holder.imgMember.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun getItemCount(): Int = memberList.size
}