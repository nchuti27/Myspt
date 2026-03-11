package com.example.myspt

import android.os.Bundle
import android.widget.ImageView
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
    private lateinit var btnSeeMore: androidx.appcompat.widget.AppCompatButton
    private lateinit var db: FirebaseFirestore

    private val recentBillList = ArrayList<RecentBillItem>()
    private lateinit var adapter: RecentBillAdapter

    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private val PAGE_SIZE = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recent_bill)

        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupListeners()
        loadRecentBills()
    }

    private fun init() {
        rvRecentBills = findViewById(R.id.rvRecentBills)
        btnBack = findViewById(R.id.backButton)
        btnSeeMore = findViewById(R.id.btnSeeMoreMain)

        // ✅ ซ่อนไว้ก่อน รอให้โหลดเสร็จก่อนค่อยแสดง
        btnSeeMore.visibility = android.view.View.GONE

        rvRecentBills.layoutManager = LinearLayoutManager(this)
        adapter = RecentBillAdapter(recentBillList)
        rvRecentBills.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSeeMore.setOnClickListener { loadMoreBills() }
    }

    private fun loadRecentBills() {
        db.collection("bills")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE)
            .get()
            .addOnSuccessListener { snapshots ->
                recentBillList.clear()
                snapshots.documents.forEach { doc ->
                    recentBillList.add(RecentBillItem(
                        id = doc.id,
                        name = doc.getString("billName") ?: "No Name",
                        total = doc.getDouble("totalAmount") ?: 0.0,
                        date = ""
                    ))
                }
                adapter.notifyDataSetChanged()

                // ✅ ถ้าได้ครบ 10 แสดงปุ่ม See More
                if (snapshots.documents.size >= PAGE_SIZE) {
                    lastDocument = snapshots.documents.last()
                    btnSeeMore.visibility = android.view.View.VISIBLE
                } else {
                    btnSeeMore.visibility = android.view.View.GONE
                }
            }
    }

    private fun loadMoreBills() {
        val last = lastDocument ?: return

        db.collection("bills")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(last)
            .limit(PAGE_SIZE)
            .get()
            .addOnSuccessListener { snapshots ->
                snapshots.documents.forEach { doc ->
                    recentBillList.add(RecentBillItem(
                        id = doc.id,
                        name = doc.getString("billName") ?: "No Name",
                        total = doc.getDouble("totalAmount") ?: 0.0,
                        date = ""
                    ))
                }
                adapter.notifyDataSetChanged()

                // ✅ ถ้าโหลดมาไม่ครบ 10 แปลว่าหมดแล้ว ซ่อนปุ่ม
                if (snapshots.documents.size >= PAGE_SIZE) {
                    lastDocument = snapshots.documents.last()
                    btnSeeMore.visibility = android.view.View.VISIBLE
                } else {
                    btnSeeMore.visibility = android.view.View.GONE
                }
            }
    }
}