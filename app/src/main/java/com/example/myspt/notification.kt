package com.example.myspt

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
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
    private lateinit var tvEmptyState: TextView // 1. ประกาศตัวแปร

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
        tvEmptyState = findViewById(R.id.tvEmptyState) // 2. ผูก View

        btnBack.setOnClickListener { finish() }
        btnClearAll.setOnClickListener { confirmClearAll() }
    }

    private fun checkEmptyState(list: ArrayList<DocumentSnapshot>) {
        if (list.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvNoti.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvNoti.visibility = View.VISIBLE
        }
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
        notiListener = db.collection("notifications")
            .whereEqualTo("to_uid", myUid)  // ✅ ใช้ to_uid แทน receiverId
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                db.collection("friend_requests")
                    .whereEqualTo("to_uid", myUid)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { friendSnapshots ->
                        notiList.clear()
                        notiList.addAll(snapshots?.documents ?: listOf())
                        notiList.addAll(friendSnapshots.documents)
                        notiList.sortByDescending { it.getTimestamp("timestamp") }

                        checkEmptyState(notiList)
                        notiAdapter.updateData(notiList, "FRIEND")
                    }
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

                checkEmptyState(notiList) // เรียกตรวจสอบ
                notiAdapter.updateData(notiList, currentTab)
            }
    }

    private fun acceptFriend(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val senderUid = doc.getString("from_uid") ?: return
        val senderName = doc.getString("from_name") ?: "Friend"

        db.collection("users").document(myUid).get().addOnSuccessListener { myDoc ->
            val myName = myDoc.getString("name") ?: "Your Friend"
            val myProfileUrl = myDoc.getString("profileUrl")

            val batch = db.batch()

            // 1. เปลี่ยนสถานะคำขอเป็น accepted
            val requestRef = db.collection("friend_requests").document(doc.id)
            batch.update(requestRef, "status", "accepted")

            val myRef = db.collection("users").document(myUid)
            batch.update(myRef, "friends", FieldValue.arrayUnion(senderUid))

            val senderRef = db.collection("users").document(senderUid)
            batch.update(senderRef, "friends", FieldValue.arrayUnion(myUid))

            val notiRef = db.collection("notifications").document()
            batch.set(notiRef, hashMapOf(
                "receiverId" to senderUid,
                "senderId" to myUid,
                "type" to "FRIEND_ACCEPTED",
                "message" to "$myName accepted your friend request.",
                "timestamp" to FieldValue.serverTimestamp()
            ))

            batch.commit().addOnSuccessListener {
                Toast.makeText(this, "You and $senderName are now friends", Toast.LENGTH_SHORT).show()
                loadFriendTab()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error accepting friend: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching your info.", Toast.LENGTH_SHORT).show()
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
        // ลบจาก Firebase
        db.document(doc.reference.path).delete()
            .addOnSuccessListener {

                val currentList = notiList.toMutableList()
                currentList.remove(doc)
                notiList = currentList as ArrayList<DocumentSnapshot>

                //อัปเดตข้อมูลทันที
                notiAdapter.updateData(notiList, currentTab)

                // เช็กว่าถ้าลบหมดแล้วให้โชว์หน้า Empty State
                checkEmptyState(notiList)

                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
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
                for (doc in notiList) {
                    batch.delete(doc.reference)
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "Cleared successfully", Toast.LENGTH_SHORT).show()
                    if (currentTab == "FRIEND") {
                        loadFriendTab()
                    }
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