package com.example.myspt

import java.io.Serializable

// เปลี่ยนจาก val เป็น var เพื่อให้แก้ไขค่าได้
data class BillItem(
    var itemName: String = "",
    var quantity: Int = 1,
    var price: Double = 0.0,
    var id: String? = null, // 🌟 เพิ่มบรรทัดนี้เพื่อแก้ตัวแดง .id
    var selectedUsers: ArrayList<String> = ArrayList() // 🌟 เพิ่มบรรทัดนี้เพื่อแก้ตัวแดง selectedUsers
) : Serializable