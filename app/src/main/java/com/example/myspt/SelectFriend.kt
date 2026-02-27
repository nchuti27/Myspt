package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SelectFriend : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var rvSelectFriends: RecyclerView? = null
    private var btnBack: ImageButton? = null
    private var tvNext: TextView? = null

    // 🌟 1. เพิ่มตัวแปรสำหรับช่องค้นหา
    private var etSearch: EditText? = null

    // 🌟 2. เพิ่ม allFriendsList เพื่อเก็บข้อมูลเพื่อนทั้งหมดไว้ใช้เป็นต้นฉบับตอนค้นหา
    private var allFriendsList = ArrayList<FriendData>()
    private var friendList = ArrayList<FriendData>() // อันนี้ไว้แสดงผล (โดนกรองได้)
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
        etSearch = findViewById(R.id.etSearch) // 🌟 3. ผูก ID ช่องค้นหา (อย่าลืมเพิ่มใน XML นะครับ)

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
                // กรณีสร้างกลุ่มใหม่ (ใช้ setResult ส่งค่ากลับไปหา CreateGroup แบบถูกต้อง)
                val resultIntent = Intent()
                resultIntent.putStringArrayListExtra("SELECTED_FRIENDS", ArrayList(selectedUids))
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        // 🌟 4. ดักจับการพิมพ์ข้อความเพื่อค้นหาเพื่อนแบบ Real-time
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 🌟 5. ฟังก์ชันสำหรับกรองรายชื่อเพื่อน
    private fun filterFriends(keyword: String) {
        val filteredList = ArrayList<FriendData>()
        for (friend in allFriendsList) {
            // ค้นหาจากชื่อ (ไม่สนใจตัวพิมพ์เล็กพิมพ์ใหญ่)
            if (friend.name.lowercase().contains(keyword.lowercase())) {
                filteredList.add(friend)
            }
        }
        // อัปเดตรายชื่อที่โชว์ใน Adapter
        friendList.clear()
        friendList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun addMembersToExistingGroup(gId: String, newMembers: List<String>) {
        val groupRef = db.collection("groups").document(gId)
        val myUid = auth.currentUser?.uid ?: return

        groupRef.get().addOnSuccessListener { groupDoc ->
            val groupName = groupDoc.getString("groupName") ?: "Unknown Group"

            groupRef.update("members", FieldValue.arrayUnion(*newMembers.toTypedArray()))
                .addOnSuccessListener {
                    val batch = db.batch()

                    for (uid in newMembers) {
                        val userRef = db.collection("users").document(uid)
                        batch.update(userRef, "groups", FieldValue.arrayUnion(gId))

                        val notiRef = db.collection("notifications").document()
                        val inviteData = hashMapOf(
                            "receiverId" to uid,
                            "senderId" to myUid,
                            "groupId" to gId,
                            "groupName" to groupName,
                            "from_name" to groupName,
                            "type" to "GROUP_INVITE",
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                        batch.set(notiRef, inviteData)
                    }

                    batch.commit().addOnSuccessListener {
                        Toast.makeText(this, "Members added and notifications sent", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    allFriendsList.clear() // 🌟 ล้างข้อมูลต้นฉบับด้วย
                    friendList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun fetchFriendDetails(uids: List<String>) {
        db.collection("users").whereIn(FieldPath.documentId(), uids.take(30))
            .get()
            .addOnSuccessListener { documents ->
                allFriendsList.clear() // 🌟 เก็บข้อมูลลง list ต้นฉบับ
                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val uid = doc.id
                    allFriendsList.add(FriendData(name, "Username: ${doc.getString("username")}", uid))
                }

                // 🌟 แทนที่จะ notify ทันที ให้เรียก filterFriends เพื่อแสดงผลตามข้อความที่พิมพ์ค้างไว้อยู่
                val currentSearchText = etSearch?.text.toString()
                filterFriends(currentSearchText)
            }
    }
}