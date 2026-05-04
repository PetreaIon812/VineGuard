package com.example.vineguard.StatsScreen.presentation

import android.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.Background
import com.example.vineguard.ui.theme.BorderBlack

@Composable
fun DayButton(
    text:String,
    selected: Boolean,
    action: () -> Unit,
    sensorColor: Color
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) sensorColor else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "BorderColorAnimation"
    )

    val borderThickness by animateDpAsState(
        targetValue = if (selected) 3.dp else 1.dp,
        animationSpec = tween(durationMillis = 300),
        label = "BorderThicknessAnimation"
    )
    Button(
        onClick = action,
        colors = ButtonDefaults.buttonColors(containerColor = Background),
        border = BorderStroke(
            width = borderThickness,
            color = borderColor
        ),
        modifier = Modifier.shadow(
            elevation = 6.dp,
            ambientColor = borderColor,
            spotColor = borderColor,
            shape = CircleShape
        )
    ){
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentTextColor
        )
    }
}