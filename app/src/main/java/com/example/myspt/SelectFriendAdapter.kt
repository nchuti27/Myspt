package com.example.myspt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectFriendAdapter(private val friends: List<FriendData>) :
    RecyclerView.Adapter<SelectFriendAdapter.ViewHolder>() {

    private val selectedUids = mutableSetOf<String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvName.text = friend.name

        // จัดการสถานะการเลือก
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = selectedUids.contains(friend.uid)

        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedUids.add(friend.uid)
            else selectedUids.remove(friend.uid)
        }
    }

    override fun getItemCount() = friends.size

    fun getSelectedFriendUids(): List<String> = selectedUids.toList()
}