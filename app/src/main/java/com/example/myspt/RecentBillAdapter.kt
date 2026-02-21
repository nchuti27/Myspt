package com.example.myspt

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
        val tvBillTotal: TextView = view.findViewById(R.id.tvBillDate) // หรือเปลี่ยนเป็น ID ยอดเงิน
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

        // ตั้งค่า PopupMenu สำหรับปุ่มจุด 3 จุด [cite: 2026-02-13]
        holder.btnBillMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Delete").setOnMenuItemClickListener {
                confirmDelete(view.context, position)
                true
            }
            popup.show()
        }
    }

    private fun confirmDelete(context: android.content.Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Confirm Delete")
            .setMessage("Do you want to delete this bill history?")
            .setPositiveButton("Delete") { _, _ ->
                // Logic การลบใน Firestore (ต้องมี ID ของเอกสาร) [cite: 2026-02-13]
                // เมื่อลบสำเร็จ:
                billList.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = billList.size
}