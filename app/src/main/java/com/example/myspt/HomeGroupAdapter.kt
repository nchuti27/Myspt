
package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class HomeGroupAdapter(
    private val groupList: List<CircleItem>,
    private val onClick: (CircleItem) -> Unit
) : RecyclerView.Adapter<HomeGroupAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgGroup: ShapeableImageView = itemView.findViewById(R.id.imgItem)
        val tvGroupName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_circle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = groupList[position]
        holder.tvGroupName.text = currentItem.name
        holder.imgGroup.setImageResource(R.drawable.ic_launcher_background)
        holder.itemView.setOnClickListener { onClick(currentItem) }
    }

    override fun getItemCount(): Int = groupList.size
}