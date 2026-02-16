package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeFriendAdapter(private val friendList: ArrayList<FriendData>) :
    RecyclerView.Adapter<HomeFriendAdapter.HomeFriendViewHolder>() {

    class HomeFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFriendViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return HomeFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFriendViewHolder, position: Int) {
        val currentItem = friendList[position]
        holder.tvName.text = currentItem.name

    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}