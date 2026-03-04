package com.example.myspt

import java.io.Serializable

data class BillItem(
    var itemName: String = "",
    var quantity: Int = 1,
    var price: Double = 0.0,
    var id: String? = null,
    var selectedUsers: ArrayList<String> = ArrayList()
) : Serializable