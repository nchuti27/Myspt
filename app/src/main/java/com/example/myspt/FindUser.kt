package com.example.myspt

import android.content.Intent
import android.graphics.Color
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
            tvFoundUserName?.text = "$receivedName"
        } else {
            tvFoundUserName?.text = "User not found"
        }

        // ตรวจสอบสถานะเพื่อนทันทีเมื่อโหลดข้อมูลเสร็จ
        if (friendUid != null && btnAddFriend != null) {
            checkFriendStatus(friendUid, btnAddFriend!!)
        }

        btnBack?.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        btnAddFriend = findViewById(R.id.btnAddFriend)
    }

    // ฟังก์ชันเช็คสถานะเพื่อน: ตรวจสอบจากลิสต์เพื่อนของเราเองใน Firestore
    private fun checkFriendStatus(targetUid: String, btnAddFriend: Button) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val myFriends = document.get("friends") as? List<String> ?: listOf()

                    if (myFriends.contains(targetUid)) {
                        // กรณีเป็นเพื่อนกันอยู่แล้ว
                        btnAddFriend.text = "Already Friends"
                        btnAddFriend.isEnabled = false
                        btnAddFriend.setBackgroundColor(Color.GRAY)
                    } else {
                        // กรณีที่ยังไม่เป็นเพื่อนกัน
                        btnAddFriend.text = "Add Friend"
                        btnAddFriend.isEnabled = true
                        btnAddFriend.setOnClickListener {
                            sendFriendRequest(targetUid)
                        }
                    }
                }
            }
            .addOnFailureListener {
                // หากดึงข้อมูลล้มเหลว ให้ตั้งเป็นปุ่ม Add Friend ปกติไว้ก่อน
                btnAddFriend.setOnClickListener { sendFriendRequest(targetUid) }
            }
    }

    private fun sendFriendRequest(friendUid: String) {
        val myUid = auth.currentUser?.uid ?: return

        if (myUid == friendUid) {
            Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show()
            return
        }

        // ดึงชื่อของเรา (ผู้ส่ง) เพื่อบันทึกลงในคำขอเพื่อน
        db.collection("users").document(myUid).get()
            .addOnSuccessListener { myDoc ->
                val myName = myDoc.getString("username") ?: "Someone"

                val request = hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to myName, // ใส่ชื่อเพื่อให้ Notification โชว์ชื่อได้ทันที
                    "to_uid" to friendUid,
                    "status" to "pending",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )

                db.collection("friend_requests")
                    .add(request)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Friend request sent!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}