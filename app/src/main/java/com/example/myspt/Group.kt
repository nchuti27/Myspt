package com.example.myspt

// แก้ตรงที่พี่หาเจอให้เป็นแบบนี้
data class Group(
    val id: String = "",
    val name: String = "",
    val members: ArrayList<String> = arrayListOf(),
    val profileUrl: String? = null
)