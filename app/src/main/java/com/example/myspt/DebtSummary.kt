package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class DebtSummary : AppCompatActivity() {
    var btnBack: ImageView? = null
    var tabFriends: TextView? = null
    var rvDebts: RecyclerView? = null

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
    }
    private fun init() {
        btnBack = findViewById(R.id.imageView)
        tabFriends = findViewById(R.id.tabFriends)
        rvDebts = findViewById(R.id.rvDebts)
    }
}