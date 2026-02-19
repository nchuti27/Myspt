package com.example.myspt

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroup : AppCompatActivity() {

    private lateinit var etGroupName: TextInputEditText
    private lateinit var btnCreate: MaterialButton
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creategroup)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        etGroupName = findViewById(R.id.etGroupName)
        btnCreate = findViewById(R.id.btnCreateGroup)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.backButton)

        btnBack?.setOnClickListener { finish() }

        btnCreate.setOnClickListener {
            val name = etGroupName.text.toString().trim()
            if (name.isNotEmpty()) {
                createNewGroup(name)
            } else {
                etGroupName.error = "Please input your group name"
            }
        }
    }

    private fun createNewGroup(groupName: String) {
        val myUid = auth.currentUser?.uid ?: return

        val groupData = hashMapOf(
            "groupName" to groupName,
            "admin" to myUid,
            "members" to arrayListOf(myUid)
        )

        db.collection("groups").add(groupData).addOnSuccessListener { ref ->
            val groupId = ref.id
            db.collection("users").document(myUid)
                .update("groups", FieldValue.arrayUnion(groupId))
                .addOnSuccessListener {
                    Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}