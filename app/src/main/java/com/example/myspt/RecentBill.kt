package com.example.myspt

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager // 1. เติม Import ตรงนี้ครับ
import androidx.recyclerview.widget.RecyclerView

class RecentBill : AppCompatActivity() {
    var backButton: ImageButton? = null
    var rvRecentBills: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recent_bill)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        backButton!!.setOnClickListener {
            finish()
        }

        val dummyData = arrayListOf(
            RecentBillItem("KFC Party", "Total : 500.00 THB"),
            RecentBillItem("หมูกระทะ", "Total : 899.00 THB"),
            RecentBillItem("ค่าแท็กซี่", "Total : 150.00 THB")
        ) // 2. เติมวงเล็บปิดตรงนี้ครับ! (ของคุณลืมปิดวงเล็บตรงนี้)

        val adapter = RecentBillAdapter(dummyData)
        rvRecentBills?.layoutManager = LinearLayoutManager(this)
        rvRecentBills?.adapter = adapter
    }

    private fun init() {
        backButton = findViewById(R.id.backButton)
        rvRecentBills = findViewById(R.id.rvRecentBills)
    }
}