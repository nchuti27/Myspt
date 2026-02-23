package com.example.myspt

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class BillAdapter(
    private val billList: ArrayList<BillItem>,
    private val selectedMembers: ArrayList<String>, // รับรายชื่อสมาชิกจากกลุ่มที่เลือกมา
    private val onTotalChange: () -> Unit // Callback เมื่อราคาเปลี่ยน
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etItemName: EditText = itemView.findViewById(R.id.etItemName)
        val etQuantity: EditText = itemView.findViewById(R.id.etQuantity)
        val etPrice: EditText = itemView.findViewById(R.id.etPrice)
        // สมมติว่าปุ่มเลือก User ใน XML ของคุณคือ ImageButton หรือ View ที่มี ID นี้
        val btnSelectUser: View = itemView.findViewById(R.id.btnSelectUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill_row, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val item = billList[position]

        // ป้องกัน TextWatcher ทำงานซ้ำซ้อนขณะ Scroll
        holder.etItemName.setText(item.itemName)
        holder.etQuantity.setText(item.quantity.toString())
        holder.etPrice.setText(if (item.price == 0.0) "" else item.price.toString())

        // 1. จัดการการเลือกสมาชิก (อำนวยความสะดวกจากกลุ่มเดิม)
        holder.btnSelectUser.setOnClickListener {
            if (selectedMembers.isEmpty()) {
                Toast.makeText(holder.itemView.context, "No group members found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val membersArray = selectedMembers.toTypedArray()
            val checkedItems = BooleanArray(membersArray.size) { index ->
                item.selectedUsers.contains(membersArray[index])
            }

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Who shared this item?")
                .setMultiChoiceItems(membersArray, checkedItems) { _, which, isChecked ->
                    val memberName = membersArray[which]
                    if (isChecked) {
                        if (!item.selectedUsers.contains(memberName)) item.selectedUsers.add(memberName)
                    } else {
                        item.selectedUsers.remove(memberName)
                    }
                }
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(holder.itemView.context, "Selected ${item.selectedUsers.size} users", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // 2. ดักจับการแก้ไขชื่อรายการ
        holder.etItemName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.itemName = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 3. ดักจับจำนวน (Quantity)
        holder.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                item.quantity = s.toString().toIntOrNull() ?: 0
                onTotalChange()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 4. ดักจับราคา (Price)
        holder.etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                item.price = s.toString().toDoubleOrNull() ?: 0.0
                onTotalChange()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = billList.size
}