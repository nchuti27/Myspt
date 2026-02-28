package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class FriendOwe : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var rvFriendOwe: RecyclerView? = null
    private var btnBack: ImageView? = null
    private var tvTotalBalance: TextView? = null
    private var tabItems: TextView? = null
    private var btnMenu: ImageView? = null

    private val oweList = ArrayList<OweItem>()
    private lateinit var adapter: OweAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_owe)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupDataFromIntent()
    }

    private fun init() {
        rvFriendOwe = findViewById(R.id.rvFriendOwe)
        btnBack = findViewById(R.id.backButton)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)

        // 🌟 ผูก ID ให้ปุ่ม Menu
        btnMenu = findViewById(R.id.btnMenu)

        tabItems = findViewById(R.id.tabItems)
        tabItems?.setOnClickListener {
            finish()
        }

        btnBack?.setOnClickListener { finish() }

        // 🌟 ดักจับการกดปุ่ม Menu เพื่อเรียกฟังก์ชันโชว์ Popup
        btnMenu?.setOnClickListener { view ->
            showMenu(view)
        }

        adapter = OweAdapter(oweList)
        rvFriendOwe?.layoutManager = LinearLayoutManager(this)
        rvFriendOwe?.adapter = adapter
    }

    private fun setupDataFromIntent() {
        val payerUid = intent.getStringExtra("PAYER_UID") ?: ""
        val splitResult = intent.getSerializableExtra("SPLIT_RESULT") as? HashMap<String, Double> ?: hashMapOf()
        val memberNames = intent.getSerializableExtra("MEMBER_NAMES") as? HashMap<String, String> ?: hashMapOf()

        oweList.clear()
        var grandTotal = 0.0

        for ((uid, amount) in splitResult) {
            if (uid != payerUid && amount > 0) {
                val name = memberNames[uid] ?: "Unknown"

                oweList.add(OweItem(
                    friendName = name,
                    amount = amount,
                    friendUid = uid
                ))
                grandTotal += amount
            }
        }

        if (oweList.isEmpty()) {
            tvTotalBalance?.text = "0.00 ฿"
            tvTotalBalance?.setTextColor(Color.GRAY)
        } else {
            tvTotalBalance?.text = String.format("%.2f ฿", grandTotal)
            tvTotalBalance?.setTextColor(if (grandTotal >= 0) Color.GREEN else Color.RED)
        }

        adapter.notifyDataSetChanged()
    }

    // ==========================================
    // 🌟 ฟังก์ชันจัดการ Popup Menu
    // ==========================================
    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        // สมมติว่าไฟล์ XML เมนูของคุณชื่อ menu_group_options.xml (ถ้าไม่ใช่ ให้เปลี่ยนชื่อให้ตรงนะครับ)
        popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_items -> {
                    finish() // กด Edit items ให้ปิดหน้านี้ เพื่อกลับไปหน้า WhoPays
                    true
                }
                R.id.action_leave_group -> {
                    showLeaveDialog() // กด Leave group โชว์หน้าต่างยืนยัน
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // ==========================================
    // 🌟 ฟังก์ชันแสดง Dialog ยืนยันการออกจากกลุ่ม
    // ==========================================
    private fun showLeaveDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_leave_group) // เรียกใช้ Layout Dialog เดียวกับหน้า WhoPays

        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

        tvMessage?.text = "Are you sure you want to\n leave this group?"

        btnNo?.setOnClickListener { dialog.dismiss() }
        btnYes?.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "You have left the group.", Toast.LENGTH_SHORT).show()

            // กลับไปหน้าแรกสุด และล้าง Stack
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}