package com.wcsim.engine.match

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QualificationTest {
    @Test fun strong_team_usually_qualifies() {
        var qualified = 0
        val rng = Random(5)
        repeat(200) {
            val res = Qualification.run(TeamRating(88.0, 88.0), opponentAvgStrength = 68.0, rng = rng)
            if (res.qualified) qualified++
        }
        assertTrue("qualified $qualified/200", qualified > 150)
    }

    @Test fun result_reports_record_and_matches() {
        val res = Qualification.run(TeamRating(75.0,75.0), 72.0, Random(6))
        assertTrue(res.matches.isNotEmpty())
        assertTrue(res.wins + res.draws + res.losses == res.matches.size)
    }
}
