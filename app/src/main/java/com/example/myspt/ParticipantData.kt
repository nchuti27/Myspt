package com.example.myspt

data class ParticipantData(
    val uid: String,
    val name: String,
    val profileUrl: String? = null // 🌟 เพิ่มอันนี้เข้าไปครับ
)