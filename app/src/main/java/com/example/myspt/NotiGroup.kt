package com.example.myspt

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotiGroup : AppCompatActivity() {
    var btnBack: ImageButton? = null
    var btnTabFriend: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noti_group)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        btnBack?.setOnClickListener {
            finish()
        }
        btnTabFriend?.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
            finish()
        }

    }private fun init() {
        btnBack = findViewById(R.id.backButton)
        btnTabFriend = findViewById(R.id.btnTabFriend)

    }
}