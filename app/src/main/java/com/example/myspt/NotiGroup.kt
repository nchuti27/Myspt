package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotiGroup : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var groupNotiListener: ListenerRegistration? = null

    private var btnBack: ImageButton? = null
    private var rvGroupNoti: RecyclerView? = null
    private var btnClearAll: ImageView? = null
    private var groupNotiList = ArrayList<DocumentSnapshot>()
    private lateinit var groupAdapter: NotificationAdapter

    private var btnTabFriend: Button? = null
    private var btnTabRequest: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noti_group)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupRecyclerView()
        listenToGroupInvites()
    }

    private fun init() {
        rvGroupNoti = findViewById(R.id.rvGroupNoti)
        btnBack = findViewById(R.id.backButton)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabRequest = findViewById(R.id.btnTabRequest)
        btnClearAll = findViewById(R.id.btnClearAll) // 🌟 ผูกปุ่มถังขยะ

        btnBack?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // 🌟 ระบบ Clear All เฉพาะแท็บ Group
        btnClearAll?.setOnClickListener {
            if (groupNotiList.isEmpty()) {
                Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show()
            } else {
                confirmClearAll()
            }
        }

        btnTabFriend?.setOnClickListener {
            startActivity(Intent(this, notification::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnTabRequest?.setOnClickListener {
            startActivity(Intent(this, NotiRequest::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ส่งแท็บ "GROUP" ไปให้ Adapter เพื่อจัดการปุ่มให้ถูกต้อง
        groupAdapter = NotificationAdapter(groupNotiList,
            onAccept = { doc -> joinGroup(doc) },
            onDelete = { doc -> declineGroup(doc) }
        )
        groupAdapter.updateData(groupNotiList, "GROUP") // 🌟 กำหนดแท็บเริ่มต้น

        rvGroupNoti?.layoutManager = LinearLayoutManager(this)
        rvGroupNoti?.adapter = groupAdapter
    }

    private fun listenToGroupInvites() {
        val myUid = auth.currentUser?.uid ?: return

        groupNotiListener = db.collection("group_invites")
            .whereEqualTo("to_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    groupNotiList.clear()
                    groupNotiList.addAll(snapshots.documents)
                    groupAdapter.updateData(groupNotiList, "GROUP")
                }
            }
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle("Clear Group Invites")
            .setMessage("Do you want to clear all group invitations?")
            .setPositiveButton("Clear All") { _, _ ->
                val batch = db.batch()
                for (doc in groupNotiList) {
                    batch.delete(doc.reference) // 🌟 ลบจาก Database จริง
                }
                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "All invites cleared", Toast.LENGTH_SHORT).show()
                    // รายการจะหายจากหน้าจออัตโนมัติเพราะ SnapshotListener
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun joinGroup(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val groupId = doc.getString("groupId") ?: return

        val batch = db.batch()
        batch.update(db.collection("group_invites").document(doc.id), "status", "accepted")
        batch.update(db.collection("groups").document(groupId), "members", FieldValue.arrayUnion(myUid))
        batch.update(db.collection("users").document(myUid), "groups", FieldValue.arrayUnion(groupId))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Joined group!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun declineGroup(doc: DocumentSnapshot) {
        db.collection("group_invites").document(doc.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Invitation removed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupNotiListener?.remove()
    }
}