package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class notification : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notiListener: ListenerRegistration? = null

    private var btnBack: ImageButton? = null
    private var btnTabGroup: Button? = null
    private var rvFriendNoti: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // จัดการ Insets โดยอ้างอิงจาก ID 'main' ใน XML [cite: 2026-02-21]
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupListeners()
        listenToFriendRequests() // เริ่มดึงข้อมูลแบบ Real-time เพื่อลดปัญหา ANR [cite: 2026-02-21]
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        rvFriendNoti = findViewById(R.id.rvFriendNoti)
        rvFriendNoti?.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        btnBack?.setOnClickListener {
            finish()
        }

        btnTabGroup?.setOnClickListener {
            val intent = Intent(this, NotiGroup::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun listenToFriendRequests() {
        val myUid = auth.currentUser?.uid ?: return

        // ใช้ addSnapshotListener เพื่อให้แอปไม่ค้างและอัปเดตข้อมูลทันทีเมื่อมีการเปลี่ยนแปลง [cite: 2026-02-21]
        notiListener = db.collection("friend_requests")
            .whereEqualTo("to_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null && !snapshots.isEmpty) {
                    val requests = snapshots.documents

                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // สำคัญมาก: ต้องลบ Listener เมื่อปิดหน้าจอเพื่อป้องกัน Memory Leak [cite: 2026-02-21]
        notiListener?.remove()
    }
}