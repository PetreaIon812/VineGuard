package com.example.vineguard.DiseaseLibraryScreen.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vineguard.R

data class Disease(
    val name: String,
    val description: String,
    val recommendations: List<String>,
    val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseLibraryScreen(
    onGetBack:() -> Unit = {}
) {
    val diseases = listOf(
        Disease(
            name = "Powdery Mildew",
            description = "White powdery fungal growth on leaves, shoots, and berries. Causes leaf distortion, reduced photosynthesis, and berry cracking. Thrives in warm, dry conditions with high humidity.",
            recommendations = listOf(
                "Apply sulfur-based fungicides every 10-14 days",
                "Ensure good air circulation through pruning",
                "Remove infected plant material immediately"
            ),
            imageRes = R.drawable.powdery_mildew // Replace with your image resource
        ),
        Disease(
            name = "Downy Mildew",
            description = "Yellow oil spots on upper leaf surfaces with white downy growth underneath. Can cause severe defoliation and crop loss. Develops in cool, wet conditions.",
            recommendations = listOf(
                "Apply copper-based fungicides preventatively",
                "Improve vineyard drainage",
                "Remove infected leaves promptly"
            ),
            imageRes = R.drawable.downy_mildew // Replace with your image resource
        ),
        Disease(
            name = "Black Rot",
            description = "Black Rot is a fungal disease that affects leaves, shoots, and fruits. Early symptoms appear as small, brown leaf spots with dark margins. On berries, circular brown spots expand and eventually turn black and shriveled, resembling mummified fruit.",
            recommendations = listOf(
                "Remove and destroy mummified berries and infected leaves.",
                "Prune to increase air circulation and reduce humidity within the canopy.",
                "Apply fungicides containing mancozeb, myclobutanil, or azoxystrobin early in the season"
            ),
            imageRes = R.drawable.black_rot // Replace with your image resource
        ),
        Disease(
            name = "Anthracnose",
            description = "Also known as “Bird’s-eye rot,” Anthracnose produces small, dark lesions with gray centers on leaves, shoots, and berries. Severe infections can lead to cracking and deformity of berries and shoot dieback.",
            recommendations = listOf(
                "Prune and destroy infected shoots.",
                "Avoid overhead irrigation.",
                "Apply protective fungicides such as captan, copper-based products, or mancozeb during early shoot growth."
            ),
            imageRes = R.drawable.anthracnose // Replace with your image resource
        ),
        Disease(
            name = "Botrytis(Gray Mold)",
            description = "Botrytis affects clusters, especially near harvest. Infected berries soften, turn brown, and are covered with gray, fuzzy mold. It can cause severe fruit rot in humid conditions but can also produce desirable \"noble rot\" for dessert wines under controlled conditions.",
            recommendations = listOf(
                "Avoid excessive nitrogen fertilization.",
                "Improve air flow with proper pruning and leaf removal.",
                "Remove infected clusters promptly."
            ),
            imageRes = R.drawable.botrytis // Replace with your image resource
        ),
        Disease(
            name = "Phomopsis",
            description = "Phomopsis causes small, dark lesions on young shoots and leaves early in the season. On canes, it can produce elongated cracks and pale spots; on fruit, infections cause poor berry set or shriveling.",
            recommendations = listOf(
                "Prune out and destroy infected wood during dormancy.",
                "Maintain good canopy airflow to accelerate drying.",
                "Apply fungicides (e.g., mancozeb or captan) at the early shoot growth stage."
            ),
            imageRes = R.drawable.phomopsis // Replace with your image resource
        )
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Disease Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
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
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(diseases) { disease ->
                DiseaseCard(disease = disease)
            }
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun DiseaseCard(disease: Disease) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Disease Image
            Image(
                painter = painterResource(id = disease.imageRes),
                contentDescription = disease.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Disease Name and Risk Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = disease.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = disease.description,
                    fontSize = 14.sp,
                    color = Color(0xFF616161),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Recommendations Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDBF3E5)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.plant),
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recommendations",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00C853)
                            )
                        }

                        disease.recommendations.forEach { recommendation ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = Color(0xFF00C853),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recommendation,
                                    fontSize = 13.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}