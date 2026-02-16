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

class Forgotpassword : AppCompatActivity() {
    var edtFemail:  EditText? = null
    var btnReset : Button? = null
    var backtxt : TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgotpassword)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        btnReset!!.setOnClickListener {
            var fMail = edtFemail!!.text.toString()
            if(fMail.isEmpty()){
                edtFemail!!.setError("Please input your email")
            }else{
                Toast.makeText(this,"If this email exists in our system, " +
                            "you will receive instructions to reset your password shortly",
                    Toast.LENGTH_LONG).show()
            }
        }
        backtxt!!.setOnClickListener {
            var intent = Intent(this, Login::class.java)
            startActivity(intent)
        }



    }
    private fun init(){
        edtFemail = findViewById(R.id.etEmail)
        btnReset = findViewById(R.id.btn_Reset)
        backtxt = findViewById(R.id.tvBackToLogin)
    }
}