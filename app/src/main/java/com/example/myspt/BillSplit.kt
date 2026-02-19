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
// import android.widget.Toast (ลบทิ้ง หรือไม่ต้องใช้แล้ว)
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
class BillSplit : AppCompatActivity() {

    // 1. ประกาศตัวแปร View ให้ครบ
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
        setContentView(R.layout.activity_bill_split) // ต้องมั่นใจว่าไฟล์นี้มีอยู่จริง
        setContentView(R.layout.activity_bill_split)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init() // เรียกฟังก์ชันเชื่อมต่อตัวแปร
        init()

        // ตั้งค่า Listener
        btnBack?.setOnClickListener { finish() }
        btnSplit?.setOnClickListener { showPaymentDialog() }

        // *** 3. แก้ไขปุ่มกดเพิ่มรายการ (หัวใจสำคัญ) ***
        btnAddItem?.setOnClickListener {
            // (ตัวอย่าง) กดปุ่มบวกแล้วแสดง Toast
            Toast.makeText(this, "Add new item", Toast.LENGTH_SHORT).show()
            // A. เพิ่มข้อมูลเปล่าลงใน List (เป็นเหมือนโครงสร้างแถวใหม่)
            billList.add(BillItem("", 1, 0.0))

            // B. บอก Adapter ว่า "เฮ้ย มีของใหม่มาแล้วนะ วาดเพิ่มหน่อย"
            adapter?.notifyItemInserted(billList.size - 1)

            // C. เลื่อนหน้าจอลงไปที่แถวใหม่ล่าสุด
            rvBillItems?.scrollToPosition(billList.size - 1)
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
        // *** แก้ชื่อไฟล์ Layout ให้ตรงกับที่มีในโฟลเดอร์ layout ***
        // ถ้าคุณตั้งชื่อไฟล์ Popup ว่า dialog_split_confirm.xml ให้ใช้บรรทัดนี้:
        // (ส่วน Popup ของคุณ ใช้โค้ดเดิมได้เลยครับ ผมละไว้เพื่อความสั้น)
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