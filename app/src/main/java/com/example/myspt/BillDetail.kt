package com.example.myspt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BillDetail : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtTotalAmount: TextView
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var db: FirebaseFirestore

    private val itemList = ArrayList<BillItem>()
    private lateinit var itemAdapter: DetailItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bill_detail)

        // ✅ ลบ ออกแล้ว
        db = FirebaseFirestore.getInstance()

        val billId = intent.getStringExtra("BILL_ID")
        val billName = intent.getStringExtra("BILL_NAME") ?: "Bill Detail"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        txtTitle.text = billName

        if (!billId.isNullOrEmpty()) {
            loadBillData(billId)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        // ตรวจสอบว่า ID ใน XML ต้องชื่อ backButton, txtTitle, txtTotalAmount, rvOrderItems
        btnBack = findViewById(R.id.backButton)
        txtTitle = findViewById(R.id.txtTitle)
        txtTotalAmount = findViewById(R.id.txtTotalAmount)
        rvOrderItems = findViewById(R.id.rvOrderItems)

        rvOrderItems.layoutManager = LinearLayoutManager(this)
        itemAdapter = DetailItemAdapter(itemList)
        rvOrderItems.adapter = itemAdapter
    }

    private fun loadBillData(id: String) {
        // ✅ ลบคำว่า ออกจากบรรทัดด้านล่างนี้ให้หมดครับ
        db.collection("bills").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val total = doc.getDouble("totalAmount") ?: 0.0
                    txtTotalAmount.text = String.format("฿ %.2f", total)

                    // ดึง Array 'items' จาก Firestore
                    val items = doc.get("items") as? List<Map<String, Any>>
                    if (items != null) {
                        itemList.clear()
                        for (itemData in items) {
                            val name = itemData["itemName"] as? String ?: ""
                            val qty = (itemData["quantity"] as? Long)?.toInt() ?: 1
                            val price = (itemData["price"] as? Double) ?: 0.0
                            itemList.add(BillItem(name, qty, price))
                        }
                        itemAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    class DetailItemAdapter(private val items: List<BillItem>) : RecyclerView.Adapter<DetailItemAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvItemName)
            val tvQty: TextView = view.findViewById(R.id.tvQuantity)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary_row, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.itemName
            holder.tvQty.text = "x${item.quantity}"
            holder.tvPrice.text = String.format("%.2f", item.price * item.quantity)
        }
        override fun getItemCount() = items.size
    }
}