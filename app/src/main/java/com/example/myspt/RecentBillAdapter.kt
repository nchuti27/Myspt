package com.example.myspt

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class RecentBillAdapter(private val billList: ArrayList<BillItem>) :
    RecyclerView.Adapter<RecentBillAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBillName: TextView = view.findViewById(R.id.tvBillName)
        val tvBillTotal: TextView = view.findViewById(R.id.tvBillTotal) // ✅ ปรับ ID ให้ตรงกับ UI ยอดเงิน
        val btnBillMenu: ImageView = view.findViewById(R.id.btnBillMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_bill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = billList[position]
        holder.tvBillName.text = currentItem.itemName
        holder.tvBillTotal.text = String.format("%.2f ฿", currentItem.price)

        // คลิกเพื่อดูรายละเอียดบิล
        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, BillDetail::class.java)
            intent.putExtra("BILL_ID", currentItem.id) // ส่ง ID ไปดึงข้อมูลละเอียด [cite: 2026-02-13]
            intent.putExtra("BILL_NAME", currentItem.itemName)
            view.context.startActivity(intent)
        }

        // เมนูจุด 3 จุดสำหรับลบ
        holder.btnBillMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Delete Bill").setOnMenuItemClickListener {
                confirmDelete(view.context, currentItem, position)
                true
            }
            popup.show()
        }
    }

    private fun confirmDelete(context: android.content.Context, item: BillItem, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete History")
            .setMessage("Are you sure you want to delete '${item.itemName}'?")
            .setPositiveButton("Delete") { _, _ ->
                // ✅ ลบข้อมูลจริงใน Firestore [cite: 2026-02-13]
                if (!item.id.isNullOrEmpty()) {
                    FirebaseFirestore.getInstance().collection("bills").document(item.id!!)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Bill deleted successfully", Toast.LENGTH_SHORT).show()
                            // ไม่ต้อง remove จาก list เองถ้าใช้ SnapshotListener ในหน้า Activity [cite: 2026-02-27]
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = billList.size
}