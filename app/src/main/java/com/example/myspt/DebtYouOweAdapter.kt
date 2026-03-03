package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DebtYouOweAdapter(private var debtList: List<Debt>) : RecyclerView.Adapter<DebtYouOweAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtBillDetail: TextView = itemView.findViewById(R.id.txtBillDetail) // 🌟 เพิ่มตัวแปรนี้
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt_you_owe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val debt = debtList[position]

        val displayName = if (debt.creditorName.isNullOrEmpty() || debt.creditorName == "Unknown") {
            debt.name
        } else {
            debt.creditorName
        }
        holder.txtName.text = displayName

        val displayBill = debt.billName

        holder.txtBillDetail.text = "Bill: $displayBill"

        holder.txtAmount.text = "฿ ${String.format("%.2f", debt.amount)}"
    }

    override fun getItemCount(): Int = debtList.size

    fun updateData(newList: List<Debt>) {
        debtList = newList
        notifyDataSetChanged()
    }
}