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

class FriendOwe : AppCompatActivity() {
    var btnBack: ImageView? = null
    var tabItems: TextView? = null
    var rvFriendOwe: RecyclerView? = null
    var btnMenu: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_owe)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()

        btnBack?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        tabItems?.setOnClickListener {
            val intent = Intent(this, DebtSummary::class.java)
            val options = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
            finish()
        }
        btnMenu?.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit_items -> {
                        val intent = Intent(this, BillSplit::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.action_leave_group -> {
                        try {
                            val dialog = android.app.Dialog(this)
                            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                            dialog.setContentView(R.layout.dialog_leave_group)

                            val btnNo = dialog.findViewById<android.widget.Button>(R.id.btnNo)
                            val btnYes = dialog.findViewById<android.widget.Button>(R.id.btnYes)
                            val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

                            tvMessage?.text = "Are you sure you want\nto leave this group?"

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
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }


    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tabItems = findViewById(R.id.tabItems)
        rvFriendOwe = findViewById(R.id.rvFriendOwe)
        btnMenu = findViewById(R.id.btnMenu)
    }
}