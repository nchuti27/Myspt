package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditProfile : AppCompatActivity() {
    var btnBack3: ImageButton? = null
    var btnLogout: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        btnBack3?.setOnClickListener {
            finish()
        }
        btnLogout?.setOnClickListener {
            showLogoutDialog() // เรียกฟังก์ชันแสดง Popup
        }
    }
        private fun init() {
            btnBack3 = findViewById(R.id.btnBack3)
            btnLogout = findViewById(R.id.btnLogout)
        }
    // ฟังก์ชันแสดง Dialog ตามแบบที่คุณต้องการ
    private fun showLogoutDialog() {
        // 1. ดึง Layout dialog_logout มาแสดง
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null)

        // 2. สร้าง Dialog
        val builder = AlertDialog.Builder(this)
        builder.setView(view)

        val dialog = builder.create()

        // 3. ทำให้พื้นหลังโปร่งใส (เพื่อให้เห็นมุมโค้งของ CardView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 4. เชื่อมปุ่มภายใน Dialog (btnCancel, btnConfirm จาก dialog_logout.xml)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        // ปุ่ม No (ยกเลิก)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // ปุ่ม Yes (ยืนยัน)
        btnConfirm.setOnClickListener {
            dialog.dismiss()

            // แสดงข้อความแจ้งเตือน
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()

            // ย้ายไปหน้า Login (ตรวจสอบชื่อไฟล์ Login ของคุณว่าชื่อ Login หรือ LoginActivity)
            val intent = Intent(this, Login::class.java)

            // ล้างประวัติหน้าเก่าทั้งหมด เพื่อไม่ให้กด Back กลับมาได้
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        dialog.show()
    }
}