package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class DebtSummary : AppCompatActivity() {
    // 🌟 ประกาศตัวแปรให้ตรงกับ Widget ใน XML
    private var btnBack: ImageView? = null
    private var tabFriends: TextView? = null
    private var rvDebts: RecyclerView? = null
    private var btnMenu: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_summary)

        // จัดการเรื่องขอบจอ (Padding) ให้ทำงานร่วมกับ fitsSystemWindows ใน XML
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        // 🌟 ปุ่มกดย้อนกลับ (เปลี่ยนจาก imageView เป็น backButton ตาม XML)
        btnBack?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        tabFriends?.setOnClickListener {
            val intent = Intent(this, FriendOwe::class.java)
            // ใช้ Animation แบบนุ่มนวลเวลาสลับ Tab
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
                        val intent = Intent(this@DebtSummary, BillSplit::class.java)
                        startActivity(intent)
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
        // 🌟 แก้ไขจุดที่ Error: เปลี่ยนไอดีให้ตรงกับไฟล์ XML
        btnBack = findViewById(R.id.backButton)
        tabFriends = findViewById(R.id.tabFriends)
        rvDebts = findViewById(R.id.rvDebts)
        btnMenu = findViewById(R.id.btnMenu)
    }

    private fun showLeaveGroupDialog() {
        try {
            val dialog = android.app.Dialog(this@DebtSummary)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setContentView(R.layout.dialog_leave_group)

            val btnNo = dialog.findViewById<android.widget.Button>(R.id.btnNo)
            val btnYes = dialog.findViewById<android.widget.Button>(R.id.btnYes)
            val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

            tvMessage?.text = "Are you sure you want\nto leave this group?"

            btnNo?.setOnClickListener { dialog.dismiss() }

            btnYes?.setOnClickListener {
                dialog.dismiss()
                Toast.makeText(this@DebtSummary, "You have left the group.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@DebtSummary, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this@DebtSummary, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}