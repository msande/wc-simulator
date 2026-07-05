package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class World(
    val countries: List<Country>,
    val clubs: List<Club>,
    val players: List<Player>,
) {
    fun playersOf(countryId: Int): List<Player> = players.filter { it.countryId == countryId }
    fun country(id: Int): Country = countries.first { it.id == id }
    fun player(id: Int): Player = players.first { it.id == id }
}
