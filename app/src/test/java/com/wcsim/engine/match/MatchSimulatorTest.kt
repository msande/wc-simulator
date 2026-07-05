package com.wcsim.engine.match

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MatchSimulatorTest {
    @Test fun stronger_team_wins_more_often_over_many_games() {
        val strong = TeamRating(attack = 85.0, defense = 85.0)
        val weak = TeamRating(attack = 60.0, defense = 60.0)
        var strongWins = 0
        val rng = Random(1)
        repeat(1000) {
            val r = MatchSimulator.simulate("Strong", strong, "Weak", weak, rng, allowDraw = true)
            if (r.homeGoals > r.awayGoals) strongWins++
        }
        assertTrue("strong won $strongWins/1000", strongWins > 600)
    }

    @Test fun decisive_mode_never_returns_a_draw() {
        val rng = Random(2)
        repeat(200) {
            val r = MatchSimulator.simulate("A", TeamRating(70.0,70.0), "B", TeamRating(70.0,70.0),
                rng, allowDraw = false)
            assertTrue(r.homeGoals != r.awayGoals || r.winnerName != null)
        }
    }

    @Test fun commentary_lines_match_total_goals() {
        val rng = Random(3)
        val r = MatchSimulator.simulate("A", TeamRating(90.0,60.0), "B", TeamRating(60.0,90.0), rng, true)
        val goalLines = r.commentary.count { it.contains("GOAL") }
        assertTrue(goalLines >= r.homeGoals + r.awayGoals - 0) // at least the goals are narrated
    }
}
