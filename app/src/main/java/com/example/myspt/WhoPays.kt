package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback // 🌟 นำเข้าคลาสใหม่สำหรับปุ่ม Back
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
    private var etBillName: EditText? = null

    private var rvSummaryItems: RecyclerView? = null
    private var tvTotalAmount: TextView? = null
    private var etPaidAmount: EditText? = null
    private var spinnerPayer: Spinner? = null

    private var amountPerPerson = HashMap<String, Double>()
    private var memberNames = HashMap<String, String>()
    private var uidList = ArrayList<String>()
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

        // 🌟 แก้ Error: ดักการกดปุ่มย้อนกลับด้วยวิธีใหม่ของ Android (แทน onBackPressed)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBack()
            }
        })

        init()
        setupData()
        setupClickListeners()
    }

    private fun init() {
        etBillName = findViewById(R.id.etBillName)
        backButton = findViewById(R.id.backButton)
        btnMenu = findViewById(R.id.btnMenu)
        Ptabfriend = findViewById(R.id.Ptabfriend)
        btnConfirm = findViewById(R.id.btnConfirm)

        rvSummaryItems = findViewById(R.id.rvSummaryItems)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        etPaidAmount = findViewById(R.id.etPaidAmount)
        spinnerPayer = findViewById(R.id.spinnerPayer)

        findViewById<TextView>(R.id.tabItems)?.setOnClickListener {
            navigateBack()
        }
    }

    private fun setupData() {
        val splitResult = intent.getSerializableExtra("SPLIT_RESULT") as? HashMap<String, Double>
        val namesMap = intent.getSerializableExtra("MEMBER_NAMES") as? HashMap<String, String>
        val itemsList = intent.getSerializableExtra("BILL_ITEMS") as? ArrayList<BillItem>

        etBillName?.setText(billName)

        if (splitResult != null) amountPerPerson.putAll(splitResult)
        if (namesMap != null) memberNames.putAll(namesMap)

        if (itemsList != null) {
            val allEnteredItems = itemsList.filter { it.itemName.isNotBlank() }
            billItems.addAll(allEnteredItems)
        }

        totalAmount = amountPerPerson.values.sum()
        tvTotalAmount?.text = String.format("%.2f ฿", totalAmount)
        etPaidAmount?.setText(String.format("%.2f", totalAmount))

        uidList.addAll(memberNames.keys)
        val nameList = uidList.map { memberNames[it] ?: "Unknown" }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nameList)
        spinnerPayer?.adapter = spinnerAdapter

        rvSummaryItems?.layoutManager = LinearLayoutManager(this)
        rvSummaryItems?.adapter = ItemSummaryAdapter(billItems)
    }

    private fun setupClickListeners() {
        backButton?.setOnClickListener { navigateBack() }

        btnConfirm?.setOnClickListener {
            if (isConfirmed) return@setOnClickListener

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Payment")
                .setMessage("Are you sure you want to confirm? Once confirmed, this bill cannot be edited again.")
                .setPositiveButton("Confirm") { dialog, _ ->
                    saveBillToDatabase()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        Ptabfriend?.setOnClickListener {
            if (isConfirmed) {
                val intent = Intent(this, FriendOwe::class.java)

                val myUid = auth.currentUser?.uid ?: ""
                val selectedPayerIndex = spinnerPayer?.selectedItemPosition ?: 0
                val actualPayerUid = if (uidList.isNotEmpty()) uidList[selectedPayerIndex] else myUid

                intent.putExtra("PAYER_UID", actualPayerUid)
                intent.putExtra("SPLIT_RESULT", amountPerPerson)
                intent.putExtra("MEMBER_NAMES", memberNames)

                startActivity(intent)
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

        val currentBillName = etBillName?.text.toString().trim().ifEmpty { billName }

        btnConfirm?.isEnabled = false
        btnConfirm?.alpha = 0.5f

        val billData = hashMapOf(
            "billName" to currentBillName,
            "totalAmount" to totalAmount,
            "paidBy" to actualPayerUid,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "status" to "pending",
            "splitDetails" to amountPerPerson,
            "items" to billItems
        )

        db.collection("bills").add(billData)
            .addOnSuccessListener {
                isConfirmed = true
                Toast.makeText(this, "Bill Saved Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnConfirm?.isEnabled = true
                btnConfirm?.alpha = 1.0f
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateBack() {
        if (isConfirmed) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_items -> { navigateBack(); true }
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

    class ItemSummaryAdapter(private val items: List<BillItem>) : RecyclerView.Adapter<ItemSummaryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvItemName: TextView = view.findViewById(R.id.tvItemName)
            val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvItemName.text = item.itemName
            holder.tvQuantity.text = item.quantity.toString()
            holder.tvPrice.text = String.format("%.2f ฿", item.price * item.quantity)
        }

        override fun getItemCount() = items.size
    }
}