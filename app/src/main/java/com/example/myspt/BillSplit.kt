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
import com.google.firebase.firestore.FirebaseFirestore

class BillSplit : AppCompatActivity() {
    private var btnBack: ImageButton? = null
    private var btnSplit: AppCompatButton? = null
    private var rvBillItems: RecyclerView? = null
    private var btnAddItem: FloatingActionButton? = null
    private var tvGrandTotal: TextView? = null

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

    private fun fetchMemberNames() {
        if (groupMemberUids.isEmpty()) return
        val uidsToFetch = groupMemberUids.take(10)

        db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), uidsToFetch)
            .get()
            .addOnSuccessListener { documents ->
                groupMemberNames.clear()
                for (uid in uidsToFetch) {
                    val doc = documents.documents.find { it.id == uid }
                    val name = doc?.getString("name") ?: "Unknown"
                    groupMemberNames.add(name)
                }
                adapter?.updateMemberNames(groupMemberNames)
            }
            .addOnFailureListener {
                Toast.makeText(this, "โหลดรายชื่อสมาชิกล้มเหลว", Toast.LENGTH_SHORT).show()
            }
    }

    fun showMemberSelectionDialog() {
        if (groupMemberNames.isEmpty()) {
            Toast.makeText(this, "กำลังโหลดรายชื่อสมาชิก...", Toast.LENGTH_SHORT).show()
            return
        }
        val namesArray = groupMemberNames.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("เลือกผู้ที่มีส่วนร่วม")
            .setMultiChoiceItems(namesArray, checkedMemberItems) { _, which, isChecked ->
                checkedMemberItems[which] = isChecked
            }
            .setPositiveButton("ตกลง") { dialog, _ ->
                val selectedNames = ArrayList<String>()
                for (i in checkedMemberItems.indices) {
                    if (checkedMemberItems[i]) {
                        selectedNames.add(namesArray[i])
                    }
                }
                if (selectedNames.isNotEmpty()) {
                    Toast.makeText(this, "เลือก: ${selectedNames.joinToString(", ")}", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("ยกเลิก") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}