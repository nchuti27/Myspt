package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog
class Grouplist : AppCompatActivity() {

    private lateinit var rvGroupList: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var backButton: ImageButton
    private lateinit var adapter: HomeGroupAdapter
    private val allGroups = ArrayList<CircleItem>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_grouplist)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupRecyclerView()
        // เรียกดึงข้อมูลครั้งแรกครั้งเดียวพอ
        fetchGroupList()
        setupSearch()
    }
    override fun onResume() {
        super.onResume()
    }

    private fun initViews() {
        rvGroupList = findViewById(R.id.rvGroupList)
        etSearch = findViewById(R.id.etSearch)
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = HomeGroupAdapter(
            allGroups,
            true,
            { group ->
                val intent = Intent(this, GroupDetail::class.java)
                intent.putExtra("GROUP_ID", group.id)
                startActivity(intent)
            },
            { group -> showLeaveGroupDialog(group) }
        )
        rvGroupList.layoutManager = LinearLayoutManager(this)
        rvGroupList.adapter = adapter
    }

    private fun fetchGroupList() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val groupIds = document.get("groups") as? List<String> ?: listOf()

            // เคลียร์รายการทิ้งทันทีเมื่อเริ่มโหลดใหม่
            allGroups.clear()

            if (groupIds.isEmpty()) {
                adapter.notifyDataSetChanged()
                return@addOnSuccessListener
            }

            db.collection("groups")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), groupIds)
                .get()
                .addOnSuccessListener { documents ->
                    allGroups.clear() // เคลียร์อีกรอบเพื่อความชัวร์ก่อนรับค่าใหม่
                    for (gDoc in documents) {
                        val name = gDoc.getString("groupName") ?: "Unknown"
                        allGroups.add(CircleItem(id = gDoc.id, name = name, isAddButton = false))
                    }
                    adapter.notifyDataSetChanged()
                }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                val filteredList = allGroups.filter {
                    it.name.contains(keyword, ignoreCase = true)
                }
                adapter.updateData(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showLeaveGroupDialog(group: CircleItem) {
        AlertDialog.Builder(this)
            .setTitle("Leave Group")
            .setMessage("Are you sure you want to leave ${group.name}?")
            .setPositiveButton("Leave") { dialog, _ ->
                leaveGroupInFirebase(group)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun leaveGroupInFirebase(group: CircleItem) {
        val myUid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(myUid)
        val groupRef = db.collection("groups").document(group.id)

        userRef.update("groups", FieldValue.arrayRemove(group.id))
            .addOnSuccessListener {
                groupRef.update("members", FieldValue.arrayRemove(myUid))
                    .addOnSuccessListener {
                        Toast.makeText(this, "You left ${group.name}", Toast.LENGTH_SHORT).show()
                        fetchGroupList()
                    }
            }
    }
}