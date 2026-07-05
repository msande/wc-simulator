package com.wcsim.engine.match

import com.wcsim.engine.model.*
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamStrengthTest {
    private fun squadOf(overall: Int): List<Player> = (0 until 11).map {
        Player(it, "P$it", 25, if (it == 0) Position.GK else Position.MID, 0, 0,
            Ratings(overall, overall, overall, overall, overall, overall))
    }

    @Test fun higher_overall_gives_higher_attack_and_defense() {
        val weak = TeamStrength.compute(squadOf(60), Tactics(aggression = 50), null)
        val strong = TeamStrength.compute(squadOf(85), Tactics(aggression = 50), null)
        assertTrue(strong.attack > weak.attack)
        assertTrue(strong.defense > weak.defense)
    }

    @Test fun aggression_trades_defense_for_attack() {
        val squad = squadOf(75)
        val def = TeamStrength.compute(squad, Tactics(aggression = 10), null)
        val att = TeamStrength.compute(squad, Tactics(aggression = 90), null)
        assertTrue(att.attack > def.attack)
        assertTrue(att.defense < def.defense)
    }

    @Test fun attacking_mastermind_boosts_attack() {
        val squad = squadOf(75)
        val plain = TeamStrength.compute(squad, Tactics(aggression = 50), null)
        val buffed = TeamStrength.compute(squad, Tactics(aggression = 50), SpecialAbility.ATTACKING_MASTERMIND)
        assertTrue(buffed.attack > plain.attack)
    }
}
