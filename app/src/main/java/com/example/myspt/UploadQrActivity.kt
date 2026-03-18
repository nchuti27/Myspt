package com.example.myspt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UploadQrActivity : AppCompatActivity() {

    private lateinit var ivQrPreview: ImageView
    private lateinit var btnSelectQr: Button
    private lateinit var btnUploadQr: Button
    private lateinit var btnBack: ImageButton

    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()


    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            ivQrPreview.setImageURI(uri)
            btnUploadQr.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_qr)

        initView()


        btnSelectQr.setOnClickListener {
            getImage.launch("image/*")
        }


        btnUploadQr.setOnClickListener {
            uploadImageToFirebase()
        }


        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initView() {
        ivQrPreview = findViewById(R.id.ivQrPreview)
        btnSelectQr = findViewById(R.id.btnSelectQr)
        btnUploadQr = findViewById(R.id.btnUploadQr)
        btnBack = findViewById(R.id.btnBackQR)
    }

    private fun uploadImageToFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("payment_qrs/$uid.jpg")

        imageUri?.let { uri ->
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            btnUploadQr.isEnabled = false

            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveUrlToFirestore(downloadUrl.toString())
                    }
                }
                .addOnFailureListener { e ->
                    btnUploadQr.isEnabled = true
                    Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUrlToFirestore(url: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .update("payment_qr", url)
            .addOnSuccessListener {
                Toast.makeText(this, "QR Code Saved Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnUploadQr.isEnabled = true
                Toast.makeText(this, "Failed to save link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}