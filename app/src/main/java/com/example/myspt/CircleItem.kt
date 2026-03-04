package com.example.myspt


data class CircleItem(
    val id: String = "",
    val name: String = "",
    val profileUrl: String? = null,
    var isExpanded: Boolean = false,
    val isAddButton: Boolean = false
)