package com.example.vineguard.HomeScreen.domain

import android.graphics.Color

fun getDiseaseRisk(
    diseaseRisk: List<DiseaseRisk>
): List<Intensity>{
    var result = mutableListOf<Intensity>()
    diseaseRisk.map{if(it.risk in 0 .. 30) result.add(Intensity.NORMAL) else if(it.risk in 31 .. 60) result.add(Intensity.MODERATE) else result.add(Intensity.HIGH) }
    return result
}
fun getDiseaseRiskLevel(
    diseaseRisk: List<DiseaseRisk>,
    filter: Int
): List<DiseaseRisk> {
    var result = mutableListOf<DiseaseRisk>()
    if (filter == 0)
        result = diseaseRisk.toMutableList()
    if (filter == 1)
        diseaseRisk.map {
            if(it.risk in 61 .. 100) result.add(it)
        }
    if(filter == 2)
        diseaseRisk.map {
            if(it.risk in 31 .. 60) result.add(it)
        }
    if(filter == 3)
        diseaseRisk.map {
            if(it.risk in 0 .. 30) result.add(it)
        }
    return result
}