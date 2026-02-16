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

class Register : AppCompatActivity() {
    var edtRname: EditText? = null
    var edtRUser: EditText? = null
    var edtRPass: EditText? = null
    var edtRConf: EditText? = null
    var btnRegis: Button? = null
    var btnRback: ImageView? = null
    var edtMail : EditText? = null

    private lateinit var auth: FirebaseAuth

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
        init()

        btnRegis!!.setOnClickListener {
            val rName = edtRname!!.text.toString().trim()
            val rUser = edtRUser!!.text.toString().trim()
            val rPass = edtRPass!!.text.toString().trim()
            val rCfpass = edtRConf!!.text.toString().trim()
            val rMail = edtMail!!.text.toString().trim()

            val checkName = "[a-zA-Z]{3,12}"
            val checkUser = "[a-zA-Z0-9_]{6,12}"
            val checkPass = "[a-zA-Z]+[0-9]+[a-zA-Z0-9]*"


            if (!rName.matches(checkName.toRegex())) {
                edtRname!!.setError("Name must be 3-12 characters")
            }
            else if (!rUser.matches(checkUser.toRegex())) {
                edtRUser!!.setError("User must be 6-12 characters (A-Z, 0-9)")
            }
            else if (rPass.isEmpty() || rPass != rCfpass) {
                edtRConf!!.setError("Passwords do not match")
            }
            else if (rPass.length < 6) {
                edtRConf!!.setError("Password must be at least 6 digits")
            }
            else if (!rPass.matches(checkPass.toRegex())) {
                edtRPass!!.setError("Password must contain letters and numbers")
            }
            else if (rMail.isEmpty() || !rMail.endsWith("@gmail.com")) {
                edtMail!!.setError("Only @gmail.com is allowed")
            }
            else {

                auth.createUserWithEmailAndPassword(rMail, rPass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Register Success", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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