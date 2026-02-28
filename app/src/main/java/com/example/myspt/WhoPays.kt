package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WhoPays : AppCompatActivity() {

    private var backButton: ImageButton? = null
    private var btnMenu: ImageView? = null
    private var Ptabfriend: TextView? = null
    private var btnConfirm: Button? = null
    private var isConfirmed: Boolean = false

    private var rvSummaryItems: RecyclerView? = null
    private var tvTotalAmount: TextView? = null
    private var etPaidAmount: EditText? = null
    private var spinnerPayer: Spinner? = null

    // ตัวแปรรับข้อมูล
    private var amountPerPerson = HashMap<String, Double>()
    private var memberNames = HashMap<String, String>()
    private var uidList = ArrayList<String>()

    // 🌟 ตัวแปรเก็บรายการอาหารที่รับมาจากหน้าบิล
    private var billItems = ArrayList<BillItem>()

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var totalAmount: Double = 0.0
    private var billName: String = "New Bill"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_who_pays)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        billName = intent.getStringExtra("BILL_NAME") ?: "New Bill"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupData()
        setupClickListeners()
    }

    private fun init() {
        backButton = findViewById(R.id.backButton)
        btnMenu = findViewById(R.id.btnMenu)
        Ptabfriend = findViewById(R.id.Ptabfriend)
        btnConfirm = findViewById(R.id.btnConfirm)

        rvSummaryItems = findViewById(R.id.rvSummaryItems)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        etPaidAmount = findViewById(R.id.etPaidAmount)
        spinnerPayer = findViewById(R.id.spinnerPayer)
    }

    private fun setupData() {
        val splitResult = intent.getSerializableExtra("SPLIT_RESULT") as? HashMap<String, Double>
        val namesMap = intent.getSerializableExtra("MEMBER_NAMES") as? HashMap<String, String>

        // 🌟 รับรายการอาหารจาก Intent
        val itemsList = intent.getSerializableExtra("BILL_ITEMS") as? ArrayList<BillItem>

        if (splitResult != null) amountPerPerson.putAll(splitResult)
        if (namesMap != null) memberNames.putAll(namesMap)

        if (itemsList != null) {
            // 🌟 แก้ไขตรงนี้: ดึงรายการทั้งหมดที่มีการพิมพ์ "ชื่อเมนู" มาแสดง
            // ไม่ต้องสนใจว่าราคาจะว่างเปล่า เป็น 0 หรือมีคนหารหรือไม่
            val allEnteredItems = itemsList.filter { it.itemName.isNotBlank() }
            billItems.addAll(allEnteredItems)
        }

        totalAmount = amountPerPerson.values.sum()
        tvTotalAmount?.text = String.format("%.2f ฿", totalAmount)
        etPaidAmount?.setText(totalAmount.toString())

        uidList.addAll(memberNames.keys)
        val nameList = uidList.map { memberNames[it] ?: "Unknown" }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nameList)
        spinnerPayer?.adapter = spinnerAdapter

        // 🌟 นำรายการอาหาร (billItems) เข้า Adapter ใหม่
        rvSummaryItems?.layoutManager = LinearLayoutManager(this)
        rvSummaryItems?.adapter = ItemSummaryAdapter(billItems)
    }

    private fun setupClickListeners() {
        backButton?.setOnClickListener { finish() }

        btnConfirm?.setOnClickListener { saveBillToDatabase() }

        Ptabfriend?.setOnClickListener {
            if (isConfirmed) {
                val intent = Intent(this, FriendOwe::class.java)
                startActivity(intent)
                finish()
            } else {
                showPleaseConfirmDialog()
            }
        }

        btnMenu?.setOnClickListener { view -> showMenu(view) }
    }

    private fun saveBillToDatabase() {
        val myUid = auth.currentUser?.uid ?: return
        val selectedPayerIndex = spinnerPayer?.selectedItemPosition ?: 0
        val actualPayerUid = if (uidList.isNotEmpty()) uidList[selectedPayerIndex] else myUid

        val billData = hashMapOf(
            "billName" to billName,
            "totalAmount" to totalAmount,
            "paidBy" to actualPayerUid,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "status" to "pending",
            "splitDetails" to amountPerPerson,
            "items" to billItems // 🌟 บันทึกรายการอาหารทั้งหมดลงฐานข้อมูลด้วย!
        )

        db.collection("bills").add(billData)
            .addOnSuccessListener {
                isConfirmed = true
                Toast.makeText(this, "Bill Saved Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_items -> { finish(); true }
                R.id.action_leave_group -> { showLeaveDialog(); true }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showLeaveDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_leave_group)

        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

        tvMessage?.text = "Are you sure you want to\n leave this group?"

        btnNo?.setOnClickListener { dialog.dismiss() }
        btnYes?.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "You have left the group.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }

    private fun showPleaseConfirmDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_confirm_payer)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
        btnOk?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ==========================================
    // 🌟 Adapter ใหม่สำหรับแสดง "รายการอาหาร" โดยเฉพาะ
    // ==========================================
    class ItemSummaryAdapter(private val items: List<BillItem>) : RecyclerView.Adapter<ItemSummaryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvItemName: TextView = view.findViewById(R.id.tvItemName)
            val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // โยงกับไฟล์ XML ใหม่ที่เพิ่งสร้าง
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvItemName.text = item.itemName
            holder.tvQuantity.text = item.quantity.toString()

            // แสดงราคา (ถ้ารวมราคาคูณจำนวนแล้ว ให้ใช้ item.price * item.quantity)
            holder.tvPrice.text = String.format("%.2f ฿", item.price * item.quantity)
        }

        override fun getItemCount() = items.size
    }
}