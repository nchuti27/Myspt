package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroup : AppCompatActivity() {

    private lateinit var etGroupName: TextInputEditText
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var btnCreate: View? = null
    private var btnAddParticipant: View? = null
    private var rvMembers: RecyclerView? = null // เพิ่มตัวแปร RecyclerView

    private var selectedMemberUids = ArrayList<String>()

    // เพิ่มตัวแปรจัดการ Adapter
    private var participantList = mutableListOf<ParticipantData>()
    private lateinit var adapter: ParticipantAdapter

    private val selectFriendLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selected = result.data?.getStringArrayListExtra("SELECTED_FRIENDS")
            if (selected != null) {
                selectedMemberUids = selected

                // สั่งอัปเดต UI ตรงนี้!
                updateParticipantUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creategroup)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        init()
        setupRecyclerView() // เรียกใช้การตั้งค่า RecyclerView
    }

    private fun init() {
        etGroupName = findViewById(R.id.etGroupName)
        btnCreate = findViewById(R.id.btnCreateGroup)
        btnAddParticipant = findViewById(R.id.btnAddParticipant)
        rvMembers =
            findViewById(R.id.rvMembers) // อย่าลืมใส่ id นี้ใน activity_creategroup.xml นะครับ
        val btnBack = findViewById<ImageButton>(R.id.backButton)

        btnBack?.setOnClickListener { finish() }

        btnAddParticipant?.setOnClickListener {
            val intent = Intent(this, SelectFriend::class.java)
            selectFriendLauncher.launch(intent)
        }

        btnCreate?.setOnClickListener {
            val name = etGroupName.text.toString().trim()
            if (name.isNotEmpty()) {
                createNewGroup(name)
            } else {
                etGroupName.error = "Please input your group name"
            }
        }
    }

    // ฟังก์ชันตั้งค่า RecyclerView
    private fun setupRecyclerView() {
        adapter = ParticipantAdapter(participantList) { uidToRemove ->
            // เมื่อกดกากบาทลบเพื่อน
            selectedMemberUids.remove(uidToRemove)
            updateParticipantUI() // โหลด UI ใหม่
        }
        rvMembers?.layoutManager = LinearLayoutManager(this)
        rvMembers?.adapter = adapter
    }

    // ฟังก์ชันอัปเดต UI และดึงชื่อเพื่อนจาก Firestore
    private fun updateParticipantUI() {
        if (selectedMemberUids.isEmpty()) {
            participantList.clear()
            adapter.notifyDataSetChanged()
            return
        }

        // ดึงรายชื่อจากตาราง users ตาม UID ที่เราเลือกมา (ป้องกันจำกัด 10 คนเผื่อไว้ก่อน)
        db.collection("users")
            .whereIn(FieldPath.documentId(), selectedMemberUids.take(10))
            .get()
            .addOnSuccessListener { documents ->
                participantList.clear()
                // ใน CreateGroup.kt
                for (doc in documents) {
                    val uid = doc.id
                    val name = doc.getString("name") ?: "Unknown"
                    val pUrl = doc.getString("profileUrl") // 🌟 ดึง URL มาจาก Firestore
                    participantList.add(ParticipantData(uid, name, pUrl)) // ✅ ส่งค่าให้ครบ
                }
                // สั่งให้ RecyclerView วาดหน้าจอใหม่
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load member list", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNewGroup(groupName: String) {
        val myUid = auth.currentUser?.uid ?: return
        val senderName = auth.currentUser?.displayName ?: "Your Friend"
        btnCreate?.isEnabled = false

        // 🌟 1. สร้างกลุ่มโดยมีสมาชิกแค่ "ตัวเอง" (Admin) คนเดียวเท่านั้น
        val groupData = hashMapOf(
            "groupName" to groupName,
            "admin" to myUid,
            "members" to arrayListOf(myUid), // ✅ มีแค่เราคนเดียว เพื่อนยังไม่เข้า
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("groups").add(groupData).addOnSuccessListener { ref ->
            val groupId = ref.id
            val batch = db.batch()

            // 2. อัปเดตรายชื่อกลุ่มให้ตัวเอง (Admin)
            val myUserRef = db.collection("users").document(myUid)
            batch.update(myUserRef, "groups", FieldValue.arrayUnion(groupId))

            // 🌟 3. ส่งคำเชิญ (Invitation) แบบ Pending ให้เพื่อนที่เลือกไว้ทุกคน
            for (uid in selectedMemberUids) {
                // สร้างเอกสารใน group_invites เพื่อให้โชว์ใน Tab Group ของเพื่อน
                val inviteRef = db.collection("group_invites").document()
                batch.set(inviteRef, hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to senderName,
                    "to_uid" to uid,
                    "groupId" to groupId,
                    "groupName" to groupName,
                    "status" to "pending", // 🌟 หัวใจสำคัญ: เพื่อนต้องกดตกลงเอง
                    "timestamp" to FieldValue.serverTimestamp()
                ))

                // ส่งแจ้งเตือนทั่วไปใน notifications
                val notiRef = db.collection("notifications").document()
                batch.set(notiRef, hashMapOf(
                    "receiverId" to uid,
                    "senderId" to myUid,
                    "type" to "GROUP_INVITE",
                    "message" to "$senderName invited you to join $groupName",
                    "timestamp" to FieldValue.serverTimestamp()
                ))
            }

            batch.commit().addOnSuccessListener {
                Toast.makeText(this, "Group Created & Invitations Sent!", Toast.LENGTH_SHORT).show()

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