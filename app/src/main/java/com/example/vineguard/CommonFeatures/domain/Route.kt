package com.example.vineguard.CommonFeatures.domain

import com.example.vineguard.HomeScreen.presentation.SensorData
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object MainScreen: Route
    @Serializable
    data object LogInScreen: Route
    @Serializable
    data class DiseaseScreen(val rain: Int,val light: Int,val soilMoisture: Int,val airHumidity: Int,val airTemp: Int,val soilTemp: Int,val soilPH: Double,val pressure: Int): Route

    @Serializable
    data object DiseaseLibraryScreen: Route

    @Serializable
    data object  StatsScreen:Route

    @Serializable
    data object  ProfileScreen: Route

}