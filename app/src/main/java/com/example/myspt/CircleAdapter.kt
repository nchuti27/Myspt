package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ✅ แนะนำให้เพิ่ม Glide เพื่อโหลดรูปจาก URL [cite: 2026-02-23]
import com.google.android.material.imageview.ShapeableImageView

class CircleAdapter(
    private val items: List<CircleItem>,
    private val onClick: (CircleItem) -> Unit
) : RecyclerView.Adapter<CircleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ShapeableImageView = view.findViewById(R.id.imgItem)
        val name: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_circle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item.isAddButton) {
            holder.name.text = "Add"
            holder.img.setImageResource(android.R.drawable.ic_input_add)
            holder.img.setPadding(40, 40, 40, 40)
            holder.img.setStrokeWidth(0f)
        } else {
            holder.name.text = item.name
            holder.img.setPadding(0, 0, 0, 0)
            holder.img.setStrokeWidth(2f)

            // ✅ แก้ไขจาก imageUrl เป็น profileUrl ให้ตรงกับ CircleItem
            if (item.profileUrl.isNullOrEmpty()) {
                holder.img.setImageResource(R.drawable.ic_launcher_background)
            } else {
                // แนะนำให้ใช้ Glide โหลดรูปจริงแทนการโชว์แค่พื้นหลังครับ [cite: 2026-02-23]
                Glide.with(holder.itemView.context)
                    .load(item.profileUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.img)
            }
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}