package com.example.myspt

data class FriendItem(
    val uid: String,
    val name: String,
    var isSelected: Boolean = false,
    val profileUrl: String? = null
)