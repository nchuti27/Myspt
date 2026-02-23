package com.example.myspt

data class BillItem(
    var itemName: String,
    var quantity: Int,
    var price: Double,
    var selectedUsers: ArrayList<String> = arrayListOf() // เพิ่มกะบะเก็บรายชื่อคนหารในก้อนข้อมูลนี้
)