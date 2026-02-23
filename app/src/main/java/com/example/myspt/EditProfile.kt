package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfile : AppCompatActivity() {

    private lateinit var etUser: EditText
    private lateinit var etUName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnQr: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnChangePhoto: FloatingActionButton // เพิ่มปุ่มกล้อง

    private lateinit var savetxt: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        // 1. ผูก View กับ ID ใน XML
        etUser = findViewById(R.id.etUser)
        etUName = findViewById(R.id.etUName)
        etEmail = findViewById(R.id.etEmail)
        btnQr = findViewById(R.id.btnQr)
        savetxt = findViewById(R.id.savetxt)
        btnBack = findViewById(R.id.btnBack2Main)
        btnChangePhoto = findViewById(R.id.btnChangePhoto) //

        // 2. ดึงข้อมูล User มาแสดง
        loadUserData()

        // 3. ตั้งค่าการกดปุ่ม
        btnBack.setOnClickListener { finish() }

        savetxt.setOnClickListener { saveUserProfile() }

        // เมื่อกดปุ่มกล้อง ให้เด้งเมนูเลือกรูปภาพ
        btnChangePhoto.setOnClickListener {
            showPhotoOptionsDialog()
        }
        btnQr.setOnClickListener { // ลบ ? ออก
            val intent = Intent(this, UploadQrActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etUser.setText(document.getString("username"))
                    etUName.setText(document.getString("name"))
                    etEmail.setText(document.getString("email"))
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPhotoOptionsDialog() {
        val dialog = BottomSheetDialog(this)
        // อย่าลืมสร้างไฟล์ layout_dialog_photo.xml ไว้ในโฟลเดอร์ layout นะครับ
        // ใช้ findViewById(android.R.id.content) เป็น root แทน null ครับ [cite: 2026-02-23]
        val view = layoutInflater.inflate(R.layout.dialogphoto, findViewById(android.R.id.content), false)

        val lnPhotoLibrary = view.findViewById<LinearLayout>(R.id.lnPhotoLibrary)
        val lnTakePhoto = view.findViewById<LinearLayout>(R.id.lnTakePhoto)

        lnPhotoLibrary.setOnClickListener {
            // TODO: โค้ดสำหรับเปิด Gallery เพื่อเลือกรูป
            Toast.makeText(this, "Opening Photo Library...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        lnTakePhoto.setOnClickListener {
            // TODO: โค้ดสำหรับเปิดกล้องถ่ายรูป
            Toast.makeText(this, "Opening Camera...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newName = etUName.text.toString().trim()

        if (newName.isEmpty()) {
            etUName.error = "Name cannot be empty"
            return
        }

        // อัปเดตเฉพาะชื่อใน Firestore
        db.collection("users").document(uid).update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}