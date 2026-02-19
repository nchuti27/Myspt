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

                auth.sendPasswordResetEmail(fMail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            Toast.makeText(this, "The password reset email has been sent to $fMail ", Toast.LENGTH_LONG).show()

                            finish()
                        } else {

                            Toast.makeText(this, "Fail: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
        edtFemail = findViewById(R.id.etName)
        btnReset = findViewById(R.id.btn_Reset)
        backtxt = findViewById(R.id.tvBackToLogin)
    }
}