package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FindUser : AppCompatActivity() {
    var btnBack: ImageButton? = null
    var tvFoundUserName: TextView? = null
    var btnAddFriend: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_find_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        val receivedName = intent.getStringExtra("USER_NAME_KEY")
        if (receivedName != null) {
            tvFoundUserName!!.text = receivedName
        } else {
            tvFoundUserName!!.text = "User not found"
        }

        btnBack!!.setOnClickListener {
            val intent = Intent(this, AddFriend::class.java)
            startActivity(intent)
        }
        btnAddFriend!!.setOnClickListener {
            Toast.makeText(this, "The request has been submitted", Toast.LENGTH_LONG).show()
        }

    }
    private fun init() {
        btnBack = findViewById(R.id.btnBackF)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        btnAddFriend = findViewById(R.id.btnAddFriend)
    }
}