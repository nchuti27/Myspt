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

class RecentBillAdapter(private val billList: ArrayList<RecentBillItem>) :
    RecyclerView.Adapter<RecentBillAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBillName: TextView = view.findViewById(R.id.tvBillName)
        val tvBillTotal: TextView = view.findViewById(R.id.tvBillTotal)
        val btnBillMenu: ImageView = view.findViewById(R.id.btnBillMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_bill, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = billList[position]

        holder.tvBillName.text = item.name
        holder.tvBillTotal.text = String.format("฿ %.2f", item.total)

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, BillDetail::class.java).apply {
                putExtra("BILL_ID", item.id)
                putExtra("BILL_NAME", item.name)
            }
            view.context.startActivity(intent)
        }

        holder.btnBillMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Delete Bill").setOnMenuItemClickListener {
                confirmDelete(view.context, item)
                true
            }
            popup.show()
        }
    }

    private fun confirmDelete(context: android.content.Context, item: RecentBillItem) {
        AlertDialog.Builder(context)
            .setTitle("Delete History")
            .setMessage("Delete '${item.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance().collection("bills").document(item.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Bill deleted successfully", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount() = billList.size
}