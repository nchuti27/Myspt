package com.example.myspt

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetail : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var groupId: String? = null

    private lateinit var etGroupName: EditText
    private lateinit var imgGroup: ImageView
    private lateinit var rvMembers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupdetail) // ตรวจสอบชื่อ XML ให้ตรงกับรูป

        db = FirebaseFirestore.getInstance()
        groupId = intent.getStringExtra("GROUP_ID")

        etGroupName = findViewById(R.id.editGroupName)
        imgGroup = findViewById(R.id.ImageView)
        rvMembers = findViewById(R.id.rvMembers)
        val btnBack = findViewById<ImageView>(R.id.backButton) // สมมติ ID ปุ่ม back
        val btnAddMember = findViewById<ImageView>(R.id.btnAddMember) // ไอคอนบวกในรูป
        val btnEditName = findViewById<ImageView>(R.id.btnEditName) // ไอคอนดินสอในรูป

        btnBack?.setOnClickListener { finish() }

        loadGroupData()

        // Logic แก้ชื่อกลุ่ม
        btnEditName?.setOnClickListener {
            val newName = etGroupName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateGroupName(newName)
            }
        }

        // Logic เพิ่มสมาชิก
        btnAddMember?.setOnClickListener {
            // ส่งไปหน้า FindUser หรือหน้าเลือกเพื่อนเพื่อเพิ่มเข้ากลุ่มนี้
            // val intent = Intent(this, FindUser::class.java)
            // intent.putExtra("GROUP_ID", groupId)
            // startActivity(intent)
        }
    }

    private fun loadGroupData() {
        groupId?.let { id ->
            db.collection("groups").document(id).get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etGroupName.setText(doc.getString("groupName"))
                    val memberUids = doc.get("members") as? List<String> ?: listOf()
                    setupMembersList(memberUids)
                }
            }
        }
    }

    private fun updateGroupName(newName: String) {
        groupId?.let { id ->
            db.collection("groups").document(id).update("groupName", newName)
                .addOnSuccessListener {
                    Toast.makeText(this, "อัปเดตชื่อกลุ่มแล้ว", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupMembersList(uids: List<String>) {
        val memberList = ArrayList<CircleItem>()
        var count = 0

        if (uids.isEmpty()) return

        for (uid in uids) {
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Unknown"
                memberList.add(CircleItem(id = uid, name = name))
                count++

                if (count == uids.size) {
                    rvMembers.layoutManager = LinearLayoutManager(this)
                    // ใช้ Adapter สำหรับแสดงรายชื่อสมาชิก (ทำคล้าย HomeGroupAdapter แบบ list)
                    rvMembers.adapter = HomeGroupAdapter(memberList, true) {
                        // กดที่สมาชิกเพื่อดูโปรไฟล์หรือลบออก
                    }
                }
            }
        }
    }
}