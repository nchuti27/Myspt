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

    // 🌟 1. แก้ไขการประกาศตัวแปรให้ตรงกับ XML ใหม่
    private var rvOweYou: RecyclerView? = null
    private var rvYouOwe: RecyclerView? = null
    private var btnBack: ImageView? = null
    private var tvTotalBalance: TextView? = null
    private var tabItems: TextView? = null
    private var btnMenu: ImageView? = null

    // 🌟 2. แยก List และ Adapter เป็น 2 ชุดตามหมวดหมู่
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
        // 🌟 3. ผูก ID ใหม่ (rvOweYou และ rvYouOwe) แทนที่ตัวเก่าที่หายไป
        rvOweYou = findViewById(R.id.rvOweYou)
        rvYouOwe = findViewById(R.id.rvYouOwe)

        btnBack = findViewById(R.id.backButton)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        btnMenu = findViewById(R.id.btnMenu)
        tabItems = findViewById(R.id.tabItems)

        // 🌟 4. ตั้งค่า Adapter ชุดที่ 1: ฝั่งเจ้าหนี้ (Owe You)
        adapterOweYou = OweAdapter(oweYouList)
        rvOweYou?.layoutManager = LinearLayoutManager(this)
        rvOweYou?.adapter = adapterOweYou

        // 🌟 5. ตั้งค่า Adapter ชุดที่ 2: ฝั่งลูกหนี้ (You Owe)
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

        // ✅ ยอดรวมทั้งบิล = sum ของทุกคนที่ต้องจ่าย
        val grandTotal = splitResult.values.sum()

        for (uid in memberNames.keys) {
            val share = splitResult[uid] ?: 0.0
            val paid = payersMap[uid] ?: 0.0
            val netOwed = share - paid

            val item = OweItem(memberNames[uid] ?: "Unknown", netOwed, uid)

            when {
                netOwed > 0.01  -> youOweList.add(item)
                netOwed < -0.01 -> oweYouList.add(item)
                else            -> youOweList.add(OweItem(memberNames[uid] ?: "Unknown", 0.0, uid))
            }
        }

        // ✅ แสดงยอดรวมทั้งบิล ไม่ใช่แค่ยอดค้าง
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