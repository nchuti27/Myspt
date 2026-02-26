package com.example.myspt

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetail : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var groupId: String? = null

    // แมปตัวแปรให้ตรงกับ ID ในไฟล์ XML
    private lateinit var editGroupName: EditText
    private lateinit var btnEditName: ImageView
    private lateinit var btnAddMember: ImageView
    private lateinit var rvMembers: RecyclerView
    private lateinit var backButton: ImageView

    private val memberList = ArrayList<CircleItem>()
    private lateinit var memberAdapter: MemberListAdapter // เปลี่ยนมาใช้ Adapter ตัวใหม่

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ตรวจสอบชื่อ Layout ให้ตรงกับไฟล์ XML ของคุณ (สมมติว่าเป็น activity_group_detail)
        setContentView(R.layout.activity_groupdetail)

        db = FirebaseFirestore.getInstance()

        // รับ ID กลุ่มที่ส่งมาจากหน้าหลัก
        groupId = intent.getStringExtra("GROUP_ID")

        initViews()

        if (!groupId.isNullOrEmpty()) {
            loadGroupData()
        } else {
            Toast.makeText(this, "Error: Group ID missing", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        editGroupName = findViewById(R.id.editGroupName)
        btnEditName = findViewById(R.id.btnEditName)
        btnAddMember = findViewById(R.id.btnAddMember)
        rvMembers = findViewById(R.id.rvMembers)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener { finish() }

        // เมื่อกดปุ่มดินสอ ให้บันทึกชื่อกลุ่มใหม่ลง Firestore
        btnEditName.setOnClickListener {
            val newName = editGroupName.text.toString().trim()
            if (newName.isNotEmpty() && groupId != null) {
                db.collection("groups").document(groupId!!).update("groupName", newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "อัปเดตชื่อกลุ่มสำเร็จ", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // ปุ่มเพิ่มเพื่อนเข้ากลุ่ม
        btnAddMember.setOnClickListener {
            Toast.makeText(this, "กดเพิ่มเพื่อน (รอสร้างหน้าเพิ่ม)", Toast.LENGTH_SHORT).show()
        }

        // ตั้งค่า RecyclerView ให้แสดงผลเป็นแนวนอน
        // ตั้งค่า RecyclerView ให้แสดงผลเป็นแนวตั้ง
        memberAdapter = MemberListAdapter(memberList) // 👈 เปลี่ยนชื่อคลาสตรงนี้
        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.adapter = memberAdapter
    }

    private fun loadGroupData() {
        // ดึงข้อมูลกลุ่มแบบ Real-time
        db.collection("groups").document(groupId!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val name = snapshot.getString("groupName") ?: "Unknown Group"
                val membersUids = snapshot.get("members") as? List<String> ?: listOf()

                // เช็คว่าผู้ใช้กำลังพิมพ์แก้ไขชื่ออยู่หรือไม่ เพื่อไม่ให้เคอร์เซอร์กระตุกเวลาข้อมูลอัปเดต
                if (!editGroupName.hasFocus()) {
                    editGroupName.setText(name)
                }

                fetchMemberDetails(membersUids)
            }
    }

    private fun fetchMemberDetails(uids: List<String>) {
        if (uids.isEmpty()) {
            memberList.clear()
            memberAdapter.notifyDataSetChanged()
            return
        }

        // ⚠️ ป้องกันแอปเด้ง: Firebase whereIn รองรับสูงสุด 10 รายการ
        val limitedUids = uids.take(10)

        db.collection("users").whereIn(FieldPath.documentId(), limitedUids)
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