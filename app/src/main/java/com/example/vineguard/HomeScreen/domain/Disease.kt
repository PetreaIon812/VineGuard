package com.example.vineguard.HomeScreen.domain

import androidx.compose.ui.graphics.Color
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentGreenTextColor
import com.example.vineguard.ui.theme.AccentRedTextColor
import com.example.vineguard.ui.theme.AccentYellowTextColor
import com.example.vineguard.ui.theme.LvlGreen
import com.example.vineguard.ui.theme.LvlRed
import com.example.vineguard.ui.theme.LvlYellow
import com.example.vineguard.ui.theme.PrimaryLvlGreen
import com.example.vineguard.ui.theme.PrimaryLvlRed
import com.example.vineguard.ui.theme.PrimaryLvlYellow
import com.example.vineguard.ui.theme.SecondaryLvlGreen
import com.example.vineguard.ui.theme.SecondaryLvlRed
import com.example.vineguard.ui.theme.SecondaryLvlYellow

data class Disease(
    val name: String,
    val description: String,
    val recommendations: List<String>,
)


val diseases = listOf(
    Disease(
        name = "Downy Mildew",
        description = "Moist conditions and warm temperatures detected, favorable for downy mildew infection.",
        recommendations = listOf(
            "Apply copper-based fungicide",
            "Improve air circulation",
            "Monitor leaves and clusters daily"
        )
    ),
    Disease(
        name = "Powdery Mildew",
        description = "Warm temperatures and high humidity favor powdery mildew development.",
        recommendations = listOf(
            "Apply sulfur-based fungicide",
            "Remove dense shoots to improve airflow",
            "Inspect grapes regularly"
        )
    ),
    Disease(
        name = "Botrytis Bunch Rot",
        description = "Wet conditions and dense grape clusters increase risk of Botrytis.",
        recommendations = listOf(
            "Remove infected clusters",
            "Increase air circulation",
            "Apply protective fungicide if needed"
        )
    ),
    Disease(
        name = "Black Rot",
        description = "Temperatures and humidity suitable for black rot infection.",
        recommendations = listOf(
            "Apply fungicide",
            "Remove mummified berries",
            "Monitor vineyard weekly"
        )
    ),
    Disease(
        name = "Phomopsis Cane and Leaf Spot",
        description = "Moderate temperatures and rainfall favor Phomopsis infection.",
        recommendations = listOf(
            "Prune infected canes",
            "Apply fungicide preventively",
            "Improve vineyard airflow"
        )
    ),
    Disease(
        name = "Anthracnose",
        description = "Warm and wet conditions detected, favorable for anthracnose.",
        recommendations = listOf(
            "Remove infected shoots",
            "Apply fungicide",
            "Monitor leaves and fruits"
        )
    ),
    Disease(
        name = "Esca (Black Measles)",
        description = "Older vines under humid conditions are at risk of Esca fungal infection.",
        recommendations = listOf(
            "Prune infected wood",
            "Monitor vine health",
            "Avoid wounding vines"
        )
    ),
    Disease(
        name = "Eutypa Dieback",
        description = "Canker symptoms detected due to Eutypa fungal infection.",
        recommendations = listOf(
            "Prune infected canes",
            "Seal pruning cuts",
            "Apply protective fungicide"
        )
    ),
    Disease(
        name = "Pierce’s Disease",
        description = "Hot and dry conditions combined with insect vectors increase risk.",
        recommendations = listOf(
            "Control sharpshooter insects",
            "Remove infected vines",
            "Plant resistant grape varieties"
        )
    ),
    Disease(
        name = "Crown Gall",
        description = "Soil-borne bacteria causing tumorous growths detected.",
        recommendations = listOf(
            "Remove infected plants",
            "Avoid root injuries",
            "Use disease-free planting material"
        )
    ),
    Disease(
        name = "Lime-Induced Chlorosis",
        description = "High soil pH detected, leading to nutrient deficiencies and chlorosis.",
        recommendations = listOf(
            "Apply chelated iron",
            "Adjust soil pH gradually",
            "Monitor leaf color"
        )
    ),
    Disease(
        name = "Bitter Rot",
        description = "Warm, humid conditions detected, favorable for bitter rot infection.",
        recommendations = listOf(
            "Remove infected fruit",
            "Apply fungicide",
            "Improve air circulation"
        )
    ),
    Disease(
        name = "Alternaria Rot",
        description = "High humidity and warm temperatures favor Alternaria rot.",
        recommendations = listOf(
            "Remove infected berries",
            "Apply fungicide",
            "Ensure good vineyard hygiene"
        )
    ),
    Disease(
        name = "Rust",
        description = "Warm and moist conditions detected, suitable for rust infection.",
        recommendations = listOf(
            "Apply fungicide",
            "Remove infected leaves",
            "Monitor vineyard weekly"
        )
    ),
    Disease(
        name = "White Rot",
        description = "Wet conditions with dense clusters detected, favorable for white rot.",
        recommendations = listOf(
            "Remove infected clusters",
            "Apply fungicide",
            "Maintain proper airflow"
        )
    ),
    Disease(
        name = "Armillaria Root Rot",
        description = "Fungal infection detected in older vines under wet soil conditions.",
        recommendations = listOf(
            "Remove infected vines",
            "Improve soil drainage",
            "Avoid replanting in the same soil"
        )
    ),
    Disease(
        name = "Ripe Rot",
        description = "Warm and humid conditions detected during ripening, favorable for ripe rot.",
        recommendations = listOf(
            "Remove infected clusters",
            "Apply fungicide preventively",
            "Monitor grapes closely"
        )
    ),
    Disease(
        name = "Leafroll Virus",
        description = "Virus causing leaf rolling detected on vines.",
        recommendations = listOf(
            "Remove infected vines",
            "Control insect vectors",
            "Plant virus-free propagation material"
        )
    ),
    Disease(
        name = "Grapevine Fanleaf Virus",
        description = "Leaf distortion and poor vine development observed, caused by virus.",
        recommendations = listOf(
            "Remove infected vines",
            "Use nematode-free rootstock",
            "Monitor new plantings"
        )
    ),
    Disease(
        name = "Grape Yellows (Flavescence dorée)",
        description = "Yellowing symptoms and reduced vine vigor detected, likely due to phytoplasma.",
        recommendations = listOf(
            "Remove infected vines",
            "Control vector insects",
            "Use certified healthy planting material"
        )
    ),
    Disease(
        name = "Grapevine Red Blotch Disease",
        description = "Red discoloration on leaves detected, caused by viral infection.",
        recommendations = listOf(
            "Remove infected vines",
            "Plant virus-free material",
            "Monitor vectors and new plants"
        )
    ),
    Disease(
        name = "Sour Rot",
        description = "Wet and warm conditions detected, leading to sour rot on grapes.",
        recommendations = listOf(
            "Remove infected clusters",
            "Apply fungicide",
            "Improve vineyard airflow"
        )
    ),
    Disease(
        name = "Bacterial Blight",
        description = "High humidity detected, favorable for bacterial blight infection.",
        recommendations = listOf(
            "Remove infected leaves and shoots",
            "Apply bactericide if needed",
            "Improve ventilation"
        )
    ),
    Disease(
        name = "Crown and Root Rot (Fusarium)",
        description = "Warm, wet soil conditions detected, favorable for Fusarium infection.",
        recommendations = listOf(
            "Remove infected plants",
            "Improve soil drainage",
            "Avoid replanting in same soil"
        )
    ),
    Disease(
        name = "Charcoal Rot",
        description = "High temperature and low soil moisture detected, increasing charcoal rot risk.",
        recommendations = listOf(
            "Irrigate properly",
            "Remove infected plants",
            "Maintain soil health"
        )
    ),
    Disease(
        name = "Verticillium Wilt",
        description = "Soil-borne fungi detected, causing wilting and yellowing of vines.",
        recommendations = listOf(
            "Remove infected plants",
            "Use resistant rootstocks",
            "Improve soil drainage"
        )
    ),
    Disease(
        name = "Xylella-Related Wilt",
        description = "Vascular wilt symptoms detected, likely caused by Xylella bacteria.",
        recommendations = listOf(
            "Remove infected vines",
            "Control insect vectors",
            "Plant resistant varieties"
        )
    ),
    Disease(
        name = "Phytophthora Root Rot",
        description = "Waterlogged soil detected, favorable for Phytophthora infection.",
        recommendations = listOf(
            "Improve drainage",
            "Remove infected roots",
            "Avoid replanting in same soil"
        )
    ),
    Disease(
        name = "Angular Leaf Spot",
        description = "Warm and humid conditions detected, favorable for angular leaf spot.",
        recommendations = listOf(
            "Remove infected leaves",
            "Apply fungicide",
            "Monitor vineyard weekly"
        )
    ),
    Disease(
        name = "Septoria Leaf Spot",
        description = "Leaf spots detected under moderate humidity and temperature.",
        recommendations = listOf(
            "Remove infected leaves",
            "Apply fungicide",
            "Ensure proper spacing"
        )
    ),
    Disease(
        name = "Cercospora Leaf Spot",
        description = "Moderate humidity and temperature detected, favoring Cercospora infection.",
        recommendations = listOf(
            "Remove infected leaves",
            "Apply fungicide",
            "Maintain good airflow"
        )
    ),
    Disease(
        name = "Ramsay Disease",
        description = "Symptoms of leaf distortion and stunted growth detected.",
        recommendations = listOf(
            "Remove infected vines",
            "Use certified planting material",
            "Monitor vectors"
        )
    ),
    Disease(
        name = "Diplodia Cane Rot",
        description = "Wet conditions detected, promoting Diplodia infection of canes.",
        recommendations = listOf(
            "Prune infected canes",
            "Apply fungicide",
            "Improve vineyard airflow"
        )
    ),
    Disease(
        name = "Phylloxera Root Damage",
        description = "Root damage and stunted growth detected, likely caused by phylloxera.",
        recommendations = listOf(
            "Use resistant rootstocks",
            "Remove infected plants",
            "Monitor roots regularly"
        )
    ),
    Disease(
        name = "Algae Leaf Spot",
        description = "Leaf spots and algae growth detected under humid conditions.",
        recommendations = listOf(
            "Remove infected leaves",
            "Improve air circulation",
            "Avoid excessive moisture on leaves"
        )
    ),
    Disease(
        name = "Bird’s Eye Spot",
        description = "Circular lesions detected on leaves, high humidity present.",
        recommendations = listOf(
            "Remove infected tissues",
            "Apply fungicide",
            "Monitor vineyard weekly"
        )
    ),
    Disease(
        name = "Black Foot Disease",
        description = "Young vines affected by soil-borne fungi causing root damage.",
        recommendations = listOf(
            "Remove infected plants",
            "Use healthy nursery stock",
            "Improve soil drainage"
        )
    ),
    Disease(
        name = "Bunch Stem Necrosis",
        description = "Necrotic symptoms on stems detected, favoring secondary infections.",
        recommendations = listOf(
            "Prune affected stems",
            "Apply fungicide",
            "Monitor vine health"
        )
    ),
    Disease(
        name = "Cane Necrosis",
        description = "Necrotic lesions on canes detected under wet conditions.",
        recommendations = listOf(
            "Prune necrotic canes",
            "Apply protective fungicide",
            "Monitor for secondary infections"
        )
    ),
    Disease(
        name = "Pink Berry Disease",
        description = "High humidity and temperature detected, causing pink discoloration of berries.",
        recommendations = listOf(
            "Remove affected berries",
            "Apply fungicide",
            "Monitor vineyard daily"
        )
    ),
    Disease(
        name = "Grapevine Virus A",
        description = "Virus symptoms detected, affecting leaves and fruit development.",
        recommendations = listOf(
            "Remove infected vines",
            "Use virus-free planting material",
            "Control insect vectors"
        )
    ),
    Disease(
        name = "Grapevine Virus B",
        description = "Virus symptoms detected, causing leaf deformation and reduced vigor.",
        recommendations = listOf(
            "Remove infected vines",
            "Plant certified virus-free material",
            "Monitor vectors"
        )
    ),
    Disease(
        name = "Grape Anthracnose (Bird’s Eye)",
        description = "Circular lesions on leaves and fruit detected, high humidity present.",
        recommendations = listOf(
            "Remove infected tissues",
            "Apply fungicide",
            "Monitor vineyard weekly"
        )
    ),
    Disease(
        name = "Phomopsis Leaf Spot",
        description = "Moderate temperatures with rainfall favor Phomopsis leaf spot.",
        recommendations = listOf(
            "Remove infected canes",
            "Apply protective fungicide",
            "Improve airflow"
        )
    ),
    Disease(
        name = "Powdery Scab",
        description = "High soil moisture and moderate temperature detected, favorable for scab.",
        recommendations = listOf(
            "Apply fungicide",
            "Avoid overhead irrigation",
            "Monitor for lesions"
        )
    ),
    Disease(
        name = "Black Spot",
        description = "Warm and humid conditions detected, suitable for black spot infection.",
        recommendations = listOf(
            "Apply fungicide",
            "Remove infected leaves",
            "Monitor vineyard weekly"
        )
    ),
    Disease(
        name = "Leaf Blight",
        description = "Moderate temperature and high humidity increase leaf blight risk.",
        recommendations = listOf(
            "Remove affected leaves",
            "Apply fungicide",
            "Ensure good vine spacing"
        )
    ),
    Disease(
        name = "Stem Canker",
        description = "High soil moisture with moderate pH favors stem canker.",
        recommendations = listOf(
            "Prune affected stems",
            "Apply fungicide",
            "Improve soil drainage"
        )
    ),
    Disease(
        name = "Leaf Spot Virus",
        description = "Mild viral infection detected, causing leaf spots.",
        recommendations = listOf(
            "Remove infected leaves",
            "Control insect vectors",
            "Plant virus-free vines"
        )
    ),
    Disease(
        name = "Bud Necrosis",
        description = "Low temperature and low soil moisture detected, causing bud death.",
        recommendations = listOf(
            "Avoid frost exposure",
            "Maintain soil moisture",
            "Remove necrotic buds"
        )
    )
)
enum class Intensity(val level: String, val accentColor: Color, val primaryColor: Color,val secondaryColor: Color,val textColor: Color,val icon: Int){
    HIGH("High", LvlRed, PrimaryLvlRed, SecondaryLvlRed, AccentRedTextColor,R.drawable.warning),
    MODERATE("Moderate", LvlYellow, PrimaryLvlYellow, SecondaryLvlYellow, AccentYellowTextColor,R.drawable.view),
    NORMAL("Normal", LvlGreen, PrimaryLvlGreen, SecondaryLvlGreen, AccentGreenTextColor,R.drawable.ph)
}