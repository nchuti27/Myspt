package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DebtSummary : AppCompatActivity() {

    private var btnBack: ImageView? = null
    private var tabFriends: TextView? = null
    private var btnMenu: ImageView? = null

    private lateinit var rvOweYou: RecyclerView
    private lateinit var rvYouOwe: RecyclerView
    private lateinit var oweYouAdapter: DebtAdapter
    private lateinit var youOweAdapter: DebtYouOweAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_summary)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupAdapters() // ตั้งค่าตัวจัดการรายการ

        btnBack?.setOnClickListener { finish() }

        tabFriends?.setOnClickListener {
            val intent = Intent(this, FriendOwe::class.java)
            val options = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
            finish()
        }

        btnMenu?.setOnClickListener { view ->
            val popupMenu = PopupMenu(this@DebtSummary, view)
            popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit_items -> {
                        startActivity(Intent(this, BillSplit::class.java))
                        true
                    }
                    R.id.action_leave_group -> {
                        showLeaveGroupDialog()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        tabFriends = findViewById(R.id.tabFriends)
        btnMenu = findViewById(R.id.btnMenu)

        // 🌟 เชื่อม ID RecyclerView ให้ตรงกับหน้า XML
        rvOweYou = findViewById(R.id.rvOweYou)
        rvYouOwe = findViewById(R.id.rvYouOwe)
    }

    private fun setupAdapters() {
        // 1. ฝั่ง Owe You
        oweYouAdapter = DebtAdapter(arrayListOf()) { debt ->
            confirmPayment(debt)
        }
        rvOweYou.layoutManager = LinearLayoutManager(this)
        rvOweYou.adapter = oweYouAdapter

        // 2. ฝั่ง You Owe
        youOweAdapter = DebtYouOweAdapter(arrayListOf())
        rvYouOwe.layoutManager = LinearLayoutManager(this)
        rvYouOwe.adapter = youOweAdapter


        loadDebtData()
    }

    private fun confirmPayment(debt: Debt) {
        val myUid = auth.currentUser?.uid ?: return

        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Did you receive ฿${String.format(java.util.Locale.getDefault(), "%.2f", debt.amount)} from ${debt.name}?")
            .setPositiveButton("Yes") { _, _ ->
                val batch = db.batch()

                // ✅ 1. ลบหนี้
                val debtRef = db.collection("debts").document(debt.debtId)
                batch.delete(debtRef)

                // ✅ 2. ส่ง notification ไปหาลูกหนี้ (friendId) ว่าเจ้าหนี้ยืนยันรับเงินแล้ว
                val notiRef = db.collection("notifications").document()
                batch.set(notiRef, hashMapOf(
                    "to_uid"    to debt.friendId,   // ลูกหนี้รับ noti
                    "from_uid"  to myUid,
                    "from_name" to (auth.currentUser?.displayName ?: debt.creditorName),
                    "type"      to "PAYMENT_RECEIVED",
                    "message"   to "${debt.creditorName} confirmed your payment of ฿${String.format("%.2f", debt.amount)} for ${debt.billName}",
                    "status"    to "pending",
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ))

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "Debt cleared & notified!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadDebtData() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("debts").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                val oweYouList = ArrayList<Debt>()
                val youOweList = ArrayList<Debt>()

                for (doc in snapshots.documents) {
                    val status = doc.getString("status") ?: "pending"
                    if (status != "pending") continue

                    val debt = doc.toObject(Debt::class.java) ?: continue
                    debt.debtId = doc.id

                    if (debt.creditorId == myUid) {
                        oweYouList.add(debt)
                    } else if (debt.friendId == myUid) {
                        youOweList.add(debt)
                    }
                }
                oweYouAdapter.updateData(oweYouList)
                youOweAdapter.updateData(youOweList)
            }
        }
    }

    private fun showLeaveGroupDialog() {
        val dialog = android.app.Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_leave_group)

        dialog.findViewById<View>(R.id.btnNo).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "You have left the group.", Toast.LENGTH_SHORT).show()
            finish()
        }
        dialog.show()
    }
}