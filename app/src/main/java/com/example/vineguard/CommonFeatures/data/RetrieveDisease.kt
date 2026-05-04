package com.example.vineguard.CommonFeatures.data

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.vineguard.CommonFeatures.domain.Disease
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await


suspend fun RetrieveDiseases(): List<Disease> {
    val db = Firebase.firestore
    return try {
        val snapshot = db.collection("diseases").get().await() // needs kotlinx-coroutines-play-services
        snapshot.documents.mapNotNull { it.toObject(Disease::class.java) }
    } catch (e: Exception) {
        Log.w("RetrieveDisease", "Error getting documents.", e)
        emptyList()
    }
}
