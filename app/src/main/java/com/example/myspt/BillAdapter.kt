package com.example.myspt

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class BillAdapter(
    private val billList: ArrayList<BillItem>,
    private val onTotalChange: () -> Unit // ฟังก์ชัน callback เมื่อมีการแก้ไขตัวเลข
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etItemName: EditText = itemView.findViewById(R.id.etItemName)
        val etQuantity: EditText = itemView.findViewById(R.id.etQuantity)
        val etPrice: EditText = itemView.findViewById(R.id.etPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bill_row, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val item = billList[position]

        // 1. แสดงค่าเดิม (ป้องกันข้อมูลหายเวลาเลื่อนจอ)
        holder.etItemName.setText(item.itemName)
        holder.etQuantity.setText(item.quantity.toString())

        // ถ้าเป็น 0.0 ให้แสดงว่างๆ จะได้พิมพ์ง่าย
        if (item.price == 0.0) holder.etPrice.setText("") else holder.etPrice.setText(item.price.toString())

        // 2. ดักจับการแก้ไขชื่อรายการ
        holder.etItemName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.itemName = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 3. ดักจับจำนวน (Quantity)
        holder.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val qty = s.toString().toIntOrNull() ?: 0
                item.quantity = qty
                onTotalChange() // สั่งคำนวณเงินใหม่
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 4. ดักจับราคา (Price)
        holder.etPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val price = s.toString().toDoubleOrNull() ?: 0.0
                item.price = price
                onTotalChange() // สั่งคำนวณเงินใหม่
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int {
        return billList.size
    }
}