package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.load

class FindUser : AppCompatActivity() {
    private var btnAddFriend: Button? = null
    private var tvFoundUserName: TextView? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finduser)
        initView()

        val friendUid = intent.getStringExtra("FRIEND_UID")
        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Unknown User"

        tvFoundUserName?.text = friendName


        val ivUserProfile = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivUserProfile)
        ivUserProfile.setOnClickListener { openProfile(friendUid, friendName) }
        tvFoundUserName?.setOnClickListener { openProfile(friendUid, friendName) }

        if (friendUid != null) {
            checkStatus(friendUid, friendName)
            loadProfileImage(friendUid, ivUserProfile)
        }
    }

    private fun openProfile(uid: String?, name: String) {
        val intent = Intent(this, FriendProfile::class.java)
        intent.putExtra("FRIEND_UID", uid)
        intent.putExtra("FRIEND_NAME", name)
        intent.putExtra("IS_FRIEND", false)
        startActivity(intent)
    }

    private fun loadProfileImage(uid: String, iv: com.google.android.material.imageview.ShapeableImageView) {
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            val url = doc.getString("profileImageUrl")
            if (!url.isNullOrEmpty()) {
                iv.load(url)
            }
        }
    }

    private fun initView() {
        btnAddFriend = findViewById(R.id.btnAddFriend)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun checkStatus(targetUid: String, targetName: String) {
        val myUid = auth.currentUser?.uid ?: return
        if (myUid == targetUid) {
            btnAddFriend?.visibility = View.GONE
            return
        }

        btnAddFriend?.visibility = View.VISIBLE

        db.collection("users").document(myUid).get().addOnSuccessListener { doc ->
            @Suppress("UNCHECKED_CAST")
            val friends = doc.get("friends") as? List<String> ?: listOf()
            if (friends.contains(targetUid)) {
                updateButton("Already Friends", false)
            } else {
                db.collection("friend_requests")
                    .whereEqualTo("from_uid", myUid)
                    .whereEqualTo("to_uid", targetUid)
                    .whereEqualTo("status", "pending")
                    .get().addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            updateButton("Requested", false)
                        } else {
                            updateButton("ADD FRIEND", true)
                            btnAddFriend?.setOnClickListener { sendRequest(targetUid, targetName) }
                        }
                    }
                    .addOnFailureListener {
                        updateButton("ADD FRIEND", true) // ถ้าเช็กพลาดให้ขึ้นปุ่มปกติไว้ก่อน
                    }
            }
        }.addOnFailureListener {
            updateButton("ADD FRIEND", true)
        }
    }

    private fun updateButton(text: String, enabled: Boolean) {
        btnAddFriend?.apply {
            this.text = text
            this.isEnabled = enabled
            this.alpha = if (enabled) 1.0f else 0.5f
        }
    }

    private fun sendRequest(targetUid: String, targetName: String) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get().addOnSuccessListener { myDoc ->
            val request = hashMapOf(
                "from_uid" to myUid,
                "from_name" to (myDoc.getString("name") ?: "Someone"),
                "from_profileUrl" to myDoc.getString("profileUrl"),
                "to_uid" to targetUid,
                "to_name" to targetName,
                "status" to "pending",
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.collection("friend_requests").add(request).addOnSuccessListener {
                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                updateButton("Requested", false)
            }
        }
    }
}