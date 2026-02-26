package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectGroupActivity : AppCompatActivity() {

    private lateinit var rvGroups: RecyclerView
    private lateinit var btnSkipGroup: Button
    private lateinit var backButton: ImageView

    // เพิ่มตัวแปร Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // สร้าง List ไว้เก็บข้อมูลกลุ่มจริงๆ
    private val groupList = ArrayList<Group>()
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_group)

        // กำหนดค่า Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ผูกตัวแปรกับ ID ใน XML
        rvGroups = findViewById(R.id.rvGroups)
        btnSkipGroup = findViewById(R.id.btnSkipGroup)
        backButton = findViewById(R.id.backButton)

        // ตั้งค่าปุ่มย้อนกลับให้กลับไปหน้า MainActivity
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // ตั้งค่า Adapter (ตอนแรกลิสต์จะยังว่างเปล่า รอข้อมูลจาก Firebase โหลดเสร็จ)
        adapter = GroupAdapter(groupList) { selectedGroup ->
            navigateToNewBill(selectedGroup.members)
        }
        rvGroups.adapter = adapter
        rvGroups.layoutManager = LinearLayoutManager(this)

        // ปุ่มข้ามการเลือกกลุ่ม
        btnSkipGroup.setOnClickListener {
            navigateToNewBill(arrayListOf())
        }

        // เรียกฟังก์ชันดึงข้อมูลกลุ่มจริงๆ ของผู้ใช้
        loadRealGroups()
    }

    private fun loadRealGroups() {
        val myUid = auth.currentUser?.uid ?: return

        // 1. ดึงข้อมูล User ปัจจุบัน เพื่อดูว่ามีกลุ่มอะไรบ้าง
        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val groupIds = document.get("groups") as? List<String> ?: listOf()
            groupList.clear() // ล้างข้อมูลเก่าก่อน

            if (groupIds.isEmpty()) {
                adapter.notifyDataSetChanged()
                return@addOnSuccessListener
            }

            var loadedCount = 0
            // 2. วนลูปดึงข้อมูลรายละเอียดของแต่ละกลุ่ม
            for (gId in groupIds) {
                db.collection("groups").document(gId).get().addOnSuccessListener { gDoc ->
                    if (gDoc.exists()) {
                        val name = gDoc.getString("groupName") ?: "Unknown Group"
                        // ดึงรายชื่อสมาชิกมาด้วย (เพื่อส่งต่อไปหน้า BillSplit)
                        val membersUids = gDoc.get("members") as? ArrayList<String> ?: arrayListOf()

                        // เพิ่มลงใน List
                        groupList.add(Group(name = name, members = membersUids))
                    }

                    loadedCount++
                    // เมื่อดึงข้อมูลครบทุกกลุ่มแล้ว ให้อัปเดตหน้าจอ
                    if (loadedCount == groupIds.size) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "ดึงข้อมูลกลุ่มล้มเหลว", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToNewBill(members: ArrayList<String>) {
        val intent = Intent(this, BillSplit::class.java)
        // ส่งรายชื่อสมาชิกของกลุ่มที่เลือกไปหน้าต่อไป
        intent.putStringArrayListExtra("SELECTED_MEMBERS", members)
        startActivity(intent)
    }
}

// Data Class สำหรับเก็บข้อมูลกลุ่ม
data class Group(val name: String, val members: ArrayList<String>)