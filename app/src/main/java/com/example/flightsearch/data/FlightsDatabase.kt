package com.example.flightsearch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.FavoriteFlight

@Database(entities = [Airport::class, FavoriteFlight::class], version = 1)
abstract class FlightsDatabase : RoomDatabase() {
    abstract fun flightsDao(): FlightsDao

    companion object {
        @Volatile
        private var instance: FlightsDatabase? = null

        fun getDatabase(context: Context): FlightsDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, FlightsDatabase::class.java, "flights")
                    .createFromAsset("database/flight_search.db")
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }

        }

    }
}