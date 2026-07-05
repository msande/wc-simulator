package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class Ratings(
    val pace: Int, val stamina: Int, val finishing: Int,
    val passing: Int, val defending: Int, val goalkeeping: Int,
)

@Serializable
data class StatLine(
    val appearances: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val tournaments: Int = 0, // only meaningful for WC stats
)

@Serializable
data class Player(
    val id: Int,
    val name: String,
    val age: Int,
    val position: Position,
    val clubId: Int,
    val ratings: Ratings,
    val clubStats: StatLine = StatLine(),
    val wcStats: StatLine = StatLine(),
) {
    /** Position-weighted 40..99 overall. */
    val overall: Int get() {
        val r = ratings
        val raw = when (position) {
            Position.GK -> r.goalkeeping * 0.85 + r.defending * 0.15
            Position.DEF -> r.defending * 0.55 + r.pace * 0.15 + r.stamina * 0.15 + r.passing * 0.15
            Position.MID -> r.passing * 0.45 + r.stamina * 0.25 + r.defending * 0.15 + r.finishing * 0.15
            Position.FWD -> r.finishing * 0.55 + r.pace * 0.25 + r.passing * 0.20
        }
        return raw.toInt().coerceIn(40, 99)
    }

    fun addClubStats(apps: Int, goals: Int, assists: Int) = copy(
        clubStats = clubStats.copy(
            appearances = clubStats.appearances + apps,
            goals = clubStats.goals + goals,
            assists = clubStats.assists + assists,
        )
    )

    fun addWcStats(apps: Int, goals: Int, assists: Int) = copy(
        wcStats = wcStats.copy(
            appearances = wcStats.appearances + apps,
            goals = wcStats.goals + goals,
            assists = wcStats.assists + assists,
            tournaments = wcStats.tournaments + 1,
        )
    )
}
