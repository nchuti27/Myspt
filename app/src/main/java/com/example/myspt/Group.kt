package com.example.myspt


data class Group(
    val id: String = "",
    val name: String = "",
    val members: ArrayList<String> = arrayListOf(),
    val profileUrl: String? = null
)