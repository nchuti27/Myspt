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

    // ตัวแปรสำหรับรับค่า GROUP_ID
    private var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friend)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // รับค่าจากหน้าที่ส่งมา
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
                // กรณี 1: มีการส่ง GROUP_ID มา (หมายถึง กดมาจากหน้า GroupDetail เพื่อเพิ่มสมาชิก)
                addMembersToExistingGroup(groupId!!, selectedUids)
            } else {
                // กรณี 2: ไม่มี GROUP_ID ส่งมา (หมายถึง กดมาจากหน้าสร้างกลุ่มใหม่ CreateGroup แบบเดิม)
                val intent = Intent(this, CreateGroup::class.java)
                intent.putStringArrayListExtra("SELECTED_FRIENDS", ArrayList(selectedUids))
                startActivity(intent)
                finish()
            }
        }
    }

    // ฟังก์ชันสำหรับบันทึกสมาชิกลงกลุ่มที่มีอยู่แล้ว
    private fun addMembersToExistingGroup(gId: String, newMembers: List<String>) {
        val groupRef = db.collection("groups").document(gId)

        // 1. นำ UIDs ที่เลือก เพิ่มเข้าไปใน Array "members" ของกลุ่ม
        // การใช้ arrayUnion จะช่วยป้องกันไม่ให้มีสมาชิกซ้ำกันในกลุ่ม
        groupRef.update("members", FieldValue.arrayUnion(*newMembers.toTypedArray()))
            .addOnSuccessListener {

                // 2. ใช้ Batch เขียนข้อมูล อัปเดตรายชื่อกลุ่มไปที่โปรไฟล์ของเพื่อนแต่ละคนด้วย (เพื่อนจะได้เห็นกลุ่มนี้)
                val batch = db.batch()
                for (uid in newMembers) {
                    val userRef = db.collection("users").document(uid)
                    batch.update(userRef, "groups", FieldValue.arrayUnion(gId))
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "เพิ่มสมาชิกสำเร็จ", Toast.LENGTH_SHORT).show()
                    finish() // เสร็จแล้วปิดหน้านี้กลับไปหน้า GroupDetail อัตโนมัติ
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
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