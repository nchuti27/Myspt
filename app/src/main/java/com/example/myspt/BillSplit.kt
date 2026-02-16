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
        setContentView(R.layout.activity_bill_split) // ต้องมั่นใจว่าไฟล์นี้มีอยู่จริง

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init() // เรียกฟังก์ชันเชื่อมต่อตัวแปร

        // ตั้งค่า Listener
        btnBack?.setOnClickListener { finish() }
        btnSplit?.setOnClickListener { showPaymentDialog() }

        btnAddItem?.setOnClickListener {
            // (ตัวอย่าง) กดปุ่มบวกแล้วแสดง Toast
            Toast.makeText(this, "Add new item", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ฟังก์ชัน init() ต้องอยู่ข้างใน Class (ก่อนปีกกาปิดตัวสุดท้าย) ---
    private fun init() {
        // เชื่อม ID จากไฟล์ activity_bill_split.xml
        btnBack = findViewById(R.id.btnBackF)
        btnSplit = findViewById(R.id.btnSplit)
        rvBillItems = findViewById(R.id.rvBillItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)

        // ตั้งค่า RecyclerView (ต้องทำ ไม่งั้น List ไม่ขึ้น)
        rvBillItems?.layoutManager = LinearLayoutManager(this)
    }

    private fun showPaymentDialog() {
        // *** แก้ชื่อไฟล์ Layout ให้ตรงกับที่มีในโฟลเดอร์ layout ***
        // ถ้าคุณตั้งชื่อไฟล์ Popup ว่า dialog_split_confirm.xml ให้ใช้บรรทัดนี้:
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_payment, null)

        // แต่ถ้าคุณตั้งชื่อไฟล์ว่า dialog_payment.xml ให้เปลี่ยนเป็น R.layout.dialog_payment แทน

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        // ทำให้พื้นหลังใส
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // เชื่อมปุ่มภายใน Popup
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnPopupBack = dialogView.findViewById<Button>(R.id.btnBackF)
        val tvPaymentMessage = dialogView.findViewById<TextView>(R.id.tvPaymentMessage)

        // (Optional) ตั้งค่าข้อความยอดเงินที่จะจ่าย
        tvPaymentMessage?.text = "You need to pay\n150.00 ฿"

        // กด OK -> ไปหน้า DebtSummary
        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DebtSummary::class.java)
            startActivity(intent)
        }

        // กด Back (ใน Popup) -> ปิด Popup
        btnPopupBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}