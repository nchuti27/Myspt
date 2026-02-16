package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BillSplit : AppCompatActivity() {

    // 1. ประกาศตัวแปร View ให้ครบ
    private var btnBack: ImageButton? = null
    private var btnSplit: AppCompatButton? = null
    private var rvBillItems: RecyclerView? = null
    private var btnAddItem: FloatingActionButton? = null
    private var tvGrandTotal: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bill_split)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        btnBack?.setOnClickListener { finish() }
        btnSplit?.setOnClickListener { showPaymentDialog() }

        btnAddItem?.setOnClickListener {

            Toast.makeText(this, "Add new item", Toast.LENGTH_SHORT).show()
        }
    }


    private fun init() {

        btnBack = findViewById(R.id.btnBackF)
        btnSplit = findViewById(R.id.btnSplit)
        rvBillItems = findViewById(R.id.rvBillItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)


        rvBillItems?.layoutManager = LinearLayoutManager(this)
    }

    private fun showPaymentDialog() {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_payment, null)



        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        // ทำให้พื้นหลังใส
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // เชื่อมปุ่มภายใน Popup
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnPopupBack = dialogView.findViewById<Button>(R.id.btnBackF)
        val tvPaymentMessage = dialogView.findViewById<TextView>(R.id.tvPaymentMessage)


        tvPaymentMessage?.text = "You need to pay\n150.00 ฿"


        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DebtSummary::class.java)
            startActivity(intent)
        }


        btnPopupBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}