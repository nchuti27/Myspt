package com.example.myspt

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Grouplist : AppCompatActivity() {

    private lateinit var rvGroupList: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ตรวจสอบชื่อไฟล์ XML ของคุณ (ถ้าในโปรเจกต์ชื่อ activity_grouplist ให้ใช้ชื่อนั้น)
        setContentView(R.layout.activity_grouplist)

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

        // ดึงข้อมูลรายชื่อกลุ่มที่ User คนนี้สังกัดอยู่
        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val groupIds = document.get("groups") as? List<String> ?: listOf()

            if (groupIds.isEmpty()) {
                // ถ้าไม่มีกลุ่มเลย ให้แสดง RecyclerView ว่างๆ (หรือใส่ปุ่ม Create Group ก็ได้)
                setupRecyclerView(groupList)
                return@addOnSuccessListener
            }

            var count = 0
            for (gId in groupIds) {
                db.collection("groups").document(gId).get().addOnSuccessListener { gDoc ->
                    val name = gDoc.getString("groupName") ?: "Unknown Group"
                    groupList.add(CircleItem(id = gId, name = name))
                    count++

                    // เมื่อดึงข้อมูลชื่อกลุ่มครบตามจำนวน ID แล้วจึงแสดงผล
                    if (count == groupIds.size) {
                        setupRecyclerView(groupList)
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการดึงข้อมูล", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView(list: List<CircleItem>) {
        // หน้า See More ใช้แนวตั้ง (Vertical)
        rvGroupList.layoutManager = LinearLayoutManager(this)

        // ส่งค่า true เข้าไปในพารามิเตอร์ที่สอง (isListView)
        // เพื่อให้ HomeGroupAdapter เลือกใช้ layout item_group_list แทน item_circle
        rvGroupList.adapter = HomeGroupAdapter(list, true) { item ->
            // เมื่อกดที่แถวกลุ่มในหน้า List
            Toast.makeText(this, "เลือกกลุ่ม: ${item.name}", Toast.LENGTH_SHORT).show()
            // ในอนาคตสามารถใช้ Intent ไปหน้าแชทกลุ่มได้จากตรงนี้
        }
    }
}