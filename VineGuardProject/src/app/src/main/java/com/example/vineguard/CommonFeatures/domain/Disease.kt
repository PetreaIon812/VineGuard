package com.example.vineguard.CommonFeatures.domain

data class Disease (
    var disease: String = "",
    var version: String = "",
    var lastReviewed: String = "",
    var source: String = "",
    var biology: Biology = Biology(),
    var weights: Weights = Weights(),
    var modifiers: Modifiers = Modifiers(),
    var lag: Lag = Lag(),
    var growthStages: GrowthStages = GrowthStages(),
    var historicalPressure: HistoricalPressure = HistoricalPressure()
)
data class  Biology(
    var minTemperature: Double = 0.0,
    var optTemperature: Double = 0.0,
    var maxTemperature: Double = 0.0,
    var minHumidity: Double = 0.0,
    var optHumidity: Double = 0.0,
    var minLeafWetnessHours: Int = 0,
    var requiresRain: Boolean = true
)
data class Weights(
    var temperature: Double = 0.0,
    var humidity: Double = 0.0,
    var leafWetness: Double = 0.0,
    var wind: Double = 0.0,
    var canopy: Double = 0.0
)
enum class ModifierLevel{
    NONE,LOW,MEDIUM,HIGH
}
data class ModifierScale(
    val none: Double = 0.0,
    val low: Double = 0.0,
    val medium: Double = 0.0,
    val high: Double = 0.0
){
    fun valueFor(level: ModifierLevel): Double =
        when(level){
            ModifierLevel.NONE -> none
            ModifierLevel.LOW -> low
            ModifierLevel.MEDIUM -> medium
            ModifierLevel.HIGH -> high
        }
}
data class Modifiers(
    val rain: ModifierScale = ModifierScale(),
    val leafWetness: ModifierScale = ModifierScale(),
    val soilMoisture: ModifierScale = ModifierScale(),
    val light: ModifierScale = ModifierScale(),
    val pH: ModifierScale = ModifierScale()
)
data class Lag(
    var days: Int = 0,
    var weights: List<Double> = emptyList()
)
data class GrowthStages(
    var dormant: Double = 0.0,
    var budBreak: Double = 0.0,
    var preFlowering: Double = 0.0,
    var flowering: Double = 0.0,
    var berryGrowth: Double = 0.0,
    var veraison: Double = 0.0,
    var harvest: Double = 0.0
)
data class HistoricalPressure(
    var enabled: Boolean = true,
    var maxBoost: Double = 0.0,
    var lookbackDays: Int = 0
)
fun GrowthStages.multiplierFor(stage: GrowthStage): Double =
    when (stage) {
        GrowthStage.DORMANT -> dormant
        GrowthStage.BUD_BREAK -> budBreak
        GrowthStage.PRE_FLOWERING -> preFlowering
        GrowthStage.FLOWERING -> flowering
        GrowthStage.BERRY_GROWTH -> berryGrowth
        GrowthStage.VERAISON -> veraison
        GrowthStage.HARVEST -> harvest
    }
enum class GrowthStage {
    DORMANT,
    BUD_BREAK,
    PRE_FLOWERING,
    FLOWERING,
    BERRY_GROWTH,
    VERAISON,
    HARVEST
}
fun soilMoistureLevel(value: Double): ModifierLevel =
    when {
        value < 0.3 -> ModifierLevel.NONE
        value < 0.5 -> ModifierLevel.LOW
        value < 0.7 -> ModifierLevel.MEDIUM
        else -> ModifierLevel.HIGH
    }
fun pHLevel(value: Double): ModifierLevel =
    when {
        value < 5.5 || value > 8.0 -> ModifierLevel.NONE
        value < 6.0 || value > 7.5 -> ModifierLevel.LOW
        value < 6.5 || value > 7.0 -> ModifierLevel.MEDIUM
        else -> ModifierLevel.HIGH
    }