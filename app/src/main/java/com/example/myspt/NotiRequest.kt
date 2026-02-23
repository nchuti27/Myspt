package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class NotiRequest : AppCompatActivity() {

   private var backButton: ImageButton? = null
    private var btnTabFriend: Button? = null // เปลี่ยนจาก RecyclerView เป็น Button
    private var btnTabGroup: Button? = null
    private var rvNotification: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noti_request)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
    }

    private fun init() {
        backButton = findViewById(R.id.backButton)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        rvNotification = findViewById(R.id.rvNotification)

        backButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        btnTabFriend?.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        btnTabGroup?.setOnClickListener {
            val intent = Intent(this, NotiGroup::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}