package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BillSplit : AppCompatActivity() {
    private var btnBack: ImageButton? = null
    private var btnSplit: AppCompatButton? = null
    private var rvBillItems: RecyclerView? = null
    private var btnAddItem: FloatingActionButton? = null
    private var tvGrandTotal: TextView? = null
    private var etBillName: EditText? = null // 🌟 ต้องประกาศตัวแปรนี้ด้วย

    private var selectedMembers = ArrayList<String>()
    private var billList = ArrayList<BillItem>()
    private var adapter: BillAdapter? = null
    private lateinit var db: FirebaseFirestore

    private var groupMemberUids = ArrayList<String>()
    private var groupMemberNames = ArrayList<String>()
    private lateinit var checkedMemberItems: BooleanArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bill_split)

        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val members = intent.getStringArrayListExtra("SELECTED_MEMBERS")
        if (members != null && members.isNotEmpty()) {
            selectedMembers = members
            groupMemberUids.addAll(members)
            checkedMemberItems = BooleanArray(groupMemberUids.size) { false }
            fetchMemberNames()
        }

        init()

        btnBack?.setOnClickListener { finish() }

        btnSplit?.setOnClickListener {
            // 🌟 1. เช็คว่ากรอกชื่อบิลหรือยัง
            val billName = etBillName?.text.toString().trim()
            if (billName.isEmpty()) {
                etBillName?.error = "Please enter a bill name" // แสดง Error ที่ช่องกรอก
                Toast.makeText(this@BillSplit, "Please enter a bill name before splitting", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // หยุดการทำงานตรงนี้
            }

            // 2. คำนวณยอดเงินที่แต่ละคนต้องจ่าย
            val amountPerPerson = HashMap<String, Double>()
            // ... (โค้ดคำนวณเหมือนเดิม)
            for (item in billList) {
                val itemTotal = item.price * item.quantity
                val sharedBy = item.selectedUsers
                if (sharedBy.isNotEmpty() && itemTotal > 0) {
                    val costPerPerson = itemTotal / sharedBy.size
                    for (uid in sharedBy) {
                        amountPerPerson[uid] = (amountPerPerson[uid] ?: 0.0) + costPerPerson
                    }
                }
            }

            if (amountPerPerson.isEmpty()) {
                Toast.makeText(this@BillSplit, "กรุณาระบุราคาและเลือกผู้ที่ต้องหารบิล", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ... (ส่วนการเปิด Dialog และส่งข้อมูล ให้คงเดิมไว้ครับ)
            val myUid = FirebaseAuth.getInstance().currentUser?.uid
            val myAmount = amountPerPerson[myUid] ?: 0.0

            val dialogView = layoutInflater.inflate(R.layout.layout_dialog_payment, null)
            val dialog = AlertDialog.Builder(this@BillSplit).setView(dialogView).create()
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

            dialogView.findViewById<TextView>(R.id.tvPaymentMessage)?.text = String.format("You need to pay\n%.2f ฿", myAmount)
            dialogView.findViewById<AppCompatButton>(R.id.backButton)?.setOnClickListener { dialog.dismiss() }

            dialogView.findViewById<AppCompatButton>(R.id.btnOk)?.setOnClickListener {
                // ใช้ billName ที่ดึงมาตอนแรกได้เลย
                val nameMap = HashMap<String, String>()
                for (i in groupMemberUids.indices) {
                    if (i < groupMemberNames.size) nameMap[groupMemberUids[i]] = groupMemberNames[i]
                }

                val intent = Intent(this@BillSplit, WhoPays::class.java).apply {
                    putExtra("BILL_NAME", billName)
                    putExtra("SPLIT_RESULT", amountPerPerson)
                    putExtra("MEMBER_NAMES", nameMap)
                    putExtra("BILL_ITEMS", billList)
                }
                startActivity(intent)
                dialog.dismiss()
            }
            dialog.show()
        }

        btnAddItem?.setOnClickListener {
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
        etBillName = findViewById(R.id.etBillName) // 🌟 เชื่อม ID จาก XML

        if (billList.isEmpty()) billList.add(BillItem("", 1, 0.0))
        adapter = BillAdapter(billList, selectedMembers) { calculateGrandTotal() }
        rvBillItems?.layoutManager = LinearLayoutManager(this)
        rvBillItems?.adapter = adapter
    }

    private fun calculateGrandTotal() {
        var total = 0.0
        for (item in billList) total += (item.price * item.quantity)
        tvGrandTotal?.text = String.format("%.2f ฿", total)
    }

    private fun fetchMemberNames() {
        if (groupMemberUids.isEmpty()) return
        db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), groupMemberUids.take(10))
            .get()
            .addOnSuccessListener { documents ->
                groupMemberNames.clear()
                for (uid in groupMemberUids.take(10)) {
                    groupMemberNames.add(documents.documents.find { it.id == uid }?.getString("name") ?: "Unknown")
                }
                adapter?.updateMemberNames(groupMemberNames)
            }
    }
}