package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddFriend : AppCompatActivity() {

    var etSearchUser: EditText? = null
    var btnAdd: Button? = null
    var btnBackAF: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_friend)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
    }
    private fun init() {
        etSearchUser = findViewById<EditText>(R.id.etSearchUser)
        btnAdd = findViewById<Button>(R.id.btnAdd)

        val btnBack = findViewById<ImageButton>(R.id.btnBackF)

        btnBack.setOnClickListener{
            val intent = Intent(this, Friend_list::class.java)
            startActivity(intent)
        }

        btnAdd?.setOnClickListener {
            val username = etSearchUser?.text.toString().trim()

            if (username.isEmpty()) {
                etSearchUser?.error = "Please enter username"
                return@setOnClickListener
            }

            // 2. ถ้ามีข้อมูล ให้เรียกฟังก์ชันค้นหา
            checkUserAndNavigate(username)
        }
    }
    private fun checkUserAndNavigate(username: String) {
        // จำลอง Database
        val mockDatabase = listOf("admin", "friend1", "somchai")
        val isUserFound = mockDatabase.contains(username)

        if (isUserFound) {
            val intent = Intent(this, FindUser::class.java)
            intent.putExtra("FRIEND_NAME", username)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Not found user", Toast.LENGTH_SHORT).show()
        }
    }
}