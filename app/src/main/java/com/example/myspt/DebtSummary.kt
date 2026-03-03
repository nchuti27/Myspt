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

    // 🌟 เพิ่มตัวแปรสำหรับ RecyclerView และ Adapter แยกสองฝั่ง
    private lateinit var rvOweYou: RecyclerView
    private lateinit var rvYouOwe: RecyclerView
    private lateinit var oweYouAdapter: DebtAdapter // ใช้ตัวที่มี Checkbox/กระดิ่ง
    private lateinit var youOweAdapter: DebtYouOweAdapter // ใช้ตัวโชว์ยอดเฉยๆ

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

        // 🌟 ปุ่มกดย้อนกลับ
        btnBack?.setOnClickListener { finish() }

        // สลับแท็บไปหน้า FriendOwe
        tabFriends?.setOnClickListener {
            val intent = Intent(this, FriendOwe::class.java)
            val options = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
            finish()
        }

        // เมนูเพิ่มเติม
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
        // 1. ฝั่ง Owe You (เขาติดหนี้เรา): ใส่ลอจิกเมื่อติ๊กช่อง Checkbox
        oweYouAdapter = DebtAdapter(arrayListOf()) { debt ->
            // 🌟 เมื่อติ๊กช่อง cbDebt (อนุมัติว่าเขาจ่ายแล้ว)
            confirmPayment(debt)
        }

        rvOweYou.layoutManager = LinearLayoutManager(this)
        rvOweYou.adapter = oweYouAdapter

        // 2. ฝั่ง You Owe (เราติดหนี้เขา): แสดงผลยอดเงินปกติ
        youOweAdapter = DebtYouOweAdapter(arrayListOf())
        rvYouOwe.layoutManager = LinearLayoutManager(this)
        rvYouOwe.adapter = youOweAdapter

        // ดึงข้อมูลจริงจาก Firestore
        loadDebtData()
    }

    private fun confirmPayment(debt: Debt) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Did you receive ฿${String.format("%.2f", debt.amount)} from ${debt.name}?")
            .setPositiveButton("Yes") { _, _ ->
                // 🌟 ลบหนี้ออกจาก Database จริงๆ เมื่อยืนยันรับเงิน
                db.collection("debts").document(debt.debtId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Debt cleared successfully!", Toast.LENGTH_SHORT).show()
                        // ไม่ต้องสั่ง updateData เองเพราะ Listener จะจัดการให้ครับ
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadDebtData() {
        val myUid = auth.currentUser?.uid ?: return

        // 🌟 ดึงข้อมูลแบบ Real-time เพื่อให้หน้าจออัปเดตทันทีที่ลบ
        db.collection("debts").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                val oweYouList = ArrayList<Debt>()
                val youOweList = ArrayList<Debt>()

                for (doc in snapshots.documents) {
                    val debt = doc.toObject(Debt::class.java) ?: continue
                    debt.debtId = doc.id

                    if (debt.creditorId == myUid) {
                        oweYouList.add(debt) // เราเป็นเจ้าหนี้
                    } else if (debt.friendId == myUid) {
                        youOweList.add(debt) // เราเป็นลูกหนี้
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