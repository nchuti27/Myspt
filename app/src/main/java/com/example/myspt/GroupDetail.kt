package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    private lateinit var editGroupName: EditText
    private lateinit var btnEditName: ImageView
    private lateinit var btnAddMember: ImageView
    private lateinit var rvMembers: RecyclerView
    private lateinit var backButton: ImageView

    // 1. เพิ่มตัวแปรสำหรับปุ่ม Save
    private lateinit var btnSave: Button

    private val memberList = ArrayList<CircleItem>()
    private lateinit var memberAdapter: MemberListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupdetail)

        db = FirebaseFirestore.getInstance()
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

        // 2. ผูก ID ของปุ่ม Save
        btnSave = findViewById(R.id.btnSave)

        backButton.setOnClickListener { finish() }

        // ให้รูปดินสอทำหน้าที่โฟกัสกล่องข้อความ (ให้คีย์บอร์ดเด้งขึ้นมาพิมพ์)
        btnEditName.setOnClickListener {
            editGroupName.requestFocus()
        }

        // 3. ย้ายคำสั่งบันทึกชื่อกลุ่มมาไว้ที่ปุ่ม Save แทน
        btnSave.setOnClickListener {
            val newName = editGroupName.text.toString().trim()
            if (newName.isNotEmpty() && groupId != null) {
                db.collection("groups").document(groupId!!).update("groupName", newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show()
                        finish() // กด Save เสร็จแล้วจะปิดหน้าต่างกลับไปหน้าเดิมให้อัตโนมัติ
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึก", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "กรุณาพิมพ์ชื่อกลุ่มก่อนบันทึก", Toast.LENGTH_SHORT).show()
            }
        }

        // ปุ่มเพิ่มเพื่อนเข้ากลุ่ม (ส่งไปหน้า SelectFriend)
        btnAddMember.setOnClickListener {
            if (groupId != null) {
                val intent = Intent(this, SelectFriend::class.java)
                intent.putExtra("GROUP_ID", groupId)
                startActivity(intent)
            }
        }

        memberAdapter = MemberListAdapter(memberList)
        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.adapter = memberAdapter
    }

    private fun loadGroupData() {
        db.collection("groups").document(groupId!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val name = snapshot.getString("groupName") ?: "Unknown Group"
                val membersUids = snapshot.get("members") as? List<String> ?: listOf()

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