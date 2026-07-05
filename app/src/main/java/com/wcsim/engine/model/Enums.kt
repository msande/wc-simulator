package com.wcsim.engine.model

import kotlinx.serialization.Serializable

@Serializable
enum class Position { GK, DEF, MID, FWD }

/** Outfield line counts (goalkeeper is always 1 and implicit). */
@Serializable
enum class Formation(val defenders: Int, val midfielders: Int, val forwards: Int) {
    F_442(4, 4, 2),
    F_433(4, 3, 3),
    F_352(3, 5, 2),
    F_532(5, 3, 2),
    F_4231(4, 5, 1); // 2 defensive + 3 attacking mids modeled as 5 mids

    val label: String get() = when (this) {
        F_442 -> "4-4-2"; F_433 -> "4-3-3"; F_352 -> "3-5-2"
        F_532 -> "5-3-2"; F_4231 -> "4-2-3-1"
    }
}

@Serializable
enum class TrainingFocus { ATTACK, DEFENSE, FITNESS, BALANCED }

@Serializable
enum class WcStage { GROUP, ROUND16, QUARTER, SEMI, FINAL, CHAMPION }
