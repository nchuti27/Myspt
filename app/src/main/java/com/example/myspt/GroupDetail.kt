package com.example.myspt

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetail : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var groupId: String? = null

    private var tvGroupName: TextView? = null
    private var rvMembers: RecyclerView? = null
    private var btnBack: ImageButton? = null

    private val memberList = ArrayList<CircleItem>()
    private lateinit var memberAdapter: CircleAdapter // ใช้ Adapter วงกลมที่คุณมีอยู่แล้ว

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupdetail)

        db = FirebaseFirestore.getInstance()

        // รับ ID กลุ่มที่ส่งมาจากหน้าหลัก [cite: 2026-02-21]
        groupId = intent.getStringExtra("GROUP_ID")

        init()
        if (groupId != null) {
            loadGroupData()
        }
    }

    private fun init() {
        tvGroupName = findViewById(R.id.tvGroupName)
        rvMembers = findViewById(R.id.rvMembers)
        btnBack = findViewById(R.id.backButton)

        btnBack?.setOnClickListener { finish() }

        // ตั้งค่า RecyclerView [cite: 2026-02-21]
        memberAdapter = CircleAdapter(memberList) { }
        rvMembers?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMembers?.adapter = memberAdapter
    }

    private fun loadGroupData() {
        // ดึงข้อมูลกลุ่มแบบ Real-time [cite: 2026-02-21]
        db.collection("groups").document(groupId!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val name = snapshot.getString("groupName") ?: "Unknown Group"
                val membersUids = snapshot.get("members") as? List<String> ?: listOf()

                tvGroupName?.text = name
                fetchMemberDetails(membersUids) // ดึงรายละเอียดสมาชิกต่อ [cite: 2026-02-21]
            }
    }

    private fun fetchMemberDetails(uids: List<String>) {
        if (uids.isEmpty()) return

        // แก้ปัญหา ANR: ใช้ whereIn เพื่อดึงข้อมูลสมาชิกทุกคนในครั้งเดียว [cite: 2026-02-21]
        db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), uids.take(30))
            .get()
            .addOnSuccessListener { documents ->
                memberList.clear()
                for (doc in documents) {
                    memberList.add(CircleItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown"
                    ))
                }
                memberAdapter.notifyDataSetChanged()
            }
    }
}