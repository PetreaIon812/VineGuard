package com.example.vineguard.StatsScreen.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentTextColor

@Composable
fun TopBar(){
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.right_arrow),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = AccentTextColor
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = "Stats",
                fontSize = 24.sp,
                color = AccentTextColor,
            )
        }
        HorizontalDivider(
            thickness = Dp.Hairline,
            color = Color.Gray
        )
    }
}