package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfile : AppCompatActivity() {

    private lateinit var etUser: EditText
    private lateinit var etUName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnQr: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnChangePhoto: FloatingActionButton
    private lateinit var savetxt: TextView
    private lateinit var ivProfilePhoto: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ✅ เปิด Gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            uploadImageToStorage(uri)
        }
    }

    // ✅ เปิด Camera
    private var cameraImageUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uploadImageToStorage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        etUser = findViewById(R.id.etUser)
        etUName = findViewById(R.id.etUName)
        etEmail = findViewById(R.id.etEmail)
        btnQr = findViewById(R.id.btnQr)
        savetxt = findViewById(R.id.savetxt)
        btnBack = findViewById(R.id.btnBack2Main)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        // ✅ ใช้ ID ตรงกับ XML
        ivProfilePhoto = findViewById(R.id.imgProfile) // ✅ ต้องมีใน XML

        loadUserData()

        btnBack.setOnClickListener { finish() }
        savetxt.setOnClickListener { saveUserProfile() }
        btnChangePhoto.setOnClickListener { showPhotoOptionsDialog() }
        btnQr.setOnClickListener {
            startActivity(Intent(this, UploadQrActivity::class.java))
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etUser.setText(document.getString("username"))
                    etUName.setText(document.getString("name"))
                    etEmail.setText(document.getString("email"))

                    // ✅ โหลดรูปโปรไฟล์
                    val photoUrl = document.getString("profileUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).circleCrop().into(ivProfilePhoto)
                    }
                }
            }
    }

    private fun showPhotoOptionsDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialogphoto, findViewById(android.R.id.content), false)

        view.findViewById<LinearLayout>(R.id.lnPhotoLibrary).setOnClickListener {
            // ✅ เปิด Gallery จริงๆ
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.lnTakePhoto).setOnClickListener {
            // ✅ เปิด Camera จริงๆ
            val photoFile = java.io.File.createTempFile("photo_", ".jpg", cacheDir)
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                photoFile
            )
            takePictureLauncher.launch(cameraImageUri!!)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun uploadImageToStorage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // ✅ บันทึก URL ลง Firestore
                    db.collection("users").document(uid)
                        .update("profileUrl", downloadUrl.toString())
                        .addOnSuccessListener {
                            Glide.with(this).load(downloadUrl).circleCrop().into(ivProfilePhoto)
                            Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newName = etUName.text.toString().trim()

        if (newName.isEmpty()) {
            etUName.error = "Name cannot be empty"
            return
        }

        db.collection("users").document(uid).update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}