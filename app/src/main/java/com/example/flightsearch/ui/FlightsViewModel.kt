package com.example.flightsearch.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.MyApplication
import com.example.flightsearch.data.FlightsRepository
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.Flight
import com.example.flightsearch.data.model.toFavoriteFlight
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlightsViewModel(private val repository: FlightsRepository) : ViewModel() {

    var input by mutableStateOf("")
        private set

    var showFavoriteFlights by mutableStateOf(true)
        private set

    var iataCode by mutableStateOf("")
        private set

    var foundAirports by mutableStateOf(emptyFlow<List<Airport>>())
        private set

    val favoriteFlights = repository.getAllFavoriteFlights().map { list ->
        list.map {
            val fromAirport = repository.getAirportByIata(it.departureCode)
            val toAirport = repository.getAirportByIata(it.destinationCode)
            Flight(it.id, fromAirport!!, toAirport!!, true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var flights = flowOf(emptyList<Flight>())
        private set

    fun inputChanged(newInput: String) {
        input = newInput

        if (input.isEmpty() && !showFavoriteFlights) {
            showFavoriteFlights = true
            flights = flowOf(emptyList())
        }
        foundAirports = repository.searchAirports(input)
    }

    fun showFlightsFromAirport(airport: Airport) {
        showFavoriteFlights = false
        input = airport.iataCode
        iataCode = airport.iataCode

        val airportIata = airport.iataCode

        foundAirports = flowOf(emptyList())
        flights = repository.getOtherAirports(airportIata).map {
            it.map { toAirport ->
                val fav = repository.getIdFavorite(airportIata, toAirport.iataCode)
                Flight(fav ?: 0, airport, toAirport, fav != null)
            }
        }
    }

    fun favoriteChanged(flight: Flight) {
        val isFavorite = flight.isFavorite
        flight.isFavorite = !isFavorite

        viewModelScope.launch {
            if (!isFavorite)
                flight.id = repository.addFavoriteFlight(flight.toFavoriteFlight())
            else
                repository.removeFavoriteFlight(flight.toFavoriteFlight())
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MyApplication)
                val repository = application.container.flightsRepository
                FlightsViewModel(repository)
            }
        }
    }
}
