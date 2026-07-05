package com.wcsim.engine.career

import com.wcsim.engine.model.SpecialAbility
import com.wcsim.engine.model.WcStage

object Contracts {
    /** Board's target for the season, based on nation strength. */
    fun expectationFor(baseStrength: Int): WcStage = when {
        baseStrength >= 85 -> WcStage.SEMI
        baseStrength >= 78 -> WcStage.QUARTER
        baseStrength >= 70 -> WcStage.ROUND16
        else -> WcStage.GROUP // just qualify + compete
    }

    fun rank(stage: WcStage): Int = stage.ordinal // GROUP=0 .. CHAMPION=5

    /** Fired if you fall two or more stages short of expectation. */
    fun isFired(expected: WcStage, achieved: WcStage, reputation: Int): Boolean {
        val shortfall = rank(expected) - rank(achieved)
        return shortfall >= 2 || (shortfall == 1 && reputation < 20)
    }

    /** Annual salary offer in currency units. */
    fun salaryOffer(baseStrength: Int, reputation: Int, ability: SpecialAbility?): Long {
        val base = 500_000L + baseStrength * 40_000L
        val repMult = 1.0 + reputation / 100.0
        val abilityMult = if (ability == SpecialAbility.NEGOTIATOR) 1.20 else 1.0
        return (base * repMult * abilityMult).toLong()
    }

    /**
     * When you push for more: high reputation + high roll -> +10%; else -> -10%.
     * roll is a 0..1 random draw supplied by the caller (deterministic in tests).
     */
    fun pushOutcome(offer: Long, reputation: Int, roll: Double): Long {
        val success = roll < (reputation / 100.0)
        return if (success) (offer * 1.10).toLong() else (offer * 0.90).toLong()
    }
}
