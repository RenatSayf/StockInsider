package com.renatsayf.stockinsider.ui.referral.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Broker(
    @SerialName("button_text")
    val buttonText: String,
    val description: String,
    val header: String,
    val logo: String,
    val name: String,
    val reference: String
)