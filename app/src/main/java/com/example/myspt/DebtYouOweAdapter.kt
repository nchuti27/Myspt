package com.example.myspt

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DebtYouOweAdapter(private var debtList: List<Debt>) : RecyclerView.Adapter<DebtYouOweAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtBillDetail: TextView = itemView.findViewById(R.id.txtBillDetail)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt_you_owe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val debt = debtList[position]


        holder.txtName.text = debt.creditorName.ifEmpty { "Unknown" }

        val context = holder.itemView.context
        holder.txtBillDetail.text = context.getString(R.string.bill_detail_format, debt.billName)
        holder.txtAmount.text = context.getString(R.string.amount_format, debt.amount)
    }

    override fun getItemCount(): Int = debtList.size
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Debt>) {
        debtList = newList
        notifyDataSetChanged()
    }
}