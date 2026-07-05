package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
data class MatchResult(
    val homeName: String,
    val awayName: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val wentToPenalties: Boolean = false,
    val winnerName: String? = null, // null only for a genuine draw (group stage)
    val commentary: List<String> = emptyList(),
) {
    val scoreline: String get() =
        "$homeName $homeGoals–$awayGoals $awayName" + if (wentToPenalties) " (pens)" else ""
}
