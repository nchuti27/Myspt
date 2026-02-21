package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelectFriend : AppCompatActivity() {

    var btnBack: ImageButton? = null
    var tvNext: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friend)

        init()

        btnBack?.setOnClickListener {
            val intent = Intent(this, CreateGroup::class.java)
            startActivity(intent)
            finish()
        }
        tvNext?.setOnClickListener {
            val intent = Intent(this, CreateGroup::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvNext = findViewById(R.id.tvNext)
    }
}