package com.example.vineguard.HomeScreen.presentation

import androidx.collection.emptyLongSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.ui.theme.Background
import com.example.vineguard.ui.theme.LvlGreen
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryLvlGreen
import com.example.vineguard.ui.theme.PrimaryTextColor

@Composable
fun DataMonitoring(
    typeSensor: SensorDataType,
    number: Double
){
    val risk = typeSensor.riskLevel(number)
    Box(
        modifier = Modifier
            .width(178.dp)
            .padding(10.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), ambientColor = Background)
            .clip(RoundedCornerShape(16.dp))
            .background(Primary),
    ){
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeSensor.primaryColor)
                ) {
                    Icon(
                        painter = painterResource(typeSensor.icon),
                        contentDescription = null,
                        tint = typeSensor.accentColor,
                        modifier = Modifier.size(20.dp).align(alignment = Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(risk.primaryColor)
                ) {
                    Text(
                        text = risk.level,
                        fontSize = 14.sp,
                        color =risk.accentColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text =
                    if(typeSensor == SensorDataType.RAIN) {
                        if(number == 1.0)
                            "It rains"
                        else
                            "It's not raining"
                    }
                    else if(typeSensor == SensorDataType.SOIL_PH)
                        number.toString()
                    else
                        "${number.toInt()} ${typeSensor.unit}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp
            )
            Text(
                text = typeSensor.factorName,
                fontSize = 13.sp,
                color = PrimaryTextColor
            )
        }
    }
}