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

class HomeGroupAdapter(
    private var groupList: List<CircleItem>,
    private val isListView: Boolean = false,
    private val onClick: (CircleItem) -> Unit,
    private val onLeaveClick: ((CircleItem) -> Unit)? = null // เพิ่มเพื่อรองรับปุ่ม Leave
) : RecyclerView.Adapter<HomeGroupAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, isListView: Boolean) : RecyclerView.ViewHolder(itemView) {
        val imgGroup: ImageView = itemView.findViewById(if (isListView) R.id.ivGroupIcon else R.id.imgItem)
        val tvGroupName: TextView = itemView.findViewById(if (isListView) R.id.tvGroupName else R.id.tvName)

        // ตัวแปรสำหรับ ListView (See More) เท่านั้น
        val btnMore: ImageButton? = if (isListView) itemView.findViewById(R.id.btnMore) else null
        val layoutExpandOptions: LinearLayout? = if (isListView) itemView.findViewById(R.id.layoutExpandOptions) else null
        val btnGroupDetail: Button? = if (isListView) itemView.findViewById(R.id.btnGroupDetail) else null
        val btnLeaveGroup: Button? = if (isListView) itemView.findViewById(R.id.btnLeaveGroup) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (isListView) R.layout.item_group_list else R.layout.item_circle
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view, isListView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = groupList[position]

        // 1. ตั้งค่าข้อมูลพื้นฐาน (ชื่อและรูป)
        if (currentItem.isAddButton) {
            holder.tvGroupName.text = "Add"
            holder.imgGroup.setImageResource(android.R.drawable.ic_input_add)
            holder.imgGroup.setPadding(40, 40, 40, 40)
        } else {
            holder.tvGroupName.text = currentItem.name
            holder.imgGroup.setPadding(0, 0, 0, 0)
            holder.imgGroup.setImageResource(R.drawable.ic_launcher_background)
        }

        // 2. จัดการส่วนขยาย (เฉพาะหน้า Grouplist ที่เป็น ListView)
        if (isListView) {
            // เช็คว่าแถวนี้ถูกกางออกหรือไม่
            holder.layoutExpandOptions?.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

            // ปุ่ม 3 จุด: กดแล้วสลับสถานะกางออก/พับเก็บ
            holder.btnMore?.setOnClickListener {
                currentItem.isExpanded = !currentItem.isExpanded
                notifyItemChanged(position)
            }

            // ปุ่ม Group Detail ในเมนูที่กางออกมา
            holder.btnGroupDetail?.setOnClickListener {
                currentItem.isExpanded = false // ปิดเมนูก่อนไปหน้าใหม่
                notifyItemChanged(position)
                onClick(currentItem)
            }

            // ปุ่ม Leave Group สีแดง
            holder.btnLeaveGroup?.setOnClickListener {
                currentItem.isExpanded = false
                notifyItemChanged(position)
                onLeaveClick?.invoke(currentItem)
            }
        } else {
            // โหมดหน้า Home ปกติ กดแล้วไปหน้า Detail เลย
            holder.itemView.setOnClickListener { onClick(currentItem) }
        }
    }

    override fun getItemCount(): Int = groupList.size

    fun updateData(newList: List<CircleItem>) {
        groupList = newList
        notifyDataSetChanged()
    }
}