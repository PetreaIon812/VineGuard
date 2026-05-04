package com.example.vineguard.CommonFeatures.domain

import android.os.Debug
import android.util.Log

fun CalculateDiseaseListRisk(
    sensorData15m: List<SensorData>,
    diseases: List<Disease>,
    growthStage: GrowthStage
): List<Double> {
    val risks = mutableListOf<Double>()

    Log.d("mra mra","${diseases.size}")
    for (disease in diseases) {
        val risk = CalculateDiseaseRisk(sensorData15m, disease, growthStage)
        Log.d("ham ham","${disease.disease} risk:${risk}")
        risks.add(risk)
    }

    return risks
}