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

        // ดึงฝั่ง Owe You (เราเป็นเจ้าหนี้)
        db.collection("debts").whereEqualTo("creditorId", myUid).whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                oweYouList.clear()
                snapshots?.forEach { doc ->
                    val debt = doc.toObject(Debt::class.java)
                    debt.debtId = doc.id
                    oweYouList.add(debt)
                }
                adapterOweYou.updateData(oweYouList)
            }

        // ดึงฝั่ง You Owe (เราเป็นลูกหนี้)
        db.collection("debts").whereEqualTo("friendId", myUid).whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                youOweList.clear()
                snapshots?.forEach { doc ->
                    val debt = doc.toObject(Debt::class.java)
                    debt.debtId = doc.id
                    youOweList.add(debt)
                }
                adapterYouOwe.updateData(youOweList)
            }
    }

    private fun markDebtAsPaid(debt: Debt) {
        db.collection("debts").document(debt.debtId).update("status", "paid")
            .addOnSuccessListener { Toast.makeText(this, "อัปเดตสถานะว่า ${debt.name} จ่ายแล้ว!", Toast.LENGTH_SHORT).show() }
    }
}