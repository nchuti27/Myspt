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

class Forgotpassword : AppCompatActivity() {
    var edtFemail: EditText? = null
    var btnReset : Button? = null
    var backtxt : TextView? = null

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgotpassword)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        init()

        btnReset!!.setOnClickListener {
            val fMail = edtFemail!!.text.toString().trim()

            if(fMail.isEmpty()){
                edtFemail!!.setError("Please input your email")
            } else {
                // 4. ใช้คำสั่ง Firebase ส่งอีเมลรีเซ็ตรหัสผ่าน
                auth.sendPasswordResetEmail(fMail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // ส่งสำเร็จ
                            Toast.makeText(this, "อีเมลรีเซ็ตรหัสผ่านถูกส่งไปที่ $fMail แล้ว", Toast.LENGTH_LONG).show()
                            // (Optional) พากลับไปหน้า Login ทันที
                            finish()
                        } else {
                            // เกิดข้อผิดพลาด (เช่น ไม่พบอีเมลนี้ในระบบ)
                            Toast.makeText(this, "ผิดพลาด: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        backtxt!!.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun init(){
        edtFemail = findViewById(R.id.etEmail)
        btnReset = findViewById(R.id.btn_Reset)
        backtxt = findViewById(R.id.tvBackToLogin)
    }
}