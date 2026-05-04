package com.example.vineguard.HomeScreen.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.HomeScreen.domain.Vineyard
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentBrightGreen
import com.example.vineguard.ui.theme.AccentTextColor
import com.example.vineguard.ui.theme.Background
import com.example.vineguard.ui.theme.BorderBlack
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryTextColor

@Composable
fun SelectVineyard(
    vineyards: List<Vineyard>
){
    var expand by remember{
        mutableStateOf(false)
    }
    var selectedIndex by remember {
        mutableStateOf(0)
    }
    val height by animateDpAsState(
        targetValue = if(expand) (vineyards.size * 90).dp else 80.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    Box(modifier = Modifier.fillMaxWidth().background(Primary),) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 20.dp)
                    .height(height)
                    .background(Background)
                    .clip(RoundedCornerShape(16.dp))
                    .border(width = 1.dp, color = BorderBlack, shape = RoundedCornerShape(16.dp))
            ) {
                Column {
                    Button(
                        onClick = {
                            expand = !expand
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Background
                        ),
                        shape = RoundedCornerShape(16.dp)

                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentBrightGreen)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pin),
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(22.dp)
                                        .align(alignment = Alignment.Center)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = vineyards[selectedIndex].name,
                                    fontSize = 16.sp,
                                    color = AccentTextColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${vineyards[selectedIndex].size} hectares",
                                    fontSize = 14.sp,
                                    color = PrimaryTextColor,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.down),
                                contentDescription = null,
                                tint = PrimaryTextColor,
                                modifier = Modifier.size(26.dp)
                                    .align(alignment = Alignment.CenterVertically)
                                    .scale(scaleY = if (expand) -1f else 1f, scaleX = 1f),
                            )
                        }
                    }
                    if (expand) {
                        for (i in vineyards.indices) {
                            Button(
                                onClick = {
                                    selectedIndex = i
                                    expand = !expand
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Background
                                )
                            ) {
                                Column {
                                    Text(
                                        text = vineyards[i].name,
                                        fontSize = 16.sp,
                                        color = AccentTextColor,
                                    )
                                    Text(
                                        text = "${vineyards[i].size} hectares",
                                        fontSize = 14.sp,
                                        color = PrimaryTextColor,
                                    )
                                    Divider(
                                        color = BorderBlack,
                                        thickness = 1.dp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }
            Divider(thickness = Dp.Hairline,color = BorderBlack)
        }
    }
}