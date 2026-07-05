package com.wcsim.engine.score

import com.wcsim.engine.model.WcStage
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    @Test fun stage_points_are_monotonic() {
        val stages = listOf(WcStage.GROUP, WcStage.ROUND16, WcStage.QUARTER,
            WcStage.SEMI, WcStage.FINAL, WcStage.CHAMPION)
        val pts = stages.map { Scoring.stagePoints(it) }
        for (i in 1 until pts.size) assertTrue("${stages[i]} must exceed ${stages[i-1]}", pts[i] > pts[i-1])
    }

    @Test fun earnings_contribute_to_score() {
        val low = Scoring.finalScore(wcPoints = 100, careerEarnings = 1_000_000, trophies = 0, seasons = 10)
        val high = Scoring.finalScore(wcPoints = 100, careerEarnings = 50_000_000, trophies = 0, seasons = 10)
        assertTrue(high > low)
    }

    @Test fun trophies_and_longevity_add_bonus() {
        val a = Scoring.finalScore(100, 0, trophies = 0, seasons = 5)
        val b = Scoring.finalScore(100, 0, trophies = 3, seasons = 40)
        assertTrue(b > a)
    }
}
