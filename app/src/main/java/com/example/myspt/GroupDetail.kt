package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

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
        val isReadOnly = intent.getBooleanExtra("READ_ONLY", false)

        android.util.Log.d("GROUPDETAIL", "groupId received = $groupId")  // ✅ debug log

        initViews(isReadOnly)

        memberAdapter = MemberListAdapter(memberList, isReadOnly) { memberId ->
            removeMemberFromGroup(memberId)
        }

        rvMembers.layoutManager = LinearLayoutManager(this)
        rvMembers.adapter = memberAdapter

        if (groupId != null) {
            loadGroupData()
        } else {
            android.util.Log.e("GROUPDETAIL", "groupId is NULL!")
            Toast.makeText(this, "Error: Group not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews(isReadOnly: Boolean = false) {
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

        if (isReadOnly) {
            btnEditName.visibility = View.GONE
            btnAddMember.visibility = View.GONE
            btnSave.visibility = View.GONE
            editGroupName.isEnabled = false
        }
    }

    private fun loadGroupData() {
        android.util.Log.d("GROUPDETAIL", "loadGroupData called for groupId = $groupId")
        db.collection("groups").document(groupId!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("GROUPDETAIL", "Firestore error: ${e.message}")
                    return@addSnapshotListener
                }
                android.util.Log.d("GROUPDETAIL", "snapshot exists = ${snapshot?.exists()}")
                android.util.Log.d("GROUPDETAIL", "snapshot data = ${snapshot?.data}")

                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val name = snapshot.getString("groupName") ?: "Unknown Group"
                val membersUids = snapshot.get("members") as? List<String> ?: listOf()

                if (!editGroupName.hasFocus()) {
                    editGroupName.setText(name)
                }
                editGroupName.hint = name
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
                    val profileUrl = doc.getString("profileImageUrl")
                        ?: doc.getString("profileUrl")
                    memberList.add(CircleItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        profileUrl = profileUrl
                    ))
                }
                memberAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("GROUPDETAIL", "fetchMemberDetails error: ${e.message}")
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
                batch.set(inviteRef, hashMapOf(
                    "from_uid" to myUid,
                    "from_name" to senderName,
                    "from_profileUrl" to senderProfileUrl,
                    "to_uid" to uid,
                    "groupId" to gId,
                    "groupName" to newName,
                    "status" to "pending",
                    "timestamp" to FieldValue.serverTimestamp()
                ))

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
        }
    }

    private fun removeMemberFromGroup(uidToRemove: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Remove Member")
        builder.setMessage("Are you sure? If this is the last member, everything will be deleted.")

        builder.setPositiveButton("Remove") { _, _ ->
            val gId = groupId ?: return@setPositiveButton
            val currentUid = auth.currentUser?.uid ?: return@setPositiveButton

            val groupRef = db.collection("groups").document(gId)
            val userToRemoveRef = db.collection("users").document(uidToRemove)
            val currentUserRef = db.collection("users").document(currentUid)

            groupRef.get().addOnSuccessListener { snapshot ->
                val members = snapshot.get("members") as? MutableList<String> ?: mutableListOf()
                val batch = db.batch()

                if (members.size <= 1) {
                    batch.delete(groupRef)
                    batch.update(userToRemoveRef, "groups", FieldValue.arrayRemove(gId))
                    batch.update(currentUserRef, "groups", FieldValue.arrayRemove(gId))

                    db.collection("bills").whereEqualTo("groupId", gId).get()
                        .addOnSuccessListener { bills ->
                            for (bill in bills) {
                                batch.delete(bill.reference)
                            }
                            batch.commit().addOnSuccessListener {
                                Toast.makeText(this, "Group and bills deleted completely.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                } else {
                    batch.update(groupRef, "members", FieldValue.arrayRemove(uidToRemove))
                    batch.update(userToRemoveRef, "groups", FieldValue.arrayRemove(gId))
                    batch.commit().addOnSuccessListener {
                        Toast.makeText(this, "Member removed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}