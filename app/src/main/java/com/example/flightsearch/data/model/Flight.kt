package com.example.flightsearch.data.model

data class Flight(
    var id: Int = 0,
    val fromAirport: Airport,
    val toAirport: Airport,
    var isFavorite: Boolean = false
)

fun Flight.toFavoriteFlight() =
    FavoriteFlight(id, fromAirport.iataCode, toAirport.iataCode)
