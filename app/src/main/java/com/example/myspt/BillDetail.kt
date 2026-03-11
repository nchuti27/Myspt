package com.example.myspt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BillDetail : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var btnDone: android.widget.Button
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
        btnBack = findViewById(R.id.backButton)
        btnDone = findViewById(R.id.btnDone)
        txtTitle = findViewById(R.id.txtTitle)
        txtTotalAmount = findViewById(R.id.txtTotalAmount)
        rvOrderItems = findViewById(R.id.rvOrderItems)

        btnDone.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        rvOrderItems.layoutManager = LinearLayoutManager(this)
        itemAdapter = DetailItemAdapter(itemList)
        rvOrderItems.adapter = itemAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadBillData(id: String) {
        db.collection("bills").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val total = doc.getDouble("totalAmount") ?: 0.0
                    txtTotalAmount.text = String.format(java.util.Locale.getDefault(), "฿ %.2f", total)

                    // ✅ ดึง items เหมือนเดิม
                    @Suppress("UNCHECKED_CAST")
                    val items = doc.get("items") as? List<Map<String, Any>>
                    if (items != null) {
                        itemList.clear()
                        for (itemData in items) {
                            val name = itemData["itemName"] as? String ?: ""
                            val qty = (itemData["quantity"] as? Long)?.toInt() ?: 1
                            val price = when (val p = itemData["price"]) {
                                is Double -> p
                                is Long -> p.toDouble()
                                is Number -> p.toDouble()
                                else -> 0.0
                            }
                            itemList.add(BillItem(name, qty, price))
                        }
                        itemAdapter.notifyDataSetChanged()
                    }

                    // ✅ ดึง payers map แล้วแปลง uid → ชื่อ
                    @Suppress("UNCHECKED_CAST")
                    val payers = doc.get("payers") as? Map<String, Any> ?: return@addOnSuccessListener
                    val uids = payers.keys.toList()
                    if (uids.isEmpty()) return@addOnSuccessListener

                    db.collection("users")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), uids.take(10))
                        .get()
                        .addOnSuccessListener { userDocs ->
                            val sb = StringBuilder("Paid by\n")
                            for (uid in uids) {
                                val name = userDocs.documents.find { it.id == uid }?.getString("name") ?: "Unknown"
                                val amount = when (val a = payers[uid]) {
                                    is Double -> a
                                    is Long -> a.toDouble()
                                    is Number -> a.toDouble()
                                    else -> 0.0
                                }
                                if (amount > 0) sb.append("     $name  :   ฿ ${String.format("%.2f ", amount)}\n")
                            }
                            // ✅ แสดงใน TextView — ต้องเพิ่ม tvPayers ใน layout ด้วย
                            findViewById<TextView>(R.id.tvPayers)?.text = sb.toString().trimEnd()
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
            val context = holder.itemView.context
            holder.tvName.text = item.itemName
            holder.tvQty.text = item.quantity.toString() // ✅ เอา x ออก
            holder.tvPrice.text = String.format(java.util.Locale.getDefault(), "%.2f", item.price * item.quantity)
        }
        override fun getItemCount() = items.size
    }
}