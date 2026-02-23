package com.example.myspt

data class FriendItem(
    val uid: String,
    val name: String,
    var isSelected: Boolean = false // เพิ่มฟิลด์นี้เพื่อจำว่า "ถูกติ๊กหรือยัง"
)