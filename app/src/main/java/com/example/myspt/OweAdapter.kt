package com.example.myspt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class OweAdapter(private val oweList: List<OweItem>) :
    RecyclerView.Adapter<OweAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFriendName: TextView = view.findViewById(R.id.tvFriendName)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friends_owe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = oweList[position]
        holder.tvFriendName.text = item.friendName

        when {
            item.amount > 0.01 -> {
                // ยังต้องจ่ายอีก
                holder.tvAmount.text = String.format(Locale.getDefault(), "Owes: %.2f ฿", item.amount)
                holder.tvAmount.setTextColor(Color.RED)
            }
            item.amount < -0.01 -> {
                // จ่ายเกิน — รอรับคืน
                holder.tvAmount.text = String.format(Locale.getDefault(), "Gets back: %.2f ฿", kotlin.math.abs(item.amount))
                holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"))
            }
            else -> {
                // จ่ายครบพอดี
                holder.tvAmount.text = "✓ Settled"
                holder.tvAmount.setTextColor(Color.GRAY)
            }
        }
    }

    override fun getItemCount() = oweList.size
}