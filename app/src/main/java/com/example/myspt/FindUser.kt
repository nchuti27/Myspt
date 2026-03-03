package com.example.myspt

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindUser : AppCompatActivity() {
    private var btnAddFriend: Button? = null
    private var tvFoundUserName: TextView? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finduser)

        // 1. ผูก ID ให้ชัวร์ (ต้องตรงกับ XML ที่พี่ส่งมาล่าสุด)
        initView()

        // 2. รับข้อมูลจาก Intent
        val friendUid = intent.getStringExtra("FRIEND_UID")
        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Unknown User"

        tvFoundUserName?.text = friendName

        // 3. เช็กสถานะเพื่อกำหนดปุ่ม
        if (friendUid != null) {
            checkStatus(friendUid, friendName)
        } else {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        btnAddFriend = findViewById(R.id.btnAddFriend)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun checkStatus(targetUid: String, targetName: String) {
        val myUid = auth.currentUser?.uid ?: return

        // ✅ ป้องกันแอดตัวเอง (ถ้าแอดตัวเอง ปุ่มจะหายไป) [cite: 2026-02-27]
        if (myUid == targetUid) {
            btnAddFriend?.visibility = View.GONE
            return
        }

        // มั่นใจว่าปุ่มต้องโชว์ไว้ก่อน
        btnAddFriend?.visibility = View.VISIBLE

        // 1. เช็กความเป็นเพื่อน
        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            @Suppress("UNCHECKED_CAST")
            val friends = doc.get("friends") as? List<String> ?: listOf()
            if (friends.contains(targetUid)) {
                updateButton("Already Friends", false)
            } else {
                // 2. ✅ เช็กคำขอค้าง (Pending) เพื่อขึ้น Requested
                db.collection("friend_requests")
                    .whereEqualTo("from_uid", myUid)
                    .whereEqualTo("to_uid", targetUid)
                    .whereEqualTo("status", "pending")
                    .get().addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            updateButton("Requested", false)
                        } else {
                            updateButton("ADD FRIEND", true)
                            btnAddFriend?.setOnClickListener { sendRequest(targetUid, targetName) }
                        }
                    }
                    .addOnFailureListener {
                        updateButton("ADD FRIEND", true) // ถ้าเช็กพลาดให้ขึ้นปุ่มปกติไว้ก่อน
                    }
            }
        }.addOnFailureListener {
            updateButton("ADD FRIEND", true)
        }
    }

    private fun updateButton(text: String, enabled: Boolean) {
        btnAddFriend?.apply {
            this.text = text
            this.isEnabled = enabled
            this.alpha = if (enabled) 1.0f else 0.5f
        }
    }

    private fun sendRequest(targetUid: String, targetName: String) {
        val myUid = auth.currentUser?.uid ?: return

        // ดึงชื่อเราไปโชว์ใน Noti เพื่อน
        db.collection("users").document(myUid).get().addOnSuccessListener { myDoc ->
            val request = hashMapOf(
                "from_uid" to myUid,
                "from_name" to (myDoc.getString("name") ?: "Someone"),
                "from_profileUrl" to myDoc.getString("profileUrl"),
                "to_uid" to targetUid,
                "to_name" to targetName,
                "status" to "pending",
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.collection("friend_requests").add(request).addOnSuccessListener {
                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                updateButton("Requested", false) // ✅ เปลี่ยนปุ่มทันที กันกดย้ำ
            }
        }
    }
}