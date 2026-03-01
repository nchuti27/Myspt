package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
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
    private var btnBack: ImageButton? = null
    private var tvFoundUserName: TextView? = null
    private var btnAddFriend: Button? = null

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
            tvFoundUserName?.text = receivedName
        } else {
            tvFoundUserName?.text = "User not found"
        }

        // ตรวจสอบสถานะเพื่อนทันทีเมื่อโหลดข้อมูลเสร็จ
        if (friendUid != null && btnAddFriend != null) {
            checkFriendStatus(friendUid, receivedName ?: "Unknown User")
        }

        btnBack?.setOnClickListener { finish() }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        btnAddFriend = findViewById(R.id.btnAddFriend)
    }

    private fun checkFriendStatus(targetUid: String, targetName: String) {
        val myUid = auth.currentUser?.uid ?: return

        // ป้องกันการแอดตัวเอง [cite: 2026-02-27]
        if (myUid == targetUid) {
            btnAddFriend?.visibility = View.GONE
            return
        }

        db.collection("users").document(myUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val myFriends = document.get("friends") as? List<String> ?: listOf()

                    if (myFriends.contains(targetUid)) {
                        btnAddFriend?.text = "Already Friends"
                        btnAddFriend?.isEnabled = false
                        btnAddFriend?.alpha = 0.5f // ทำให้ปุ่มดูจางลง
                    } else {
                        btnAddFriend?.text = "Add Friend"
                        btnAddFriend?.isEnabled = true
                        btnAddFriend?.setOnClickListener {
                            sendFriendRequest(targetUid, targetName)
                        }
                    }
                }
            }
    }

    private fun sendFriendRequest(friendUid: String, friendName: String) {
        val myUid = auth.currentUser?.uid ?: return

        // 1. ดึงข้อมูลโปรไฟล์ของเราเอง (คนส่ง) เพื่อเอาชื่อและรูปไปโชว์ในหน้า Noti เพื่อน
        db.collection("users").document(myUid).get()
            .addOnSuccessListener { myDoc ->
                // 🌟 เปลี่ยนจาก "username" เป็น "name" ให้ตรงกับหน้าอื่นๆ ของพี่
                val myName = myDoc.getString("name") ?: "Someone"
                val myProfileUrl = myDoc.getString("profileUrl")

                // 2. สร้างข้อมูลคำขอเพื่อน
                val request = hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to myName,      // ✅ ชื่อเรา (ไปโชว์ที่ Tab Friend ของเพื่อน)
                    "from_profileUrl" to myProfileUrl, // 🌟 เพิ่มรูปเราไปด้วย
                    "to_uid" to friendUid,
                    "to_name" to friendName,    // ✅ ชื่อเพื่อน (ไปโชว์ที่ Tab Request ของเรา)
                    "status" to "pending",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )

                // 3. บันทึกลง Firestore
                db.collection("friend_requests")
                    .add(request)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Friend request sent to $friendName!", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}