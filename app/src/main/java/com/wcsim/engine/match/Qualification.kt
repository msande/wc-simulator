package com.wcsim.engine.match

import com.wcsim.engine.model.MatchResult
import kotlin.random.Random

data class QualificationResult(
    val qualified: Boolean,
    val points: Int,
    val wins: Int, val draws: Int, val losses: Int,
    val matches: List<MatchResult>,
)

object Qualification {
    private const val GAMES = 8
    private const val QUALIFY_POINTS = 14 // out of 24 possible

    fun run(team: TeamRating, opponentAvgStrength: Double, rng: Random): QualificationResult {
        val opp = TeamRating(opponentAvgStrength, opponentAvgStrength)
        val matches = mutableListOf<MatchResult>()
        var pts = 0; var w = 0; var d = 0; var l = 0
        repeat(GAMES) { i ->
            val homeGame = i % 2 == 0
            val r = if (homeGame) MatchSimulator.simulate("You", team, "Rival", opp, rng, allowDraw = true)
                    else MatchSimulator.simulate("Rival", opp, "You", team, rng, allowDraw = true)
            matches += r
            val youGoals = if (homeGame) r.homeGoals else r.awayGoals
            val oppGoals = if (homeGame) r.awayGoals else r.homeGoals
            when {
                youGoals > oppGoals -> { pts += 3; w++ }
                youGoals == oppGoals -> { pts += 1; d++ }
                else -> l++
            }
        }
        return QualificationResult(pts >= QUALIFY_POINTS, pts, w, d, l, matches)
    }
}
