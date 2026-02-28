package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Friend_list : AppCompatActivity() {

    private lateinit var rvFriendList: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var etSearch: EditText

    private var fullFriendList = ArrayList<FriendData>() // เก็บข้อมูลทั้งหมดจาก DB

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        loadFriendsFromFirestore()
    }

    private fun init() {
        val btnAddFriendPage = findViewById<ImageButton>(R.id.btnAddFriendPage)
        val btnBack = findViewById<ImageButton>(R.id.backButton)
        rvFriendList = findViewById(R.id.rvFriendList)
        etSearch = findViewById(R.id.etSearch)

        btnAddFriendPage.setOnClickListener {
            startActivity(Intent(this, AddFriend::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        // ตั้งค่า Adapter
        friendAdapter = FriendAdapter(fullFriendList)
        rvFriendList.layoutManager = LinearLayoutManager(this)
        rvFriendList.adapter = friendAdapter

        // ระบบค้นหา (Search Filter)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                friendAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadFriendsFromFirestore() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsUids = document.get("friends") as? List<String> ?: emptyList()
                    if (friendsUids.isEmpty()) {
                        fullFriendList.clear()
                        friendAdapter.updateData(fullFriendList)
                        return@addOnSuccessListener
                    }
                    fetchFriendsDetails(friendsUids)
                }
            }
    }

    private fun fetchFriendsDetails(uids: List<String>) {
        // ใช้ whereIn เพื่อดึงข้อมูลครั้งเดียวแทนการวน Loop .get() ทีละคน (ประสิทธิภาพดีกว่า)
        db.collection("users").whereIn("__name__", uids).get()
            .addOnSuccessListener { querySnapshot ->
                fullFriendList.clear()
                for (doc in querySnapshot.documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val username = doc.getString("username") ?: ""
                    val uid = doc.id
                    fullFriendList.add(FriendData(name, username, uid))
                }
                friendAdapter.updateData(fullFriendList)
            }
    }
}

// --- Model Data ---
data class FriendData(
    val name: String,
    val username: String,
    val uid: String,
    var isExpanded: Boolean = false
)

// --- Adapter ---
class FriendAdapter(private var originalList: ArrayList<FriendData>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private var filteredList = ArrayList<FriendData>(originalList)

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val btnExpand: ImageButton = itemView.findViewById(R.id.btnExpand)
        val layoutHidden: LinearLayout = itemView.findViewById(R.id.layoutHidden)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnViewProfile: Button = itemView.findViewById(R.id.btnViewProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_exp, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = filteredList[position]
        val context = holder.itemView.context

        holder.tvName.text = currentItem.name
        holder.layoutHidden.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

        // คลิกเพื่อกาง/หุบเมนู
        holder.btnExpand.setOnClickListener {
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        // คลิกไปหน้าโปรไฟล์
        holder.btnViewProfile.setOnClickListener {
            val intent = Intent(context, FriendProfile::class.java).apply {
                putExtra("FRIEND_UID", currentItem.uid)
                putExtra("FRIEND_NAME", currentItem.name)
            }
            context.startActivity(intent)
        }

        // ลบเพื่อน
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Remove Friend")
                .setMessage("Remove ${currentItem.name} from friend list?")
                .setPositiveButton("Remove") { _, _ ->
                    val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(myUid)
                        .update("friends", FieldValue.arrayRemove(currentItem.uid))
                        .addOnSuccessListener {
                            // ลบออกจากทั้ง Original และ Filtered list
                            originalList.removeAll { it.uid == currentItem.uid }
                            val pos = holder.bindingAdapterPosition
                            filteredList.removeAt(pos)
                            notifyItemRemoved(pos)
                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount(): Int = filteredList.size

    // ฟังก์ชันสำหรับ Search Filter
    fun filter(query: String) {
        val searchText = query.lowercase().trim()
        filteredList = if (searchText.isEmpty()) {
            ArrayList(originalList)
        } else {
            val result = originalList.filter {
                it.name.lowercase().contains(searchText) || it.username.lowercase().contains(searchText)
            }
            ArrayList(result)
        }
        notifyDataSetChanged()
    }

    // ฟังก์ชันอัปเดตข้อมูลเมื่อโหลดเสร็จ
    fun updateData(newList: ArrayList<FriendData>) {
        originalList = newList
        filteredList = ArrayList(newList)
        notifyDataSetChanged()
    }
}