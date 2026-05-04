package com.example.vineguard.HomeScreen.domain

import com.example.vineguard.HomeScreen.presentation.SensorData

data class DiseaseRisk(
    val diseases: Disease,
    val risk: Int
)
fun detectDiseaseRisks(s: SensorData): List<DiseaseRisk> {
    val results = mutableListOf<DiseaseRisk>()
    var score: Int

    // 1. Downy Mildew
    score = 0
    if (s.airHumidity > 85) score += 40 else score -= 25
    if (s.airTemp in 18..26) score += 30 else score -= 10
    if (s.rain == 1) score += 20
    if (s.soilMoisture > 70) score += 10
    results.add(DiseaseRisk(diseases[0], score.coerceIn(0, 100)))

    // 2. Powdery Mildew
    score = 0
    if (s.airTemp in 20..30) score += 40 else score -= 10
    if (s.airHumidity in 40..80) score += 30 else score -= 10
    if (s.light < 30000) score += 20
    if (s.rain == 0) score += 10
    results.add(DiseaseRisk(diseases[1], score.coerceIn(0, 100)))

    // 3. Botrytis Bunch Rot
    score = 0
    if (s.airHumidity > 90) score += 40
    if (s.airTemp in 15..22) score += 30
    if (s.rain == 1) score += 20
    if (s.light < 20000) score += 10
    results.add(DiseaseRisk(diseases[2], score.coerceIn(0, 100)))

    // 4. Black Rot
    score = 0
    if (s.airTemp in 24..30) score += 30
    if (s.airHumidity > 80) score += 30
    if (s.rain == 1) score += 20
    if (s.soilMoisture > 70) score += 20
    results.add(DiseaseRisk(diseases[3], score.coerceIn(0, 100)))

    // 5. Phomopsis Cane and Leaf Spot
    score = 0
    if (s.airTemp in 15..20) score += 40
    if (s.rain == 1) score += 30
    if (s.airHumidity > 85) score += 20
    results.add(DiseaseRisk(diseases[4], score.coerceIn(0, 100)))

    // 6. Anthracnose
    score = 0
    if (s.airTemp in 2..32) score += 30
    if (s.airHumidity > 85) score += 30
    if (s.rain == 1) score += 20
    if (s.soilPH in 5.5..7.0) score += 20
    results.add(DiseaseRisk(diseases[5], score.coerceIn(0, 100)))

    // 7. Esca (Black Measles)
    score = 0
    if (s.airTemp > 28) score += 40
    if (s.soilPH > 7) score += 20
    if (s.soilTemp > 22) score += 20
    if (s.soilMoisture < 40) score += 20
    results.add(DiseaseRisk(diseases[6], score.coerceIn(0, 100)))

    // 8. Eutypa Dieback
    score = 0
    if (s.airTemp in 10..20) score += 40
    if (s.rain == 1) score += 30
    if (s.soilPH < 6) score += 15
    if (s.soilTemp < 15) score += 15
    results.add(DiseaseRisk(diseases[7], score.coerceIn(0, 100)))

    // 9. Pierce’s Disease
    score = 0
    if (s.airTemp > 26) score += 50
    if (s.airHumidity < 60) score += 20
    if (s.soilMoisture < 50) score += 30
    results.add(DiseaseRisk(diseases[8], score.coerceIn(0, 100)))

    // 10. Crown Gall
    score = 0
    if (s.soilTemp < 10) score += 25
    if (s.airTemp < 10) score += 25
    if (s.soilPH > 7) score += 50
    results.add(DiseaseRisk(diseases[9], score.coerceIn(0, 100)))

    // 11. Lime-Induced Chlorosis
    score = 0
    if (s.soilPH > 7.5) score += 50
    if (s.soilMoisture > 60) score += 30
    results.add(DiseaseRisk(diseases[10], score.coerceIn(0, 100)))

    // 12. Bitter Rot
    score = 0
    if (s.airTemp in 23..29) score += 40
    if (s.airHumidity > 85) score += 30
    if (s.soilMoisture > 70) score += 30
    results.add(DiseaseRisk(diseases[11], score.coerceIn(0, 100)))

    // 13. Alternaria Rot
    score = 0
    if (s.airTemp > 22) score += 40
    if (s.airHumidity > 90) score += 30
    if (s.light < 15000) score += 30
    results.add(DiseaseRisk(diseases[12], score.coerceIn(0, 100)))

    // 14. Rust
    score = 0
    if (s.airHumidity > 85) score += 50
    if (s.airTemp in 25..30) score += 50
    results.add(DiseaseRisk(diseases[13], score.coerceIn(0, 100)))

    // 15. White Rot
    score = 0
    if (s.soilMoisture > 75) score += 40
    if (s.airHumidity > 90) score += 30
    if (s.airTemp in 20..25) score += 30
    results.add(DiseaseRisk(diseases[14], score.coerceIn(0, 100)))

    // 16. Armillaria Root Rot
    score = 0
    if (s.soilMoisture > 80) score += 40
    if (s.soilTemp in 10..20) score += 30
    if (s.soilPH < 6) score += 30
    results.add(DiseaseRisk(diseases[15], score.coerceIn(0, 100)))

    // 17. Ripe Rot
    score = 0
    if (s.airTemp in 25..32) score += 40
    if (s.airHumidity > 85) score += 30
    if (s.light < 20000) score += 30
    results.add(DiseaseRisk(diseases[16], score.coerceIn(0, 100)))

    // 18. Leafroll Virus
    score = 0
    if (s.airTemp > 28) score += 40
    if (s.soilPH > 7) score += 30
    if (s.soilMoisture < 40) score += 30
    results.add(DiseaseRisk(diseases[17], score.coerceIn(0, 100)))

    // 19. Grapevine Fanleaf Virus
    score = 0
    if (s.soilPH < 6) score += 50
    if (s.soilTemp < 12) score += 30
    if (s.soilMoisture > 70) score += 20
    results.add(DiseaseRisk(diseases[18], score.coerceIn(0, 100)))

    // 20. Grape Yellows (Flavescence dorée)
    score = 0
    if (s.airTemp > 20) score += 40
    if (s.airHumidity > 70) score += 30
    if (s.light < 25000) score += 30
    results.add(DiseaseRisk(diseases[19], score.coerceIn(0, 100)))

    // 21. Grapevine Red Blotch Disease
    score = 0
    if (s.airTemp in 25..32) score += 40
    if (s.soilPH > 7) score += 30
    if (s.soilMoisture < 45) score += 30
    results.add(DiseaseRisk(diseases[20], score.coerceIn(0, 100)))

    // 22. Sour Rot
    score = 0
    if (s.airTemp > 28) score += 40
    if (s.airHumidity > 85) score += 30
    if (s.rain == 1) score += 30
    results.add(DiseaseRisk(diseases[21], score.coerceIn(0, 100)))

    // 23. Bacterial Blight
    score = 0
    if (s.airTemp < 12) score += 40
    if (s.airHumidity > 85) score += 40
    if (s.rain == 1) score += 20
    results.add(DiseaseRisk(diseases[22], score.coerceIn(0, 100)))

    // 24. Crown and Root Rot (Fusarium)
    score = 0
    if (s.soilMoisture > 80) score += 40
    if (s.soilTemp > 20) score += 30
    if (s.soilPH < 6) score += 30
    results.add(DiseaseRisk(diseases[23], score.coerceIn(0, 100)))

    // 25. Charcoal Rot
    score = 0
    if (s.soilMoisture < 40) score += 40
    if (s.soilTemp > 30) score += 30
    if (s.airTemp > 33) score += 30
    results.add(DiseaseRisk(diseases[24], score.coerceIn(0, 100)))

    // 26. Verticillium Wilt
    score = 0
    if (s.soilTemp in 18..25) score += 40
    if (s.soilPH in 5.0..7.0) score += 30
    if (s.soilMoisture in 60..80) score += 30
    results.add(DiseaseRisk(diseases[25], score.coerceIn(0, 100)))

    // 27. Xylella-Related Wilt
    score = 0
    if (s.airTemp > 30) score += 40
    if (s.airHumidity < 50) score += 30
    if (s.soilMoisture < 40) score += 30
    results.add(DiseaseRisk(diseases[26], score.coerceIn(0, 100)))

    // 28. Phytophthora Root Rot
    score = 0
    if (s.soilMoisture > 85) score += 40
    if (s.soilTemp in 16..22) score += 30
    if (s.rain == 1) score += 30
    results.add(DiseaseRisk(diseases[27], score.coerceIn(0, 100)))

    // 29. Angular Leaf Spot
    score = 0
    if (s.airTemp in 18..25) score += 40
    if (s.airHumidity > 85) score += 30
    if (s.rain == 1) score += 20
    if (s.light < 20000) score += 10
    results.add(DiseaseRisk(diseases[28], score.coerceIn(0, 100)))

    // 30. Septoria Leaf Spot
    score = 0
    if (s.airTemp in 18..23) score += 40
    if (s.airHumidity > 80) score += 30
    if (s.light < 25000) score += 30
    if (s.rain == 1) score += 10
    results.add(DiseaseRisk(diseases[29], score.coerceIn(0, 100)))

    // 31. Cercospora Leaf Spot
    score = 0
    if (s.airTemp in 20..28) score += 40
    if (s.airHumidity > 75) score += 30
    if (s.soilPH < 6.5) score += 30
    results.add(DiseaseRisk(diseases[30], score.coerceIn(0, 100)))

    // 32. Ramsay Disease
    score = 0
    if (s.soilPH > 7.5) score += 50
    if (s.airTemp > 30) score += 30
    if (s.soilMoisture < 45) score += 20
    results.add(DiseaseRisk(diseases[31], score.coerceIn(0, 100)))

    // 33. Diplodia Cane Rot
    score = 0
    if (s.airTemp in 25..32) score += 40
    if (s.light < 20000) score += 30
    if (s.airHumidity in 70..85) score += 30
    results.add(DiseaseRisk(diseases[32], score.coerceIn(0, 100)))

    // 34. Phylloxera Root Damage
    score = 0
    if (s.soilPH in 5.0..6.0) score += 40
    if (s.soilMoisture in 50..70) score += 30
    if (s.soilTemp in 20..30) score += 30
    results.add(DiseaseRisk(diseases[33], score.coerceIn(0, 100)))

    // 35. Algae Leaf Spot
    score = 0
    if (s.airHumidity > 90) score += 40
    if (s.airTemp in 20..28) score += 30
    if (s.light < 18000) score += 30
    if (s.rain == 1) score += 10
    results.add(DiseaseRisk(diseases[34], score.coerceIn(0, 100)))

    // 36. Bird’s Eye Spot
    score = 0
    if (s.airTemp in 18..25) score += 40
    if (s.rain == 1) score += 30
    if (s.airHumidity > 85) score += 30
    results.add(DiseaseRisk(diseases[35], score.coerceIn(0, 100)))

    // 37. Black Foot Disease
    score = 0
    if (s.soilTemp in 10..20) score += 40
    if (s.soilPH < 6) score += 30
    if (s.soilMoisture < 50) score += 30
    results.add(DiseaseRisk(diseases[36], score.coerceIn(0, 100)))

    // 38. Bunch Stem Necrosis
    score = 0
    if (s.airTemp < 15 || s.airTemp > 32) score += 40
    if (s.soilMoisture < 40) score += 30
    if (s.light < 20000) score += 30
    results.add(DiseaseRisk(diseases[37], score.coerceIn(0, 100)))

    // 39. Cane Necrosis
    score = 0
    if (s.airTemp < 12) score += 40
    if (s.rain == 1) score += 30
    if (s.soilPH < 6) score += 30
    results.add(DiseaseRisk(diseases[38], score.coerceIn(0, 100)))

    // 40. Pink Berry Disease
    score = 0
    if (s.airTemp > 28) score += 40
    if (s.airHumidity > 85) score += 30
    if (s.soilMoisture > 70) score += 30
    results.add(DiseaseRisk(diseases[39], score.coerceIn(0, 100)))

    // 41. Grapevine Virus A
    score = 0
    if (s.airTemp > 26) score += 40
    if (s.soilPH > 7) score += 30
    if (s.soilMoisture < 45) score += 30
    results.add(DiseaseRisk(diseases[40], score.coerceIn(0, 100)))

    // 42. Grapevine Virus B
    score = 0
    if (s.airTemp in 20..30) score += 40
    if (s.airHumidity in 50..80) score += 30
    if (s.soilPH < 6.5) score += 30
    results.add(DiseaseRisk(diseases[41], score.coerceIn(0, 100)))

    // 43. Grape Anthracnose (Bird’s Eye)
    score = 0
    if (s.airHumidity > 85) score += 40
    if (s.airTemp in 18..28) score += 30
    if (s.light < 25000) score += 30
    results.add(DiseaseRisk(diseases[42], score.coerceIn(0, 100)))

    // 44. Phomopsis Leaf Spot
    score = 0
    if (s.airTemp in 15..22) score += 40
    if (s.rain == 1) score += 30
    if (s.airHumidity > 80) score += 30
    results.add(DiseaseRisk(diseases[43], score.coerceIn(0, 100)))

    // 45. Powdery Scab
    score = 0
    if (s.airTemp > 24) score += 40
    if (s.soilMoisture > 70) score += 30
    if (s.soilPH in 6.0..7.0) score += 30
    results.add(DiseaseRisk(diseases[44], score.coerceIn(0, 100)))

    // 46. Black Spot
    score = 0
    if (s.airTemp in 20..28) score += 40
    if (s.airHumidity > 85) score += 30
    if (s.rain == 1) score += 30
    results.add(DiseaseRisk(diseases[45], score.coerceIn(0, 100)))

    // 47. Leaf Blight
    score = 0
    if (s.airTemp in 18..26) score += 40
    if (s.airHumidity > 80) score += 30
    if (s.light < 20000) score += 30
    results.add(DiseaseRisk(diseases[46], score.coerceIn(0, 100)))

    // 48. Stem Canker
    score = 0
    if (s.airTemp in 15..25) score += 40
    if (s.soilPH < 6) score += 30
    if (s.soilMoisture > 75) score += 30
    results.add(DiseaseRisk(diseases[47], score.coerceIn(0, 100)))

    // 49. Leaf Spot Virus
    score = 0
    if (s.airTemp in 20..30) score += 40
    if (s.airHumidity > 70) score += 30
    if (s.soilPH < 6.5) score += 30
    results.add(DiseaseRisk(diseases[48], score.coerceIn(0, 100)))

    // 50. Bud Necrosis
    score = 0
    if (s.airTemp < 15) score += 40
    if (s.soilMoisture < 40) score += 30
    if (s.soilPH < 6) score += 30
    results.add(DiseaseRisk(diseases[49], score.coerceIn(0, 100)))

    return results.sortedByDescending { it.risk }
}