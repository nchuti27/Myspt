package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemberListAdapter(
    private val memberList: List<CircleItem>,
    private val onRemoveClick: (String) -> Unit // รับฟังก์ชันลบจาก Activity
) : RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMember: ImageView = view.findViewById(R.id.imgMemberAvatar)
        val tvMemberName: TextView = view.findViewById(R.id.tvMemberName)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member_manage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = memberList[position]
        holder.tvMemberName.text = member.name

        // เมื่อกดปุ่มกากบาท ให้ส่ง ID ของสมาชิกคนนั้นไปทำงานต่อ
        holder.btnRemove.setOnClickListener {
            onRemoveClick(member.id)
        }
    }

    override fun getItemCount(): Int = memberList.size
}