package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotiGroup : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var groupNotiListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noti_group)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        listenToGroupInvites()
    }

    private fun init() {
        findViewById<ImageButton>(R.id.backButton)?.setOnClickListener { finish() }

        findViewById<Button>(R.id.btnTabFriend)?.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun listenToGroupInvites() {
        val myUid = auth.currentUser?.uid ?: return

        // คอยฟังคำเชิญเข้ากลุ่ม (สมมติว่าคุณเก็บในคอลเลกชัน group_invites) [cite: 2026-02-09]
        groupNotiListener = db.collection("group_invites")
            .whereEqualTo("to_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (snapshots != null) {
                    // อัปเดตข้อมูลกลุ่มที่นี่
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupNotiListener?.remove()
    }
}