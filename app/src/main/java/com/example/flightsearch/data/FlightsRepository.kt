package com.example.flightsearch.data

import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.FavoriteFlight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

interface FlightsRepository {
    fun getAllAirports(): Flow<List<Airport>>
    fun getAllFavoriteFlights(): Flow<List<FavoriteFlight>>
    fun searchAirports(term: String): Flow<List<Airport>>
    fun getOtherAirports(iataCode: String): Flow<List<Airport>>

    suspend fun addFavoriteFlight(favoriteFlight: FavoriteFlight): Int
    suspend fun removeFavoriteFlight(favoriteFlight: FavoriteFlight)
    suspend fun getIdFavorite(from: String, to: String): Int?
    suspend fun getAirportByIata(iataCode: String): Airport?
}

class DatabaseFlightsRepository(private val dao: FlightsDao) : FlightsRepository {
    override fun getAllAirports(): Flow<List<Airport>> {
        return dao.getAllAirports()
    }

    override fun getAllFavoriteFlights(): Flow<List<FavoriteFlight>> {
        return dao.getAllFavorites()
    }

    override fun searchAirports(term: String): Flow<List<Airport>> {
        return if (term.isEmpty()) flowOf(emptyList()) else dao.searchAirports(term)
    }

    override fun getOtherAirports(iataCode: String): Flow<List<Airport>> {
        return dao.getOtherAirports(iataCode)
    }

    override suspend fun addFavoriteFlight(favoriteFlight: FavoriteFlight): Int {
        dao.insertFavoriteFlight(favoriteFlight)
        return dao.getFavoriteId(favoriteFlight.departureCode, favoriteFlight.destinationCode)!!
    }

    override suspend fun removeFavoriteFlight(favoriteFlight: FavoriteFlight) {
        dao.deleteFavoriteFlight(favoriteFlight)
    }

    override suspend fun getIdFavorite(from: String, to: String): Int? {
        return dao.getFavoriteId(from, to)
    }

    override suspend fun getAirportByIata(iataCode: String): Airport? {
        return dao.getAirportByIata(iataCode)
    }
}