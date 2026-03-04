package com.example.myspt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
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

    private fun fetchFriendsDetails(uids: List<String>) {
        db.collection("users").whereIn("__name__", uids).get()
            .addOnSuccessListener { querySnapshot ->
                fullFriendList.clear()
                for (doc in querySnapshot.documents) {
                    val name = doc.getString("name") ?: "Unknown"
                    val username = doc.getString("username") ?: ""
                    val pUrl = doc.getString("profileUrl")
                    val uid = doc.id
                    // ส่งค่าเริ่มต้น isExpanded = false เพื่อให้แผงปุ่มซ่อนอยู่ตอนเริ่ม
                    fullFriendList.add(FriendData(name, username, uid, pUrl, false))
                }
                friendAdapter.updateData(fullFriendList)
            }
    }
}


class FriendAdapter(private var originalList: ArrayList<FriendData>) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private var filteredList = ArrayList<FriendData>(originalList)

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ShapeableImageView = itemView.findViewById(R.id.ivFriendProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvFriendName)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
        val layoutActions: LinearLayout = itemView.findViewById(R.id.layoutActions)
        val divider: View = itemView.findViewById(R.id.divider)
        val btnFriendDetail: MaterialButton = itemView.findViewById(R.id.btnFriendDetail)
        val btnRemoveFriend: MaterialButton = itemView.findViewById(R.id.btnRemoveFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_list, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = filteredList[position]
        val context = holder.itemView.context

        holder.tvName.text = currentItem.name

        // กางหรือซ่อisExpanded
        holder.layoutActions.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE
        holder.divider.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

        Glide.with(context)
            .load(currentItem.profileUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(holder.ivProfile)

        holder.btnMore.setOnClickListener {
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(position)
        }

        holder.btnRemoveFriend.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Remove Friend")
                .setMessage("Do you want to remove ${currentItem.name}?")
                .setPositiveButton("Remove") { _, _ ->
                    removeFriendFromFirestore(currentItem.uid, context, holder.bindingAdapterPosition)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    currentItem.isExpanded = false // พับแผงเก็บเมื่อกดยกเลิก
                    notifyItemChanged(position)
                    dialog.dismiss()
                }
                .show()
        }

        holder.btnFriendDetail.setOnClickListener {
            currentItem.isExpanded = false
            notifyItemChanged(position)
            val intent = Intent(context, FriendProfile::class.java).apply {
                putExtra("FRIEND_UID", currentItem.uid)
                putExtra("FRIEND_NAME", currentItem.name)
                putExtra("FRIEND_USERNAME", currentItem.username)
                putExtra("FRIEND_IMG", currentItem.profileUrl)
                // ส่งค่าว่าเป็นเพื่อนกันเพื่อให้โชว์ QR
                putExtra("IS_FRIEND", true)
            }
            context.startActivity(intent)
        }
    }

    private fun removeFriendFromFirestore(friendUid: String, context: Context, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // ลบทั้ง 2 ฝั่งพร้อมกัน
        val batch = db.batch()

        // ลบเพื่อนออกจากเรา
        val myRef = db.collection("users").document(myUid)
        batch.update(myRef, "friends", FieldValue.arrayRemove(friendUid))

        // ลบเราออกจากเพื่อน
        val friendRef = db.collection("users").document(friendUid)
        batch.update(friendRef, "friends", FieldValue.arrayRemove(myUid))

        batch.commit().addOnSuccessListener {
            originalList.removeAll { it.uid == friendUid }
            if (position != RecyclerView.NO_POSITION && position < filteredList.size) {
                filteredList.removeAt(position)
                notifyItemRemoved(position)
            }
            Toast.makeText(context, "Unfriended successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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