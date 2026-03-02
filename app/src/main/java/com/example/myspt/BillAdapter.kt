package com.example.myspt

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class BillAdapter(
    private val billList: ArrayList<BillItem>,
    private val selectedMembers: ArrayList<String>, // UIDs
    private val memberNames: ArrayList<String>,     // เพิ่มพารามิเตอร์นี้เข้าไป
    private val onTotalChange: () -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

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

        holder.btnSelectUser.setOnClickListener {
            if (memberNames.isEmpty()) return@setOnClickListener

            val namesArray = memberNames.toTypedArray()
            val checkedItems = BooleanArray(selectedMembers.size) { i -> item.selectedUsers.contains(selectedMembers[i]) }

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Select Members")
                .setMultiChoiceItems(namesArray, checkedItems) { _, which, isChecked ->
                    val uid = selectedMembers[which]
                    if (isChecked) {
                        if (!item.selectedUsers.contains(uid)) item.selectedUsers.add(uid)
                    } else {
                        item.selectedUsers.remove(uid)
                    }
                }
                .setPositiveButton("OK") { _, _ -> onTotalChange() }
                .show()
        }

        // TextWatchers
        holder.etItemName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.itemName = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        holder.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.quantity = s.toString().toIntOrNull() ?: 0; onTotalChange() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        holder.etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.price = s.toString().toDoubleOrNull() ?: 0.0; onTotalChange() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = billList.size
}