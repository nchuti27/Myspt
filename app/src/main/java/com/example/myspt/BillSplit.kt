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

        // โหลดข้อมูลให้เสร็จก่อน แล้วค่อย init UI
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
                init() // เรียกตรงนี้เพื่อให้แน่ใจว่าได้ชื่อเพื่อนครบแล้ว
            }
    }

    private fun fetchAllFriends() {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            // ดึงเพื่อนทั้งหมด
            val friends = doc.get("friends") as? List<String> ?: listOf()

            // 🌟 รวม UID ของเราเข้าไปด้วย
            val allMembers = ArrayList<String>()
            allMembers.add(myUid)
            allMembers.addAll(friends)

            if (allMembers.isNotEmpty()) {
                selectedMembers.clear()
                selectedMembers.addAll(allMembers)
                // ส่งไปดึงชื่อของทุกคน (รวมชื่อเราด้วย)
                fetchMemberNames(selectedMembers)
            } else {
                // กรณีไม่มีเพื่อนเลย ก็ยังมีชื่อเราคนเดียว
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

        val amountPerPerson = HashMap<String, Double>()
        for (item in billList) {
            if (item.selectedUsers.isNotEmpty() && item.price > 0) {
                val cost = (item.price * item.quantity) / item.selectedUsers.size
                for (uid in item.selectedUsers) amountPerPerson[uid] = (amountPerPerson[uid] ?: 0.0) + cost
            }
        }

        if (amountPerPerson.isEmpty()) { Toast.makeText(this, "กรุณาเลือกผู้หาร", Toast.LENGTH_SHORT).show(); return }

        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.layout_dialog_payment, null)
        dialog.setView(view)
        view.findViewById<TextView>(R.id.tvPaymentMessage).text = "You need to pay: ${String.format("%.2f", amountPerPerson[FirebaseAuth.getInstance().currentUser?.uid] ?: 0.0)} ฿"
        view.findViewById<AppCompatButton>(R.id.btnOk).setOnClickListener {
            val nameMap = HashMap<String, String>()
            selectedMembers.forEachIndexed { i, uid -> if (i < memberNames.size) nameMap[uid] = memberNames[i] }
            startActivity(Intent(this, WhoPays::class.java).apply {
                putExtra("BILL_NAME", billName); putExtra("SPLIT_RESULT", amountPerPerson)
                putExtra("MEMBER_NAMES", nameMap); putExtra("BILL_ITEMS", billList)
            })
            dialog.dismiss()
        }
        dialog.show()
    }
}