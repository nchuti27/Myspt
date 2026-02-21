package com.example.myspt

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecentBill : AppCompatActivity() {

    private lateinit var rvRecentBills: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var db: FirebaseFirestore

    private val billList = ArrayList<BillItem>()
    private lateinit var adapter: RecentBillAdapter // ต้องสร้างไฟล์ Adapter แยกไว้ด้วย

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recent_bill)

        db = FirebaseFirestore.getInstance() // [cite: 2026-02-13]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupListeners()
        loadRecentBillsFromFirestore()
    }

    private fun init() {
        rvRecentBills = findViewById(R.id.rvRecentBills)
        btnBack = findViewById(R.id.backButton)

        rvRecentBills.layoutManager = LinearLayoutManager(this)

        // เชื่อมต่อ Adapter
        adapter = RecentBillAdapter(billList)
        rvRecentBills.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
    }

    private fun loadRecentBillsFromFirestore() {
        // ดึงข้อมูลบิลล่าสุด เรียงจากใหม่ไปเก่า [cite: 2026-02-13, 2026-02-21]
        db.collection("bills")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                billList.clear()
                for (doc in documents) {
                    val name = doc.getString("billName") ?: "Unknown Bill"
                    val total = doc.getDouble("totalAmount") ?: 0.0
                    // สร้าง Object BillItem และเก็บ ID ไว้สำหรับสั่งลบ [cite: 2026-02-13]
                    val item = BillItem(name, 1, total)
                    // หมายเหตุ: คุณอาจต้องเพิ่ม field 'id' ใน Data Class BillItem เพื่อใช้เก็บ doc.id
                    billList.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}