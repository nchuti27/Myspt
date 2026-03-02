package com.example.myspt

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth // 🌟 เพิ่ม Import นี้
import com.google.firebase.firestore.FirebaseFirestore

class Owe : AppCompatActivity() {

    private var btnBack: ImageView? = null

    // ประกาศตัวแปรสำหรับ 2 ฝั่ง
    private lateinit var rvOweYou: RecyclerView
    private lateinit var rvYouOwe: RecyclerView

    private lateinit var adapterOweYou: DebtAdapter
    private lateinit var adapterYouOwe: DebtYouOweAdapter // 🌟 Adapter สำหรับหน้าที่เอา CheckBox ออก

    private val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: "" // 🌟 ดึง UID ของตัวเราเอง

    // แยก List ข้อมูลเป็น 2 ชุด
    private var oweYouList = mutableListOf<Debt>()
    private var youOweList = mutableListOf<Debt>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_owe)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupRecyclerViews()
        fetchDebtsFromFirebase() // ดึงข้อมูลมาแสดงทั้ง 2 ฝั่ง

        btnBack?.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        rvOweYou = findViewById(R.id.rvOweYou)
        rvYouOwe = findViewById(R.id.rvYouOwe) // 🌟 ผูก ID ให้กับส่วน You Owe
    }

    private fun setupRecyclerViews() {
        // 1. ตั้งค่า Owe You (เพื่อนติดเรา - มีปุ่ม CheckBox)
        adapterOweYou = DebtAdapter(oweYouList) { selectedDebt ->
            markDebtAsPaid(selectedDebt)
        }
        rvOweYou.layoutManager = LinearLayoutManager(this)
        rvOweYou.adapter = adapterOweYou

        // 2. ตั้งค่า You Owe (เราติดเพื่อน - ไม่มี CheckBox)
        adapterYouOwe = DebtYouOweAdapter(youOweList)
        rvYouOwe.layoutManager = LinearLayoutManager(this)
        rvYouOwe.adapter = adapterYouOwe
    }

    private fun fetchDebtsFromFirebase() {
        if (myUid.isEmpty()) return // ถ้าหา UID ไม่เจอให้หยุดทำงานป้องกันแอปเด้ง

        // 🌟 ดึงข้อมูลฝั่ง Owe You (เพื่อนติดเรา แปลว่าเราคือ "เจ้าหนี้" creditorId)
        db.collection("debts")
            .whereEqualTo("creditorId", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                oweYouList.clear()
                for (doc in snapshots!!) {
                    val debt = doc.toObject(Debt::class.java)
                    debt.debtId = doc.id
                    oweYouList.add(debt)
                }
                adapterOweYou.updateData(oweYouList)
            }

        // 🌟 ดึงข้อมูลฝั่ง You Owe (เราติดเพื่อน แปลว่าเราคือ "ลูกหนี้" friendId)
        db.collection("debts")
            .whereEqualTo("friendId", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                youOweList.clear()
                for (doc in snapshots!!) {
                    val debt = doc.toObject(Debt::class.java)
                    debt.debtId = doc.id
                    youOweList.add(debt)
                }
                adapterYouOwe.updateData(youOweList)
            }
    }

    private fun markDebtAsPaid(debt: Debt) {
        db.collection("debts").document(debt.debtId)
            .update("status", "paid")
            .addOnSuccessListener {
                Toast.makeText(this, "อัปเดตสถานะว่า ${debt.name} จ่ายแล้ว!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการอัปเดต", Toast.LENGTH_SHORT).show()
            }
    }
}