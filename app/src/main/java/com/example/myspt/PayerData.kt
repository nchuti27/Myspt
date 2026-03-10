package com.example.myspt

data class PayerData(
    val uid: String,
    val name: String,
    var amountPaid: Double = 0.0
)
