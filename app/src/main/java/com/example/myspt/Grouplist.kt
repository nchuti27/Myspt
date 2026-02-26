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
        fetchGroupList()
        setupSearch()
    }
    override fun onResume() {
        super.onResume()
        fetchGroupList()
    }

    private fun initViews() {
        rvGroupList = findViewById(R.id.rvGroupList)
        etSearch = findViewById(R.id.etSearch)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ต้องส่ง parameter ให้ครบตามลำดับของ Constructor ใน Adapter
        adapter = HomeGroupAdapter(
            allGroups,       // 1. groupList
            true,            // 2. isListView
            { group ->       // 3. onClick (จัดการกดเข้าหน้า Detail)
                val intent = Intent(this, GroupDetail::class.java)
                intent.putExtra("GROUP_ID", group.id)
                startActivity(intent)
            },
            { group ->       // 4. onLeaveClick (จัดการกดปุ่ม Leave สีแดง)
                showLeaveGroupDialog(group)
            }
        )

        rvGroupList.layoutManager = LinearLayoutManager(this)
        rvGroupList.adapter = adapter
    }
    private fun fetchGroupList() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val groupIds = document.get("groups") as? List<String> ?: listOf()
            allGroups.clear()

            if (groupIds.isEmpty()) {
                adapter.notifyDataSetChanged()
                return@addOnSuccessListener
            }

            var loadedCount = 0
            for (gId in groupIds) {
                db.collection("groups").document(gId).get().addOnSuccessListener { gDoc ->
                    val name = gDoc.getString("groupName") ?: "Unknown"
                    // เอาปุ่ม Add ออกในหน้า See More เพราะเน้นดูรายชื่อ
                    allGroups.add(CircleItem(id = gId, name = name, isAddButton = false))
                    loadedCount++

                    if (loadedCount == groupIds.size) {
                        adapter.notifyDataSetChanged()
                    }
                }
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
            .setPositiveButton("Leave") { dialogInterface, _ -> // เปลี่ยนชื่อเป็น dialogInterface เพื่อความชัดเจน
                leaveGroupInFirebase(group)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }
    private fun leaveGroupInFirebase(group: CircleItem) {
        val myUid = auth.currentUser?.uid ?: return
        val groupId = group.id

        val userRef = db.collection("users").document(myUid)
        val groupRef = db.collection("groups").document(groupId)

        // ลบกลุ่มออกจาก User
        userRef.update("groups", FieldValue.arrayRemove(groupId))
            .addOnSuccessListener {
                // ลบ User ออกจาก Group (เช็คชื่อฟิลด์ 'members' ให้ตรงกับใน Firebase ของคุณด้วยนะครับ)
                groupRef.update("members", FieldValue.arrayRemove(myUid))
                    .addOnSuccessListener {
                        Toast.makeText(this, "You left ${group.name}", Toast.LENGTH_SHORT).show()
                        fetchGroupList()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Updated your profile, but group member list remains.", Toast.LENGTH_SHORT).show()
                        fetchGroupList()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}