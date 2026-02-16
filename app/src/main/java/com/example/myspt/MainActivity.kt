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
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var imgUserProfile: ImageView? = null
    private var btnNotification: ImageView? = null
    private var tvSeeMoreFriend: TextView? = null
    private var tvSeeMoreGroup: TextView? = null
    private var btnSplitBill: LinearLayout? = null
    private var btnRecentBill: LinearLayout? = null
    private var btnOwe: LinearLayout? = null
    private var btnLogout: ImageView? = null

    private var rvFriends: RecyclerView? = null
    private var rvGroups: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        init()

        setupFriendList() // แสดงแค่ปุ่มบวกที่ท้ายเพื่อน
        setupGroupList()  // แสดงแค่ปุ่มบวกที่ท้ายกลุ่ม

        imgUserProfile?.setOnClickListener { startActivity(Intent(this, EditProfile::class.java)) }
        btnNotification?.setOnClickListener { startActivity(Intent(this, notification::class.java)) }
        tvSeeMoreFriend?.setOnClickListener { startActivity(Intent(this, Friend_list::class.java)) }
        tvSeeMoreGroup?.setOnClickListener { startActivity(Intent(this, Group_list::class.java)) }
        btnSplitBill?.setOnClickListener { startActivity(Intent(this, BillSplit::class.java)) }
        btnRecentBill?.setOnClickListener { startActivity(Intent(this, RecentBill::class.java)) }
        btnOwe?.setOnClickListener { startActivity(Intent(this, Owe::class.java)) }
        btnLogout?.setOnClickListener { showLogoutDialog() }
    }

    private fun init() {
        imgUserProfile = findViewById(R.id.imgUserProfile)
        btnNotification = findViewById(R.id.btnNotification)
        tvSeeMoreFriend = findViewById(R.id.tvSeeMoreFriend)
        tvSeeMoreGroup = findViewById(R.id.tvSeeMoreGroup)
        btnSplitBill = findViewById(R.id.btnSplitBill)
        btnRecentBill = findViewById(R.id.btnRecentBill)
        btnOwe = findViewById(R.id.btnOwe)
        btnLogout = findViewById(R.id.btnLogout)
        rvFriends = findViewById(R.id.rvFriends)
        rvGroups = findViewById(R.id.rvGroups)
    }

    // ฟังก์ชันเพื่อน: ไม่มีข้อมูลคนอื่น มีแค่ปุ่มบวกอันเดียว
    private fun setupFriendList() {
        val friendItems = ArrayList<CircleItem>()

        // ใส่แค่ปุ่มบวก (+) อย่างเดียว
        friendItems.add(CircleItem(name = "เพิ่มเพื่อน", isAddButton = true))

        rvFriends?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CircleAdapter(friendItems) { item ->
                if (item.isAddButton) {
                    startActivity(Intent(this@MainActivity, AddFriend::class.java))
                }
            }
        }
    }

    // ฟังก์ชันกลุ่ม: ไม่มีข้อมูลกลุ่ม มีแค่ปุ่มบวกอันเดียว
    private fun setupGroupList() {
        val groupItems = ArrayList<CircleItem>()

        // ใส่แค่ปุ่มบวก (+) สำหรับสร้างกลุ่มใหม่
        groupItems.add(CircleItem(name = "สร้างกลุ่ม", isAddButton = true))

        rvGroups?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CircleAdapter(groupItems) { item ->
                if (item.isAddButton) {
                    startActivity(Intent(this@MainActivity, FindUser::class.java))
                }
            }
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

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            dialog.dismiss()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}