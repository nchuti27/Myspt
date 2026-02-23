package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

    private var selectedMembers = ArrayList<String>()

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
        btnBack?.setOnClickListener {
            finish()
        }

        val members = intent.getStringArrayListExtra("SELECTED_MEMBERS")
        if (members != null) {
            selectedMembers = members
        }

        btnSplit?.setOnClickListener {
            val intent = Intent(this, WhoPays::class.java)
            startActivity(intent)
        }

            btnAddItem?.setOnClickListener {
            Toast.makeText(this, "Add new item", Toast.LENGTH_SHORT).show()
            billList.add(BillItem("", 1, 0.0))
            adapter?.notifyItemInserted(billList.size - 1)
            rvBillItems?.scrollToPosition(billList.size - 1)
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        btnSplit = findViewById(R.id.btnSplit)
        rvBillItems = findViewById(R.id.rvBillItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)

        if (billList.isEmpty()) {
            billList.add(BillItem("", 1, 0.0))
        }

        // แก้บรรทัดนี้: ส่ง selectedMembers (ที่คุณรับมาจาก Intent) เข้าไปด้วย
        adapter = BillAdapter(billList, selectedMembers) {
            calculateGrandTotal()
        }

        rvBillItems?.layoutManager = LinearLayoutManager(this)
        rvBillItems?.adapter = adapter
    }

    private fun calculateGrandTotal() {
        var total = 0.0
        for (item in billList) {
            total += (item.price * item.quantity)
        }
        tvGrandTotal?.text = String.format("%.2f ฿", total)
    }
}