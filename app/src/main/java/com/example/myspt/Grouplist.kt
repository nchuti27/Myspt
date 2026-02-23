package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Grouplist : AppCompatActivity() {

    private lateinit var rvGroupList: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var backButton: ImageButton

    // ใช้ HomeGroupAdapter ตัวเก่งของคุณได้เลย
    private lateinit var adapter: HomeGroupAdapter
    private val allGroups = ArrayList<CircleItem>()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // เปลี่ยนชื่อ Layout ให้ตรงกับไฟล์ XML ของคุณ (เช่น activity_grouplist)
        setContentView(R.layout.activity_grouplist)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupRecyclerView()
        fetchGroupList()
        setupSearch()
    }

    private fun initViews() {
        rvGroupList = findViewById(R.id.rvGroupList)
        etSearch = findViewById(R.id.etSearch)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        // กำหนด isListView = true เพื่อให้มันแสดงผลเป็นแนวยาว
        adapter = HomeGroupAdapter(allGroups, isListView = true) { group ->
            // เมื่อกดที่กลุ่ม ให้ไปหน้า GroupDetail
            val intent = Intent(this, GroupDetail::class.java)
            intent.putExtra("GROUP_ID", group.id)
            startActivity(intent)
        }

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
}