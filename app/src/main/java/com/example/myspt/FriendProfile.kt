package com.example.myspt

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View // 🌟 ต้องมีตัวนี้เพื่อใช้ View.GONE
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
    private lateinit var tvQrLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_profile)

        initView()

        // 1. รับข้อมูลจาก Intent
        val friendUid = intent.getStringExtra("FRIEND_UID")
        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Unknown"
        val friendUsername = intent.getStringExtra("FRIEND_USERNAME") ?: ""


        val isFriend = intent.getBooleanExtra("IS_FRIEND", false)

        // 3. แสดงข้อมูลเบื้องต้น
        tvFullName.text = friendName
        tvUsername.text = if (friendUsername.startsWith("@")) friendUsername else "@$friendUsername"

        // แสดง QR Code
        if (!isFriend) {
            ivQrCode.visibility = View.GONE
            btnSaveQr.visibility = View.GONE
            tvQrLabel.text = "Connect with friend to see QR Code"
            Toast.makeText(this, "Add friend to see QR Code", Toast.LENGTH_SHORT).show()
        } else {
            ivQrCode.visibility = View.VISIBLE
            btnSaveQr.visibility = View.VISIBLE
            tvQrLabel.text = "PromptPay QR Code"
        }

        if (friendUid != null) {
            loadFriendDataFromFirestore(friendUid, isFriend)
        }

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
        tvQrLabel = findViewById(R.id.tvQrLabel)

        val btnBack = findViewById<ImageButton>(R.id.backButton)
        btnBack.setOnClickListener { finish() }
    }

    private fun loadFriendDataFromFirestore(uid: String, isFriend: Boolean) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val username = doc.getString("username") ?: ""
                    if (username.isNotEmpty()) {
                        tvUsername.text = if (username.startsWith("@")) username else "@$username"
                    }

                    val profileUrl = doc.getString("profileImageUrl")
                    val qrUrl = doc.getString("qrImageUrl")

                    if (!profileUrl.isNullOrEmpty()) {
                        ivProfile.load(profileUrl) { crossfade(true) }
                    }

                    if (isFriend && !qrUrl.isNullOrEmpty()) {
                        ivQrCode.load(qrUrl) { crossfade(true) }
                    }
                }
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