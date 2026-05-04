package com.example.vineguard.CommonFeatures

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.vineguard.CommonFeatures.domain.Route
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentBrightGreen
import com.example.vineguard.ui.theme.BorderBlack
import com.example.vineguard.ui.theme.Primary
import com.example.vineguard.ui.theme.PrimaryTextColor

@Composable
fun BottomNavigation(
    navController: NavController,
    isOpened: Boolean,
    selectedTabIndex: Int
){
    var tabs = listOf(
        BottomBarTab(
            icon = R.drawable.home,
            name = "Home",
            action = {
                navController.navigate(Route.MainScreen)
            }
        ),
        BottomBarTab(
            icon = R.drawable.stats,
            name = "Stats",
            action = {
                navController.navigate(Route.StatsScreen)
            }
        ),
        BottomBarTab(
            icon = R.drawable.book,
            name = "Diseases",
            action = {
                navController.navigate(Route.DiseaseLibraryScreen)
            }
        ),
        BottomBarTab(
            icon = R.drawable.user,
            name = "Profile",
            action = {
                navController.navigate(Route.ProfileScreen)
            }
        ),
    )
    if(isOpened) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(BorderBlack, BorderBlack.copy(.1f))
                    ),
                    shape = RoundedCornerShape(topEnd = 26.dp, topStart = 26.dp)
                )
                .background(Primary)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,

                ) {
                for (i in tabs.indices) {
                    val alpha by animateFloatAsState(
                        targetValue = if (tabs[selectedTabIndex] == tabs[i]) 1f else 0.35f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (tabs[selectedTabIndex] == tabs[i]) 1f else 0.98f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        visibilityThreshold = .000001f
                    )
                    val color by animateColorAsState(
                        targetValue = if (tabs[selectedTabIndex] == tabs[i]) Color(0xFF00C853) else PrimaryTextColor,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )

                    Column(
                        modifier = Modifier
                            .scale(scale)
                            .alpha(alpha = alpha)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                }
                            }
                    ) {
                        IconButton(
                            onClick = tabs[i].action,
                            modifier = Modifier.size(36.dp)
                                .align(alignment = Alignment.CenterHorizontally),
                        ) {
                            Icon(
                                painter = painterResource(tabs[i].icon),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = tabs[i].name,
                            fontSize = 12.sp,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

data class BottomBarTab (
    val icon: Int,
    val name: String,
    val action:() -> Unit
)
