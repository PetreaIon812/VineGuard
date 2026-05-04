package com.example.vineguard.StatsScreen.presentation

import android.widget.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentYellow

@Composable
fun CategoryIconsRow()
{
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        item{

        }
    }
}


@Preview
@Composable
fun SensorButton(
    label: String = "Lightness",
    iconRes: Int = R.drawable.sun, // R.drawable.ic_light, etc.
    isSelected: Boolean = true,
    onClick: () -> Unit = {}
) {
    // Definim culorile bazate pe starea de selecție
    val backgroundColor = if (isSelected) Color(0xFFFBC02D) else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF546E7A)
    val shadowElevation = if (isSelected) 8.dp else 2.dp

    Surface(
        modifier = Modifier
            .width(75.dp)
            .height(95.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = shadowElevation
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )
        }
    }
}