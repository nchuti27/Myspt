package com.example.myspt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class SelectFriend : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var rvSelectFriends: RecyclerView? = null
    private var btnBack: ImageButton? = null
    private var tvNext: TextView? = null
    private var etSearch: EditText? = null

    private var allFriendsList = ArrayList<FriendItem>()
    private var friendList = ArrayList<FriendItem>()
    private lateinit var adapter: SelectFriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_friend)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        init()
        setupRecyclerView()
        loadFriendsRealtime()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvNext = findViewById(R.id.tvNext)
        rvSelectFriends = findViewById(R.id.rvSelectFriends)
        etSearch = findViewById(R.id.etSearch)

        btnBack?.setOnClickListener { finish() }

        tvNext?.setOnClickListener {
            val selectedUids = adapter.getSelectedFriendUids()
            if (selectedUids.isEmpty()) {
                Toast.makeText(this, "Please select at least one friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ เปลี่ยน Logic: ส่งรายชื่อกลับไปให้ GroupDetail เป็นคนกด Save แล้วค่อยส่ง Invite
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("SELECTED_FRIENDS", ArrayList(selectedUids))
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFriends(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFriends(keyword: String) {
        val filteredList = ArrayList<FriendItem>()
        for (friend in allFriendsList) {
            if (friend.name.lowercase().contains(keyword.lowercase())) {
                filteredList.add(friend)
            }
        }
        friendList.clear()
        friendList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        adapter = SelectFriendAdapter(friendList)
        rvSelectFriends?.layoutManager = LinearLayoutManager(this)
        rvSelectFriends?.adapter = adapter
    }

    private fun loadFriendsRealtime() {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("users").document(myUid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val friendsUids = snapshot?.get("friends") as? List<String> ?: listOf()
                if (friendsUids.isNotEmpty()) fetchFriendDetails(friendsUids)
                else {
                    allFriendsList.clear()
                    friendList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun fetchFriendDetails(uids: List<String>) {
        db.collection("users").whereIn(FieldPath.documentId(), uids.take(30))
            .get()
            .addOnSuccessListener { documents ->
                allFriendsList.clear()
                for (doc in documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val pUrl = doc.getString("profileUrl")
                    allFriendsList.add(FriendItem(doc.id, name, false, pUrl))
                }
                filterFriends(etSearch?.text.toString())
            }
    }
}