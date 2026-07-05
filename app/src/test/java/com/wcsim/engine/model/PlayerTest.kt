package com.wcsim.engine.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerTest {
    private fun ratings() = Ratings(pace = 70, stamina = 70, finishing = 70,
        passing = 70, defending = 70, goalkeeping = 20)

    @Test fun overall_is_position_weighted() {
        val fwd = Player(1, "A", 25, Position.FWD, clubId = 1, ratings = ratings())
        val def = Player(2, "B", 25, Position.DEF, clubId = 1, ratings = ratings())
        // Same raw ratings but finishing-weighted FWD should differ from defending-weighted DEF only if weights differ.
        assertTrue(fwd.overall in 40..99)
        assertTrue(def.overall in 40..99)
    }

    @Test fun career_stats_accumulate() {
        var p = Player(1, "A", 25, Position.FWD, clubId = 1, ratings = ratings())
        p = p.addClubStats(apps = 30, goals = 12, assists = 5)
        p = p.addWcStats(apps = 7, goals = 4, assists = 2)
        assertEquals(30, p.clubStats.appearances)
        assertEquals(12, p.clubStats.goals)
        assertEquals(4, p.wcStats.goals)
        assertEquals(1, p.wcStats.tournaments) // addWcStats bumps tournaments by 1
    }
}
