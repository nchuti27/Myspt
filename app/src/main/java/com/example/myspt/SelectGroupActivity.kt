package com.example.myspt // แก้ให้ตรงกับโฟลเดอร์โปรเจกต์คุณ

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
// ลบ import yourpackage.databinding ออก แล้วเปลี่ยนเป็นอันนี้:
import com.example.myspt.databinding.ActivitySelectGroupBinding

class SelectGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ข้อมูลจำลอง
        val groupList = listOf(
            Group("Office Friends", arrayListOf("You", "Alice", "Bob")),
            Group("Roommates", arrayListOf("You", "Charlie"))
        )

        // เรียกใช้ Adapter (ต้องสร้างไฟล์ GroupAdapter.kt ด้วยนะถ้ายังไม่มี)
        val adapter = GroupAdapter(groupList) { selectedGroup ->
            navigateToNewBill(selectedGroup.members)
        }

        binding.rvGroups.adapter = adapter
        binding.rvGroups.layoutManager = LinearLayoutManager(this)

        binding.btnSkipGroup.setOnClickListener {
            navigateToNewBill(arrayListOf())
        }
    }

    private fun navigateToNewBill(members: ArrayList<String>) {
        val intent = Intent(this, BillSplit::class.java) // เปลี่ยนจาก NewBillActivity เป็น BillSplit ตามชื่อไฟล์ที่คุณมี
        intent.putStringArrayListExtra("SELECTED_MEMBERS", members)
        startActivity(intent)
    }
}

data class Group(val name: String, val members: ArrayList<String>)