package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroup : AppCompatActivity() {

    private lateinit var etGroupName: TextInputEditText
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var btnCreate: View? = null
    private var btnAddParticipant: View? = null

    // ตัวแปรสำหรับเก็บรายชื่อเพื่อนที่เลือกมาจากหน้า SelectFriend
    private var selectedMemberUids = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creategroup)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        init()
        handleIncomingFriends()
    }

    private fun init() {
        etGroupName = findViewById(R.id.etGroupName)
        btnCreate = findViewById(R.id.btnCreateGroup)
        btnAddParticipant = findViewById(R.id.btnAddParticipant)
        val btnBack = findViewById<ImageButton>(R.id.backButton)

        btnBack?.setOnClickListener { finish() }

        // กดเพื่อไปเลือกเพื่อน
        btnAddParticipant?.setOnClickListener {
            val intent = Intent(this, SelectFriend::class.java)
            startActivity(intent)
            // หมายเหตุ: ไม่ต้อง finish() เพราะเราจะให้เขากลับมาหน้านี้พร้อมข้อมูลเพื่อน
        }

        // กดเพื่อสร้างกลุ่ม
        btnCreate?.setOnClickListener {
            val name = etGroupName.text.toString().trim()
            if (name.isNotEmpty()) {
                createNewGroup(name)
            } else {
                etGroupName.error = "Please input your group name"
            }
        }
    }

    // ฟังก์ชันรับข้อมูลเพื่อนที่ส่งกลับมาจาก SelectFriend
    private fun handleIncomingFriends() {
        val incomingFriends = intent.getStringArrayListExtra("SELECTED_FRIENDS")
        if (incomingFriends != null) {
            selectedMemberUids = incomingFriends
            Toast.makeText(this, "Add ${selectedMemberUids.size} friends to group", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewGroup(groupName: String) {
        val myUid = auth.currentUser?.uid ?: return

        btnCreate?.isEnabled = false

        // เตรียมรายชื่อสมาชิก (ตัวเอง + เพื่อนที่เลือก)
        val allMembers = ArrayList<String>()
        allMembers.add(myUid)
        allMembers.addAll(selectedMemberUids)

        val groupData = hashMapOf(
            "groupName" to groupName,
            "admin" to myUid,
            "members" to allMembers,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        // 1. สร้างกลุ่มใหม่ในคอลเลกชัน "groups"
        db.collection("groups").add(groupData).addOnSuccessListener { ref ->
            val groupId = ref.id

            // 2. อัปเดตรายชื่อกลุ่มให้สมาชิกทุกคน (ใช้ Write Batch เพื่อประสิทธิภาพ)
            val batch = db.batch()
            for (uid in allMembers) {
                val userRef = db.collection("users").document(uid)
                batch.update(userRef, "groups", FieldValue.arrayUnion(groupId))
            }

            batch.commit().addOnSuccessListener {
                Toast.makeText(this, "Group '$groupName' Created!", Toast.LENGTH_SHORT).show()

                // กลับไปหน้าหลักและล้าง Stack เพื่อความลื่นไหล
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {
            btnCreate?.isEnabled = true
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}