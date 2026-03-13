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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Owe : AppCompatActivity() {

    private var btnBack: ImageView? = null
    private lateinit var rvOweYou: RecyclerView
    private lateinit var rvYouOwe: RecyclerView

    private lateinit var adapterOweYou: DebtAdapter
    private lateinit var adapterYouOwe: DebtYouOweAdapter

    private val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private var oweYouList = mutableListOf<Debt>()
    private var youOweList = mutableListOf<Debt>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_owe)

        init()
        setupRecyclerViews()
        fetchDebtsFromFirebase()

        btnBack?.setOnClickListener { finish() }
    }

    private fun init() {
        btnBack = findViewById(R.id.backButton)
        rvOweYou = findViewById(R.id.rvOweYou)
        rvYouOwe = findViewById(R.id.rvYouOwe)
    }

    private fun setupRecyclerViews() {
        adapterOweYou = DebtAdapter(oweYouList) { selectedDebt -> markDebtAsPaid(selectedDebt) }
        rvOweYou.layoutManager = LinearLayoutManager(this)
        rvOweYou.adapter = adapterOweYou

        adapterYouOwe = DebtYouOweAdapter(youOweList)
        rvYouOwe.layoutManager = LinearLayoutManager(this)
        rvYouOwe.adapter = adapterYouOwe
    }

    private fun fetchDebtsFromFirebase() {
        if (myUid.isEmpty()) return

        // 1. ดึงฝั่ง Owe You (คนอื่นค้างเรา)
        db.collection("debts")
            .whereEqualTo("creditorId", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                val list = mutableListOf<Debt>()
                snapshots?.forEach { doc ->
                    val debt = doc.toObject(Debt::class.java)
                    debt.debtId = doc.id // 🌟 สำคัญ: ต้องเก็บ ID ไว้ใช้ตอนกดจ่ายเงิน
                    list.add(debt)
                }
                oweYouList = list
                adapterOweYou.updateData(oweYouList)
            }

        // 2. ดึงฝั่ง You Owe (เราค้างคนอื่น)
        db.collection("debts")
            .whereEqualTo("friendId", myUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                val list = mutableListOf<Debt>()
                snapshots?.forEach { doc ->
                    val debt = doc.toObject(Debt::class.java)
                    debt.debtId = doc.id
                    list.add(debt)
                }
                youOweList = list
                adapterYouOwe.updateData(youOweList)
            }
    }


    private fun markDebtAsPaid(debt: Debt) {
        val batch = db.batch()
        val debtRef = db.collection("debts").document(debt.debtId)
        val notiRef = db.collection("notifications").document()

        batch.update(debtRef, "status", "paid")

        val notiData = hashMapOf(
            "to_uid" to debt.friendId,
            "from_uid" to myUid,
            "from_name" to debt.name,
            "type" to "PAYMENT_RECEIVED",
            "message" to "Received payment: ฿${String.format("%.2f", debt.amount)} from ${debt.name}",
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        batch.set(notiRef, notiData)

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
        }
    }
}