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
    private var rvMembers: RecyclerView? = null

    private var selectedMemberUids = ArrayList<String>()
    private var participantList = mutableListOf<ParticipantData>()
    private lateinit var adapter: ParticipantAdapter

    private val selectFriendLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selected = result.data?.getStringArrayListExtra("SELECTED_FRIENDS")
            if (selected != null) {
                selectedMemberUids = selected
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
        setupRecyclerView()
    }

    private fun init() {
        etGroupName = findViewById(R.id.etGroupName)
        btnCreate = findViewById(R.id.btnCreateGroup)
        btnAddParticipant = findViewById(R.id.btnAddParticipant)
        rvMembers = findViewById(R.id.rvMembers)
        val btnBack = findViewById<ImageButton>(R.id.backButton)

        btnBack?.setOnClickListener { finish() }

        btnAddParticipant?.setOnClickListener {
            val intent = Intent(this, SelectFriend::class.java)
            selectFriendLauncher.launch(intent)
        }

        btnCreate?.setOnClickListener {
            val name = etGroupName.text.toString().trim()

            // 🌟 ส่วนที่แก้: ห้ามสร้างกลุ่มคนเดียว (ต้องมีเพื่อนอย่างน้อย 1 คน)
            if (selectedMemberUids.isEmpty()) {
                Toast.makeText(this, "Please select at least one friend to create a group", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isNotEmpty()) {
                fetchMyInfoAndCreateGroup(name)
            } else {
                etGroupName.error = "Please input your group name"
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ParticipantAdapter(participantList) { uidToRemove ->
            selectedMemberUids.remove(uidToRemove)
            updateParticipantUI()
        }
        rvMembers?.layoutManager = LinearLayoutManager(this)
        rvMembers?.adapter = adapter
    }

    private fun updateParticipantUI() {
        if (selectedMemberUids.isEmpty()) {
            participantList.clear()
            adapter.notifyDataSetChanged()
            return
        }

        db.collection("users")
            .whereIn(FieldPath.documentId(), selectedMemberUids.take(10))
            .get()
            .addOnSuccessListener { documents ->
                participantList.clear()
                for (doc in documents) {
                    val uid = doc.id
                    val name = doc.getString("name") ?: "Unknown"
                    val pUrl = doc.getString("profileUrl")
                    participantList.add(ParticipantData(uid, name, pUrl))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load member list", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMyInfoAndCreateGroup(groupName: String) {
        val myUid = auth.currentUser?.uid ?: return
        btnCreate?.isEnabled = false

        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            val myName = doc.getString("name") ?: "Your Friend"
            val myProfileUrl = doc.getString("profileUrl")
            createNewGroup(groupName, myName, myProfileUrl)
        }.addOnFailureListener {
            createNewGroup(groupName, "Your Friend", null)
        }
    }

    private fun createNewGroup(groupName: String, senderName: String, senderProfileUrl: String?) {
        val myUid = auth.currentUser?.uid ?: return

        val groupData = hashMapOf(
            "groupName" to groupName,
            "admin" to myUid,
            "members" to arrayListOf(myUid), // เริ่มต้นมีแค่เรา แต่คำเชิญจะถูกส่งออกไปทันที
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("groups").add(groupData).addOnSuccessListener { ref ->
            val groupId = ref.id
            val batch = db.batch()

            val myUserRef = db.collection("users").document(myUid)
            batch.update(myUserRef, "groups", FieldValue.arrayUnion(groupId))

            for (uid in selectedMemberUids) {
                val inviteRef = db.collection("group_invites").document()
                batch.set(inviteRef, hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to senderName,
                    "from_profileUrl" to senderProfileUrl,
                    "to_uid" to uid,
                    "groupId" to groupId,
                    "groupName" to groupName,
                    "status" to "pending",
                    "timestamp" to FieldValue.serverTimestamp()
                ))

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