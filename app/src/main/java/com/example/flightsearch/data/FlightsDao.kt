package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.FavoriteFlight
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightsDao {
    @Query(
        """
        SELECT id FROM favorite
        WHERE departure_code = :departure
        AND destination_code = :arrival
        """
    )
    suspend fun getFavoriteId(departure: String, arrival: String): Int?

    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<FavoriteFlight>>

    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<Airport>>

    @Query(
        """
        SELECT * FROM airport 
        WHERE iata_code LIKE :term || '%'
        OR name LIKE '%' || :term || '%'
        """
    )
    fun searchAirports(term: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode")
    suspend fun getAirportByIata(iataCode: String): Airport?

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code != :iataCode
        ORDER BY passengers DESC
        """
    )
    fun getOtherAirports(iataCode: String): Flow<List<Airport>>

    @Insert
    suspend fun insertFavoriteFlight(flight: FavoriteFlight)

    @Delete
    suspend fun deleteFavoriteFlight(flight: FavoriteFlight)
}