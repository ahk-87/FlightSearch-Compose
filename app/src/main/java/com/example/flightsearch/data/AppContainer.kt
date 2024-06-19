package com.example.flightsearch.data

import android.content.Context

interface AppContainer {
    val flightsRepository: FlightsRepository
}

class DefaultContainer(context: Context) : AppContainer {

    override val flightsRepository: FlightsRepository by lazy {
        DatabaseFlightsRepository(FlightsDatabase.getDatabase(context).flightsDao())
    }

}