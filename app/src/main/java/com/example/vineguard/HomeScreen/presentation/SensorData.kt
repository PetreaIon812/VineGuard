package com.example.vineguard.HomeScreen.presentation

import androidx.compose.ui.graphics.Color
import com.example.vineguard.HomeScreen.domain.Intensity
import com.example.vineguard.R
import com.example.vineguard.ui.theme.AccentBlue
import com.example.vineguard.ui.theme.AccentCyan
import com.example.vineguard.ui.theme.AccentGreen
import com.example.vineguard.ui.theme.AccentRed
import com.example.vineguard.ui.theme.AccentYellow
import com.example.vineguard.ui.theme.PrimaryBlue
import com.example.vineguard.ui.theme.PrimaryCyan
import com.example.vineguard.ui.theme.PrimaryGreen
import com.example.vineguard.ui.theme.PrimaryRed
import com.example.vineguard.ui.theme.PrimaryYellow
import kotlinx.serialization.Serializable

data class SensorData(
    val rain: Int = 0,            // 0 = nu ploua, 1 = ploua
    val light: Int = 100,           // în lx
    val soilMoisture: Int = 55,    // 0-100%
    val airHumidity: Int = 25,     // 0-100%
    val airTemp: Int = 20,         // °C
    val soilTemp: Int = 10,        // °C
    val soilPH: Double = 5.6,       // 1-14
    val pressure: Int = 200         // hPa
)
enum class SensorDataType(val factorName:String,val unit:String,val icon: Int,val accentColor: Color,val primaryColor: Color)  {

    RAIN(
        factorName = "Rain",
        unit = "",
        icon = R.drawable.rain,
        accentColor = AccentCyan,
        primaryColor = PrimaryCyan
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value == 0.0 -> Intensity.NORMAL     // No rain → low risk
                value == 1.0 -> Intensity.HIGH     // Rain → high risk
                else -> Intensity.MODERATE
            }
        }
    },
    LIGHT(
        factorName = "Lightness",
        unit = "lx",
        icon = R.drawable.sun,
        accentColor = AccentYellow,
        primaryColor = PrimaryYellow
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 10000.0 -> Intensity.HIGH   // Too dark → high risk
                value in 10000.0..30000.0 -> Intensity.MODERATE
                else -> Intensity.NORMAL
            }
        }
    },
    SOIL_MOISTURE(
        factorName = "Soil moisture",
        unit = "%",
        icon = R.drawable.moisture,
        accentColor = AccentBlue,
        primaryColor = PrimaryBlue
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 30.0 -> Intensity.HIGH     // Too dry
                value in 30.0..70.0 -> Intensity.MODERATE
                else -> Intensity.NORMAL            // Moist enough
            }
        }
    },
    AIR_HUMIDITY(
        factorName = "Air Humidity",
        unit = "%",
        icon = R.drawable.drop,
        accentColor = AccentCyan,
        primaryColor = PrimaryCyan

    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 40.0 -> Intensity.HIGH     // Too dry
                value in 40.0..80.0 -> Intensity.MODERATE
                else -> Intensity.NORMAL            // Safe
            }
        }
    },
    AIR_TEMPERATURE(
        factorName = "Air Temperature",
        unit = "°C",
        icon = R.drawable.temperature,
        accentColor = AccentRed,
        primaryColor = PrimaryRed
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 15.0 || value > 30.0 -> Intensity.HIGH  // Too cold or too hot
                value in 15.0..20.0 || value in 26.0..30.0 -> Intensity.MODERATE
                else -> Intensity.NORMAL
            }
        }
    },
    SOIL_TEMPERATURE(
        factorName = "Soil Temperature",
        unit = "°C",
        icon = R.drawable.temperature,
        accentColor = AccentBlue,
        primaryColor = PrimaryBlue
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 10.0 || value > 25.0 -> Intensity.HIGH
                value in 10.0..15.0 || value in 22.0..25.0 -> Intensity.MODERATE
                else -> Intensity.NORMAL
            }
        }
    },
    SOIL_PH(
        factorName = "Soil PH",
        unit = "",
        icon = R.drawable.ph,
        accentColor = AccentGreen,
        primaryColor = PrimaryGreen
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 5.5 || value > 7.5 -> Intensity.HIGH
                value in 5.5..6.0 || value in 7.0..7.5 -> Intensity.MODERATE
                else -> Intensity.NORMAL
            }
        }
    },
    PRESSURE(
        factorName = "Atmospheric Pressure",
        unit = "hPa",
        icon = R.drawable.atmospheric,
        accentColor = AccentYellow,
        primaryColor = PrimaryYellow
    ){
        override fun riskLevel(value: Double): Intensity {
            return when {
                value < 980.0 || value > 1050.0 -> Intensity.HIGH
                value in 980.0..995.0 || value in 1035.0..1050.0 -> Intensity.MODERATE
                else -> Intensity.NORMAL
            }
        }
    };
    abstract fun riskLevel(value: Double): Intensity
}


