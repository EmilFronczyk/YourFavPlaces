package com.example.yourfavplaces.models

import java.io.Serializable

data class YourFavPlaceModule (
    val id: Int,
    val title: String,
    val pathOfImage: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
): Serializable //Mozemy przekazywac obiekt