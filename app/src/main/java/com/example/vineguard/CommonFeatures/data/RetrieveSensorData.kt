package com.example.vineguard.CommonFeatures.data

import android.util.Log
import com.example.vineguard.CommonFeatures.domain.Disease
import com.example.vineguard.CommonFeatures.domain.SensorData
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

suspend fun RetrieveSensorData(lookbackDays: Long): List<SensorData> {
    val db = Firebase.firestore
    return try {
        val snapshot = db.collection("sensorData")
            .orderBy("timestampId", Query.Direction.DESCENDING)
            .limit(lookbackDays * 96) // assuming 96 readings per day (15-min intervals)
            .get()
            .await() // wait for Firebase asynchronously

        val sensorDatas = snapshot.documents.mapNotNull { it.toObject(SensorData::class.java) }

        // Optional: log each timestamp
        sensorDatas.forEach { Log.d("SensorData", it.timestampId) }

        sensorDatas
    } catch (e: Exception) {
        Log.w("SensorData", "Error getting documents", e)
        emptyList()
    }
}