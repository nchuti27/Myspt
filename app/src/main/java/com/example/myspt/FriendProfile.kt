package com.example.myspt

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import java.io.OutputStream

class FriendProfile : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var ivQrCode: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var btnSaveQr: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        initView()

        // 1. รับข้อมูลจาก Intent ที่ส่งมาจาก FriendAdapter
        val friendUid = intent.getStringExtra("FRIEND_UID")
        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Unknown"
        val friendUsername = intent.getStringExtra("FRIEND_USERNAME") ?: ""

        // 2. แสดงข้อมูลเบื้องต้นทันที
        tvFullName.text = friendName
        tvUsername.text = if (friendUsername.startsWith("@")) friendUsername else "@$friendUsername"

        // 3. ดึงข้อมูลเชิงลึก (รูปภาพ) จาก Firestore
        if (friendUid != null) {
            loadFriendDataFromFirestore(friendUid)
        }

        // 4. ปุ่มบันทึก QR Code
        btnSaveQr.setOnClickListener {
            saveQrToGallery(friendName)
        }
    }

    private fun initView() {
        ivProfile = findViewById(R.id.ivProfile)
        ivQrCode = findViewById(R.id.ivQrCode)
        tvFullName = findViewById(R.id.tvFriendFullName)
        tvUsername = findViewById(R.id.tvFriendUsername)
        btnSaveQr = findViewById(R.id.btnSaveQr)
        val btnBack = findViewById<ImageButton>(R.id.backButton)

        btnBack.setOnClickListener { finish() }
    }

    private fun loadFriendDataFromFirestore(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // สมมติว่าชื่อฟิลด์ใน Firestore คือ profileImageUrl และ qrImageUrl
                    val profileUrl = doc.getString("profileImageUrl")
                    val qrUrl = doc.getString("qrImageUrl")

                    // ใช้ Coil โหลดรูป (ถ้ามี URL)
                    if (!profileUrl.isNullOrEmpty()) {
                        ivProfile.load(profileUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_background) // รูปตอนกำลังโหลด
                        }
                    }

                    if (!qrUrl.isNullOrEmpty()) {
                        ivQrCode.load(qrUrl) {
                            crossfade(true)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveQrToGallery(name: String) {
        val bitmap = (ivQrCode.drawable as? BitmapDrawable)?.bitmap
        if (bitmap == null) {
            Toast.makeText(this, "QR Code not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val filename = "QR_PromptPay_${name}_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MySPT_QR")
            }
        }

        val imageUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            fos = contentResolver.openOutputStream(it)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos!!)
            fos?.close()
            Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }
}