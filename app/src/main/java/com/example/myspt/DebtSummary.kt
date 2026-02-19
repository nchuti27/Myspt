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
            val options = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
            finish()
        }

        btnMenu?.setOnClickListener { view ->
            // สร้าง PopupMenu โดยยึดติดกับ view (ตัว btnMenu)
            val popupMenu = PopupMenu(this, view)

            // ดึงไฟล์เมนูมาแสดง
            popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)

            // ดักจับว่าผู้ใช้กดเลือกเมนูไหน
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_add_member -> {
                        Toast.makeText(this, "กด Add member", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_edit_items -> {
                        Toast.makeText(this, "กด Edit items", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_leave_group -> {
                        Toast.makeText(this, "กด Leave group", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            // สั่งให้ PopupMenu แสดงขึ้นมา
            popupMenu.show()
        }
    } // สิ้นสุดฟังก์ชัน onCreate

    private fun init() {
        btnBack = findViewById(R.id.imageView)
        tabFriends = findViewById(R.id.tabFriends)
        rvDebts = findViewById(R.id.rvDebts)
        btnMenu = findViewById(R.id.btnMenu)
    }
} // สิ้นสุดคลาส DebtSummary