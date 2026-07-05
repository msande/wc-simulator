package com.wcsim.engine.match

import com.wcsim.engine.model.MatchResult
import kotlin.math.exp
import kotlin.random.Random

object MatchSimulator {
    /** Expected goals from attacker vs defender ratings. */
    private fun xg(attack: Double, oppDefense: Double): Double {
        val diff = attack - oppDefense
        return (1.35 * exp(diff / 40.0)).coerceIn(0.2, 5.0)
    }

    private fun poisson(lambda: Double, rng: Random): Int {
        val l = exp(-lambda)
        var k = 0; var p = 1.0
        do { k++; p *= rng.nextDouble() } while (p > l)
        return k - 1
    }

    fun simulate(
        homeName: String, home: TeamRating,
        awayName: String, away: TeamRating,
        rng: Random,
        allowDraw: Boolean,
    ): MatchResult {
        var hg = poisson(xg(home.attack, away.defense), rng)
        var ag = poisson(xg(away.attack, home.defense), rng)
        val commentary = buildCommentary(homeName, awayName, hg, ag, rng)

        var pens = false
        var winner: String? = when {
            hg > ag -> homeName
            ag > hg -> awayName
            else -> null
        }
        if (winner == null && !allowDraw) {
            pens = true
            // penalty shootout weighted by combined strength
            val homeEdge = home.attack + home.defense
            val awayEdge = away.attack + away.defense
            winner = if (rng.nextDouble() < homeEdge / (homeEdge + awayEdge)) homeName else awayName
        }
        return MatchResult(homeName, awayName, hg, ag, pens, winner, commentary)
    }

    private fun buildCommentary(home: String, away: String, hg: Int, ag: Int, rng: Random): List<String> {
        val lines = mutableListOf<String>()
        data class G(val minute: Int, val team: String)
        val goals = buildList {
            repeat(hg) { add(G(rng.nextInt(1, 91), home)) }
            repeat(ag) { add(G(rng.nextInt(1, 91), away)) }
        }.sortedBy { it.minute }
        lines += "Kickoff: $home vs $away."
        for (g in goals) lines += "${g.minute}' GOAL — ${g.team} find the net!"
        lines += "Full time: $home $hg–$ag $away."
        return lines
    }
}
