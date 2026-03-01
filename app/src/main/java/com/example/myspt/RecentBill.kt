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
    private lateinit var adapter: RecentBillAdapter

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
        // ✅ เปลี่ยนมาโหลดข้อมูลจริงจาก Firestore [cite: 2026-02-13, 2026-02-21]
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
        // ✅ ดึงบิลล่าสุด เรียงตามเวลาที่สร้าง (Timestamp) [cite: 2026-02-13, 2026-02-21]
        db.collection("bills")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { documents, e -> // ใช้ SnapshotListener เพื่อให้อัปเดต Realtime [cite: 2026-02-27]
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (documents != null) {
                    billList.clear()
                    for (doc in documents) {
                        // ดึงชื่อบิล (ตรวจสอบว่าไม่เป็น String "null")
                        val name = doc.getString("billName") ?: "Untitled Bill"
                        val total = doc.getDouble("totalAmount") ?: 0.0

                        // สร้าง BillItem โดยใช้ doc.id เป็น ID เพื่อใช้สั่งลบ [cite: 2026-02-13]
                        val item = BillItem(name, 1, total)
                        item.id = doc.id // 🌟 สำคัญมาก: ต้องเซ็ต ID เพื่อใช้ลบใน Firestore

                        billList.add(item)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}