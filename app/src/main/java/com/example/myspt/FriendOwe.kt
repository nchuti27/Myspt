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

class FriendOwe : AppCompatActivity() {

    var btnBack: ImageView? = null
    var tabItems: TextView? = null
    var rvFriendOwe: RecyclerView? = null

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
            finish()
        }
        tabItems?.setOnClickListener {
            val intent = Intent(this, DebtSummary::class.java)
            val options = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
            finish()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tabItems = findViewById(R.id.tabItems)
        rvFriendOwe = findViewById(R.id.rvFriendOwe)
    }
}