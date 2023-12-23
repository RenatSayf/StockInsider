package com.renatsayf.stockinsider.ui.donate

import kotlinx.serialization.Serializable

@Serializable
data class UserPurchase(
    val productId: String,
    val purchaseToken: String,
    val purchaseTime: Long,
    var quantity: Int = 0,
    var acknowledged: Boolean = false
)
