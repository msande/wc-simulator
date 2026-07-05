package com.wcsim.engine.model

import kotlinx.serialization.Serializable

/** aggression 0 = ultra-defensive, 100 = all-out attack. */
@Serializable
data class Tactics(
    val formation: Formation = Formation.F_442,
    val training: TrainingFocus = TrainingFocus.BALANCED,
    private val rawAggression: Int = 50,
) {
    constructor(aggression: Int) : this(rawAggression = aggression)
    val aggression: Int get() = rawAggression.coerceIn(0, 100)
}

/** A chosen starting XI: exactly 11 player ids, first is GK by convention. */
@Serializable
data class Squad(val playerIds: List<Int>) {
    val isValid: Boolean get() = playerIds.size == 11 && playerIds.toSet().size == 11
}
