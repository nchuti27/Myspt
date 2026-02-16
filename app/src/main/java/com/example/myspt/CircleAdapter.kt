package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
            holder.name.text = "เพิ่ม"

            holder.img.setImageResource(android.R.drawable.ic_input_add)
            holder.img.setPadding(40, 40, 40, 40)
            holder.img.setStrokeWidth(0f)
        } else {
            holder.name.text = item.name
            holder.img.setPadding(0, 0, 0, 0)
            holder.img.setStrokeWidth(2f)

            if (item.imageUrl.isEmpty()) {
                holder.img.setImageResource(R.drawable.ic_launcher_background)
            }
        }


        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}