package com.example.vineguard.HomeScreen.domain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.HomeScreen.presentation.DataMonitoring
import com.example.vineguard.HomeScreen.presentation.DiseasesAlert
import com.example.vineguard.HomeScreen.presentation.DiseasesAlertBox
import com.example.vineguard.HomeScreen.presentation.SelectVineyard
import com.example.vineguard.HomeScreen.presentation.SensorData
import com.example.vineguard.HomeScreen.presentation.SensorDataType
import com.example.vineguard.HomeScreen.presentation.TopBar
import com.example.vineguard.LogInScreen.presentation.UserData
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.Background
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryLvlRed
import com.example.vineguard.ui.theme.SecondaryLvlRed
import com.example.vineguard.ui.theme.SecondaryLvlYellow

@Composable
fun HomeScreen(
    modifier: Modifier,
    userData: UserData,
    onSignOut:() -> Unit,
    onGoToDisease:(SensorData) -> Unit
){
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background)
    ){
        item {
            TopBar(userData = userData)
            SelectVineyard(vineyards = Vineyards)
            Text(
                text = "Real-time Monitoring",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentTextColor,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 30.dp, bottom = 10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DataMonitoring(typeSensor = SensorDataType.SOIL_PH, number = 7.0)
                DataMonitoring(typeSensor = SensorDataType.RAIN, number = 1.0)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DataMonitoring(typeSensor = SensorDataType.LIGHT, number = 3000.0)
                DataMonitoring(typeSensor = SensorDataType.AIR_TEMPERATURE, number = 25.0)
            }
            Spacer(modifier = Modifier.height(20.dp))
            DiseasesAlertBox(
                sensorData = SensorData(),
                onGoToDisease = onGoToDisease
            )

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}