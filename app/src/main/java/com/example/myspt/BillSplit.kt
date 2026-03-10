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
    private var etBillName: EditText? = null
    private var selectedMembers = ArrayList<String>()
    private var memberNames = ArrayList<String>()
    private var billList = ArrayList<BillItem>()
    private var adapter: BillAdapter? = null
    private lateinit var db: FirebaseFirestore

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
        if (!members.isNullOrEmpty()) {
            selectedMembers = members
            fetchMemberNames(members)
        } else {
            fetchAllFriends()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        btnSplit = findViewById(R.id.btnSplit)
        rvBillItems = findViewById(R.id.rvBillItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        tvGrandTotal = findViewById(R.id.tvGrandTotal)
        etBillName = findViewById(R.id.etBillName)

        if (billList.isEmpty()) billList.add(BillItem("", 1, 0.0))

        adapter = BillAdapter(billList, selectedMembers, memberNames) { calculateGrandTotal() }
        rvBillItems?.layoutManager = LinearLayoutManager(this)
        rvBillItems?.adapter = adapter

        btnBack?.setOnClickListener { finish() }
        btnSplit?.setOnClickListener { processSplit() }
        btnAddItem?.setOnClickListener {
            billList.add(BillItem("", 1, 0.0))
            adapter?.notifyItemInserted(billList.size - 1)
        }
        calculateGrandTotal()
    }

    private fun fetchMemberNames(uids: ArrayList<String>) {
        db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), uids.take(10))
            .get().addOnSuccessListener { docs ->
                memberNames.clear()
                uids.take(10).forEach { uid ->
                    memberNames.add(docs.documents.find { it.id == uid }?.getString("name") ?: "Unknown")
                }
                init()
            }
    }

    private fun fetchAllFriends() {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            val friends = doc.get("friends") as? List<String> ?: listOf()
            val allMembers = ArrayList<String>()
            allMembers.add(myUid)
            allMembers.addAll(friends)

            if (allMembers.isNotEmpty()) {
                selectedMembers.clear()
                selectedMembers.addAll(allMembers)
                fetchMemberNames(selectedMembers)
            } else {
                selectedMembers.add(myUid)
                fetchMemberNames(selectedMembers)
            }
        }
    }

    private fun calculateGrandTotal() {
        var total = 0.0
        for (item in billList) total += (item.price * item.quantity)
        tvGrandTotal?.text = String.format("%.2f ฿", total)
    }

    private fun processSplit() {
        val billName = etBillName?.text.toString().trim()
        if (billName.isEmpty()) { etBillName?.error = "Enter name"; return }

        var grandTotal = 0.0
        val amountPerPerson = HashMap<String, Double>()

        // 🌟 1. คำนวณยอดที่ทุกคนต้องจ่าย (Liability)
        for (item in billList) {
            val itemTotal = item.price * item.quantity
            grandTotal += itemTotal // รวมยอดบิลทั้งหมด

            if (item.selectedUsers.isNotEmpty() && item.price > 0) {
                val costPerUser = itemTotal / item.selectedUsers.size
                for (uid in item.selectedUsers) {
                    amountPerPerson[uid] = (amountPerPerson[uid] ?: 0.0) + costPerUser
                }
            }
        }

        if (amountPerPerson.isEmpty()) {
            Toast.makeText(this, "Please select members for items", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.layout_dialog_payment, null)
        dialog.setView(view)

        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        view.findViewById<TextView>(R.id.tvPaymentMessage).text =
            "Your share: ${String.format("%.2f", amountPerPerson[myUid] ?: 0.0)} ฿"

        view.findViewById<AppCompatButton>(R.id.btnOk).setOnClickListener {
            val nameMap = HashMap<String, String>()
            selectedMembers.forEachIndexed { i, uid ->
                if (i < memberNames.size) nameMap[uid] = memberNames[i]
            }

            // 🌟 2. ส่งข้อมูลไปหน้า WhoPays เพื่อทำระบบคนจ่ายหลายคน
            // ใน BillSplit.kt (หน้าที่มีปุ่ม SPLIT BILL)
            val intent = Intent(this, WhoPays::class.java).apply {
                putExtra("BILL_NAME", billName)
                putExtra("GRAND_TOTAL", grandTotal)
                putExtra("SPLIT_RESULT", amountPerPerson)
                putExtra("MEMBER_NAMES", nameMap)
                putExtra("SELECTED_MEMBERS", selectedMembers)
                // 🌟 หัวใจสำคัญ: ต้องชื่อ "BILL_ITEMS" และต้องส่ง billList ไปครับ
                putExtra("BILL_ITEMS", billList)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        view.findViewById<AppCompatButton>(R.id.backButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
