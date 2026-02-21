package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WhoPays : AppCompatActivity() {

    private var backButton: ImageButton? = null
    private var btnMenu: ImageView? = null
    private var Ptabfriend: TextView? = null
    private var btnConfirm: Button? = null
    private var isConfirmed: Boolean = false

    // เพิ่มตัวแปรสำหรับ Database และข้อมูลบิล [cite: 2026-02-13]
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var totalAmount: Double = 0.0
    private var billName: String = "New Bill"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_who_pays)

        // เชื่อมต่อ Firebase [cite: 2026-01-18, 2026-02-13]
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // รับข้อมูลจากหน้า BillSplit (ถ้ามี) [cite: 2026-02-13]
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        billName = intent.getStringExtra("BILL_NAME") ?: "New Bill"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupClickListeners()
    }

    private fun init() {
        backButton = findViewById(R.id.backButton)
        btnMenu = findViewById(R.id.btnMenu)
        Ptabfriend = findViewById(R.id.Ptabfriend)
        btnConfirm = findViewById(R.id.btnConfirm)
    }

    private fun setupClickListeners() {
        backButton?.setOnClickListener { finish() }

        btnConfirm?.setOnClickListener {
            saveBillToDatabase() // เรียกฟังก์ชันบันทึกข้อมูล [cite: 2026-02-13]
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

        btnMenu?.setOnClickListener { view -> showMenu(view) }
    }

    // ฟังก์ชันสำหรับบันทึกบิลลง Firestore [cite: 2026-02-13]
    private fun saveBillToDatabase() {
        val myUid = auth.currentUser?.uid ?: return

        val billData = hashMapOf(
            "billName" to billName,
            "totalAmount" to totalAmount,
            "paidBy" to myUid,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "status" to "pending"
        )

        db.collection("bills")
            .add(billData)
            .addOnSuccessListener {
                isConfirmed = true
                Toast.makeText(this, "Bill Saved Successfully!", Toast.LENGTH_SHORT).show()
                // เมื่อบันทึกสำเร็จ สามารถกดไปหน้าถัดไปได้
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

        btnNo?.setOnClickListener { dialog.dismiss() }
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
        btnOk?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}