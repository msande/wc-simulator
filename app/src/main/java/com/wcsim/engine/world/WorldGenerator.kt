package com.wcsim.engine.world

import com.wcsim.engine.model.*
import kotlin.random.Random

object WorldGenerator {
    private const val PLAYERS_PER_COUNTRY = 23
    private const val CLUBS = 40

    fun generate(rng: Random): World {
        val countries = NameData.COUNTRIES.mapIndexed { i, (name, str) ->
            Country(id = i, name = name, baseStrength = str)
        }
        val clubs = (0 until CLUBS).map { id ->
            val name = "${NameData.CLUB_PREFIXES.random(rng)} ${NameData.CLUB_SUFFIXES.random(rng)}"
            Club(id = id, name = name, strength = rng.nextInt(50, 90))
        }
        var pid = 0
        val players = mutableListOf<Player>()
        for (c in countries) {
            // Position quota for a 23-man squad: 3 GK, 7 DEF, 8 MID, 5 FWD.
            val quota = listOf(
                Position.GK to 3, Position.DEF to 7, Position.MID to 8, Position.FWD to 5
            )
            for ((pos, count) in quota) {
                repeat(count) {
                    players += makePlayer(pid++, c, pos, clubs, rng)
                }
            }
        }
        return World(countries, clubs, players)
    }

    private fun makePlayer(id: Int, country: Country, pos: Position,
                           clubs: List<Club>, rng: Random): Player {
        val name = "${NameData.FIRST_NAMES.random(rng)} ${NameData.LAST_NAMES.random(rng)}"
        val age = rng.nextInt(18, 34)
        // Center ratings around country strength with position emphasis + noise.
        fun near(center: Int, spread: Int = 8) =
            (center + rng.nextInt(-spread, spread + 1)).coerceIn(30, 99)
        val base = country.baseStrength
        val r = when (pos) {
            Position.GK -> Ratings(near(base-15), near(base), 20, near(base-20), near(base-10), near(base+3))
            Position.DEF -> Ratings(near(base-4), near(base), near(base-25), near(base-8), near(base+4), 15)
            Position.MID -> Ratings(near(base-2), near(base+2), near(base-6), near(base+4), near(base-4), 12)
            Position.FWD -> Ratings(near(base+2), near(base), near(base+5), near(base-4), near(base-25), 10)
        }
        return Player(id, name, age, pos, clubId = clubs.random(rng).id,
            countryId = country.id, ratings = r)
    }
}
