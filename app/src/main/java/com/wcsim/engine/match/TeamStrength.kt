package com.wcsim.engine.match

import com.wcsim.engine.model.*

data class TeamRating(val attack: Double, val defense: Double)

object TeamStrength {
    /** ability is the coach's, applied to THIS team. knockout flag handled by caller for MOTIVATOR. */
    fun compute(
        squad: List<Player>,
        tactics: Tactics,
        ability: SpecialAbility?,
        knockout: Boolean = false,
    ): TeamRating {
        val avg = if (squad.isEmpty()) 50.0 else squad.map { it.overall }.average()
        // aggression 0..100 -> shift of +/- 15% between attack and defense.
        val a = (tactics.aggression - 50) / 50.0 // -1..1
        var attack = avg * (1.0 + 0.15 * a)
        var defense = avg * (1.0 - 0.15 * a)
        when (tactics.training) {
            TrainingFocus.ATTACK -> attack *= 1.04
            TrainingFocus.DEFENSE -> defense *= 1.04
            TrainingFocus.FITNESS -> { attack *= 1.02; defense *= 1.02 }
            TrainingFocus.BALANCED -> {}
        }
        when (ability) {
            SpecialAbility.ATTACKING_MASTERMIND -> attack *= 1.08
            SpecialAbility.IRON_WALL -> defense *= 1.08
            SpecialAbility.FITNESS_GURU -> { attack *= 1.03; defense *= 1.03 }
            SpecialAbility.MOTIVATOR -> if (knockout) { attack *= 1.06; defense *= 1.06 }
            else -> {}
        }
        return TeamRating(attack, defense)
    }
}
