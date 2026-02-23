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

    // ประกาศตัวแปรเป็นแบบ Nullable ตามที่คุณต้องการ
    private var btnBack: ImageButton? = null
    private var rvGroupNoti: RecyclerView? = null // 🌟 เปลี่ยนกลับเป็น RecyclerView ให้ตรงกับ XML
    private var groupNotiList = ArrayList<DocumentSnapshot>()
    private lateinit var groupAdapter: NotificationAdapter

    private var btnTabFriend: Button? = null
    private var btnTabRequest: Button? = null

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
        // 🌟 ผูก ID ให้ตรงกับใน activity_noti_group.xml ที่เราเพิ่งแก้ไป
        rvGroupNoti = findViewById(R.id.rvGroupNoti)
        btnBack = findViewById(R.id.backButton)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabRequest = findViewById(R.id.btnTabRequest)

        // 🌟 ปุ่ม Back: กลับไปหน้า MainActivity (หน้าหลัก)
        btnBack?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // 🌟 ปุ่ม Friend: ไปหน้า notification
        btnTabFriend?.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0) // สลับหน้าแบบเนียนๆ
            finish()
        }

        // 🌟 ปุ่ม Request: ไปหน้า NotiRequest
        btnTabRequest?.setOnClickListener {
            val intent = Intent(this, NotiRequest::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ใช้ NotificationAdapter ตัวเดิมที่คุณเขียนไว้
        groupAdapter = NotificationAdapter(groupNotiList,
            onAccept = { doc -> joinGroup(doc) },
            onDelete = { doc -> declineGroup(doc) }
        )

        // 🌟 ตั้งค่า RecyclerView
        rvGroupNoti?.layoutManager = LinearLayoutManager(this)
        rvGroupNoti?.adapter = groupAdapter
    }

    private fun listenToGroupInvites() {
        val myUid = auth.currentUser?.uid ?: return

        // ดึงข้อมูลคำเชิญกลุ่มที่สถานะเป็น pending
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

        // 2. เพิ่ม UID เราเข้าไปในสมาชิกของกลุ่ม
        val groupRef = db.collection("groups").document(groupId)
        batch.update(groupRef, "members", FieldValue.arrayUnion(myUid))

        // 3. เพิ่ม Group ID เข้าไปในโปรไฟล์ของเรา
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
        groupNotiListener?.remove() // ล้าง Listener ป้องกันหน่วยความจำรั่ว
    }
}