package com.example.vineguard.StatsScreen.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentRed
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GraphData() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FA)) // Fundalul ușor albăstrui
            .padding(16.dp)
    ) {
        // --- Header cu buton înapoi și Titlu ---
        TopBar()

        Spacer(modifier = Modifier.height(16.dp))

        // --- Selector Perioadă (Today, Week, etc.) ---
        LazyRow()
        {
            item{
                DayButton(
                    text = "12 Hours",
                    selected = true,
                    action = {},
                    sensorColor = Color.Black
                )
                DayButton(
                    text = "1 day",
                    selected = true,
                    action = {},
                    sensorColor = Color.Black
                )
                DayButton(
                    text = "1 week",
                    selected = true,
                    action = {},
                    sensorColor = Color.Black
                )
                DayButton(
                    text = "1 month",
                    selected = true,
                    action = {},
                    sensorColor = Color.Black)

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Iconițe Categorii (Light, Rain, etc.) ---
        CategoryIconsRow()

        Spacer(modifier = Modifier.height(24.dp))

        // --- Cardul Principal cu Graficul de Lumină ---
        LightIntensityCard()

        Spacer(modifier = Modifier.height(24.dp))

        // --- Grid-ul de Statistici (Average, Peak, etc.) ---
        StatsGrid()
    }
}
@Composable
fun StatsGrid() {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatBox("Average", "2,380", "Lux", Color(0xFF4CAF50), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatBox("Peak", "3,120", "Lux", Color(0xFF42A5F5), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatBox("Lowest", "1,890", "Lux", Color(0xFFFF7043), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            StatBox("Duration", "12h", "Active", Color(0xFFAB47BC), Modifier.weight(1f))
        }
    }
}

@Composable
fun StatBox(label: String, value: String, unit: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = unit, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}


@Composable
fun LightIntensityCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header-ul cardului (Titlu + Badge-ul galben)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Light Intensity",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF102A43)
                    )
                )

                // Badge-ul galben cu valoarea curentă
                Surface(
                    color = Color(0xFFFBC02D),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sun), // Inlocuieste cu iconita ta
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "2,450 Lux",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Zona Graficului
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Aici vine componenta de grafic (exemplu cu Canvas sau librarie)
                SimpleAreaChart()
            }
        }
    }
}
@Composable
fun SimpleAreaChart() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path().apply {
            moveTo(0f, size.height * 0.7f)
            // Exemplu de curba Bezier pentru aspectul de "val"
            cubicTo(
                size.width * 0.2f, size.height * 0.5f,
                size.width * 0.4f, size.height * 0.1f,
                size.width * 0.5f, size.height * 0.2f
            )
            cubicTo(
                size.width * 0.7f, size.height * 0.3f,
                size.width * 0.9f, size.height * 0.6f,
                size.width, size.height * 0.8f
            )
        }

        // 1. Desenăm gradientul de sub linie
        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFBC02D).copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

        // 2. Desenăm linia galbenă groasă
        drawPath(
            path = path,
            color = Color(0xFFFBC02D),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}