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

class Register : AppCompatActivity() {
    var edtRname: EditText? = null
    var edtRUser: EditText? = null
    var edtRPass: EditText? = null
    var edtRConf: EditText? = null
    var btnRegis: Button? = null
    var btnRback: ImageView? = null
    var edtMail : EditText? = null
    /*Register
    name_pt
    username_pt
    pw_pt
    conf_pt
    regis_btn
    back2login*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        btnRegis!!.setOnClickListener {
            val rName = edtRname!!.text
            val rUser = edtRUser!!.text
            val rPass = edtRPass!!.text.toString().trim()
            val rCfpass = edtRConf!!.text.toString().trim()
            val rMail = edtMail!!.text
            val checkName = "[a-zA-Z]{3,12}"
            val checkUser = "[a-zA-Z0-9_]{6,12}"
            val checkPass = "[a-zA-Z]+[0-9]+[a-zA-Z0-9]*"

            if(rUser.matches(checkUser.toRegex())==false){
                edtRUser!!.setError("Pattern not match ")
            }
            if(rPass.isEmpty())
                edtRConf!!.setError("Password and Confirm not qual")
            else if (rPass.length < 6){
                edtRConf!!.setError("Password length < 6 digits")

            }else if(!(rPass.matches(checkPass.toRegex()))) {
                edtRPass!!.setError("Password pattern not match")
            }

            if(rMail.isEmpty())
                edtMail!!.setError("Email must @gmail.com")

            else if (!(rMail.endsWith("@gmail.com"))){
                edtMail!!.setError("Email must @gmail.com")
            }else{
                Toast.makeText(this,"Register Success",Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            btnRback!!.setOnClickListener {
                Toast.makeText(this,"Thank you",Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }


        }
    }
    private fun init(){
        edtRname = findViewById(R.id.name_pt)
        edtRUser = findViewById(R.id.usern_pt)
        edtRPass= findViewById(R.id.pw_pt)
        edtRConf = findViewById(R.id.conf_pt)

        btnRback = findViewById(R.id.back2login)
        btnRegis = findViewById(R.id.regis_btn)
    }
}