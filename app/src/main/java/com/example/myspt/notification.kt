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

class notification : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notiListener: ListenerRegistration? = null

    private var btnBack: ImageButton? = null
    private var btnTabGroup: Button? = null
    private var rvFriendNoti: RecyclerView? = null

    // รายการข้อมูลแจ้งเตือนและ Adapter
    private var notiList = ArrayList<DocumentSnapshot>()
    private lateinit var notiAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupRecyclerView()
        setupListeners()
        listenToFriendRequests()
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        rvFriendNoti = findViewById(R.id.rvFriendNoti)
    }

    private fun setupRecyclerView() {
        // สร้าง Adapter พร้อมกำหนดสิ่งที่ต้องทำเมื่อกดปุ่ม Accept หรือ Delete [cite: 2026-02-21]
        notiAdapter = NotificationAdapter(notiList,
            onAccept = { doc -> acceptFriend(doc) },
            onDelete = { doc -> deleteRequest(doc) }
        )
        rvFriendNoti?.apply {
            layoutManager = LinearLayoutManager(this@notification)
            adapter = notiAdapter
        }
    }

    private fun setupListeners() {
        btnBack?.setOnClickListener { finish() }

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

        // แก้ปัญหา ANR: ใช้ addSnapshotListener ดึงข้อมูลแบบ Real-time เฉพาะคำขอที่รอดำเนินการ [cite: 2026-02-21]
        notiListener = db.collection("friend_requests")
            .whereEqualTo("to_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null) {
                    notiList.clear()
                    notiList.addAll(snapshots.documents)
                    notiAdapter.notifyDataSetChanged() // อัปเดตรายการบนหน้าจอทันที [cite: 2026-02-21]
                }
            }
    }

    // ฟังก์ชันเมื่อกด Accept: อัปเดตทั้งเราและเพื่อน
    private fun acceptFriend(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val senderUid = doc.getString("from_uid") ?: return
        val senderName = doc.getString("from_name") ?: "Unknown User" // ดึงชื่อจากฟิลด์ที่เราบันทึกไว้
        val requestId = doc.id

        val batch = db.batch()

        // 1. ลบคำขอเพื่อนทิ้งหลังจากยอมรับ (แทนการเปลี่ยน status เพื่อไม่ให้เปลืองพื้นที่)
        val requestRef = db.collection("friend_requests").document(requestId)
        batch.delete(requestRef)

        // 2. เพิ่มเพื่อนในรายการของเรา (Array 'friends' ในคอลเลกชัน 'users')
        val myUserRef = db.collection("users").document(myUid)
        batch.update(myUserRef, "friends", FieldValue.arrayUnion(senderUid))

        // 3. เพิ่มเราในรายการของเพื่อน
        val senderUserRef = db.collection("users").document(senderUid)
        batch.update(senderUserRef, "friends", FieldValue.arrayUnion(myUid))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "You are now friends with $senderName", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    // ฟังก์ชันเมื่อกด Delete: ลบรายการแจ้งเตือนออก
    private fun deleteRequest(doc: DocumentSnapshot) {
        db.collection("friend_requests").document(doc.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Request deleted", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        notiListener?.remove() // หยุดการทำงานของ Listener เพื่อป้องกันการรั่วไหลของหน่วยความจำ [cite: 2026-02-21]
    }
}