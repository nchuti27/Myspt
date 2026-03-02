package com.example.myspt

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// ✅ รวมคลาส Debt ไว้ในไฟล์นี้เพื่อแก้ปัญหา Unresolved reference
data class Debt(
    var debtId: String = "",
    var creditorId: String = "",
    var friendId: String = "",
    var name: String = "",
    var creditorName: String = "",
    var billName: String = "",
    var amount: Double = 0.0,
    var status: String = "pending"
)

class DebtAdapter(
    private var debtList: List<Debt>,
    private val onDebtChecked: (Debt) -> Unit
) : RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {

    class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtBillDetail: TextView = itemView.findViewById(R.id.txtBillDetail)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val cbDebt: CheckBox = itemView.findViewById(R.id.cbDebt)
        val btnNotification: ImageView = itemView.findViewById(R.id.btnNotification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debt, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val debt = debtList[position]

        holder.txtName.text = debt.name
        holder.txtBillDetail.text = "บิล: ${debt.billName}"
        holder.txtAmount.text = "฿ ${String.format("%.2f", debt.amount)}"

        holder.cbDebt.isChecked = (debt.status == "paid")
        holder.cbDebt.setOnClickListener { onDebtChecked(debt) }

        // ✅ แก้ไข: ใช้ holder.itemView.context แทนคำว่า context
        holder.btnNotification.setOnClickListener {
            sendDebtNotification(holder.itemView.context, debt)
        }
    }

    private fun sendDebtNotification(context: Context, debt: Debt) {
        val db = FirebaseFirestore.getInstance()
        val notiData = hashMapOf(
            "to_uid" to debt.friendId,
            "from_uid" to debt.creditorId,
            "message" to "ทวงหนี้: บิล ${debt.billName} จำนวน ฿${debt.amount}",
            "type" to "debt_reminder",
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("notifications").add(notiData)
            .addOnSuccessListener {
                Toast.makeText(context, "ส่งแจ้งเตือนไปหาเพื่อนแล้ว", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "ส่งแจ้งเตือนไม่สำเร็จ", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = debtList.size

    fun updateData(newList: List<Debt>) {
        debtList = newList
        notifyDataSetChanged()
    }
}