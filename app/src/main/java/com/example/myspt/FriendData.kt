package com.example.myspt
import java.io.Serializable

data class FriendData(
    val name: String,
    val username: String,
    val uid: String,
    val profileUrl: String? = null,
    var isExpanded: Boolean = false
) : Serializable