package com.example.vineguard.HomeScreen.presentation

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.vineguard.LogInScreen.presentation.UserData
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentBrightGreen
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.BorderBlack
import com.example.vineguard.ui.theme.Primary

@Composable
fun TopBar(
    userData: UserData
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
    ){
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp)){
                if(userData.profilePictureUrl != null) {
                    AsyncImage(
                        model = userData.profilePictureUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(shape = CircleShape)
                            .align(alignment = Alignment.CenterVertically),
                        contentScale = ContentScale.Crop
                    )
                }
                else{
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(shape = CircleShape)
                            .align(alignment = Alignment.CenterVertically)
                            .background(AccentBrightGreen),
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "Welcome back, \n${userData.username}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = AccentTextColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.bell),
                    contentDescription = null,
                    tint = AccentTextColor,
                    modifier = Modifier.size(20.dp).align(alignment = Alignment.CenterVertically)
                )

            }
            Divider(
                color = BorderBlack.copy(alpha = 0.1f),
                thickness = 1.dp
            )
        }
    }
}