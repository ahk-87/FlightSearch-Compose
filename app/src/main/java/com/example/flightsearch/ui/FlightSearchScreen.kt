package com.example.flightsearch.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.data.FlightsRepository
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.FavoriteFlight
import com.example.flightsearch.data.model.Flight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun FlightSearchScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: FlightsViewModel = viewModel(factory = FlightsViewModel.factory)
) {
    val foundAirports by viewModel.foundAirports.collectAsState(emptyList())
    val flights by viewModel.flights.collectAsState(emptyList())
    val favFlights by viewModel.favoriteFlights.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        MainTextField(
            input = viewModel.input,
            onInputChange = viewModel::inputChanged,
            modifier = Modifier.padding(16.dp)
        )
        Box {
            if (!viewModel.showFavoriteFlights) {
                FlightsList(
                    title = "Flights from ${viewModel.iataCode}",
                    cardColor = Color(0xFFD2F4FA),
                    flights = flights,
                    favoriteChanged = viewModel::favoriteChanged,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                if (favFlights.isNotEmpty()) {
                    FlightsList(
                        title = "Favorite flights",
                        cardColor = MaterialTheme.colorScheme.inverseSurface,
                        flights = favFlights,
                        favoriteChanged = viewModel::favoriteChanged,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    Box(modifier = modifier.fillMaxSize()) {
                        Text(
                            text = "No favorite flights",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            SuggestionList(
                airports = foundAirports,
                onSelectAirport = viewModel::showFlightsFromAirport,
            )
        }
    }
}


@Composable
fun MainTextField(
    input: String,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lfm = LocalFocusManager.current

    BackHandler(enabled = input.isNotEmpty()) {
        onInputChange("")
    }

    TextField(
        value = input,
        singleLine = true,
        onValueChange = onInputChange,
        placeholder = { Text(text = "Search airport") },
        keyboardActions = KeyboardActions(
            onDone = { lfm.clearFocus() }
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            FilledTonalIconButton(
                onClick = { },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardVoice,
                    contentDescription = null,
                )
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = modifier.fillMaxWidth()
    )
}

var lastAirports = emptyList<Airport>()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuggestionList(
    airports: List<Airport>,
    onSelectAirport: (Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    val lfm = LocalFocusManager.current
    if (airports.isNotEmpty()) lastAirports = airports

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        AnimatedVisibility(visible = airports.isNotEmpty()) {
            LazyColumn {
                items(airports.ifEmpty { lastAirports }, key = { it.id }) { airport ->
                    AirportText(
                        iataCode = airport.iataCode,
                        name = airport.name,
                        modifier = Modifier
                            .heightIn(min = 32.dp)
                            .fillMaxWidth()
                            .animateItemPlacement()
                            .clickable {
                                lastAirports = emptyList()
                                lfm.clearFocus()
                                onSelectAirport(airport)
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun FlightsList(
    title: String,
    cardColor: Color,
    flights: List<Flight>,
    favoriteChanged: (Flight) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            state = LazyListState(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(flights, key = { it.hashCode() }) { flight ->
                FlightCard(
                    flight = flight,
                    cardColor = cardColor,
                    favoriteChanged = { favoriteChanged(flight) }
                )
            }
        }
    }
}

@Composable
fun FlightCard(
    flight: Flight,
    favoriteChanged: () -> Unit,
    cardColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(topEnd = 24.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                DestinationText(
                    destination = "DEPART",
                    airport = flight.fromAirport
                )
                Spacer(modifier = Modifier.height(16.dp))
                DestinationText(
                    destination = "ARRIVE",
                    airport = flight.toAirport
                )
            }
            StarIcon(isFavoriteFlight = flight.isFavorite, favoriteChanged = favoriteChanged)
        }
    }
}

@Composable
fun StarIcon(
    isFavoriteFlight: Boolean,
    favoriteChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(isFavoriteFlight) }
    Icon(
        tint = if (isFavorite) Color(0xFFCE5A0C) else Color(0xFF999999),
        imageVector = Icons.Rounded.Star,
        contentDescription = null,
        modifier = modifier
            .size(48.dp)
            .clickable {
                isFavorite = !isFavorite
                favoriteChanged()
            }
    )
}

@Composable
fun DestinationText(destination: String, airport: Airport) {
    Text(
        text = destination,
        fontSize = 16.sp,
        fontWeight = FontWeight.W500,
        modifier = Modifier.padding(start = 4.dp)
    )
    AirportText(
        iataCode = airport.iataCode,
        name = airport.name
    )
}

@Composable
fun AirportText(
    iataCode: String,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = iataCode,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.W900,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name)
    }
}


@Preview
@Composable
fun FlightSearchScreenPreview() {
    FlightSearchScreen(
        viewModel = FlightsViewModel(
            object : FlightsRepository {
                override fun getAllAirports(): Flow<List<Airport>> {
                    return flowOf(listOf(Airport(1, "FCO", "Washington", 23)))
                }

                override fun getAllFavoriteFlights(): Flow<List<FavoriteFlight>> {
                    return flowOf(listOf(FavoriteFlight(1, "FCO", "FCO")))
                }

                override fun searchAirports(term: String): Flow<List<Airport>> {
                    return flowOf(listOf(Airport(1, "FCO", "Washington", 23)))
                }

                override fun getOtherAirports(iataCode: String): Flow<List<Airport>> {
                    return flowOf(emptyList())
                }

                override suspend fun addFavoriteFlight(favoriteFlight: FavoriteFlight): Int {
                    return 1
                }

                override suspend fun removeFavoriteFlight(favoriteFlight: FavoriteFlight) {
                }

                override suspend fun getIdFavorite(from: String, to: String): Int {
                    return 1
                }

                override suspend fun getAirportByIata(iataCode: String): Airport {
                    return Airport(1, "FCO", "Washington", 23)
                }
            }
        )
    )
}

@Preview
@Composable
fun FlightCardPreview() {
    val a1 = Airport(1, "FCO", "Washington", 23)
    val a2 = Airport(1, "FCO", "Sofia", 23)
    FlightCard(
        flight = Flight(0, a1, a2, true),
        favoriteChanged = {},
        cardColor = Color(0xFFD2F4FA)
    )
}