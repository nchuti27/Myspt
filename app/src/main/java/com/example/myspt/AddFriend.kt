package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddFriend : AppCompatActivity() {
    private lateinit var etSearchUser: EditText
    private lateinit var btnAdd: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addfriend)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        init()
    }

    private fun init() {
        etSearchUser = findViewById(R.id.etSearchUser)
        btnAdd = findViewById(R.id.btnAdd)
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        btnAdd.setOnClickListener {
            val username = etSearchUser.text.toString().trim()
            if (username.isEmpty()) {
                etSearchUser.error = "Please enter username"
                return@setOnClickListener
            }
            searchUser(username)
        }
    }

    private fun searchUser(username: String) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val intent = Intent(this, FindUser::class.java).apply {
                        putExtra("FRIEND_NAME", doc.getString("name"))
                        putExtra("FRIEND_UID", doc.id)
                        putExtra("FRIEND_USERNAME", doc.getString("username"))
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
    }
}