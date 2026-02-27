package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SelectFriend : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var rvSelectFriends: RecyclerView? = null
    private var btnBack: ImageButton? = null
    private var tvNext: TextView? = null

    private var friendList = ArrayList<FriendData>()
    private lateinit var adapter: SelectFriendAdapter

    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friend)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        groupId = intent.getStringExtra("GROUP_ID")

        init()
        setupRecyclerView()
        loadFriendsRealtime()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvNext = findViewById(R.id.tvNext)
        rvSelectFriends = findViewById(R.id.rvSelectFriends)

        btnBack?.setOnClickListener { finish() }

        tvNext?.setOnClickListener {
            val selectedUids = adapter.getSelectedFriendUids()
            if (selectedUids.isEmpty()) {
                Toast.makeText(this, "Please select at least one friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (groupId != null) {
                // กรณีเพิ่มเข้ากลุ่มที่มีอยู่แล้ว พร้อมส่งแจ้งเตือน
                addMembersToExistingGroup(groupId!!, selectedUids)
            } else {
                // กรณีสร้างกลุ่มใหม่
                val intent = Intent(this, CreateGroup::class.java)
                intent.putStringArrayListExtra("SELECTED_FRIENDS", ArrayList(selectedUids))
                startActivity(intent)
                finish()
            }
        }
    }

    private fun addMembersToExistingGroup(gId: String, newMembers: List<String>) {
        val groupRef = db.collection("groups").document(gId)
        val myUid = auth.currentUser?.uid ?: return

        // 1. ดึงชื่อกลุ่มปัจจุบันมาก่อนเพื่อนำไปใส่ใน Notification
        groupRef.get().addOnSuccessListener { groupDoc ->
            val groupName = groupDoc.getString("groupName") ?: "Unknown Group"

            // 2. อัปเดตรายชื่อสมาชิกในกลุ่ม
            groupRef.update("members", FieldValue.arrayUnion(*newMembers.toTypedArray()))
                .addOnSuccessListener {

                    val batch = db.batch()

                    for (uid in newMembers) {
                        // 3. อัปเดตรายชื่อกลุ่มในโปรไฟล์ของเพื่อนแต่ละคน
                        val userRef = db.collection("users").document(uid)
                        batch.update(userRef, "groups", FieldValue.arrayUnion(gId))

                        // 4. สร้าง Notification ส่งหาเพื่อน (ให้ไปโผล่ที่แท็บ Group)
                        val notiRef = db.collection("notifications").document()
                        val inviteData = hashMapOf(
                            "receiverId" to uid,
                            "senderId" to myUid,
                            "groupId" to gId,
                            "groupName" to groupName,
                            "from_name" to groupName, // ชื่อกลุ่มที่จะแสดงในหน้าแจ้งเตือน
                            "type" to "GROUP_INVITE",  // ประเภทแจ้งเตือน [cite: 2026-02-23]
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        batch.set(notiRef, inviteData)
                    }

                    // 5. รัน Batch ทั้งหมดพร้อมกัน
                    batch.commit().addOnSuccessListener {
                        Toast.makeText(this, "Members added and notifications sent", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = SelectFriendAdapter(friendList)
        rvSelectFriends?.layoutManager = LinearLayoutManager(this)
        rvSelectFriends?.adapter = adapter
    }

    private fun loadFriendsRealtime() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val friendsUids = snapshot?.get("friends") as? List<String> ?: listOf()
                if (friendsUids.isNotEmpty()) {
                    fetchFriendDetails(friendsUids)
                } else {
                    friendList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun fetchFriendDetails(uids: List<String>) {
        db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), uids.take(30))
            .get()
            .addOnSuccessListener { documents ->
                friendList.clear()
                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val uid = doc.id
                    friendList.add(FriendData(name, "Username: ${doc.getString("username")}", uid))
                }
                adapter.notifyDataSetChanged()
            }
    }
}