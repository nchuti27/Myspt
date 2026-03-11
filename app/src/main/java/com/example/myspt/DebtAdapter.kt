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
import com.google.firebase.firestore.Exclude
data class Debt(
var debtId: String = "",
var billId: String = "",
var creditorId: String = "",
var friendId: String = "",
var name: String = "",
var creditorName: String = "",
var billName: String = "",
var amount: Double = 0.0,
var status: String = "pending",

@get:Exclude
@set:Exclude
var timestamp: Any? = null  // ✅ รับ field แต่ไม่ map เข้า class
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

        // ✅ Owe You = คนอื่นค้างเรา → แสดงชื่อลูกหนี้ (friendId)
        holder.txtName.text = debt.name.ifEmpty { "Unknown" }
        holder.txtBillDetail.text = "Bill: ${debt.billName}"
        holder.txtAmount.text = "฿ ${String.format("%.2f", debt.amount)}"

        holder.cbDebt.setOnCheckedChangeListener(null)
        holder.cbDebt.isChecked = (debt.status == "paid")

        holder.cbDebt.setOnClickListener { onDebtChecked(debt) }

        holder.btnNotification.setOnClickListener {
            sendDebtNotification(holder.itemView.context, debt)
        }
    }
    private fun sendDebtNotification(context: Context, debt: Debt) {
        val db = FirebaseFirestore.getInstance()
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: return

        // ✅ ดึงชื่อจาก Firestore แทน displayName
        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            val myName = doc.getString("name") ?: auth.currentUser?.displayName ?: "A friend"

            val notiData = hashMapOf(
                "to_uid"    to debt.friendId,
                "from_uid"  to debt.creditorId,
                "from_name" to myName,  // ✅ ชื่อจริงจาก Firestore
                "message"   to "Payment Reminder: ${debt.billName} | Amount: ฿${String.format("%.2f", debt.amount)}",
                "type"      to "debt_reminder",
                "status"    to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("notifications").add(notiData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Notification sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Unable to send reminder.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = debtList.size

    fun updateData(newList: List<Debt>) {
        debtList = newList
        notifyDataSetChanged()
    }
}