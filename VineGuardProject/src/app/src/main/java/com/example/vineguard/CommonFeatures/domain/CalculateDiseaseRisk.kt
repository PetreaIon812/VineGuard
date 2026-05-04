package com.example.vineguard.CommonFeatures.domain

import java.time.Instant
import java.time.ZoneOffset

fun CalculateDiseaseRisk(
    sensorData15m: List<SensorData>,
    disease: Disease,
    growthStage: GrowthStage
): Double {

    if (sensorData15m.isEmpty()) return 0.0

    //Group by day
    val dailyData = sensorData15m.groupBy { data ->
        Instant.ofEpochSecond(data.timestamp)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }

    //Calculate daily base risk
    val dailyRisks = dailyData.entries
        .sortedBy { it.key }
        .map { (_, dayData) ->
            calculateDailyRisk(dayData, disease)
        }

    //Apply lag
    val laggedRisk = applyLag(
        dailyRisks,
        disease.lag.weights
    )

    //Apply growth stage
    val growthMultiplier = disease.growthStages.multiplierFor(growthStage)
    var finalRisk = laggedRisk * growthMultiplier

    //Apply historical pressure
    if (disease.historicalPressure?.enabled == true) {
        finalRisk += finalRisk * disease.historicalPressure.maxBoost
    }

    //Clamp
    finalRisk = finalRisk.coerceIn(0.0, 100.0)

    return finalRisk
}
private fun calculateDailyRisk(
    dayData: List<SensorData>,
    disease: Disease
): Double {

    val avgTemp = dayData.map { it.temperature }.average()
    val avgHumidity = dayData.map { it.humidity }.average()
    val totalLeafWetness = dayData.count { it.leafWet } * 0.25
    val totalRain = dayData.count { it.leafWet } * 0.25
    val avgSoilMoisture = dayData.map { it.soilMoisture }.average()
    val avgPH = dayData.map { it.pH }.average()

    var risk = 0.0

    risk += scoreRange(
        avgTemp,
        disease.biology.minTemperature,
        disease.biology.optTemperature,
        disease.biology.maxTemperature
    ) * disease.weights.temperature

    risk += scoreAbove(
        avgHumidity,
        disease.biology.minHumidity,
        disease.biology.optHumidity
    ) * disease.weights.humidity

    if (totalLeafWetness >= disease.biology.minLeafWetnessHours) {
        risk += disease.weights.leafWetness
    }

    if (disease.biology.requiresRain && totalRain <= 0) {
        risk *= 0.6
    }

    val modifiers = disease.modifiers

    risk *= modifiers.soilMoisture.valueFor(
        soilMoistureLevel(avgSoilMoisture)
    )

    risk *= modifiers.pH.valueFor(
        pHLevel(avgPH)
    )

    return risk * 100.0
}
private fun applyLag(
    dailyRisks: List<Double>,
    lagWeights: List<Double>
): Double {

    val window = dailyRisks.takeLast(lagWeights.size)

    return window.reversed().mapIndexed { index, risk ->
        risk * lagWeights.getOrElse(index) { 0.0 }
    }.sum()
}
private fun scoreRange(
    value: Double,
    min: Double,
    opt: Double,
    max: Double
): Double = when {
    value < min || value > max -> 0.0
    value == opt -> 1.0
    value < opt -> (value - min) / (opt - min)
    else -> (max - value) / (max - opt)
}

private fun scoreAbove(
    value: Double,
    min: Double,
    opt: Double
): Double = when {
    value < min -> 0.0
    value >= opt -> 1.0
    else -> (value - min) / (opt - min)
}