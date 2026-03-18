package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NotiRequest : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var requestListener: ListenerRegistration? = null

    private var backButton: ImageButton? = null
    private var btnTabFriend: Button? = null
    private var btnTabGroup: Button? = null
    private var btnClearAll: ImageView? = null
    private var rvNotification: RecyclerView? = null

    private var requestList = ArrayList<DocumentSnapshot>()
    private lateinit var notiAdapter: NotificationAdapter
    private var tvEmptyState: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_noti_request)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupRecyclerView()
        listenToSentRequests()
    }

    private fun init() {
        backButton = findViewById(R.id.backButton)
        btnTabFriend = findViewById(R.id.btnTabFriend)
        btnTabGroup = findViewById(R.id.btnTabGroup)
        btnClearAll = findViewById(R.id.btnClearAll)
        rvNotification = findViewById(R.id.rvNotification)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        backButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        btnClearAll?.setOnClickListener {
            if (requestList.isEmpty()) {
                Toast.makeText(this, "No sent requests to clear", Toast.LENGTH_SHORT).show()
            } else {
                confirmClearAll()
            }
        }

        btnTabFriend?.setOnClickListener {
            startActivity(Intent(this, notification::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnTabGroup?.setOnClickListener {
            startActivity(Intent(this, NotiGroup::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
    private fun checkEmptyState(list: ArrayList<DocumentSnapshot>) {
        if (list.isEmpty()) {
            tvEmptyState?.visibility = View.VISIBLE
            rvNotification?.visibility = View.GONE
        } else {
            tvEmptyState?.visibility = View.GONE
            rvNotification?.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        notiAdapter = NotificationAdapter(requestList,
            onAccept = { /* แท็บนี้ไม่มี Accept */ },
            onDelete = { doc -> cancelRequest(doc) }
        )
        notiAdapter.updateData(requestList, "REQUEST")

        rvNotification?.layoutManager = LinearLayoutManager(this)
        rvNotification?.adapter = notiAdapter
    }

    private fun listenToSentRequests() {
        val myUid = auth.currentUser?.uid ?: return

        requestListener = db.collection("friend_requests")
            .whereEqualTo("from_uid", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null) {
                    requestList.clear()
                    requestList.addAll(snapshots.documents)

                    checkEmptyState(requestList)

                    notiAdapter.updateData(requestList, "REQUEST")
                }
            }
    }

    private fun cancelRequest(doc: DocumentSnapshot) {
        // ลบคำขอออกจาก Database
        db.collection("friend_requests").document(doc.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show()
            }
    }
    private fun confirmClearAll() {
        if (requestList.isEmpty()) {
            Toast.makeText(this, "No requests to clear", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Clear All Sent Requests")
            .setMessage("Are you sure you want to cancel all pending requests?")
            .setPositiveButton("Clear All") { _, _ ->
                val batch = db.batch()
                for (doc in requestList) {
                    batch.delete(doc.reference)
                }

                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, "All requests cleared successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        requestListener?.remove()
    }
}