package com.example.myspt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Group_list : AppCompatActivity() {

    private var btnBack: ImageButton? = null

    // 1. ตัวแปรสำหรับ List
    private var rvGroupList: RecyclerView? = null
    private var groupAdapter: GroupAdapter? = null
    private var groupList: ArrayList<GroupData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_group_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        // กดปุ่มย้อนกลับ
        btnBack?.setOnClickListener {
            finish()
        }

        // 3. เรียกฟังก์ชันสร้างข้อมูลตัวอย่าง
        setupRecyclerView()
    }

    private fun init() {
        // เชื่อม ID ให้ตรงกับ activity_group_list.xml
        btnBack = findViewById(R.id.btnBackF)
        rvGroupList = findViewById(R.id.rvGroupList)
    }

    private fun setupRecyclerView() {
        groupList = ArrayList()

        // ==========================================
        // *** เพิ่มตัวอย่างข้อมูลกลุ่มตรงนี้ ***
        // ==========================================
        groupList?.add(GroupData("Trip to Japan \uD83C\uDDEF\uD83C\uDDF5"))
        groupList?.add(GroupData("Office Lunch \uD83C\uDF71"))
        groupList?.add(GroupData("Friday Party \uD83C\uDF7B"))
        groupList?.add(GroupData("Football Team ⚽"))
        groupList?.add(GroupData("Family ❤️"))
        groupList?.add(GroupData("Project A \uD83D\uDCCA"))
        groupList?.add(GroupData("Room 404 \uD83D\uDC7B"))

        // ตรวจสอบและยัด Adapter ใส่ RecyclerView
        if (rvGroupList != null && groupList != null) {
            groupAdapter = GroupAdapter(groupList!!)
            rvGroupList?.layoutManager = LinearLayoutManager(this)
            rvGroupList?.adapter = groupAdapter
        }
    }
}

// ==========================================
// ส่วน Data และ Adapter
// ==========================================

// 1. Data Class: เก็บชื่อกลุ่ม
data class GroupData(
    val groupName: String
)

// 2. Adapter: ตัวจัดการการแสดงผล
class GroupAdapter(private var groupList: ArrayList<GroupData>) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // เชื่อมต่อ ID จากไฟล์ item_group_list.xml
        val tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        val ivGroupIcon: ImageView = itemView.findViewById(R.id.ivGroupIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        // ดึง Layout item_group_list มาใช้
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_list, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentItem = groupList[position]

        // แสดงชื่อกลุ่ม
        holder.tvGroupName.text = currentItem.groupName

        // (Option) ถ้าอยากเปลี่ยนรูปไอคอน ให้ใช้คำสั่งนี้
        // holder.ivGroupIcon.setImageResource(R.drawable.ชื่อรูป)

        // คลิกที่กลุ่มแล้วมีข้อความเด้งขึ้นมา
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Open Group: ${currentItem.groupName}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }
}