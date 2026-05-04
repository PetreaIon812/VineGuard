package com.example.vineguard.CommonFeatures.domain

import  com.google.firebase.Timestamp

data class SensorData (
    var timestampId: String = "",
    var timestamp: Long = 0,
    var temperature: Double = 0.0,
    var humidity: Double = 0.0,
    var leafWet: Boolean = true,
    var rain: Boolean = true,
    var soilMoisture: Double = 0.0,
    var lightLux: Int = 0,
    var pH: Double = 0.0
)