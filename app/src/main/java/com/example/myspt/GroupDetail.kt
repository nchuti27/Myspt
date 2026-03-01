package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetail : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var groupId: String? = null

    private lateinit var editGroupName: EditText
    private lateinit var btnEditName: ImageView
    private lateinit var btnAddMember: ImageView
    private lateinit var rvMembers: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var btnSave: Button

    private val memberList = ArrayList<CircleItem>()
    private lateinit var memberAdapter: MemberListAdapter

    // 🌟 เพิ่มตัวแปรสำหรับเก็บ UIDs เพื่อนที่รอส่งคำขอ (Pending)
    private var pendingSelectedUids = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupdetail)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
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

        backButton.setOnClickListener { finish() }

        btnEditName.setOnClickListener {
            editGroupName.requestFocus()
            Toast.makeText(this, "Edit group name now", Toast.LENGTH_SHORT).show()
        }

        // 🌟 ส่งไปเลือกเพื่อน (ใช้ startActivityForResult เพื่อเอาค่ากลับมาส่ง Invite ทีหลัง)
        btnAddMember.setOnClickListener {
            if (groupId != null) {
                val intent = Intent(this, SelectFriend::class.java)
                intent.putExtra("GROUP_ID", groupId)
                startActivityForResult(intent, 100)
            }
        }

        // 🌟 ปุ่ม Save: บันทึกชื่อกลุ่ม + ส่งคำเชิญพร้อมกัน
        btnSave.setOnClickListener {
            val newName = editGroupName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveChangesAndSendInvites(newName)
        }

        memberAdapter = MemberListAdapter(memberList)
        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.adapter = memberAdapter
    }

    // 🌟 รับรายชื่อเพื่อนกลับมาจากหน้า SelectFriend
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uids = data?.getStringArrayListExtra("SELECTED_FRIENDS")
            if (uids != null) {
                pendingSelectedUids.addAll(uids)
                Toast.makeText(this, "Added ${uids.size} friends to invite list. Click Save to send.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveChangesAndSendInvites(newName: String) {
        val gId = groupId ?: return
        val myUid = auth.currentUser?.uid ?: return
        val senderName = auth.currentUser?.displayName ?: "Your Friend"

        val batch = db.batch()
        val groupRef = db.collection("groups").document(gId)

        // 1. อัปเดตชื่อกลุ่ม
        batch.update(groupRef, "groupName", newName)

        // 2. ส่งคำเชิญให้เพื่อนที่รออยู่ (ถ้ามี)
        if (pendingSelectedUids.isNotEmpty()) {
            for (uid in pendingSelectedUids) {
                val inviteRef = db.collection("group_invites").document()
                val inviteData = hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to senderName,
                    "to_uid" to uid,
                    "groupId" to gId,
                    "groupName" to newName,
                    "status" to "pending",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                batch.set(inviteRef, inviteData)

                // แจ้งเตือนทั่วไป
                val notiRef = db.collection("notifications").document()
                batch.set(notiRef, hashMapOf(
                    "receiverId" to uid,
                    "senderId" to myUid,
                    "type" to "GROUP_INVITE",
                    "message" to "$senderName invited you to join $newName",
                    "timestamp" to FieldValue.serverTimestamp()
                ))
            }
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Changes saved and invitations sent!", Toast.LENGTH_SHORT).show()
            pendingSelectedUids.clear()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGroupData() {
        db.collection("groups").document(groupId!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val name = snapshot.getString("groupName") ?: "Unknown Group"
                val membersUids = snapshot.get("members") as? List<String> ?: listOf()
                if (!editGroupName.hasFocus()) editGroupName.setText(name)
                fetchMemberDetails(membersUids)
            }
    }

    private fun fetchMemberDetails(uids: List<String>) {
        if (uids.isEmpty()) {
            memberList.clear()
            memberAdapter.notifyDataSetChanged()
            return
        }
        db.collection("users").whereIn(FieldPath.documentId(), uids.take(10))
            .get()
            .addOnSuccessListener { documents ->
                memberList.clear()
                for (doc in documents) {
                    memberList.add(CircleItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        profileUrl = doc.getString("profileUrl")
                    ))
                }
                memberAdapter.notifyDataSetChanged()
            }
    }
}