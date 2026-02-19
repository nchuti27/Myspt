package com.example.myspt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SelectFriend : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ต้องมั่นใจว่าไฟล์ xml ชื่อ activity_select_friend
        setContentView(R.layout.activity_select_friend)
    }
}