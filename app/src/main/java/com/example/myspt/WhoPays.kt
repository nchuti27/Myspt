package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class WhoPays : AppCompatActivity() {

    private var backButton: ImageButton? = null
    private var btnMenu: ImageView? = null
    private var Ptabfriend: TextView? = null
    private var btnConfirm: Button? = null
    private var isConfirmed: Boolean = false
    private var etBillName: EditText? = null


    private var rvSummaryItems: RecyclerView? = null
    private var tvTotalAmount: TextView? = null


    private var rvPayersList: RecyclerView? = null
    private lateinit var payerAdapter: PayerAdapter
    private var payersDataList = ArrayList<PayerData>()

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { navigateBack() }
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
        rvPayersList = findViewById(R.id.rvPayersList) // 🌟 ต้องมีใน XML นะครับ

        findViewById<TextView>(R.id.tabItems)?.setOnClickListener { navigateBack() }
    }

    private fun setupData() {
        val splitResult = intent.getSerializableExtra("SPLIT_RESULT") as? HashMap<String, Double>
        val namesMap = intent.getSerializableExtra("MEMBER_NAMES") as? HashMap<String, String>
        val selectedUids = intent.getStringArrayListExtra("SELECTED_MEMBERS") ?: arrayListOf()


        val rawItems = intent.getSerializableExtra("BILL_ITEMS") as? ArrayList<*>
        billItems.clear()

        rawItems?.forEach { item ->
            if (item is BillItem) {
                billItems.add(item)
            }
        }


        if (billItems.isEmpty()) {
            billItems.add(BillItem("Loading...", 1, 0.0))
        }

        etBillName?.setText(billName)
        if (splitResult != null) amountPerPerson.putAll(splitResult)
        if (namesMap != null) memberNames.putAll(namesMap)

        totalAmount = amountPerPerson.values.sum()
        tvTotalAmount?.text = String.format("%.2f ฿", totalAmount)

        payersDataList.clear()
        for (uid in selectedUids) {
            payersDataList.add(PayerData(uid, memberNames[uid] ?: "Unknown", 0.0))
        }


        val itemSummaryAdapter = ItemSummaryAdapter(billItems)
        rvSummaryItems?.layoutManager = LinearLayoutManager(this)
        rvSummaryItems?.adapter = itemSummaryAdapter
        itemSummaryAdapter.notifyDataSetChanged() //


        payerAdapter = PayerAdapter(payersDataList)
        rvPayersList?.layoutManager = LinearLayoutManager(this) //
        rvPayersList?.adapter = payerAdapter
        payerAdapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        backButton?.setOnClickListener { navigateBack() }
        btnConfirm?.setOnClickListener { processConfirm() }
        Ptabfriend?.setOnClickListener {
            if (isConfirmed) {
                val payersMap = HashMap<String, Double>()
                payersDataList.forEach { payersMap[it.uid] = it.amountPaid }
                val intent = Intent(this, FriendOwe::class.java).apply {
                    putExtra("SPLIT_RESULT", amountPerPerson)
                    putExtra("MEMBER_NAMES", memberNames)
                    putExtra("PAYERS_MAP", payersMap)
                }
                startActivity(intent)
            } else {

                Toast.makeText(this, "Please confirm payment first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processConfirm() {
        if (isConfirmed) return

        val totalPaid = payersDataList.sumOf { it.amountPaid }


        android.util.Log.d("WhoPays", "totalPaid=$totalPaid, totalAmount=$totalAmount")

        if (Math.abs(totalPaid - totalAmount) > 0.01) {
            Toast.makeText(
                this,
                "Total Amount Paid ฿${String.format("%.2f", totalPaid)} does not match bill ฿${String.format("%.2f", totalAmount)}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Save this bill and notify friends?")
            .setPositiveButton("Confirm") { _, _ -> saveBillWithPoolOffset() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveBillWithPoolOffset() {
        val finalBillName = etBillName?.text.toString().trim().ifEmpty { billName }
        val payersMap = payersDataList.associate { it.uid to it.amountPaid }

        val billData = hashMapOf(
            "billName" to finalBillName,
            "totalAmount" to totalAmount,
            "payers" to payersMap,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "status" to "pending",
            "items" to billItems
        )

        db.collection("bills").add(billData).addOnSuccessListener { billRef ->

            val creditors = mutableListOf<Triple<String, String, Double>>() // uid, name, เงินส่วนเกิน
            val debtors = mutableListOf<Triple<String, String, Double>>()   // uid, name, เงินที่ขาด

            for (memberId in memberNames.keys) {
                val share = amountPerPerson[memberId] ?: 0.0
                val paid = payersMap[memberId] ?: 0.0
                val offset = paid - share

                when {
                    offset > 0.01  -> creditors.add(Triple(memberId, memberNames[memberId] ?: "Unknown", offset))
                    offset < -0.01 -> debtors.add(Triple(memberId, memberNames[memberId] ?: "Unknown", Math.abs(offset)))
                }
            }


            for (debtor in debtors) {
                for (creditor in creditors) {
                    val debtData = hashMapOf(
                        "billId"       to billRef.id,
                        "billName"     to finalBillName,
                        "status"       to "pending",
                        "amount"       to debtor.third,
                        "name"         to debtor.second,      // ชื่อลูกหนี้
                        "creditorName" to creditor.second,    // ชื่อเจ้าหนี้จริง
                        "creditorId"   to creditor.first,     // uid เจ้าหนี้
                        "friendId"     to debtor.first,       // uid ลูกหนี้
                        "timestamp"    to FieldValue.serverTimestamp()
                    )
                    db.collection("debts").add(debtData)
                }
            }

            isConfirmed = true
            btnConfirm?.isEnabled = false
            btnConfirm?.backgroundTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)
            Toast.makeText(this, "Bill & Debts Saved!", Toast.LENGTH_SHORT).show()
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
        dialog.findViewById<Button>(R.id.btnNo)?.setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            dialog.dismiss()
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
        dialog.findViewById<Button>(R.id.btnOk)?.setOnClickListener { dialog.dismiss() }
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