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

        // ==========================================
        // ส่วนที่แก้ไข: เพิ่ม Popup ยืนยันก่อนไปหน้า WhoPays
        // ==========================================
        btnSplit?.setOnClickListener {
            // 1. คำนวณยอดเงินที่แต่ละคนต้องจ่าย
            val amountPerPerson = HashMap<String, Double>()

            for (item in billList) {
                val itemTotal = item.price * item.quantity
                val sharedBy = item.selectedUsers

                if (sharedBy.isNotEmpty() && itemTotal > 0) {
                    val costPerPerson = itemTotal / sharedBy.size

                    for (uid in sharedBy) {
                        val currentAmount = amountPerPerson[uid] ?: 0.0
                        amountPerPerson[uid] = currentAmount + costPerPerson
                    }
                }
            }

            // ถ้าไม่ได้ระบุราคาหรือเลือกคนเลย
            if (amountPerPerson.isEmpty()) {
                Toast.makeText(this@BillSplit, "กรุณาระบุราคาและเลือกผู้ที่ต้องหารบิล", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🌟 2. ดึง UID ของตัวเราเอง และหายอดที่เราต้องจ่าย
            val myUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val myAmount = if (myUid != null) amountPerPerson[myUid] ?: 0.0 else 0.0

            // 3. ดึงหน้าตา Dialog จากไฟล์ layout_dialog_payment.xml
            val dialogView = layoutInflater.inflate(R.layout.layout_dialog_payment, null)
            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this@BillSplit)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

            val tvPaymentMessage = dialogView.findViewById<TextView>(R.id.tvPaymentMessage)
            val btnCancelDialog = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.backButton)
            val btnOkDialog = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnOk)

            // 🌟 อัปเดตข้อความใน Popup ให้แสดงเฉพาะยอดที่ "เรา" ต้องจ่าย
            tvPaymentMessage?.text = String.format("You need to pay\n%.2f ฿", myAmount)

            // 4. กด Back ปิด Popup
            btnCancelDialog?.setOnClickListener {
                dialog.dismiss()
            }

            // 5. กด OK ส่งข้อมูลไปหน้า WhoPays
            btnOkDialog?.setOnClickListener {
                val nameMap = HashMap<String, String>()
                for (i in groupMemberUids.indices) {
                    if (i < groupMemberNames.size) {
                        nameMap[groupMemberUids[i]] = groupMemberNames[i]
                    }
                }

                val intent = Intent(this@BillSplit, WhoPays::class.java)
                intent.putExtra("SPLIT_RESULT", amountPerPerson)
                intent.putExtra("MEMBER_NAMES", nameMap)
                intent.putExtra("BILL_ITEMS", billList)
                startActivity(intent)

                dialog.dismiss()
            }

            dialog.show()
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