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

        db = FirebaseFirestore.getInstance()

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
        db.collection("bills")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                billList.clear()
                for (doc in snapshots!!) {
                    val name = doc.getString("billName") ?: "No Name"
                    val total = doc.getDouble("totalAmount") ?: 0.0
                    val item = BillItem(name, 1, total)
                    item.id = doc.id
                    billList.add(item)
                }
                adapter.notifyDataSetChanged()
            }
    }
}