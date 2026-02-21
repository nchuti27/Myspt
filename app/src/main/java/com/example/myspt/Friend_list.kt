package com.example.myspt

import android.content.Intent
import android.os.Bundle
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
        loadFriendsFromFirestore()
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

        friendAdapter = FriendAdapter(friendList)
        rvFriendList.layoutManager = LinearLayoutManager(this)
        rvFriendList.adapter = friendAdapter
    }

    private fun loadFriendsFromFirestore() {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users").document(myUid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsUids = document.get("friends") as? List<String> ?: emptyList()
                    if (friendsUids.isEmpty()) {
                        friendList.clear()
                        friendAdapter.notifyDataSetChanged()
                        Toast.makeText(this, "No friends found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    fetchFriendsDetails(friendsUids)
                }
            }
    }

    private fun fetchFriendsDetails(uids: List<String>) {
        friendList.clear()
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
        val context = holder.itemView.context
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid

        holder.tvName.text = currentItem.name
        holder.layoutHidden.visibility = if (currentItem.isExpanded) View.VISIBLE else View.GONE

        holder.btnExpand.setOnClickListener {
            currentItem.isExpanded = !currentItem.isExpanded
            notifyItemChanged(position)
        }

        // English version of Delete Confirmation System
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Remove Friend")
                .setMessage("Are you sure you want to remove ${currentItem.name} from your friend list?")
                .setPositiveButton("Remove") { _, _ ->
                    if (myUid != null) {
                        // 1. Remove from our collection
                        db.collection("users").document(myUid)
                            .update("friends", FieldValue.arrayRemove(currentItem.uid))
                            .addOnSuccessListener {
                                // 2. Remove us from friend's collection
                                db.collection("users").document(currentItem.uid)
                                    .update("friends", FieldValue.arrayRemove(myUid))

                                // 3. Update UI
                                if (position < friendList.size) {
                                    friendList.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, friendList.size)
                                    Toast.makeText(context, "Friend removed successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to remove: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        holder.btnViewProfile.setOnClickListener {
            Toast.makeText(context, "Viewing Profile: ${currentItem.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = friendList.size
}