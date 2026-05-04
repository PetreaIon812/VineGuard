package com.example.vineguard.StatsScreen.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vineguard.NHomeScreen.VineGuardViewModel
import com.example.vineguard.R
import kotlin.math.pow

data class WeatherOptions(
    val name: String,
    val unit: String,
    val icon: Int,
    val color: Color,
    val measurementPoints: List<String>
)

val weatherOptionsList = listOf(
    WeatherOptions(
        name = "UV Index",
        unit = "",
        icon = R.drawable.sun,
        color = Color(0xFFFFAC33),
        measurementPoints = listOf("10", "8", "6", "4", "2", "0")
    ),
    WeatherOptions(
        name = "Rain",
        unit = "mm",
        icon = R.drawable.rain,
        color = Color(0xFF52AEF8),
        measurementPoints = listOf("10.0mm", "9.0mm", "7.0mm", "5.0mm", "3.0mm", "1.0mm")
    ),
    WeatherOptions(
        name = "Humidity",
        unit = "%",
        icon = R.drawable.drop,
        color = Color(0xFF26C6DA),
        measurementPoints = listOf("100%", "90%", "70%", "50%", "30%", "10%")
    ),
    WeatherOptions(
        name = "Temperature",
        unit = "℃",
        icon = R.drawable.temperature,
        color = Color(0xFFEF5350),
        measurementPoints = listOf("40", "35", "30", "25", "20", "10")
    ),
    WeatherOptions(
        name = "Wind",
        unit = "m/s",
        icon = R.drawable.wind,
        color = Color(0xFF7A73DA),
        measurementPoints = listOf("10.0", "9.0", "7.0", "5.0", "3.0", "1.0")
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onGetBack: () -> Unit = {},
    viewModel: VineGuardViewModel = viewModel()
) {
    val sensors by viewModel.sensorData.collectAsStateWithLifecycle()
    val history by viewModel.historyData.collectAsStateWithLifecycle()

    var selectedWeatherIndex by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(1) } // Default: Today

    // Calculează stats folosind ViewModel
    val stats = remember(selectedWeatherIndex, selectedTab, history) {
        viewModel.getStatsForOption(selectedWeatherIndex, selectedTab)
    }

    // Calculează valoarea curentă pentru display
    var valueSensor by remember { mutableDoubleStateOf(0.0) }
    when(selectedWeatherIndex) {
        0 -> valueSensor = sensors.uv_index
        1 -> valueSensor = sensors.rain_1h_mm
        2 -> valueSensor = sensors.humidity_pct
        3 -> valueSensor = sensors.temp_c
        4 -> valueSensor = sensors.wind_avg_ms
    }

    val tabs = listOf("12 Hours", "Today", "1 Week", "2 Weeks")
    val weatherOptions = listOf(
        WeatherOption("UV Index", R.drawable.sun, Color(0xFFFFAC33)),
        WeatherOption("Rain", R.drawable.rain, Color(0xFF52AEF8)),
        WeatherOption("Humidity", R.drawable.drop, Color(0xFF26C6DA)),
        WeatherOption("Temp", R.drawable.temperature, Color(0xFFEF5350)),
        WeatherOption("Wind", R.drawable.wind, Color(0xFF7A73DA))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color=Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onGetBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    edgePadding = 0.dp,
                    indicator = { },
                    divider = { }
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (selectedTab == index) Color(0xFF00C853)
                                    else Color.White
                                )
                                .height(40.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (selectedTab == index) Color.White else Color(0xFF9E9E9E),
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weather Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weatherOptions.forEachIndexed { index, option ->
                        WeatherButton(
                            option = option,
                            isSelected = selectedWeatherIndex == index,
                            onClick = { selectedWeatherIndex = index },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Chart Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = weatherOptionsList[selectedWeatherIndex].name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF2C3E50)
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = weatherOptionsList[selectedWeatherIndex].color
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(weatherOptionsList[selectedWeatherIndex].icon),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${if(selectedWeatherIndex != 1) "%.1f".format(valueSensor) else valueSensor} ${weatherOptionsList[selectedWeatherIndex].unit}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chart
                        LightIntensityChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            selectedWeatherIndex = selectedWeatherIndex,
                            colorG = weatherOptionsList[selectedWeatherIndex].color,
                            points = stats.graphPoints,
                            yLabels = stats.yLabels,
                            xLabels = stats.xLabels
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid - Customizat pentru Rain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = if (selectedWeatherIndex == 1) "Rain %" else "Average",
                            value = stats.avg,
                            unit = if (selectedWeatherIndex == 1) "" else weatherOptionsList[selectedWeatherIndex].unit,
                            backgroundColor = Color(0xFF00C853),
                            icon = R.drawable.trendup
                        )
                        StatCard(
                            title = if (selectedWeatherIndex == 1) "Rain Events" else "Lowest",
                            value = stats.lowest,
                            unit = if (selectedWeatherIndex == 1) "times" else weatherOptionsList[selectedWeatherIndex].unit,
                            backgroundColor = Color(0xFFFF6F00),
                            icon = R.drawable.trenddown
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = if (selectedWeatherIndex == 1) "Total Rain" else "Peak",
                            value = stats.peak,
                            unit = if (selectedWeatherIndex == 1) "" else weatherOptionsList[selectedWeatherIndex].unit,
                            backgroundColor = Color(0xFF2196F3),
                            icon = R.drawable.trendup
                        )
                        StatCard(
                            title = "Variations",
                            value = try{"%.1f".format(stats.peak.toDouble() - stats.lowest.toDouble())} catch (e: Exception){stats.avg},
                            unit = if (selectedWeatherIndex == 1) "" else weatherOptionsList[selectedWeatherIndex].unit,
                            backgroundColor = Color(0xFFBB21F3),
                            icon = R.drawable.trendup
                        )
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

public fun calculateLux(
    adcValue: Int,
    vcc: Double = 3.3,
    rFixed: Double = 10000.0
): Double {
    if (adcValue <= 0) return 0.0
    if (adcValue >= 4095) return 0.1

    val voltage = (adcValue.toDouble() / 4095.0) * vcc
    val rLdr = (vcc - voltage) * rFixed / voltage
    val rLdrInKiloOhms = rLdr / 1000.0
    val lux = 500.0 / rLdrInKiloOhms.pow(1.4)

    return lux
}

data class WeatherOption(
    val label: String,
    val icon: Int,
    val selectedColor: Color
)

@Composable
fun WeatherButton(
    option: WeatherOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) option.selectedColor else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = option.label,
                tint = if (isSelected) Color.White else Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.label,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    backgroundColor: Color,
    icon: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LightIntensityChart(
    modifier: Modifier = Modifier,
    selectedWeatherIndex: Int,
    colorG: Color,
    points: List<Float>,
    yLabels: List<String>,
    xLabels: List<String>
) {
    val weatherOption = weatherOptionsList[selectedWeatherIndex]

    val textPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#9E9E9E")
            textSize = 28f
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val chartHeight = height * 0.75f
        val chartTop = height * 0.05f
        val chartBottom = chartTop + chartHeight
        val chartStartX = width * 0.12f
        val chartWidth = width * 0.88f

        // 1. DESENARE ETICHETE Y ȘI LINII ORIZONTALE
        yLabels.forEachIndexed { index, label ->
            val y = chartTop + (chartHeight * index / (yLabels.size - 1))

            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(x = chartStartX, y = y),
                end = Offset(x = chartStartX + chartWidth, y = y),
                strokeWidth = 1.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                10f,
                y + 5f,
                textPaint.apply { textAlign = android.graphics.Paint.Align.LEFT }
            )
        }

        // 2. CONSTRUIRE PATH PENTRU CURBĂ
        val path = Path()
        points.forEachIndexed { index, yRatio ->
            val xRatio = index.toFloat() / (points.size - 1).toFloat()
            val x = chartStartX + (chartWidth * xRatio)
            val y = chartBottom - (chartHeight * yRatio)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                val prevIndex = index - 1
                val prevXRatio = prevIndex.toFloat() / (points.size - 1).toFloat()
                val prevX = chartStartX + (chartWidth * prevXRatio)
                val prevY = chartBottom - (chartHeight * points[prevIndex])

                val controlX1 = prevX + (x - prevX) * 0.5f
                val controlY1 = prevY
                val controlX2 = prevX + (x - prevX) * 0.5f
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }

        // 3. DESENARE GRADIENT
        val filledPath = Path().apply {
            addPath(path)
            lineTo(chartStartX + chartWidth, chartBottom)
            lineTo(chartStartX, chartBottom)
            close()
        }

        drawPath(
            path = filledPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorG.copy(alpha = 0.25f),
                    colorG.copy(alpha = 0.07f),
                    Color.Transparent
                ),
                startY = chartTop,
                endY = chartBottom
            )
        )

        // 4. DESENARE LINIA CURBĂ
        drawPath(
            path = path,
            color = colorG,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 5. DESENARE PUNCTE
        points.forEachIndexed { index, yRatio ->
            val xRatio = index.toFloat() / (points.size - 1).toFloat()
            val x = chartStartX + (chartWidth * xRatio)
            val y = chartBottom - (chartHeight * yRatio)

            drawCircle(
                color = colorG,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // 6. DESENARE ETICHETE X
        xLabels.forEachIndexed { index, label ->
            val x = chartStartX + (chartWidth * index / (xLabels.size - 1))
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x,
                height - 10f,
                textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER }
            )
        }
    }
}