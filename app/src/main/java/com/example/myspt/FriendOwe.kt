package com.example.myspt

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class FriendOwe : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    private var rvOweYou: RecyclerView? = null
    private var rvYouOwe: RecyclerView? = null
    private var btnBack: ImageView? = null
    private var tvTotalBalance: TextView? = null
    private var tabItems: TextView? = null
    private var btnMenu: ImageView? = null


    private val oweYouList = ArrayList<OweItem>()
    private val youOweList = ArrayList<OweItem>()
    private lateinit var adapterOweYou: OweAdapter
    private lateinit var adapterYouOwe: OweAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_owe)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        setupDataFromIntent()
    }

    private fun init() {

        rvOweYou = findViewById(R.id.rvOweYou)
        rvYouOwe = findViewById(R.id.rvYouOwe)

        btnBack = findViewById(R.id.backButton)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        btnMenu = findViewById(R.id.btnMenu)
        tabItems = findViewById(R.id.tabItems)


        adapterOweYou = OweAdapter(oweYouList)
        rvOweYou?.layoutManager = LinearLayoutManager(this)
        rvOweYou?.adapter = adapterOweYou


        adapterYouOwe = OweAdapter(youOweList)
        rvYouOwe?.layoutManager = LinearLayoutManager(this)
        rvYouOwe?.adapter = adapterYouOwe

        tabItems?.setOnClickListener { finish() }
        btnBack?.setOnClickListener { finish() }
        btnMenu?.setOnClickListener { view -> showMenu(view) }
    }

    private fun setupDataFromIntent() {
        val splitResult = intent.getSerializableExtra("SPLIT_RESULT") as? HashMap<String, Double> ?: hashMapOf()
        val memberNames = intent.getSerializableExtra("MEMBER_NAMES") as? HashMap<String, String> ?: hashMapOf()
        val payersMap = intent.getSerializableExtra("PAYERS_MAP") as? HashMap<String, Double> ?: hashMapOf()

        oweYouList.clear()
        youOweList.clear()

        val grandTotal = splitResult.values.sum()


        val creditors = mutableListOf<Pair<String, Double>>()
        val debtors = mutableListOf<Pair<String, Double>>()

        for (uid in memberNames.keys) {
            val share = splitResult[uid] ?: 0.0
            val paid = payersMap[uid] ?: 0.0
            val net = paid - share

            when {
                net > 0.01  -> creditors.add(Pair(memberNames[uid] ?: "Unknown", net))
                net < -0.01 -> debtors.add(Pair(memberNames[uid] ?: "Unknown", Math.abs(net)))
            }
        }


        for (debtor in debtors) {
            for (creditor in creditors) {
                oweYouList.add(OweItem("${debtor.first} pays ${creditor.first}", minOf(debtor.second, creditor.second), ""))
            }
        }

        tvTotalBalance?.text = String.format("%.2f ฿", grandTotal)
        tvTotalBalance?.setTextColor(Color.parseColor("#4CAF50"))

        adapterOweYou.notifyDataSetChanged()
        adapterYouOwe.notifyDataSetChanged()
    }
    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_group_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_items -> { finish(); true }
                R.id.action_leave_group -> { showLeaveDialog(); true }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showLeaveDialog() {
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_leave_group)
        dialog.findViewById<Button>(R.id.btnNo)?.setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnYes)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        dialog.show()
    }
}