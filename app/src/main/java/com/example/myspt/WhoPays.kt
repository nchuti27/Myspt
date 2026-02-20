package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WhoPays : AppCompatActivity() {

    private var backButton: ImageButton? = null
    private var btnMenu: ImageView? = null
    private var Ptabfriend: TextView? = null
    private var btnConfirm: Button? = null
    private var isConfirmed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_who_pays)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        backButton?.setOnClickListener {
            finish()
        }
        btnConfirm?.setOnClickListener {
            isConfirmed = true
            Toast.makeText(this, "Payer Confirmed!", Toast.LENGTH_SHORT).show()
        }
        Ptabfriend?.setOnClickListener {
            if (isConfirmed) {
                val intent = Intent(this, FriendOwe::class.java)
                startActivity(intent)
                finish()
            } else {
                showPleaseConfirmDialog()
            }
        }

        btnMenu?.setOnClickListener { view ->
            showMenu(view)
        }
    }

    private fun init() {
        backButton = findViewById(R.id.backButton)
        btnMenu = findViewById(R.id.btnMenu)
        Ptabfriend = findViewById(R.id.Ptabfriend)
        btnConfirm = findViewById(R.id.btnConfirm)
    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_items -> {
                    startActivity(Intent(this, BillSplit::class.java))
                    true
                }
                R.id.action_leave_group -> {
                    showLeaveDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showLeaveDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_leave_group)

        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvMessage)

        tvMessage?.text = "Are you sure you want to\n leave this group?"

        btnNo?.setOnClickListener {
            dialog.dismiss()
        }

        btnYes?.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "You have left the group.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
    private fun showPleaseConfirmDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_confirm_payer)

        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        btnOk?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}