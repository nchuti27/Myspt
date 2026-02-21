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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindUser : AppCompatActivity() {
    var btnBack: ImageButton? = null
    var tvFoundUserName: TextView? = null
    var btnAddFriend: Button? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finduser)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()

        val receivedName = intent.getStringExtra("FRIEND_NAME")
        val friendUid = intent.getStringExtra("FRIEND_UID")

        if (receivedName != null) {
            tvFoundUserName!!.text = "Found User: $receivedName"
        } else {
            tvFoundUserName!!.text = "User not found"
        }

        btnBack!!.setOnClickListener {
            finish()
        }

        btnAddFriend!!.setOnClickListener {
            if (friendUid != null) {
                sendFriendRequest(friendUid) // เปลี่ยนชื่อฟังก์ชันให้ชัดเจนขึ้น
            } else {
                Toast.makeText(this, "Cannot add this user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendFriendRequest(friendUid: String) {
        val myUid = auth.currentUser?.uid ?: return

        if (myUid == friendUid) {
            Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show()
            return
        }

        // สร้างข้อมูลคำขอเพื่อน [cite: 2026-02-09]
        val request = hashMapOf(
            "from_uid" to myUid,
            "to_uid" to friendUid,
            "status" to "pending", // สถานะเริ่มต้นคือรอดำเนินการ [cite: 2026-02-09]
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // บันทึกลงคอลเลกชัน friend_requests
        db.collection("friend_requests")
            .add(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Friend request sent!", Toast.LENGTH_LONG).show()

                // กลับไปหน้าหลัก
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        btnAddFriend = findViewById(R.id.btnAddFriend)
    }
}