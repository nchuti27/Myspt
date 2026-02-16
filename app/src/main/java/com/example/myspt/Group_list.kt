package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Group_list : AppCompatActivity() {

    private lateinit var rvGroupList: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBackF)
        rvGroupList = findViewById(R.id.rvGroupList)
        etSearch = findViewById(R.id.etSearch)
        btnBack.setOnClickListener { finish() }
        fetchUserGroups()
    }

    private fun fetchUserGroups() {
        val myUid = auth.currentUser?.uid ?: return
        val groupList = ArrayList<CircleItem>()

        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val groupIds = document.get("groups") as? List<String> ?: listOf()

            var count = 0
            if (groupIds.isEmpty()) return@addOnSuccessListener

            for (gId in groupIds) {
                db.collection("groups").document(gId).get().addOnSuccessListener { gDoc ->
                    val name = gDoc.getString("groupName") ?: "Unknown Group"
                    groupList.add(CircleItem(id = gId, name = name))
                    count++

                    if (count == groupIds.size) {
                        setupRecyclerView(groupList)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(list: List<CircleItem>) {
        rvGroupList.layoutManager = LinearLayoutManager(this)
        rvGroupList.adapter = HomeGroupAdapter(list) { item ->
            Toast.makeText(this, "Click your group: ${item.name}", Toast.LENGTH_SHORT).show()
        }
    }
}