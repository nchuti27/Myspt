package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

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
    private var pendingSelectedUids = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupdetail)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        groupId = intent.getStringExtra("GROUP_ID")

        // 1. เชื่อมต่อ Views
        initViews()

        // 2. ตั้งค่า Adapter (ส่งฟังก์ชันลบเข้าไปด้วย)
        memberAdapter = MemberListAdapter(memberList) { memberId ->
            removeMemberFromGroup(memberId)
        }

        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.adapter = memberAdapter

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
        }

        btnAddMember.setOnClickListener {
            if (groupId != null) {
                val intent = Intent(this, SelectFriend::class.java)
                intent.putExtra("GROUP_ID", groupId)
                startActivityForResult(intent, 100)
            }
        }

        btnSave.setOnClickListener {
            val newName = editGroupName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            fetchMyNameAndSave(newName)
        }
    }

    private fun fetchMyNameAndSave(newName: String) {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            val myName = doc.getString("name") ?: "Unknown"
            val myProfileUrl = doc.getString("profileUrl")
            saveChangesAndSendInvites(newName, myName, myProfileUrl)
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uids = data?.getStringArrayListExtra("SELECTED_FRIENDS")
            if (uids != null) {
                pendingSelectedUids.addAll(uids)
                Toast.makeText(this, "Added ${uids.size} friends. Click Save.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveChangesAndSendInvites(newName: String, senderName: String, senderProfileUrl: String?) {
        val gId = groupId ?: return
        val myUid = auth.currentUser?.uid ?: return

        val batch = db.batch()
        val groupRef = db.collection("groups").document(gId)

        batch.update(groupRef, "groupName", newName)

        if (pendingSelectedUids.isNotEmpty()) {
            for (uid in pendingSelectedUids) {
                val inviteRef = db.collection("group_invites").document()
                val inviteData = hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to senderName,
                    "from_profileUrl" to senderProfileUrl,
                    "to_uid" to uid,
                    "groupId" to gId,
                    "groupName" to newName,
                    "status" to "pending",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                batch.set(inviteRef, inviteData)

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
            Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
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
    private fun removeMemberFromGroup(uidToRemove: String) {
        // สร้าง Dialog เพื่อยืนยันการลบ
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Remove Member")
        builder.setMessage("Are you sure you want to remove this member from the group?")

        builder.setPositiveButton("Remove") { _, _ ->
            // ถ้าผู้ใช้กดตกลง ให้ดำเนินการลบ
            val gId = groupId ?: return@setPositiveButton
            db.collection("groups").document(gId)
                .update("members", FieldValue.arrayRemove(uidToRemove))
                .addOnSuccessListener {
                    Toast.makeText(this, "Member removed successfully", Toast.LENGTH_SHORT).show()
                    // ระบบจะอัปเดต List อัตโนมัติผ่าน SnapshotListener
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: \${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // ปุ่มยกเลิก
        builder.setNegativeButton("Cancel", null)
        builder.show()

    }
}