package com.example.myspt // อย่าลืมเช็คบรรทัดนี้ให้ตรงกับชื่อ Package ของแอปคุณนะครับ

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 1. โครงสร้างข้อมูลสำหรับเก็บรายละเอียดหนี้
data class Debt(
    var debtId: String = "",
    var creditorId: String = "",
    var friendId: String = "",
    var name: String = "",
    var creditorName: String = "", // 🌟 เพิ่ม: ชื่อคนที่เราติดหนี้ (เจ้าหนี้)
    var billName: String = "",     // 🌟 เพิ่ม: ชื่อบิล/กลุ่ม
    var amount: Double = 0.0,
    var status: String = "pending"
)
// 2. ตัว Adapter สำหรับจัดการ RecyclerView
class DebtAdapter(
    private var debtList: List<Debt>,
    private val onDebtChecked: (Debt) -> Unit
) : RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {

    // เชื่อมต่อกับตัวแปรในหน้า item_debt.xml
    class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtBillDetail: TextView = itemView.findViewById(R.id.txtBillDetail) // 🌟 เพิ่มตัวแปรดึงชื่อบิล
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val cbDebt: CheckBox = itemView.findViewById(R.id.cbDebt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = debtList[position]

        // สำหรับ Owe You ฝั่งนี้ debt.name จะเป็นชื่อเพื่อนลูกหนี้ถูกต้องแล้วครับ
        holder.txtName.text = debt.name

        // 🌟 แสดงชื่อบิล
        holder.txtBillDetail.text = "บิล: ${debt.billName}"

        holder.txtAmount.text = "฿ ${String.format("%.2f", debt.amount)}"

        holder.cbDebt.setOnCheckedChangeListener(null)
        holder.cbDebt.isChecked = (debt.status == "paid")

        holder.cbDebt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onDebtChecked(debt)
            }
        }
    }

    override fun getItemCount(): Int {
        return debtList.size
    }

    fun updateData(newList: List<Debt>) {
        debtList = newList
        notifyDataSetChanged()
    }
}