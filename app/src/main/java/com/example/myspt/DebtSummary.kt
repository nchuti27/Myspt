package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class DebtSummary : AppCompatActivity() {
    var btnBack: ImageView? = null
    var tabFriends: TextView? = null
    var rvDebts: RecyclerView? = null
    var btnMenu: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_debt_summary)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        btnBack?.setOnClickListener {
            finish()
        }

        tabFriends?.setOnClickListener {
            val intent = Intent(this, FriendOwe::class.java)
            startActivity(intent)
            finish()
        }

        btnMenu?.setOnClickListener { view ->
            val popupMenu = PopupMenu(this@DebtSummary, view)
            popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_add_member -> {
                        try {
                            val intent = Intent(this@DebtSummary, SelectFriend::class.java)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@DebtSummary, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    R.id.action_edit_items -> {
                        val intent = Intent(this@DebtSummary, BillSplit::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.action_leave_group -> {
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
                        true
                    }
                    else -> false
                }
            } // ปิด setOnMenuItemClickListener
            popupMenu.show()
        } // ปิด btnMenu.setOnClickListener
    } // ปิด onCreate

    private fun init() {
        btnBack = findViewById(R.id.imageView)
        tabFriends = findViewById(R.id.tabFriends)
        rvDebts = findViewById(R.id.rvDebts)
        btnMenu = findViewById(R.id.btnMenu)
    }
}