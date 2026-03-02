package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    var edtRname: EditText? = null
    var edtRUser: EditText? = null
    var edtRPass: EditText? = null
    var edtRConf: EditText? = null
    var btnRegis: Button? = null
    var btnRback: ImageView? = null
    var edtMail : EditText? = null

    private lateinit var auth: FirebaseAuth
    // 2. ประกาศตัวแปร Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        init()

        btnRegis!!.setOnClickListener {
            val rName = edtRname!!.text.toString().trim()
            val rUser = edtRUser!!.text.toString().trim()
            val rPass = edtRPass!!.text.toString().trim()
            val rCfpass = edtRConf!!.text.toString().trim()
            val rMail = edtMail!!.text.toString().trim()

            val checkName = "[a-zA-Z]{3,12}"
            val checkUser = "[a-zA-Z0-9_]{6,12}"
            val checkPass = "^[A-Za-z0-9]{6,12}$"

            when {
                rName.isEmpty() -> edtRname!!.error = "Name is required"
                rUser.isEmpty() -> edtRUser!!.error = "Username is required"
                rMail.isEmpty() -> edtMail!!.error = "Email is required"
                rPass.isEmpty() -> edtRPass!!.error = "Password is required"
                rCfpass.isEmpty() -> edtRConf!!.error = "Please confirm your password"

                !rName.matches("[a-zA-Z]{3,12}".toRegex()) -> edtRname!!.error = "Name must be 3-12 characters"
                !rUser.matches("[a-zA-Z0-9_]{6,12}".toRegex()) -> edtRUser!!.error = "User must be 6-12 characters"
                rPass != rCfpass -> edtRConf!!.error = "Passwords do not match"
                rPass.length < 6 -> edtRPass!!.error = "Password must be at least 6 digits"
                !rPass.matches("^[A-Za-z0-9]{6,12}$".toRegex()) -> edtRPass!!.error = "Password must contain letters and numbers"
                !rMail.endsWith("@gmail.com") -> edtMail!!.error = "Only @gmail.com is allowed"

                else -> {
                    // ส่วนเดิม: เช็ค Username ใน Firestore และสมัคร Auth
                    db.collection("users")
                        .whereEqualTo("username", rUser)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                edtRUser!!.error = "Username is already taken"
                                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                            } else {
                                createUserAccount(rMail, rPass, rName, rUser)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        btnRback!!.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun createUserAccount(email: String, pass: String, name: String, username: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    saveUser(uid, name, username, email)
                } else {
                    val exception = task.exception
                    if (exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        edtMail!!.error = "This email is already registered"
                        Toast.makeText(this, "Email already in use", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Authentication Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    private fun saveUser(uid: String?, name: String, username: String, email: String) {
        if (uid == null) return

        val userProfile = hashMapOf(
            "uid" to uid,
            "name" to name,
            "username" to username,
            "email" to email,
            "qr_code" to "",
            "friends" to arrayListOf<String>()
        )

        db.collection("users").document(uid)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Register & Profile Saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun init() {
        edtRname = findViewById(R.id.name_pt)
        edtRUser = findViewById(R.id.usern_pt)
        edtRPass = findViewById(R.id.pw_pt)
        edtRConf = findViewById(R.id.conf_pt)
        edtMail = findViewById(R.id.Rmail_pt)
        btnRback = findViewById(R.id.back2login)
        btnRegis = findViewById(R.id.regis_btn)
    }
}