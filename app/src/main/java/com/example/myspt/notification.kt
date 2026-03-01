package com.example.myspt

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
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
import androidx.appcompat.app.AlertDialog

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
        // ✅ ส่ง currentTab เข้าไปด้วยเพื่อให้ Adapter รู้ว่าควรโชว์ข้อความแบบไหน
        notiAdapter = NotificationAdapter(notiList,
            onAccept = { doc ->
                if (currentTab == "GROUP") acceptGroupInvite(doc) else acceptFriend(doc)
            },
            onDelete = { doc -> deleteRequest(doc) }
        )
        rvNoti.apply {
            layoutManager = LinearLayoutManager(this@notification)
            adapter = notiAdapter
        }
    }

    private fun setupTabListeners() {
        btnTabFriend.setOnClickListener { loadFriendTab() }
        btnTabGroup.setOnClickListener { loadGroupTab() }
        btnTabRequest.setOnClickListener { loadRequestTab() }
    }

    private fun loadFriendTab() {
        currentTab = "FRIEND"
        updateTabUI(btnTabFriend)
        fetchData("friend_requests", "to_uid", auth.currentUser?.uid ?: "")
    }

    private fun loadGroupTab() {
        currentTab = "GROUP"
        updateTabUI(btnTabGroup)
        // ✅ ดึงจาก group_invites เพื่อจัดการคำเชิญเข้ากลุ่มโดยเฉพาะ
        fetchData("group_invites", "to_uid", auth.currentUser?.uid ?: "")
    }

    private fun loadRequestTab() {
        currentTab = "REQUEST"
        updateTabUI(btnTabRequest)
        fetchData("friend_requests", "from_uid", auth.currentUser?.uid ?: "")
    }

    private fun fetchData(collection: String, field: String, uid: String) {
        notiListener?.remove()
        notiListener = db.collection(collection)
            .whereEqualTo(field, uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                notiList.clear()
                snapshots?.let { notiList.addAll(it.documents) }
                // ✅ แจ้ง Adapter ว่า Tab เปลี่ยนไปแล้วนะ
                notiAdapter.updateData(notiList, currentTab)
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

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle("Clear All")
            .setMessage("Clear all $currentTab items?")
            .setPositiveButton("Clear") { _, _ ->
                for (doc in notiList) deleteRequest(doc)
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