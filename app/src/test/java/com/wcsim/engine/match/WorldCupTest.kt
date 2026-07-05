package com.wcsim.engine.match

import com.wcsim.engine.model.WcStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WorldCupTest {
    // 32 teams: index 0 is "You". Provide ratings for all.
    private fun field(youRating: Double): List<WorldCup.Entrant> {
        val rng = Random(11)
        return (0 until 32).map { i ->
            val s = if (i == 0) youRating else rng.nextDouble(60.0, 85.0)
            WorldCup.Entrant("T$i", TeamRating(s, s), isYou = i == 0)
        }
    }

    @Test fun requires_exactly_32_teams() {
        try {
            WorldCup.run(field(80.0).take(30), Random(1), null)
            assertTrue("should have thrown", false)
        } catch (e: IllegalArgumentException) { /* expected */ }
    }

    @Test fun produces_single_champion_and_your_stage() {
        val res = WorldCup.run(field(99.0), Random(1), null)
        assertNotNull(res.champion)
        assertNotNull(res.yourStage)
    }

    @Test fun dominant_you_reaches_at_least_knockouts() {
        // Overwhelming favorite should usually escape the group.
        var deepRuns = 0
        repeat(50) { seed ->
            val res = WorldCup.run(field(99.0), Random(seed.toLong()), null)
            if (res.yourStage != WcStage.GROUP) deepRuns++
        }
        assertTrue("deep runs $deepRuns/50", deepRuns > 35)
    }
}
