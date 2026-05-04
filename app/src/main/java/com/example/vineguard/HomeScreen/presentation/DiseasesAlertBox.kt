package com.example.vineguard.HomeScreen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vineguard.HomeScreen.domain.Disease
import com.example.vineguard.HomeScreen.domain.detectDiseaseRisks
import com.example.vineguard.HomeScreen.domain.getDiseaseRisk
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.LvlRed
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryTextColor
import com.example.vineguard.ui.theme.SecondaryLvlRed
import com.example.vineguard.ui.theme.SecondaryLvlYellow

@Composable
fun DiseasesAlertBox(
     sensorData: SensorData,
     onGoToDisease:(SensorData) -> Unit
){
    val diseases = detectDiseaseRisks(sensorData)
    val intensityList = getDiseaseRisk(diseaseRisk = diseases)
    val colorsGradient = intensityList.map{it.secondaryColor}
    Box(modifier = Modifier.background(Primary)) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = colorsGradient,
                )
            )
        ) {
            LazyColumn(modifier = Modifier) {
                item {
//                    val listState = rememberLazyListState()
//                    val visibleItems = listState.layoutInfo.visibleItemsInfo
//                    val visibleColors = visibleItems.map{ info ->
//                        val index = info.index
//                        diseases[index].diseases.
//                    Programat sa se schimbe gradientul pe baza la bolile visible din lista
                        Row(
                            modifier = Modifier
                                .padding(start = 15.dp, end = 15.dp, bottom = 5.dp, top = 25.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Disease Alerts",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                color = AccentTextColor,
                                modifier = Modifier.align(alignment = Alignment.CenterVertically)
                            )
                            IconButton(
                                onClick = { onGoToDisease(sensorData) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.right_arrow),
                                    contentDescription = null,
                                    tint = AccentTextColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                        for (i in diseases.indices) {
                            DiseasesAlert(diseases[i], intensityList[i])
                        }
                }
            }
        }
    }
}