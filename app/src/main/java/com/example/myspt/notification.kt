package com.example.myspt

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.content.res.ColorStateList
import android.graphics.Color

class notification : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notiListener: ListenerRegistration? = null

    private lateinit var btnBack: ImageButton
    private lateinit var btnTabGroup: Button
    private lateinit var btnTabFriend: Button
    private lateinit var btnTabRequest: Button
    private lateinit var rvFriendNoti: RecyclerView

    private var notiList = ArrayList<DocumentSnapshot>()
    private lateinit var notiAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupRecyclerView()
        setupTabListeners()

        // เริ่มต้นให้แสดงหน้า Request เป็นหน้าแรกตามที่คุณต้องการ
        loadRequestTab()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.backButton)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabRequest = findViewById(R.id.btnTabRequest)
        rvFriendNoti = findViewById(R.id.rvFriendNoti)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        notiAdapter = NotificationAdapter(notiList,
            onAccept = { doc -> acceptFriend(doc) },
            onDelete = { doc -> deleteRequest(doc) }
        )
        rvFriendNoti.apply {
            layoutManager = LinearLayoutManager(this@notification)
            adapter = notiAdapter
        }
    }

    private fun setupTabListeners() {
        btnTabRequest.setOnClickListener { loadRequestTab() }
        btnTabGroup.setOnClickListener { loadGroupTab() }
        btnTabFriend.setOnClickListener { loadFriendTab() }
    }

    private fun loadRequestTab() {
        updateTabUI(btnTabRequest)
        listenToFriendRequests() // ย้ายระบบขอเพื่อนมาไว้ที่นี่
    }

    private fun loadGroupTab() {
        updateTabUI(btnTabGroup)
        // TODO: ดึงข้อมูลแจ้งเตือนกลุ่ม
        notiList.clear()
        notiAdapter.notifyDataSetChanged()
    }

    private fun loadFriendTab() {
        updateTabUI(btnTabFriend)
        // TODO: ดึงข้อมูลประวัติการเป็นเพื่อน
        notiList.clear()
        notiAdapter.notifyDataSetChanged()
    }

    private fun updateTabUI(activeButton: Button) {
        // รายชื่อปุ่มทั้งหมดในหน้า Notification ของคุณ
        val buttons = listOf(btnTabRequest, btnTabGroup, btnTabFriend)

        buttons.forEach { button ->
            if (button == activeButton) {
                // ✅ ถ้าเป็นปุ่มที่ถูกเลือก: เปลี่ยนเป็นสีฟ้า ตัวหนังสือขาว [cite: 2026-02-27]
                button?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3D79CA"))
                button?.setTextColor(Color.WHITE)
            } else {
                // ✅ ถ้าไม่ใช่ปุ่มที่เลือก: เปลี่ยนเป็นสีเทา ตัวหนังสือเทาเข้ม [cite: 2026-02-27]
                button?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
                button?.setTextColor(Color.parseColor("#64748B"))
            }
        }
    }

    private fun listenToFriendRequests() {
        val myUid = auth.currentUser?.uid ?: return
        notiListener?.remove() // ล้าง Listener เก่าก่อนป้องกันข้อมูลเบิ้ล

        notiListener = db.collection("friend_requests")
            .whereEqualTo("to_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                notiList.clear()
                notiList.addAll(snapshots.documents)
                notiAdapter.notifyDataSetChanged()
            }
    }

    private fun acceptFriend(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val senderUid = doc.getString("from_uid") ?: return
        val senderName = doc.getString("from_name") ?: "Unknown User"

        val batch = db.batch()
        batch.delete(db.collection("friend_requests").document(doc.id))
        batch.update(db.collection("users").document(myUid), "friends", FieldValue.arrayUnion(senderUid))
        batch.update(db.collection("users").document(senderUid), "friends", FieldValue.arrayUnion(myUid))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "You are now friends with $senderName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteRequest(doc: DocumentSnapshot) {
        db.collection("friend_requests").document(doc.id).delete()
    }

    override fun onDestroy() {
        super.onDestroy()
        notiListener?.remove()
    }
}