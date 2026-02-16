package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // ประกาศตัวแปร View
    private var imgUserProfile: ImageView? = null
    private var btnNotification: ImageView? = null

    private var tvFriendLabel: TextView? = null
    private var tvSeeMoreFriend: TextView? = null

    private var tvGroupLabel: TextView? = null
    private var tvSeeMoreGroup: TextView? = null

    private var btnSplitBill: LinearLayout? = null
    private var btnRecentBill: LinearLayout? = null
    private var btnOwe: LinearLayout? = null
    private var btnLogout: ImageView? = null

    // *** 1. เพิ่มตัวแปรสำหรับ RecyclerView ***
    private var rvFriends: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // ตั้งค่า Padding
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        init() // เชื่อมตัวแปร
        setupFriendList() // *** 2. เรียกฟังก์ชันสร้างรายการเพื่อน ***

        // ตั้งค่าปุ่มกดต่างๆ
        imgUserProfile?.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        btnNotification?.setOnClickListener {
            startActivity(Intent(this, notification::class.java))
        }

        tvSeeMoreFriend?.setOnClickListener {
            startActivity(Intent(this, Friend_list::class.java))
        }

        tvSeeMoreGroup?.setOnClickListener {
            startActivity(Intent(this, Group_list::class.java))
        }

        btnSplitBill?.setOnClickListener {
            startActivity(Intent(this, BillSplit::class.java))
        }

        btnRecentBill?.setOnClickListener {
            startActivity(Intent(this, BillDetail::class.java))
        }

        btnOwe?.setOnClickListener {
            startActivity(Intent(this, Owe::class.java))
        }

        btnLogout?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun init() {
        // เชื่อม ID
        imgUserProfile = findViewById(R.id.imgUserProfile)
        btnNotification = findViewById(R.id.btnNotification)

        tvFriendLabel = findViewById(R.id.tvFriendLabel)
        tvSeeMoreFriend = findViewById(R.id.tvSeeMoreFriend)

        tvGroupLabel = findViewById(R.id.tvGroupLabel)
        tvSeeMoreGroup = findViewById(R.id.tvSeeMoreGroup)

        btnSplitBill = findViewById(R.id.btnSplitBill)
        btnRecentBill = findViewById(R.id.btnRecentBill)
        btnOwe = findViewById(R.id.btnOwe)
        btnLogout = findViewById(R.id.btnLogout)

        // *** 3. เชื่อมต่อ ID ของ RecyclerView (ต้องตรงกับใน XML) ***
        rvFriends = findViewById(R.id.rvFriends)
    }

    // *** 4. ฟังก์ชันสำหรับสร้างข้อมูลและแสดงผล List ***
    // (เหลือไว้แค่อันเดียว ที่ใช้ HomeFriendAdapter)
    private fun setupFriendList() {
        val friendList = ArrayList<FriendData>()
        friendList.add(FriendData("Somchai", "ID: 001"))
        friendList.add(FriendData("Somsak", "ID: 002"))
        friendList.add(FriendData("John", "ID: 003"))
        friendList.add(FriendData("Somsri", "ID: 004"))

        // ตรวจสอบว่า rvFriends เชื่อมต่อแล้ว
        if (rvFriends != null) {

            // ใช้ Adapter ตัวใหม่ (HomeFriendAdapter) เพื่อแสดงผลแบบวงกลม
            val adapter = HomeFriendAdapter(friendList)
            rvFriends?.adapter = adapter

            // กำหนดเป็นแนวนอน (Horizontal)
            rvFriends?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showLogoutDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        dialog.show()
    }
}