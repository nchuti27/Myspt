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
    var friendId: String = "",
    var name: String = "",
    var amount: Double = 0.0,
    var status: String = "pending"
)

// 2. ตัว Adapter สำหรับจัดการ RecyclerView
class DebtAdapter(
    private var debtList: List<Debt>,
    private val onDebtChecked: (Debt) -> Unit // ฟังก์ชันส่งข้อมูลกลับเมื่อมีการติ๊ก CheckBox
) : RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {

    // เชื่อมต่อกับตัวแปรในหน้า item_debt.xml
    class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        // นี่คือ cbDebt ที่เราพูดถึงกันครับ ต้องไปตั้ง ID ในไฟล์ item_debt.xml ด้วยนะ
        val cbDebt: CheckBox = itemView.findViewById(R.id.cbDebt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        // ตรงนี้คือจุดที่ Adapter ไปดึงไฟล์ item_debt.xml มาใช้งานครับ
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = debtList[position]

        // นำข้อมูลมาแสดงบนหน้าจอ
        holder.txtName.text = debt.name
        holder.txtAmount.text = "฿ ${String.format("%.2f", debt.amount)}"

        // รีเซ็ตสถานะ CheckBox ก่อน เพื่อป้องกันบั๊กเวลามันเลื่อนหน้าจอ
        holder.cbDebt.setOnCheckedChangeListener(null)
        holder.cbDebt.isChecked = (debt.status == "paid")

        // ดักจับเหตุการณ์ตอนที่ผู้ใช้กดติ๊กถูก (CheckBox)
        holder.cbDebt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // ถ้าถูกติ๊ก ให้ส่งข้อมูลหนี้ก้อนนี้กลับไปให้ Activity จัดการต่อ (ส่งไปอัปเดต Firebase)
                onDebtChecked(debt)
            }
        }
    }

    override fun getItemCount(): Int {
        return debtList.size
    }

    // ฟังก์ชันสำหรับอัปเดตข้อมูลใน List เมื่อมีการเปลี่ยนแปลงจาก Firebase
    fun updateData(newList: List<Debt>) {
        debtList = newList
        notifyDataSetChanged()
    }
}