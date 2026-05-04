package com.example.vineguard.NHomeScreen

import android.R.attr.value
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vineguard.ui.theme.VineguardAppTheme
import com.example.vineguard.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.pow

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VineguardAppTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: VineGuardViewModel = viewModel()) {
    val sensors by viewModel.sensorData.collectAsStateWithLifecycle()
    val prediction by viewModel.aiPrediction.collectAsStateWithLifecycle()
    val selectedNode by viewModel.selectedNode.collectAsStateWithLifecycle()

    val animateUV by animateFloatAsState(
        targetValue = sensors.uv_index.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sensorAnimation"
    )
    val animateRain by animateFloatAsState(
        targetValue = sensors.rain_1h_mm.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sensorAnimation"
    )
    val animateHumidity by animateFloatAsState(
        targetValue = sensors.humidity_pct.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sensorAnimation"
    )
    val animateTemperature by animateFloatAsState(
        targetValue = sensors.temp_c.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sensorAnimation"
    )
    val animateWind by animateFloatAsState(
        targetValue = sensors.wind_avg_ms.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "sensorAnimation"
    )

    var expandedDropdown by remember { mutableStateOf(false) }

    val sensorData = listOf(
        SensorCard("UV Index", "%.1f".format(animateUV), "uv index", Color(0xFFFFA726), R.drawable.sun),
        SensorCard("Rain 1h", "${"%.1f".format(animateRain)} mm", "mm", Color(0xFF42A5F5), R.drawable.rain),
        SensorCard("Humidity", "${"%.1f".format(animateHumidity)} %", "relative", Color(0xFF26C6DA), R.drawable.moisture),
        SensorCard("Temperature", "${"%.1f".format(animateTemperature)}°C", "ambient", Color(0xFFEF5350), R.drawable.temperature),
        SensorCard("Wind", "${"%.1f".format(animateWind)} m/s", "avg speed", Color(0xFF9575CD), R.drawable.drop)
    )

    val sortedList = prediction.predictions.map { (_, diseaseInfo) ->
        PredictionEntry(
            name = diseaseInfo.label_ro,
            percentage = diseaseInfo.probability * 100.0,
            riskLevel = diseaseInfo.risk_level
        )
    }.sortedByDescending { it.percentage }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Header Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00C853),
                                    Color(0xFF00E676)
                                )
                            )
                        )
                        .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.user),
                                    contentDescription = "Profile",
                                    tint = Color(0xFF00C853),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Welcome back,",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "User",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFEF5350),
                                        modifier = Modifier.offset(x = 3.dp, y = 0.dp)
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.notification),
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Node Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-16).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SELECT NODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedNode,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    Icon(
                                        painter = if (expandedDropdown)
                                            painterResource(R.drawable.up_arrow)
                                        else
                                            painterResource(R.drawable.down_arrow),
                                        contentDescription = null,
                                        tint = Color(0xFF00C853),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00C853),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedTextColor = Color(0xFF2C3E50),
                                    unfocusedTextColor = Color(0xFF2C3E50)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                viewModel.availableNodes.forEach { nodeId ->
                                    DropdownMenuItem(
                                        text = { Text(nodeId) },
                                        onClick = {
                                            viewModel.selectNode(nodeId)
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sensor Data Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Sensor Data",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // First row - UV Index and Rain
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SensorDataCard(
                            sensor = sensorData[0],
                            modifier = Modifier.weight(1f)
                        )
                        SensorDataCard(
                            sensor = sensorData[1],
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Second row - Humidity and Temperature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SensorDataCard(
                            sensor = sensorData[2],
                            modifier = Modifier.weight(1f)
                        )
                        SensorDataCard(
                            sensor = sensorData[3],
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Third row - Wind (full width)
                    SensorDataCard(
                        sensor = sensorData[4],
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Possible Diseases Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp)
                ) {
                    Text(
                        text = "Possible Diseases",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Disease Cards
            items(items = sortedList, key = { it.name }) { disease ->
                DiseaseCard(
                    disease = disease,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

data class SensorCard(
    val label: String,
    val value: String,
    val unit: String,
    val color: Color,
    val icon: Int
)

data class DiseaseData(
    val name: String,
    val risk: String,
    val percentage: Int,
    val riskColor: Color,
    val description: String
)

@Composable
fun SensorDataCard(
    sensor: SensorCard,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (sensor.label) {
        "UV Index" -> listOf(Color(0xFFFFAC33), Color(0xFFFF9800))
        "Rain 1h" -> listOf(Color(0xFF52AEF8), Color(0xFF2196F3))
        "Humidity" -> listOf(Color(0xFF26C6DA), Color(0xFF00BCD4))
        "Temperature" -> listOf(Color(0xFFEF5350), Color(0xFFEC407A))
        "Wind" -> listOf(Color(0xFF7A73DA), Color(0xFF7E57C2))
        else -> listOf(sensor.color, sensor.color)
    }

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = sensor.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(sensor.icon),
                        contentDescription = sensor.label,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = sensor.label,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = sensor.value,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sensor.unit,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DiseaseCard(
    disease: PredictionEntry,
    modifier: Modifier = Modifier
) {
    var animationPlayed by rememberSaveable(key = disease.name) {
        mutableStateOf(false)
    }
    // 2. Definim animația pentru procent (de la 0 la valoarea reală)
    val curPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) disease.percentage.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = 1000, // 1 secundă
            delayMillis = 100,
            easing = FastOutSlowInEasing
        ), label = "progressAnimation"
    )

    // Pornim animația imediat ce Composable-ul este desenat
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }
    var riskColor = when (disease.riskLevel) {
        "SCAZUT" -> Color(0XFF66BB6A)
        "MEDIU"  -> Color(0XFFFFA726)
        "RIDICAT" -> Color(0XFFEF5350)
        else -> Color(0XFF66BB6A)
    }
    var riskLevel = when (disease.riskLevel) {
        "SCAZUT"  -> "Low"
        "MEDIU"   -> "Medium"
        "RIDICAT" -> "High"
        else -> "Low"
    }




    if(disease.name == "Healthy")
    {
        if(riskLevel == "Low")
            riskLevel = "High"
        else if(riskLevel == "High")
            riskLevel = "Low"
        if(riskColor == Color(0XFF66BB6A))
            riskColor = Color(0XFFEF5350)
        else if(riskColor == Color(0XFFEF5350))
            riskColor = Color(0XFF66BB6A)
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = disease.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = riskColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        // Folosim curPercentage.toInt() pentru ca numerele să "crească" vizual
                        text = riskLevel,
                        color = riskColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar Animată
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            // Animăm lățimea barei folosind curPercentage
                            .fillMaxWidth(curPercentage / 100f)
                            .background(riskColor)
                    )
                }

                Text(
                    text = "${"%.1f".format(curPercentage)}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =
                    if(disease.name != "Healthy") {
                        if (riskLevel == "High")
                            "High risk! Immediate intervention with specific fungicides is recommended to prevent spreading."
                        else if (riskLevel == "Medium")
                            "Moderate risk. Consider preventive treatment and check for early physical symptoms."
                        else
                            "Low risk detected. Continue routine monitoring and ensure proper ventilation."
                    }
                else{
                        if (riskLevel == "High")
                            "Healthy status is losing confidence. AI is detecting subtle patterns that might indicate the onset of a condition."
                        else if (riskLevel == "Medium")
                            "The system indicates a healthy state, but environmental factors are shifting. Keep an eye on recent sensor alerts."
                        else
                            "The vines appear healthy. Environmental conditions are stable and the risk of infection remains very low."
                }
                ,
                fontSize = 13.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
        }
    }
}


data class PredictionEntry(
    val name: String,
    val percentage: Double,
    val riskLevel: String = "SCAZUT"
)