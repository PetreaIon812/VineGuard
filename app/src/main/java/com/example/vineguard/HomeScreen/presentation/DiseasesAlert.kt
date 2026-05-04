package com.example.vineguard.HomeScreen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.HomeScreen.domain.Disease
import com.example.vineguard.HomeScreen.domain.DiseaseRisk
import com.example.vineguard.HomeScreen.domain.Intensity
import com.example.vineguard.ui.theme.PrimaryLvlRed
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentRedTextColor
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.LvlGreen
import com.example.vineguard.ui.theme.LvlRed
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryLvlGreen
import com.example.vineguard.ui.theme.PrimaryTextColor
import com.example.vineguard.ui.theme.SecondaryLvlRed


@Composable
fun DiseasesAlert(
    disease: DiseaseRisk,
    intensity: Intensity
){
    Box(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()
            .dropShadow(
                shape = RoundedCornerShape(16.dp),
                shadow = Shadow(
                    radius = 8.dp,
                    spread = 1.dp,
                    color = intensity.primaryColor
                )
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Primary)
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)){
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(intensity.primaryColor)
            ) {
                Icon(
                    painter = painterResource(intensity.icon),
                    contentDescription = null,
                    tint = intensity.accentColor,
                    modifier = Modifier.size(20.dp).align(alignment = Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = disease.diseases.name,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentTextColor,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = disease.diseases.description,
                    fontSize = 14.sp,
                    color = PrimaryTextColor,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(intensity.secondaryColor)
                ) {
                    Text(
                        text = "Probability: ${disease.risk}%",
                        fontSize = 14.sp,
                        color = intensity.accentColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(intensity.secondaryColor)
                ){
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Recommendations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = intensity.textColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        for(text in disease.diseases.recommendations) {
                            Text(
                                text = "● ${text}",
                                fontSize = 14.sp,
                                color = intensity.textColor
                            )
                        }
                    }
                }
            }
        }
    }
}