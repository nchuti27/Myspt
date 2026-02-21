package com.example.myspt

import android.graphics.Color // เพิ่มบรรทัดนี้เพื่อแก้ Error 'Color'
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendOwe : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var rvFriendOwe: RecyclerView? = null
    private var btnBack: ImageButton? = null

    // 1. เพิ่มการประกาศตัวแปรนี้เพื่อแก้ Error 'tvTotalBalance' [cite: 2026-02-21]
    private var tvTotalBalance: TextView? = null

    private val oweList = ArrayList<OweItem>()
    private lateinit var adapter: OweAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_owe)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        calculateNetBalances()
    }

    private fun init() {
        rvFriendOwe = findViewById(R.id.rvFriendOwe)
        btnBack = findViewById(R.id.backButton)

        // 2. ผูกไอดีให้เรียบร้อย (เช็คให้ตรงกับใน activity_friend_owe.xml) [cite: 2026-02-21]
        tvTotalBalance = findViewById(R.id.tvTotalBalance)

        btnBack?.setOnClickListener { finish() }

        adapter = OweAdapter(oweList)
        rvFriendOwe?.layoutManager = LinearLayoutManager(this)
        rvFriendOwe?.adapter = adapter
    }

    private fun calculateNetBalances() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("bills")
            .whereArrayContains("members", myUid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val balances = mutableMapOf<String, Double>()

                snapshots?.forEach { doc ->
                    val paidBy = doc.getString("paidBy") ?: ""
                    val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                    val members = doc.get("members") as? List<String> ?: listOf()
                    val splitAmount = totalAmount / members.size

                    if (paidBy == myUid) {
                        members.forEach { memberUid ->
                            if (memberUid != myUid) {
                                balances[memberUid] = (balances[memberUid] ?: 0.0) + splitAmount
                            }
                        }
                    } else {
                        balances[paidBy] = (balances[paidBy] ?: 0.0) - splitAmount
                    }
                }
                updateUI(balances)
            }
    }

    private fun updateUI(balances: Map<String, Double>) {
        oweList.clear()
        var grandTotal = 0.0

        if (balances.isEmpty()) {
            tvTotalBalance?.text = "0.00 ฿"
            adapter.notifyDataSetChanged()
            return
        }

        db.collection("users").whereIn(com.google.firebase.firestore.FieldPath.documentId(), balances.keys.toList())
            .get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    val amount = balances[doc.id] ?: 0.0
                    oweList.add(OweItem(
                        friendName = doc.getString("name") ?: "Unknown",
                        amount = amount,
                        friendUid = doc.id
                    ))
                    grandTotal += amount
                }

                // 3. ใช้งานตัวแปร tvTotalBalance และ Color ได้อย่างถูกต้องแล้ว [cite: 2026-02-21]
                tvTotalBalance?.text = String.format("%.2f ฿", grandTotal)
                tvTotalBalance?.setTextColor(if (grandTotal >= 0) Color.GREEN else Color.RED)
                adapter.notifyDataSetChanged()
            }
    }
}