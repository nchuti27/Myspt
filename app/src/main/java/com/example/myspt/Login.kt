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

class Login : AppCompatActivity() {
    var btnLogin: Button? = null
    var edtLuser : EditText? = null
    var edtLpass : EditText? = null
    var jointxt : TextView? = null
    var forgotP : TextView? = null
    /*Login
    title_txt *
    user_pt*
    pass_pt*
    forgot_txt
    login_btn *
    join_txt*/
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        btnLogin!!.setOnClickListener {
            val uUsername = edtLuser!!.text.toString().trim()
            val uPass = edtLpass!!.text.toString().trim()

            if (uUsername.isEmpty()) {
                edtLuser!!.setError("Please input your username")
            } else if (uPass.isEmpty()) {
                edtLpass!!.setError("Please input your password")
            } else {
                Toast.makeText(this, "Welcome $uUsername", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

        }
        forgotP?.setOnClickListener {
            val intent = Intent(this, Forgotpassword::class.java)
            startActivity(intent)

        }

        jointxt?.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
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