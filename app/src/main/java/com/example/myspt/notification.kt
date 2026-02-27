package com.example.myspt

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AlertDialog

class notification : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notiListener: ListenerRegistration? = null

    private lateinit var btnBack: ImageButton
    private lateinit var btnTabGroup: Button
    private lateinit var btnTabFriend: Button
    private lateinit var btnTabRequest: Button
    private lateinit var rvFriendNoti: RecyclerView

    private var notiList = ArrayList<DocumentSnapshot>()
    private lateinit var notiAdapter: NotificationAdapter

    // Track current tab to handle button logic in Adapter
    private var currentTab = "REQUEST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupRecyclerView()
        setupTabListeners()

        // Default to Request Tab (Sent Requests) [cite: 2026-02-27]
        loadRequestTab()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.backButton)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabRequest = findViewById(R.id.btnTabRequest)
        rvFriendNoti = findViewById(R.id.rvFriendNoti)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        notiAdapter = NotificationAdapter(notiList,
            onAccept = { doc -> acceptFriend(doc) },
            onDelete = { doc ->
                if (currentTab == "REQUEST") {
                    // Show confirmation before canceling sent request [cite: 2026-02-27]
                    showCancelConfirmation(doc)
                } else {
                    // Decline incoming request
                    deleteRequest(doc)
                }
            }
        )
        rvFriendNoti.apply {
            layoutManager = LinearLayoutManager(this@notification)
            adapter = notiAdapter
        }
    }

    private fun setupTabListeners() {
        btnTabRequest.setOnClickListener { loadRequestTab() }
        btnTabGroup.setOnClickListener { loadGroupTab() }
        btnTabFriend.setOnClickListener { loadFriendTab() }
    }

    // Tab 1: Requests you sent to others [cite: 2026-02-27]
    private fun loadRequestTab() {
        currentTab = "REQUEST"
        updateTabUI(btnTabRequest)
        val myUid = auth.currentUser?.uid ?: return
        notiListener?.remove()

        notiListener = db.collection("friend_requests")
            .whereEqualTo("from_uid", myUid) // You are the sender
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                notiList.clear()
                snapshots?.let { notiList.addAll(it.documents) }
                notiAdapter.notifyDataSetChanged()
            }
    }

    // Tab 2: Friend requests sent to you [cite: 2026-02-27]
    private fun loadFriendTab() {
        currentTab = "FRIEND"
        updateTabUI(btnTabFriend)
        val myUid = auth.currentUser?.uid ?: return
        notiListener?.remove()

        notiListener = db.collection("friend_requests")
            .whereEqualTo("to_uid", myUid) // You are the receiver
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                notiList.clear()
                snapshots?.let { notiList.addAll(it.documents) }
                notiAdapter.notifyDataSetChanged()
            }
    }

    // Tab 3: Group invitations
    private fun loadGroupTab() {
        currentTab = "GROUP"
        updateTabUI(btnTabGroup)
        val myUid = auth.currentUser?.uid ?: return
        notiListener?.remove()

        notiListener = db.collection("notifications")
            .whereEqualTo("receiverId", myUid)
            .whereEqualTo("type", "GROUP_INVITE")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                notiList.clear()
                snapshots?.let { notiList.addAll(it.documents) }
                notiAdapter.notifyDataSetChanged()
            }
    }

    private fun showCancelConfirmation(doc: DocumentSnapshot) {
        val targetName = doc.getString("to_name") ?: "this user"
        AlertDialog.Builder(this)
            .setTitle("Cancel Request")
            .setMessage("Are you sure you want to cancel the request sent to $targetName?")
            .setPositiveButton("Yes") { _, _ ->
                deleteRequest(doc)
                Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateTabUI(activeButton: Button) {
        val buttons = listOf(btnTabRequest, btnTabGroup, btnTabFriend)
        buttons.forEach { button ->
            if (button == activeButton) {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3D79CA"))
                button.setTextColor(Color.WHITE)
            } else {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
                button.setTextColor(Color.parseColor("#64748B"))
            }
        }
    }

    private fun acceptFriend(doc: DocumentSnapshot) {
        val myUid = auth.currentUser?.uid ?: return
        val senderUid = doc.getString("from_uid") ?: return
        val senderName = doc.getString("from_name") ?: "Unknown User"

        val batch = db.batch()
        batch.delete(db.collection("friend_requests").document(doc.id))
        batch.update(db.collection("users").document(myUid), "friends", FieldValue.arrayUnion(senderUid))
        batch.update(db.collection("users").document(senderUid), "friends", FieldValue.arrayUnion(myUid))

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "You are now friends with $senderName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteRequest(doc: DocumentSnapshot) {
        val collection = if (doc.reference.path.contains("friend_requests")) "friend_requests" else "notifications"
        db.collection(collection).document(doc.id).delete()
    }

    override fun onDestroy() {
        super.onDestroy()
        notiListener?.remove()
    }
}