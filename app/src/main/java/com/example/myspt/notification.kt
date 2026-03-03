package com.example.myspt

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class notification : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notiListener: ListenerRegistration? = null

    private lateinit var btnBack: ImageButton
    private lateinit var btnTabGroup: Button
    private lateinit var btnTabFriend: Button
    private lateinit var btnTabRequest: Button
    private lateinit var btnClearAll: ImageView
    private lateinit var rvNoti: RecyclerView

    private var notiList = ArrayList<DocumentSnapshot>()
    private lateinit var notiAdapter: NotificationAdapter
    private var currentTab = "FRIEND"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupRecyclerView()
        setupTabListeners()

        loadFriendTab()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.backButton)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabRequest = findViewById(R.id.btnTabRequest)
        btnClearAll = findViewById(R.id.btnClearAll)
        rvNoti = findViewById(R.id.rvNoti)

        btnBack.setOnClickListener { finish() }
        btnClearAll.setOnClickListener { confirmClearAll() }
    }

    private fun setupRecyclerView() {
        notiAdapter = NotificationAdapter(notiList,
            onAccept = { doc ->
                if (currentTab == "GROUP") acceptGroupInvite(doc) else acceptFriend(doc)
            },
            onDelete = { doc -> deleteRequest(doc) }
        )
        rvNoti.layoutManager = LinearLayoutManager(this)
        rvNoti.adapter = notiAdapter
    }

    private fun setupTabListeners() {
        btnTabFriend.setOnClickListener { loadFriendTab() }
        btnTabGroup.setOnClickListener { loadGroupTab() }
        btnTabRequest.setOnClickListener { loadRequestTab() }
    }

    private fun loadFriendTab() {
        currentTab = "FRIEND"
        updateTabUI(btnTabFriend)
        notiListener?.remove()
        val myUid = auth.currentUser?.uid ?: return

        // ดึงคำขอที่ส่ง "มาหาเรา" (to_uid == myUid)
        val friendReq = db.collection("friend_requests").whereEqualTo("to_uid", myUid).whereEqualTo("status", "pending").get()
        val debtNoti = db.collection("notifications").whereEqualTo("to_uid", myUid).whereEqualTo("type", "debt_reminder").get()

        Tasks.whenAllSuccess<QuerySnapshot>(friendReq, debtNoti).addOnSuccessListener { results ->
            notiList.clear()
            results.forEach { snapshot -> notiList.addAll(snapshot.documents) }
            notiAdapter.updateData(notiList, "FRIEND")
        }
    }

    private fun loadGroupTab() {
        currentTab = "GROUP"
        updateTabUI(btnTabGroup)
        fetchData("group_invites", "to_uid")
    }

    private fun loadRequestTab() {
        currentTab = "REQUEST"
        updateTabUI(btnTabRequest)
        // 🌟 ดึงคำขอที่ "เราส่งออกไป" (from_uid == myUid)
        fetchData("friend_requests", "from_uid")
    }

    private fun fetchData(collection: String, field: String) {
        notiListener?.remove()
        val uid = auth.currentUser?.uid ?: return
        notiListener = db.collection(collection)
            .whereEqualTo(field, uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                notiList.clear()
                snapshots?.let { notiList.addAll(it.documents) }
                notiAdapter.updateData(notiList, currentTab) // 🌟 ส่งแท็บปัจจุบันไปด้วย
            }
    }

    private fun acceptFriend(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val senderUid = doc.getString("from_uid") ?: return
        val senderName = doc.getString("from_name") ?: "Friend"

        val batch = db.batch()
        batch.update(db.collection("friend_requests").document(doc.id), "status", "accepted")
        batch.update(db.collection("users").document(myUid), "friends", FieldValue.arrayUnion(senderUid))
        batch.update(db.collection("users").document(senderUid), "friends", FieldValue.arrayUnion(myUid))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "You and $senderName are now friends", Toast.LENGTH_SHORT).show()
        }
    }

    private fun acceptGroupInvite(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val groupId = doc.getString("groupId") ?: return
        val groupName = doc.getString("groupName") ?: "Group"

        val batch = db.batch()
        batch.update(db.collection("group_invites").document(doc.id), "status", "accepted")
        batch.update(db.collection("groups").document(groupId), "members", FieldValue.arrayUnion(myUid))
        batch.update(db.collection("users").document(myUid), "groups", FieldValue.arrayUnion(groupId))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Joined $groupName!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteRequest(doc: DocumentSnapshot) {
        db.document(doc.reference.path).delete()
    }

    // ในไฟล์ notification.kt หรือ NotiGroup.kt
    private fun confirmClearAll() {
        if (notiList.isEmpty()) {
            Toast.makeText(this, "No items to clear", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Clear All")
            .setMessage("Clear all $currentTab notifications?")
            .setPositiveButton("Clear") { _, _ ->
                val batch = db.batch()
                // 1. วนลูปสั่งลบทุก Document ที่โชว์อยู่ใน List ปัจจุบัน
                for (doc in notiList) {
                    batch.delete(doc.reference)
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "Cleared successfully", Toast.LENGTH_SHORT).show()

                    // 🌟 จุดสำคัญ: ถ้าเป็นแท็บ FRIEND ต้องสั่งโหลดใหม่เองเพราะไม่ได้ใช้ Listener
                    if (currentTab == "FRIEND") {
                        loadFriendTab()
                    }
                    // ส่วนแท็บอื่นที่ใช้ SnapshotListener หน้าจอจะหายไปเองอัตโนมัติครับ

                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTabUI(activeButton: Button) {
        val buttons = listOf(btnTabFriend, btnTabGroup, btnTabRequest)
        buttons.forEach { button ->
            if (button == activeButton) {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3D79CA"))
                button.setTextColor(Color.WHITE)
            } else {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
                button.setTextColor(Color.parseColor("#64748B"))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notiListener?.remove()
    }
}