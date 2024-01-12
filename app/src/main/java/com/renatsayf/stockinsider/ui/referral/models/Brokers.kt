package com.renatsayf.stockinsider.ui.referral.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Brokers(
    @SerialName("brokers")
    val list: List<Broker>
)