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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Friend_list : AppCompatActivity() {

    // ประกาศตัวแปร RecyclerView และ Adapter
    private var rvFriendList: RecyclerView? = null
    private var friendAdapter: FriendAdapter? = null
    private var friendList: ArrayList<FriendData>? = null

    // *** ลบตัวแปร btnAddFriendPage ที่เป็น ArrayList ออก เพราะเราใช้เป็น Local variable ใน init() แทน ***

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
        val btnBack = findViewById<ImageButton>(R.id.btnBackF)
        rvFriendList = findViewById(R.id.rvFriendList)

        // 2. ตั้งค่าปุ่มกด
        btnAddFriendPage.setOnClickListener {
            // ไปหน้าเพิ่มเพื่อน (ต้องมีไฟล์ AddFriend.kt)
            val intent = Intent(this, AddFriend::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        friendList = ArrayList()

        friendList?.add(FriendData("Somchai", "User ID: 001"))
        friendList?.add(FriendData("Somsak", "User ID: 002"))
        friendList?.add(FriendData("Somsri", "User ID: 003"))
        friendList?.add(FriendData("John Doe", "User ID: 004"))

        // ตรวจสอบว่ามีข้อมูลและ rv เชื่อมต่อแล้ว
        if (friendList != null && rvFriendList != null) {
            friendAdapter = FriendAdapter(friendList!!)
            rvFriendList!!.layoutManager = LinearLayoutManager(this)
            rvFriendList!!.adapter = friendAdapter
        }
    }
}

// ==========================================
// Class ภายนอก (Data & Adapter)
// ==========================================

data class FriendData(
    val name: String,
    val detail: String,
    var isExpanded: Boolean = false
)

class FriendAdapter(private var friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    // ลบ inner ออก เพื่อประสิทธิภาพที่ดีกว่า (ถ้าไม่จำเป็นต้องเรียกตัวแปรจาก Class แม่)
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val btnExpand: ImageButton = itemView.findViewById(R.id.btnExpand)
        val layoutHidden: LinearLayout = itemView.findViewById(R.id.layoutHidden)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnViewProfile: Button = itemView.findViewById(R.id.btnViewProfile) // ต้องมี ID นี้ใน xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_exp, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = friendList[position]

        holder.tvName.text = currentItem.name

        // Logic การซ่อน/แสดง เมนู
        holder.layoutHidden.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

        // เปลี่ยนรูป icon ลูกศรขึ้นลง (Optional: ถ้าคุณมีรูป ic_expand_less / ic_expand_more)
        // if (currentItem.isExpanded) holder.btnExpand.setImageResource(R.drawable.ic_expand_less)
        // else holder.btnExpand.setImageResource(R.drawable.ic_expand_more)

        holder.btnExpand.setOnClickListener {
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(position) // รีเฟรชเฉพาะแถวนี้
        }

        holder.btnDelete.setOnClickListener {
            friendList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, friendList.size)
        }

        // เพิ่ม Logic ปุ่ม View Profile (ถ้ามี)
        holder.btnViewProfile.setOnClickListener {
            Toast.makeText(holder.itemView.context, "View Profile: ${currentItem.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}