package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotiGroup : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var groupNotiListener: ListenerRegistration? = null

    private var rvGroupNoti: RecyclerView? = null
    private var groupNotiList = ArrayList<DocumentSnapshot>()
    private lateinit var groupAdapter: NotificationAdapter

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
        setupRecyclerView()
        listenToGroupInvites()
    }

    private fun init() {
        rvGroupNoti = findViewById(R.id.rvGroupNoti) // ผูกไอดีให้ตรงกับ XML
        findViewById<ImageButton>(R.id.backButton)?.setOnClickListener { finish() }

        findViewById<Button>(R.id.btnTabFriend)?.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ใช้ NotificationAdapter ตัวเดิม [cite: 2026-02-21]
        groupAdapter = NotificationAdapter(groupNotiList,
            onAccept = { doc -> joinGroup(doc) },
            onDelete = { doc -> declineGroup(doc) }
        )
        rvGroupNoti?.apply {
            layoutManager = LinearLayoutManager(this@NotiGroup)
            adapter = groupAdapter
        }
    }

    private fun listenToGroupInvites() {
        val myUid = auth.currentUser?.uid ?: return

        // แก้ปัญหา ANR: ใช้ SnapshotListener ดึงข้อมูลคำเชิญแบบ Real-time [cite: 2026-02-21]
        groupNotiListener = db.collection("group_invites")
            .whereEqualTo("to_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null) {
                    groupNotiList.clear()
                    groupNotiList.addAll(snapshots.documents)
                    groupAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun joinGroup(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val groupId = doc.getString("groupId") ?: return
        val inviteId = doc.id

        val batch = db.batch()

        // 1. อัปเดตสถานะคำเชิญ
        val inviteRef = db.collection("group_invites").document(inviteId)
        batch.update(inviteRef, "status", "accepted")

        // 2. เพิ่ม UID เราเข้าไปในสมาชิกของกลุ่ม (members)
        val groupRef = db.collection("groups").document(groupId)
        batch.update(groupRef, "members", FieldValue.arrayUnion(myUid))

        // 3. เพิ่ม Group ID เข้าไปใน Profile ของเรา [cite: 2026-02-09]
        val userRef = db.collection("users").document(myUid)
        batch.update(userRef, "groups", FieldValue.arrayUnion(groupId))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Joined group!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun declineGroup(doc: DocumentSnapshot) {
        db.collection("group_invites").document(doc.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupNotiListener?.remove() // ล้าง Listener ป้องกัน Memory Leak [cite: 2026-02-21]
    }
}