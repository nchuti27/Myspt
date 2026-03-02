package com.example.myspt

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class BillAdapter(
    private val billList: ArrayList<BillItem>,
    private val selectedMembers: ArrayList<String>, // ตัวนี้คือ UIDs
    private val onTotalChange: () -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    // 🌟 1. เพิ่มตัวแปรสำหรับเก็บ "ชื่อ" ของเพื่อน
    private var memberNames: ArrayList<String> = ArrayList()

    // 🌟 2. เพิ่มฟังก์ชันให้หน้า BillSplit ส่ง "ชื่อเพื่อน" มาอัปเดตที่นี่ได้
    fun updateMemberNames(names: ArrayList<String>) {
        this.memberNames = names
        notifyDataSetChanged()
    }

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etItemName: EditText = itemView.findViewById(R.id.etItemName)
        val etQuantity: EditText = itemView.findViewById(R.id.etQuantity)
        val etPrice: EditText = itemView.findViewById(R.id.etPrice)
        val btnSelectUser: View = itemView.findViewById(R.id.btnSelectUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill_row, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val item = billList[position]

        holder.etItemName.setText(item.itemName)
        holder.etQuantity.setText(item.quantity.toString())
        holder.etPrice.setText(if (item.price == 0.0) "" else item.price.toString())

        // 🌟 3. จัดการการเลือกสมาชิก (โชว์ชื่อ แต่เก็บ UID)
        holder.btnSelectUser.setOnClickListener {
            // เช็คว่าโหลดชื่อเพื่อนเสร็จหรือยัง
            if (memberNames.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Loading friends list...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // นำ "ชื่อ" มาแสดงเป็นตัวเลือกใน Popup
            val namesArray = memberNames.toTypedArray()

            // เช็คว่าช่องไหนถูกติ๊กบ้าง (โดยอิงจาก UID)
            val checkedItems = BooleanArray(selectedMembers.size) { index ->
                item.selectedUsers.contains(selectedMembers[index])
            }

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Who shared this item?")
                .setMultiChoiceItems(namesArray, checkedItems) { _, which, isChecked ->
                    // แม้จะคลิกที่ "ชื่อ" แต่เราจะเอา "UID" ไปบันทึกเก็บไว้หารเงิน
                    val memberUid = selectedMembers[which]
                    if (isChecked) {
                        if (!item.selectedUsers.contains(memberUid)) item.selectedUsers.add(memberUid)
                    } else {
                        item.selectedUsers.remove(memberUid)
                    }
                }
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(holder.itemView.context, "Selected ${item.selectedUsers.size} users", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        holder.etItemName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.itemName = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                item.quantity = s.toString().toIntOrNull() ?: 0
                onTotalChange()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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