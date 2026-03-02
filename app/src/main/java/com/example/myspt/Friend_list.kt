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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore



class Friend_list : AppCompatActivity() {

    private lateinit var rvFriendList: RecyclerView
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var etSearch: EditText
    private var fullFriendList = ArrayList<FriendData>()

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

        btnAddFriendPage.setOnClickListener { startActivity(Intent(this, AddFriend::class.java)) }
        btnBack.setOnClickListener { finish() }

        friendAdapter = FriendAdapter(fullFriendList)
        rvFriendList.layoutManager = LinearLayoutManager(this)
        rvFriendList.adapter = friendAdapter

        // ระบบค้นหาเพื่อน
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

    // ✅ 2. ดึงข้อมูลเพื่อนพร้อมรูปโปรไฟล์
    private fun fetchFriendsDetails(uids: List<String>) {
        db.collection("users").whereIn("__name__", uids).get()
            .addOnSuccessListener { querySnapshot ->
                fullFriendList.clear()
                for (doc in querySnapshot.documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val username = doc.getString("username") ?: ""
                    val pUrl = doc.getString("profileUrl") // 🌟 กู้คืนการดึง URL รูปภาพ
                    val uid = doc.id
                    fullFriendList.add(FriendData(name, username, uid, pUrl))
                }
                friendAdapter.updateData(fullFriendList)
            }
    }
}

// ✅ 3. Adapter สำหรับแสดงผล (ใช้ Layout item_friend_list ตัวใหม่)
class FriendAdapter(private var originalList: ArrayList<FriendData>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private var filteredList = ArrayList<FriendData>(originalList)

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: com.google.android.material.imageview.ShapeableImageView = itemView.findViewById(R.id.ivFriendProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvFriendName)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // 🌟 เปลี่ยนกลับมาใช้ไฟล์ XML ที่พี่ออกแบบมาสวยๆ
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_list, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = filteredList[position]
        val context = holder.itemView.context

        holder.tvName.text = currentItem.name

        // 🌟 ใช้ Glide โหลดรูปวงกลม
        Glide.with(context)
            .load(currentItem.profileUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(holder.ivProfile)

        // ปุ่มเมนูเพิ่มเติม (สำหรับลบเพื่อน)
        holder.btnMore.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Remove Friend")
                .setMessage("Do you want to remove ${currentItem.name}?")
                .setPositiveButton("Remove") { _, _ ->
                    removeFriendFromFirestore(currentItem.uid, context, holder.bindingAdapterPosition)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun removeFriendFromFirestore(friendUid: String, context: android.content.Context, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(myUid)
            .update("friends", FieldValue.arrayRemove(friendUid))
            .addOnSuccessListener {
                originalList.removeAll { it.uid == friendUid }
                if (position != RecyclerView.NO_POSITION && position < filteredList.size) {
                    filteredList.removeAt(position)
                    notifyItemRemoved(position)
                }
                Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        val searchText = query.lowercase().trim()
        filteredList = if (searchText.isEmpty()) ArrayList(originalList)
        else ArrayList(originalList.filter { it.name.lowercase().contains(searchText) || it.username.lowercase().contains(searchText) })
        notifyDataSetChanged()
    }

    fun updateData(newList: ArrayList<FriendData>) {
        originalList = newList
        filteredList = ArrayList(newList)
        notifyDataSetChanged()
    }
}