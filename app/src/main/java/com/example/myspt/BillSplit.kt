package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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

    private var btnBack: ImageButton? = null
    private var btnSplit: AppCompatButton? = null
    private var rvBillItems: RecyclerView? = null
    private var btnAddItem: FloatingActionButton? = null
    private var tvGrandTotal: TextView? = null

    // ตัวแปรสำหรับรายการอาหารและ Adapter
    private var billList = ArrayList<BillItem>()
    private var adapter: BillAdapter? = null

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

        // กดปุ่ม Back -> ปิดหน้านี้
        btnBack?.setOnClickListener { finish() }

        // กดปุ่ม Split Bill -> โชว์ Popup
        btnSplit?.setOnClickListener { showPaymentDialog() }

        // *** กดปุ่ม + เพื่อเพิ่มรายการใหม่ ***
        btnAddItem?.setOnClickListener {
            addNewItem()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBackF)
        btnSplit = findViewById(R.id.btnSplit)
        rvBillItems = findViewById(R.id.rvBillItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)

        // เริ่มต้นให้มี 1 แถวว่างๆ เสมอ
        if (billList.isEmpty()) {
            billList.add(BillItem("", 1, 0.0))
        }

        // ตั้งค่า Adapter พร้อมฟังก์ชันคำนวณยอดเงิน
        adapter = BillAdapter(billList) {
            calculateGrandTotal()
        }

        rvBillItems?.layoutManager = LinearLayoutManager(this)
        rvBillItems?.adapter = adapter
    }

    private fun addNewItem() {
        // 1. เพิ่มข้อมูลเปล่าลงใน List
        billList.add(BillItem("", 1, 0.0))

        // 2. แจ้ง Adapter ว่ามีของใหม่มาที่แถวสุดท้าย
        adapter?.notifyItemInserted(billList.size - 1)

        // 3. เลื่อนหน้าจอลงไปที่แถวใหม่
        rvBillItems?.scrollToPosition(billList.size - 1)
    }

    // ฟังก์ชันคำนวณยอดรวมทั้งหมด
    private fun calculateGrandTotal() {
        var total = 0.0
        for (item in billList) {
            total += (item.price * item.quantity)
        }
        // แสดงผลยอดรวม (ทศนิยม 2 ตำแหน่ง)
        tvGrandTotal?.text = String.format("%.2f ฿", total)
    }

    private fun showPaymentDialog() {
        // ใช้ไฟล์ Popup ที่ถูกต้อง (ถ้าชื่อไฟล์ dialog_split_confirm.xml ให้แก้ตรงนี้ด้วย)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_payment, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnPopupBack = dialogView.findViewById<Button>(R.id.btnBackF)
        val tvPaymentMessage = dialogView.findViewById<TextView>(R.id.tvPaymentMessage)

        // --- คำนวณยอดหารต่อคน ---
        val rawText = tvGrandTotal?.text.toString()
        val cleanText = rawText.replace(" ฿", "").replace(",", "")
        val totalAmount = cleanText.toDoubleOrNull() ?: 0.0
        val splitAmount = totalAmount / 3 // หาร 3 คน (หรือเปลี่ยนตามจำนวนเพื่อน)

        tvPaymentMessage?.text = String.format("You need to pay\n%.2f ฿", splitAmount)
        // -----------------------

        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DebtSummary::class.java)
            // ส่งยอดเงินไปด้วยก็ได้
            intent.putExtra("TOTAL_AMOUNT", totalAmount)
            startActivity(intent)
        }

        btnPopupBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}