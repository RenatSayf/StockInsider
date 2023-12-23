package com.renatsayf.stockinsider.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetInfo(

    @SerialName("as")
    val provider: String,
    val city: String,
    val country: String,
    val countryCode: String,
    val isp: String,
    val lat: Double,
    val lon: Double,
    val org: String,
    val query: String,
    val region: String,
    val regionName: String,
    val status: String,
    val timezone: String,
    val zip: String
)