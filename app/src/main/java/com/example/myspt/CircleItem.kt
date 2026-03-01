package com.example.myspt


data class CircleItem(
    val id: String = "",
    val name: String = "",
    val profileUrl: String? = null, // 🌟 เพิ่มบรรทัดนี้เพื่อให้หายแดงครับ!
    var isExpanded: Boolean = false,
    val isAddButton: Boolean = false
)