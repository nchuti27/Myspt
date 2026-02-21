package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class notification : AppCompatActivity() {
    var btnBack: ImageButton? = null
    var btnTabGroup: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        btnBack?.setOnClickListener {
            finish()
        }
        btnTabGroup?.setOnClickListener {
            val intent = Intent(this, NotiGroup::class.java)
            startActivity(intent)
            finish()
        }

    }
    private fun init() {
        btnBack = findViewById(R.id.backButton)
        btnTabGroup = findViewById(R.id.btnTabGroup)
    }

}