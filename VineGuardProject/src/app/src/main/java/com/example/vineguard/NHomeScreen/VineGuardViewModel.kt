package com.example.vineguard.NHomeScreen

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

data class SensorReadings(
    val dew_point_c: Double = 0.0,
    val feels_like_c: Double = 0.0,
    val humidity_pct: Double = 0.0,
    val pressure_hpa: Double = 0.0,
    val rain_1h_mm: Double = 0.0,
    val rain_24h_mm: Double = 0.0,
    val temp_c: Double = 0.0,
    val timestamp: Long = 0,
    val uv_index: Double = 0.0,
    val wind_avg_ms: Double = 0.0,
    val wind_gust_ms: Double = 0.0
)

data class DiseaseInfo(
    val label_ro: String = "",
    val probability: Double = 0.0,
    val risk_level: String = ""
)

data class AiPrediction(
    val global_max_probability: Double = 0.0,
    val global_risk_level: String = "",
    val model_version: String = "",
    val node_id: String = "",
    val timestamp: Long = 0,
    val timestamp_iso: String = "",
    val predictions: Map<String, DiseaseInfo> = emptyMap()
)

data class StatsResult(
    val graphPoints: List<Float>,
    val avg: String,
    val peak: String,
    val lowest: String,
    val yLabels: List<String> = emptyList(),
    val xLabels: List<String> = emptyList()
)

class VineGuardViewModel : ViewModel() {

    private val rtdb = FirebaseDatabase.getInstance("https://vineguard-f8483-default-rtdb.europe-west1.firebasedatabase.app")
    private val firestore = FirebaseFirestore.getInstance()

    val availableNodes = listOf("node_00", "node_01", "node_02")

    private val _selectedNode = MutableStateFlow("node_00")
    val selectedNode: StateFlow<String> = _selectedNode.asStateFlow()

    private val _sensorData = MutableStateFlow(SensorReadings())
    val sensorData: StateFlow<SensorReadings> = _sensorData.asStateFlow()

    private val _aiPrediction = MutableStateFlow(AiPrediction())
    val aiPrediction: StateFlow<AiPrediction> = _aiPrediction.asStateFlow()

    private val _historyData = MutableStateFlow<List<SensorReadings>>(emptyList())
    val historyData: StateFlow<List<SensorReadings>> = _historyData.asStateFlow()

    private var sensorRtdbListener: ValueEventListener? = null
    private var historyRtdbListener: ValueEventListener? = null
    private var predictionListener: ListenerRegistration? = null

    init {
        startListening("node_00")
    }

    fun selectNode(nodeId: String) {
        if (_selectedNode.value == nodeId) return
        _selectedNode.value = nodeId
        stopListening()
        startListening(nodeId)
    }

    private fun stopListening() {
        sensorRtdbListener?.let {
            rtdb.getReference("weather_station/readings").removeEventListener(it)
        }
        historyRtdbListener?.let {
            rtdb.getReference("weather_station/readings").removeEventListener(it)
        }
        sensorRtdbListener = null
        historyRtdbListener = null
        predictionListener?.remove()
        predictionListener = null
    }

    private fun startListening(nodeId: String) {

        // 1. Listener Senzori RTDB (Ultima citire LIVE)
        val sensorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    child.getValue(SensorReadings::class.java)
                        ?.let { _sensorData.value = it }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        rtdb.getReference("weather_station/readings")
            .limitToLast(1)
            .addValueEventListener(sensorListener)
        sensorRtdbListener = sensorListener

        // 2. Listener Istoric RTDB (2 săptămâni = 1344 citiri)
        val histListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SensorReadings>()
                for (child in snapshot.children) {
                    child.getValue(SensorReadings::class.java)?.let { list.add(it) }
                }
                _historyData.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        rtdb.getReference("weather_station/readings")
            .limitToLast(1344)
            .addValueEventListener(histListener)
        historyRtdbListener = histListener

        // 3. Listener AI Predictions Firestore (per nod)
        predictionListener = firestore.document("predictions/$nodeId/latest/current")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                snapshot.toObject(AiPrediction::class.java)
                    ?.let { _aiPrediction.value = it }
            }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    /**
     * sensorIndex:
     *   0 -> UV Index
     *   1 -> Rain 1h (mm)
     *   2 -> Humidity (%)
     *   3 -> Temperature (°C)
     *   4 -> Wind (m/s)
     *
     * tabIndex:
     *   0 -> 12h
     *   1 -> 24h
     *   2 -> 1 Săptămână
     *   3 -> 2 Săptămâni
     */
    fun getStatsForOption(sensorIndex: Int, tabIndex: Int): StatsResult {
        val rawList = _historyData.value
        if (rawList.isEmpty()) return StatsResult(
            List(7) { 0.5f },
            "0", "0", "0",
            yLabels = List(6) { "0" },
            xLabels = List(7) { "" }
        )

        val readingsCount = when (tabIndex) {
            0 -> 12 * 4
            1 -> 24 * 4
            2 -> 7 * 96
            3 -> 14 * 96
            else -> 48
        }

        val periodData = rawList.takeLast(readingsCount)

        val xLabels = when (tabIndex) {
            0 -> listOf("0h", "2h", "4h", "6h", "8h", "10h", "12h")
            1 -> listOf("0h", "4h", "8h", "12h", "16h", "20h", "24h")
            2 -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            3 -> listOf("W1", "", "", "", "", "", "W2")
            else -> List(7) { "$it" }
        }

        val chunkSize = (periodData.size / 7).coerceAtLeast(1)

        // Extrage valorile și unitatea în funcție de senzor
        val (rawValues, unitSuffix, defaultScaledMax) = when (sensorIndex) {
            0 -> Triple(
                periodData.chunked(chunkSize).take(7).map { chunk -> chunk.map { it.uv_index }.average() },
                "",
                10.0
            )
            1 -> Triple(
                periodData.chunked(chunkSize).take(7).map { chunk -> chunk.sumOf { it.rain_1h_mm } },
                "mm",
                when (tabIndex) { 0 -> 5.0; 1 -> 10.0; 2 -> 30.0; 3 -> 60.0; else -> 10.0 }
            )
            2 -> Triple(
                periodData.chunked(chunkSize).take(7).map { chunk -> chunk.map { it.humidity_pct }.average() },
                "%",
                100.0
            )
            3 -> Triple(
                periodData.chunked(chunkSize).take(7).map { chunk -> chunk.map { it.temp_c }.average() },
                "°C",
                40.0
            )
            4 -> Triple(
                periodData.chunked(chunkSize).take(7).map { chunk -> chunk.map { it.wind_avg_ms }.average() },
                "m/s",
                10.0
            )
            else -> Triple(
                periodData.chunked(chunkSize).take(7).map { chunk -> chunk.map { it.temp_c }.average() },
                "°C",
                40.0
            )
        }

        val maxVal = rawValues.maxOrNull() ?: 0.0
        val minVal = rawValues.minOrNull() ?: 0.0
        val totalVal = rawValues.sum()
        val avgVal = rawValues.average()

        val scaledMax = if (maxVal == 0.0) defaultScaledMax else maxVal

        val graphPoints = rawValues.map { value ->
            ((value / scaledMax) * 0.8f + 0.1f).toFloat().coerceIn(0.1f, 0.9f)
        }

        val step = scaledMax / 5.0
        val yLabels = List(6) { i ->
            "%.1f%s".format(scaledMax - (i * step), unitSuffix)
        }

        fun formatVal(v: Double) = "%.1f%s".format(v, unitSuffix)

        return StatsResult(
            graphPoints = graphPoints,
            avg = formatVal(avgVal),
            peak = formatVal(maxVal),
            lowest = formatVal(minVal),
            yLabels = yLabels,
            xLabels = xLabels
        )
    }
}