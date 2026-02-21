package com.example.myspt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecentBillAdapter(private val billList: List<RecentBillItem>) :
    RecyclerView.Adapter<RecentBillAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_bill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, BillDetail::class.java)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount() = billList.size
}