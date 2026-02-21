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
import com.google.firebase.firestore.FirebaseFirestore

class SelectFriend : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var rvSelectFriends: RecyclerView? = null
    private var btnBack: ImageButton? = null
    private var tvNext: TextView? = null

    private var friendList = ArrayList<FriendData>()
    private lateinit var adapter: SelectFriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friend)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        init()
        setupRecyclerView()
        loadFriendsRealtime()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvNext = findViewById(R.id.tvNext)
        rvSelectFriends = findViewById(R.id.rvSelectFriends) // ตรวจสอบ ID ใน XML

        btnBack?.setOnClickListener { finish() }

        tvNext?.setOnClickListener {
            val selectedUids = adapter.getSelectedFriendUids()
            if (selectedUids.isEmpty()) {
                Toast.makeText(this, "Please select at least one friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ส่ง UIDs กลับไปที่หน้า CreateGroup
            val intent = Intent(this, CreateGroup::class.java)
            intent.putStringArrayListExtra("SELECTED_FRIENDS", ArrayList(selectedUids))
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = SelectFriendAdapter(friendList)
        rvSelectFriends?.layoutManager = LinearLayoutManager(this)
        rvSelectFriends?.adapter = adapter
    }

    private fun loadFriendsRealtime() {
        val myUid = auth.currentUser?.uid ?: return

        // แก้ปัญหา ANR: ฟังการเปลี่ยนแปลงรายชื่อเพื่อนแบบ Real-time
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
        // ใช้ whereIn เพื่อดึงข้อมูลเพื่อนทีเดียว (ลดภาระ Main Thread)
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