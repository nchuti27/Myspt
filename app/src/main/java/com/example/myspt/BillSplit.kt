package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
// import android.widget.Toast (ลบทิ้ง หรือไม่ต้องใช้แล้ว)
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

    // *** 1. เพิ่มตัวแปร List และ Adapter ***
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

        btnBack?.setOnClickListener { finish() }
        btnSplit?.setOnClickListener { showPaymentDialog() }

        // *** 3. แก้ไขปุ่มกดเพิ่มรายการ (หัวใจสำคัญ) ***
        btnAddItem?.setOnClickListener {
            // A. เพิ่มข้อมูลเปล่าลงใน List (เป็นเหมือนโครงสร้างแถวใหม่)
            billList.add(BillItem("", 1, 0.0))

            // B. บอก Adapter ว่า "เฮ้ย มีของใหม่มาแล้วนะ วาดเพิ่มหน่อย"
            adapter?.notifyItemInserted(billList.size - 1)

            // C. เลื่อนหน้าจอลงไปที่แถวใหม่ล่าสุด
            rvBillItems?.scrollToPosition(billList.size - 1)
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBackF)
        btnSplit = findViewById(R.id.btnSplit)
        rvBillItems = findViewById(R.id.rvBillItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)

        // *** 2. เชื่อมต่อ Adapter เข้ากับ RecyclerView ***
        // ถ้า List ว่าง ให้เพิ่มแถวแรกไปก่อน 1 อัน
        if (billList.isEmpty()) {
            billList.add(BillItem("", 1, 0.0))
        }

        adapter = BillAdapter(billList) {
            calculateGrandTotal() // สั่งให้คำนวณเงินใหม่ทุกครั้งที่มีการพิมพ์
        }
        rvBillItems?.layoutManager = LinearLayoutManager(this)
        rvBillItems?.adapter = adapter // ยัด Adapter ใส่ RecyclerView
    }

    private fun showPaymentDialog() {
        // (ส่วน Popup ของคุณ ใช้โค้ดเดิมได้เลยครับ ผมละไว้เพื่อความสั้น)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_payment, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnPopupBack = dialogView.findViewById<Button>(R.id.btnBackF)
        val tvPaymentMessage = dialogView.findViewById<TextView>(R.id.tvPaymentMessage)

        tvPaymentMessage?.text = "You need to pay\n150.00 ฿"

        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DebtSummary::class.java)
            startActivity(intent)
        }
        btnPopupBack.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun calculateGrandTotal() {
        var total = 0.0
        for (item in billList) {
            total += (item.price * item.quantity)
        }
        tvGrandTotal?.text = String.format("%.2f ฿", total)
    }
}