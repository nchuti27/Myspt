package com.example.myspt

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private var imgUserProfile: ImageView? = null
    private var btnNotification: ImageView? = null
    private var tvSeeMoreFriend: TextView? = null
    private var tvSeeMoreGroup: TextView? = null
    private var btnSplitBill: LinearLayout? = null
    private var btnRecentBill: LinearLayout? = null
    private var btnOwe: LinearLayout? = null
    private var btnLogout: ImageView? = null

    private var rvFriends: RecyclerView? = null
    private var rvGroups: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        init()
        setupFriendList()
        setupGroupList()


        imgUserProfile?.setOnClickListener { startActivity(Intent(this, EditProfile::class.java)) }
        btnNotification?.setOnClickListener { startActivity(Intent(this, notification::class.java)) }
        tvSeeMoreFriend?.setOnClickListener { startActivity(Intent(this, Friend_list::class.java)) }
        tvSeeMoreGroup?.setOnClickListener { startActivity(Intent(this, Group_list::class.java)) }
        btnSplitBill?.setOnClickListener { startActivity(Intent(this, BillSplit::class.java)) }
        btnRecentBill?.setOnClickListener { startActivity(Intent(this, BillDetail::class.java)) }
        btnOwe?.setOnClickListener { startActivity(Intent(this, Owe::class.java)) }
        btnLogout?.setOnClickListener { showLogoutDialog() }
    }

    private fun init() {
        imgUserProfile = findViewById(R.id.imgUserProfile)
        btnNotification = findViewById(R.id.btnNotification)
        tvSeeMoreFriend = findViewById(R.id.tvSeeMoreFriend)
        tvSeeMoreGroup = findViewById(R.id.tvSeeMoreGroup)
        btnSplitBill = findViewById(R.id.btnSplitBill)
        btnRecentBill = findViewById(R.id.btnRecentBill)
        btnOwe = findViewById(R.id.btnOwe)
        btnLogout = findViewById(R.id.btnLogout)
        rvFriends = findViewById(R.id.rvFriends)
        rvGroups = findViewById(R.id.rvGroups)
    }


    private fun setupFriendList() {
        val db = FirebaseFirestore.getInstance()
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val friendItems = ArrayList<CircleItem>()

        if (myUid == null) return


        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val friendUids = document.get("friends") as? List<String> ?: listOf()

            if (friendUids.isEmpty()) {

                friendItems.add(CircleItem(name = "Add Friend", isAddButton = true))
                updateFriendAdapter(friendItems)
            } else {

                var count = 0
                for (fUid in friendUids) {
                    db.collection("users").document(fUid).get().addOnSuccessListener { fDoc ->
                        val name = fDoc.getString("name") ?: "Unknown"
                        friendItems.add(CircleItem(id = fUid, name = name))
                        count++


                        if (count == friendUids.size) {
                            friendItems.add(CircleItem(name = "Add Friend", isAddButton = true))
                            updateFriendAdapter(friendItems)
                        }
                    }
                }
            }
        }.addOnFailureListener {

            friendItems.add(CircleItem(name = "Add Friend", isAddButton = true))
            updateFriendAdapter(friendItems)
        }
    }


    private fun updateFriendAdapter(items: List<CircleItem>) {
        rvFriends?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CircleAdapter(items) { item ->
                if (item.isAddButton) {
                    startActivity(Intent(this@MainActivity, AddFriend::class.java))
                } else {
                    Toast.makeText(this@MainActivity, "Friend: ${item.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setupGroupList() {
        val db = FirebaseFirestore.getInstance()
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val groupItems = ArrayList<CircleItem>()

        if (myUid == null) return

        db.collection("users").document(myUid).get().addOnSuccessListener { document ->
            val groupIds = document.get("groups") as? List<String> ?: listOf()

            if (groupIds.isEmpty()) {
                groupItems.add(CircleItem(name = "Create group", isAddButton = true))
                updateGroupAdapter(groupItems)
            } else {
                var count = 0
                for (gId in groupIds) {
                    db.collection("groups").document(gId).get().addOnSuccessListener { gDoc ->
                        val name = gDoc.getString("groupName") ?: "Unknown Group"
                        groupItems.add(CircleItem(id = gId, name = name))
                        count++

                        if (count == groupIds.size) {
                            groupItems.add(CircleItem(name = "Create group", isAddButton = true))
                            updateGroupAdapter(groupItems)
                        }
                    }
                }
            }
        }
    }

    private fun updateGroupAdapter(items: List<CircleItem>) {
        rvGroups?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)

            adapter = HomeGroupAdapter(items) { item ->
                if (item.isAddButton) {

                    val intent = Intent(this@MainActivity, CreateGroupActivity::class.java)
                    startActivity(intent)
                } else {

                    Toast.makeText(this@MainActivity, "Group: ${item.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLogoutDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            dialog.dismiss()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}