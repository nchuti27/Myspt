package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// --- 1. เพิ่ม Import ที่จำเป็น ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FindUser : AppCompatActivity() {
    var btnBack: ImageButton? = null
    var tvFoundUserName: TextView? = null
    var btnAddFriend: Button? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_finduser)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()


        val receivedName = intent.getStringExtra("FRIEND_NAME")
        val friendUid = intent.getStringExtra("FRIEND_UID")

        if (receivedName != null) {
            tvFoundUserName!!.text = "Found User: $receivedName"
        } else {
            tvFoundUserName!!.text = "User not found"
        }

        btnBack!!.setOnClickListener {
            finish()
        }

        btnAddFriend!!.setOnClickListener {
            if (friendUid != null) {
                addFriendToFirestore(friendUid)
            } else {
                Toast.makeText(this, "Cannot add this user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addFriendToFirestore(friendUid: String) {
        val myUid = auth.currentUser?.uid

        if (myUid != null) {
            if (myUid == friendUid) {
                Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show()
                return
            }


            db.collection("users").document(myUid)
                .update("friends", FieldValue.arrayUnion(friendUid))
                .addOnSuccessListener {
                    Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_LONG).show()


                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBackF)
        tvFoundUserName = findViewById(R.id.tvFoundUserName)
        btnAddFriend = findViewById(R.id.btnAddFriend)
    }
}