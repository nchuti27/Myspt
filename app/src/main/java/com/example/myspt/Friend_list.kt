package com.example.myspt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Friend_list : AppCompatActivity() {

    private lateinit var rvFriendList: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    private var friendList = ArrayList<FriendData>()

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
        loadFriendsFromFirestore() // เรียกใช้ฟังก์ชันดึงข้อมูลจริง
    }

    private fun init() {
        val btnAddFriendPage = findViewById<ImageButton>(R.id.btnAddFriendPage)
        val btnBack = findViewById<ImageButton>(R.id.backButton)
        rvFriendList = findViewById(R.id.rvFriendList)

        btnAddFriendPage.setOnClickListener {
            startActivity(Intent(this, AddFriend::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        // ตั้งค่า RecyclerView
        friendAdapter = FriendAdapter(friendList)
        rvFriendList.layoutManager = LinearLayoutManager(this)
        rvFriendList.adapter = friendAdapter
    }

    private fun loadFriendsFromFirestore() {
        val myUid = auth.currentUser?.uid ?: return

        // 1. เข้าไปดึงรายชื่อ UID เพื่อนจากตัวเราก่อน
        db.collection("users").document(myUid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsUids = document.get("friends") as? List<String> ?: emptyList()

                    if (friendsUids.isEmpty()) {
                        Toast.makeText(this, "No friends found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // 2. เอา UID ที่ได้ไปดึงข้อมูล ชื่อ/รายละเอียด ของเพื่อนแต่ละคนมาโชว์
                    fetchFriendsDetails(friendsUids)
                }
            }
    }

    private fun fetchFriendsDetails(uids: List<String>) {
        friendList.clear() // ล้างข้อมูลเก่า (Somchai/Somsak) ออกให้หมด

        for (uid in uids) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: "Unknown"
                    val username = doc.getString("username") ?: ""

                    friendList.add(FriendData(name, "Username: $username", uid))
                    friendAdapter.notifyDataSetChanged()
                }
        }
    }
}

// เพิ่ม uid เข้าไปใน Data Class เพื่อใช้ตอนลบเพื่อน
data class FriendData(
    val name: String,
    val detail: String,
    val uid: String,
    var isExpanded: Boolean = false
)

class FriendAdapter(private var friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

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
        val currentItem = friendList[position]
        holder.tvName.text = currentItem.name
        holder.layoutHidden.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

        holder.btnExpand.setOnClickListener {
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(position)
        }

        holder.btnDelete.setOnClickListener {
            // โค้ดสำหรับลบเพื่อนจริงๆ ใน Firestore (ถ้าต้องการ) สามารถเขียนเพิ่มได้ที่นี่ครับ
            friendList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, friendList.size)
        }

        holder.btnViewProfile.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Profile: ${currentItem.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = friendList.size
}