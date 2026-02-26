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
        btnSave = findViewById(R.id.btnSave)

        // ปุ่มย้อนกลับ
        backButton.setOnClickListener { finish() }

        // 1. ไอคอนรูปดินสอ: กดปุ๊บให้เคอร์เซอร์ไปโผล่ที่กล่องข้อความเพื่อเตรียมพิมพ์
        btnEditName.setOnClickListener {
            editGroupName.requestFocus()
            Toast.makeText(this, "พิมพ์แก้ไขชื่อกลุ่มได้เลย", Toast.LENGTH_SHORT).show()
        }

        // 2. ปุ่มเพิ่มเพื่อนเข้ากลุ่ม: ส่งไปหน้า SelectFriend พร้อมแนบ GROUP_ID
        btnAddMember.setOnClickListener {
            if (groupId != null) {
                val intent = Intent(this, SelectFriend::class.java)
                intent.putExtra("GROUP_ID", groupId)
                startActivity(intent)
            }
        }

        // 3. ปุ่ม Save ด้านล่างสุด: ทำหน้าที่บันทึกชื่อกลุ่มใหม่ลงฐานข้อมูล
        btnSave.setOnClickListener {
            val newName = editGroupName.text.toString().trim()

            if (newName.isNotEmpty() && groupId != null) {
                // อัปเดตชื่อกลุ่มลง Firebase
                db.collection("groups").document(groupId!!).update("groupName", newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "บันทึกการแก้ไขเรียบร้อย", Toast.LENGTH_SHORT).show()
                        finish() // กด Save สำเร็จ ให้ปิดหน้านี้แล้วเด้งกลับไปหน้า Grouplist
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "เกิดข้อผิดพลาด: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // ดักไว้เผื่อผู้ใช้เผลอลบชื่อกลุ่มจนว่างเปล่าแล้วกด Save
                Toast.makeText(this, "กรุณากรอกชื่อกลุ่ม", Toast.LENGTH_SHORT).show()
                editGroupName.requestFocus()
            }
        }

        // ตั้งค่า RecyclerView สำหรับรายชื่อสมาชิก
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