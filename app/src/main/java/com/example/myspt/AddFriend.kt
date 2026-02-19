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

    private var etSearchUser: EditText? = null
    private var btnAdd: Button? = null
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
        val btnBack = findViewById<ImageButton>(R.id.backButton)

        btnBack.setOnClickListener {
            finish()
        }

        btnAdd?.setOnClickListener {
            val username = etSearchUser?.text.toString().trim()

            if (username.isEmpty()) {
                etSearchUser?.error = "Please enter username"
                return@setOnClickListener
            }

            checkUserAndNavigate(username)
        }
    }

    private fun checkUserAndNavigate(username: String) {

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {

                    for (document in documents) {
                        val friendName = document.getString("name")
                        val friendUid = document.id

                        val intent = Intent(this, FindUser::class.java)
                        intent.putExtra("FRIEND_NAME", friendName)
                        intent.putExtra("FRIEND_UID", friendUid)
                        startActivity(intent)
                    }
                } else {

                    Toast.makeText(this, "Not found user", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}