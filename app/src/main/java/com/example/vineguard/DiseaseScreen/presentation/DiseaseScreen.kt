package com.example.vineguard.DiseaseScreen.presentation

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.HomeScreen.domain.Intensity
import com.example.vineguard.HomeScreen.domain.detectDiseaseRisks
import com.example.vineguard.HomeScreen.domain.getDiseaseRisk
import com.example.vineguard.HomeScreen.domain.getDiseaseRiskLevel
import com.example.vineguard.HomeScreen.presentation.DiseasesAlert
import com.example.vineguard.HomeScreen.presentation.SensorData
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.Background
import com.example.vineguard.ui.theme.LvlRed
import com.example.vineguard.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun DiseaseScreen(
    sensorData: SensorData,
    getToHomeScreen: () -> Unit,
    modifier: Modifier = Modifier
){
    // 0 - Niciun filtru
    // 1 - High risk disease
    // 2- Moderate risk disease
    // 3 - Normal risk disease
    var filter by remember{
        mutableStateOf(0)
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val diseases = detectDiseaseRisks(sensorData)
    val filteredDisease = getDiseaseRiskLevel(diseaseRisk = diseases, filter = filter)
    val intensity = getDiseaseRisk(filteredDisease)
    Column(modifier = Modifier.fillMaxWidth().background(Primary).padding(10.dp)) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()){
            IconButton(
                onClick = getToHomeScreen
            ) {
                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).scale(-1f)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Diseases",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = AccentTextColor,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            FilterButton(
                intensity = Intensity.HIGH,
                selected = filter == 1,
                onChangeFilter = {
                    if (filter == 1)
                        filter = 0
                    else
                        filter = 1
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
            FilterButton(
                intensity = Intensity.MODERATE,
                selected = filter == 2,
                onChangeFilter = {
                    if (filter == 2)
                        filter = 0
                    else
                        filter = 2
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
            FilterButton(
                intensity = Intensity.NORMAL,
                selected = filter == 3,
                onChangeFilter = {
                    if (filter == 3)
                        filter = 0
                    else
                        filter = 3
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 90.dp), state = listState) {
            items(filteredDisease.size){ index ->
                    DiseasesAlert(
                        disease = filteredDisease[index],
                        intensity = intensity[index]
                    )
            }
        }

    }
}
@Composable
fun FilterButton(
    intensity: Intensity,
    selected: Boolean,
    onChangeFilter: () -> Unit
){
    Button(
        onClick = onChangeFilter,
        modifier = Modifier
            .height(50.dp)
            .clip(CircleShape)
            .border(width = 1.dp, color = intensity.accentColor, shape = CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = if(selected) intensity.accentColor else Primary
        )
    ) {
        Text(
            text = intensity.level,
            color = if(selected) Background else intensity.accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}