package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // ✅ ใช้ Glide สำหรับรูปภาพ [cite: 2026-02-23]

class HomeGroupAdapter(
    private var groupList: List<CircleItem>,
    private val isListView: Boolean = false,
    private val onClick: (CircleItem) -> Unit,
    private val onLeaveClick: ((CircleItem) -> Unit)? = null
) : RecyclerView.Adapter<HomeGroupAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, isListView: Boolean) : RecyclerView.ViewHolder(itemView) {
        val imgGroup: ImageView? = if (isListView)
            itemView.findViewById(R.id.ivGroupIcon) else itemView.findViewById(R.id.imgItem)

        val tvGroupName: TextView? = if (isListView)
            itemView.findViewById(R.id.tvGroupName) else itemView.findViewById(R.id.tvName)

        val btnMore: ImageButton? = itemView.findViewById(R.id.btnMore)
        val layoutExpandOptions: LinearLayout? = itemView.findViewById(R.id.layoutExpandOptions)
        val btnGroupDetail: Button? = itemView.findViewById(R.id.btnGroupDetail)
        val btnLeaveGroup: Button? = itemView.findViewById(R.id.btnLeaveGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (isListView) R.layout.item_group_list else R.layout.item_circle
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view, isListView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = groupList[position]

        holder.tvGroupName?.text = if (currentItem.isAddButton) "Add" else currentItem.name

        // ✅ โหลดรูปภาพกลุ่ม/โปรไฟล์
        if (currentItem.isAddButton) {
            holder.imgGroup?.setImageResource(android.R.drawable.ic_input_add)
            holder.imgGroup?.setPadding(40, 40, 40, 40)
        } else {
            // ใช้ Glide โหลดรูปภาพ ถ้าไม่มีให้ใช้รูปพื้นฐาน [cite: 2026-02-23]
            holder.imgGroup?.let {
                Glide.with(holder.itemView.context)
                    .load(currentItem.profileUrl ?: R.drawable.ic_launcher_background)
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(it)
            }
            holder.imgGroup?.setPadding(0, 0, 0, 0)
        }

        if (isListView) {
            holder.layoutExpandOptions?.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

            holder.btnMore?.setOnClickListener {
                currentItem.isExpanded = !currentItem.isExpanded
                notifyItemChanged(position)
            }

            holder.btnGroupDetail?.setOnClickListener {
                currentItem.isExpanded = false
                onClick(currentItem)
            }

            holder.btnLeaveGroup?.setOnClickListener {
                currentItem.isExpanded = false
                onLeaveClick?.invoke(currentItem)
            }
        } else {
            holder.itemView.setOnClickListener { onClick(currentItem) }
        }
    }

    override fun getItemCount(): Int = groupList.size

    fun updateData(newList: List<CircleItem>) {
        this.groupList = newList
        notifyDataSetChanged()
    }
}