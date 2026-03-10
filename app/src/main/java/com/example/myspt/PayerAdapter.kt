package com.example.myspt

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PayerAdapter(private val payerList: List<PayerData>) :
    RecyclerView.Adapter<PayerAdapter.PayerViewHolder>() {

    class PayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPayerName: TextView = view.findViewById(R.id.tvPayerName)
        val etPaidAmount: EditText = view.findViewById(R.id.etPaidAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payer_row, parent, false)
        return PayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PayerViewHolder, position: Int) {
        val payer = payerList[position]
        holder.tvPayerName.text = payer.name
        holder.etPaidAmount.setText(if (payer.amountPaid == 0.0) "" else payer.amountPaid.toString())

        // 🌟 อัปเดตยอดเงินลงใน Object ทันทีที่พิมพ์
        holder.etPaidAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                payer.amountPaid = s.toString().toDoubleOrNull() ?: 0.0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount() = payerList.size
}