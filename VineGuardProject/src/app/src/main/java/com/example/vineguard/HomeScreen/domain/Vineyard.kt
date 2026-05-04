package com.example.vineguard.HomeScreen.domain

data class Vineyard(
    val id: Int,
    val name: String,
    val type: String,
    val size: Float
)
//Warnings lists for vineyard
//History data pentru istoric

val Vineyards = listOf(
    Vineyard(
        id = 0,
        name = "Aligote",
        type = "Aligote",
        size = 1.3f
    ),
    Vineyard(
        id = 1,
        name = "Sovinion",
        type = "Moldova",
        size = 0.7f
    ),
    Vineyard(
        id = 2,
        name = "Badea Visa",
        type = "Modlova",
        size = 0.6f
    ),
    Vineyard(
        id = 3,
        name = "Viorica",
        type = "Viorica",
        size = 0.7f
    ),

)