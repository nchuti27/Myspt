package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Friend_list : AppCompatActivity() {
    private var rvFriendList: RecyclerView? = null
    private var friendAdapter: FriendAdapter? = null
    private var friendList: ArrayList<FriendData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
    }

    private fun init() {
        // 1. เชื่อมต่อ View
        val btnAddFriendPage = findViewById<ImageButton>(R.id.btnAddFriendPage)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        rvFriendList = findViewById(R.id.rvFriendList) // เชื่อมต่อ RecyclerView

        // 2. ตั้งค่าปุ่มกด
        /*btnAddFriendPage.setOnClickListener {
            val intent = Intent(this, AddFriend::class.java)
            startActivity(intent)
        }*/

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // ควรใส่ finish() เพื่อปิดหน้าปัจจุบัน
        }

        // ==========================================
        // 3. ส่วนการตั้งค่า List (RecyclerView)
        // ==========================================

        friendList = ArrayList()
        if (friendList != null) {
            friendAdapter = FriendAdapter(friendList!!)
            rvFriendList?.layoutManager = LinearLayoutManager(this)
            rvFriendList?.adapter = friendAdapter
        }
    }

}

// ==========================================
// Class ที่เพิ่มเข้ามา (อยู่นอก Class Friend_list)
// ==========================================

// 1. Data Class: เก็บข้อมูลและสถานะการยืด/หด
data class FriendData(
    val name: String,
    val detail: String,
    var isExpanded: Boolean = false // ค่าเริ่มต้นคือ false (ปิดอยู่)
)

// 2. Adapter Class: จัดการการแสดงผลและการกดปุ่ม
//รับ friendList (รายชื่อเพื่อน) เข้ามาทำงาน
class FriendAdapter(private var friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // เชื่อมต่อ ID จากไฟล์ XML (item_friend_exp.xml)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val btnExpand: ImageButton = itemView.findViewById(R.id.btnExpand)
        val layoutHidden: LinearLayout = itemView.findViewById(R.id.layoutHidden)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnViewProfile: Button = itemView.findViewById(R.id.btnViewProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // *** ตรวจสอบชื่อไฟล์ Layout ตรงนี้ (R.layout.xxxx) ให้ตรงกับที่คุณสร้าง ***
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_exp, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = friendList[position]

        // 1. แสดงชื่อ
        holder.tvName.text = currentItem.name
        // 2. ตรวจสอบสถานะ: ถ้า isExpanded = true ให้โชว์เมนู, ถ้า false ให้ซ่อน
        holder.layoutHidden.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE
        // 3. เมื่อกดปุ่ม 3 จุด (Expand)
        holder.btnExpand.setOnClickListener {
            // สลับสถานะ (True <-> False)
            currentItem.isExpanded = !currentItem.isExpanded
            // สั่งให้รีเฟรชเฉพาะแถวนี้ (เพื่อให้เมนูเด้งออกมา)
            notifyItemChanged(position)
        }

        // 4. ปุ่ม Delete
        holder.btnDelete.setOnClickListener {
            friendList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, friendList.size)
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}