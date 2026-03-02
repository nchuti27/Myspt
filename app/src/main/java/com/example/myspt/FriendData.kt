package com.example.myspt
import java.io.Serializable

data class FriendData(
    val name: String,
    val username: String,
    val uid: String,
    val profileUrl: String? = null,
    var isExpanded: Boolean = false // ✅ ต้องเป็น var เท่านั้น
) : Serializable