package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var edtLuser: EditText
    private lateinit var edtLpass: EditText
    private lateinit var jointxt: TextView
    private lateinit var forgotP: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        auth = FirebaseAuth.getInstance()

        // เช็ค Login ไว้แล้ว ให้ไปหน้าหลักเลย
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        init()

        btnLogin.setOnClickListener {
            val uEmail = edtLuser.text.toString().trim()
            val uPass = edtLpass.text.toString().trim()

            if (uEmail.isEmpty()) {
                edtLuser.error = "Please input your email"
            } else if (uPass.isEmpty()) {
                edtLpass.error = "Please input your password"
            } else {
                auth.signInWithEmailAndPassword(uEmail, uPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Welcome to Split Next", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        forgotP.setOnClickListener {
            startActivity(Intent(this, Forgotpassword::class.java))
        }

        jointxt.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }

    private fun init() {
        btnLogin = findViewById(R.id.login_btn)
        edtLpass = findViewById(R.id.pass_pt)
        edtLuser = findViewById(R.id.user_pt)
        forgotP = findViewById(R.id.forgot_txt)
        jointxt = findViewById(R.id.join_txt)
    }
}